package org.ufl.cise.cn.handler;

import org.ufl.cise.cn.log.Logger;
import org.ufl.cise.cn.messages.ActualMessage;
import org.ufl.cise.cn.messages.MessageType;
import sun.rmi.runtime.Log;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class ActualMessageHandler implements Runnable {

    private final FileHandler fileHandler;
    private final PeerHandler peerHandler;
    private final BlockingQueue<ActualMessage> messageQueue;
    private final AtomicInteger defaultPeerId = new AtomicInteger(-1);
    private final AtomicInteger peerId = new AtomicInteger(-1);
    private final DataOutputStream outputStream;
    private boolean isRemotePeerChocked = true;

    public ActualMessageHandler(BlockingQueue<ActualMessage> queue, FileHandler fileHandler, DataOutputStream outputStream,
                                PeerHandler peerHandler, int peerId){
        this.messageQueue = queue;
        this.fileHandler = fileHandler;
        this.outputStream = outputStream;
        this.peerHandler = peerHandler;
        this.peerId.set(peerId);

    }
            @Override
            public void run() {
                while (true) {
                    try {
                        final ActualMessage message = messageQueue.take();
                        if (message != null && peerId.get() != defaultPeerId.get()) {

                            if(message.getType().equals(MessageType.CHOKE)){
                                choke(message);
                            }
                            else if(message.getType().equals(MessageType.UNCHOKE)){
                                unChoke(message);
                            }
                            else {
                                sendMessage(message);
                            }

                        }
                    } catch (Exception ex) {
                        peerHandler.initiateQuitPeer();
                        System.exit(0);
                    }
                }
            }

    private synchronized void choke(ActualMessage message) throws IOException {
        if (!isRemotePeerChocked) {
            isRemotePeerChocked = true;
            if(message == null)
                return;

            sendMessage(message);
        }
    }

    private synchronized void unChoke(ActualMessage message) throws IOException {
        if (isRemotePeerChocked) {
            isRemotePeerChocked = false;
            if(message == null)
                return;

            sendMessage(message);
        }
    }

    private  synchronized void sendMessage(ActualMessage message) throws IOException {
        if(message == null)
            return;

        message.writeMessageAsByte(outputStream);
    }
}