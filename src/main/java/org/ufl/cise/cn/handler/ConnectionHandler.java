package org.ufl.cise.cn.handler;

import org.ufl.cise.cn.log.LogType;
import org.ufl.cise.cn.log.Logger;
import org.ufl.cise.cn.messages.HandShakeMessage;
import org.ufl.cise.cn.messages.ActualMessage;
import org.ufl.cise.cn.messages.MessageType;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;


public class ConnectionHandler implements Runnable {

    private final int peerId;
    private final Socket clientSocket;
    private final DataOutputStream outputStream;
    private final FileHandler fileHandler;
    private final PeerHandler peerHandler;
    private final boolean connectionStatus;
    private final int clientRemotePeerId;
    private final AtomicInteger remotePeerId;
    private final BlockingQueue<ActualMessage> actualMessagesQueue = new LinkedBlockingQueue<ActualMessage>();

    public ConnectionHandler(int localPeerId, boolean connectionStatus, int expectedRemotePeerId,
                             Socket clientSocket, FileHandler fileHandler, PeerHandler peerHandler) throws IOException {
        this.clientSocket = clientSocket;
        this.peerId = localPeerId;
        this.connectionStatus = connectionStatus;
        this.clientRemotePeerId = expectedRemotePeerId;
        this.fileHandler = fileHandler;
        this.peerHandler = peerHandler;
        this.outputStream = new DataOutputStream(this.clientSocket.getOutputStream());
        this.remotePeerId = new AtomicInteger(-1);
    }

    public int getClientRemotePeerId() {
        return remotePeerId.get();
    }

    @Override
    public void run() {
        new Thread(new ActualMessageHandler(actualMessagesQueue, fileHandler, outputStream, peerHandler, peerId)).start();

        try {
            final DataInputStream inputStream = new DataInputStream(clientSocket.getInputStream());

            new HandShakeMessage(peerId).writeMessageAsByte(outputStream);

            HandShakeMessage handShakeMessage = new HandShakeMessage();
            handShakeMessage.readMessageAsByte(inputStream);

            remotePeerId.set(handShakeMessage.getPeerId());

            final Logger logger = new Logger(peerId);
            final MessageHandler messageHandler = new MessageHandler(remotePeerId.get(), fileHandler, peerHandler, logger);
            if (connectionStatus && (remotePeerId.get() != clientRemotePeerId)) {
                throw new Exception("Remote peer id " + remotePeerId + " does not match with the expected id: " + clientRemotePeerId);
            }

            if(connectionStatus)
                logger.logMessage(Integer.toString(remotePeerId.get()), LogType.CONNECTED, 0);
            ActualMessage message = messageHandler.generateBitFieldsMessages(handShakeMessage);
            if(message != null)
                message.writeMessageAsByte(outputStream);

            while (true) {
                try {
                    synchronized (this) {
                        ActualMessage incomingMessage = ActualMessage.getActualMessage(inputStream.readInt() -1 , MessageType.valueOf(inputStream.readByte()));
                        incomingMessage.readMessageAsByte(inputStream);
                        ActualMessage responseMessage = messageHandler.generateMessages(incomingMessage);
                        if (responseMessage == null)
                            continue;
                        responseMessage.writeMessageAsByte(outputStream);
                    }
                } catch (Exception ex) {
                    break;
                }
            }
        } catch (Exception ex) {
        } finally {
            try {
                if(peerHandler.allPeersDone()){
                    peerHandler.initiateQuitPeer();
                }
                clientSocket.close();
            } catch (Exception e) {
            }
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ConnectionHandler) {
            return ((ConnectionHandler) obj).remotePeerId.equals(remotePeerId);
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 41 * hash + peerId;
        return hash;
    }

    public void addMessageinQueue(final ActualMessage message) {
        actualMessagesQueue.add(message);
    }

}