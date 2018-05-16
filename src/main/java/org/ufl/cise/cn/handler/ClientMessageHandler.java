package org.ufl.cise.cn.handler;

import org.ufl.cise.cn.conf.PeerInfoConfig;
import org.ufl.cise.cn.exceptions.BitTorrentPrototypeException;
import org.ufl.cise.cn.log.LogType;
import org.ufl.cise.cn.model.Peer;
import org.ufl.cise.cn.model.PeerListener;
import org.ufl.cise.cn.log.Logger;
import org.ufl.cise.cn.messages.*;
import org.ufl.cise.cn.utilities.Utils;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

public class ClientMessageHandler implements Runnable, PeerListener {
    private final Peer peer;
    private final FileHandler fileHandler;
    private final PeerHandler peerHandler;
    private final Logger logger;
    private final AtomicBoolean fileCompleted = new AtomicBoolean(false);
    private final AtomicBoolean peersFileCompleted = new AtomicBoolean(false);
    private final AtomicBoolean neighboursStatus = new AtomicBoolean(false);
    private final Collection<ConnectionHandler> connectionHandlers =
            Collections.newSetFromMap(new ConcurrentHashMap<ConnectionHandler, Boolean>());

    public ClientMessageHandler(Peer peer) throws BitTorrentPrototypeException, IOException {
        this.peer = peer;
        this.fileHandler = new FileHandler(peer.peerId);
        this.peerHandler = new PeerHandler(peer.peerId, getPeerList(), fileHandler.getBitmapSize());
        this.logger = new Logger(peer.peerId);
        this.fileCompleted.set(peer.hasFile);
    }

    public ArrayList<Peer> getPeerList() throws IOException {
        ArrayList<Peer> remotePeers = new ArrayList<Peer>(PeerInfoConfig.loadProperty());
        remotePeers.remove(peer);
        return remotePeers;
    }

    public void startPeerConnection() {
        fileHandler.addClientConnections(this);
        peerHandler.addClientConnections(this);
        new Thread(peerHandler).start();
    }

    public void splitFile() throws BitTorrentPrototypeException {
        fileHandler.splitFile();
        fileHandler.setAllParts();
    }

    @Override
    public void run() {
        try {
            ServerSocket serverSocket = new ServerSocket(peer.portID);
            while (!neighboursStatus.get()) {
                try {
                    establishConnections(new ConnectionHandler(peer.peerId, false, -1, serverSocket.accept(), fileHandler, peerHandler));
                } catch (Exception e) {
                    Logger.getLogger().error(e);
                }
            }
        } catch (IOException ex) {
            Logger.getLogger().error(ex);
        }
    }

    private synchronized boolean establishConnections(ConnectionHandler connectionHandler) {
        if (!connectionHandlers.contains(connectionHandler)) {
            connectionHandlers.add(connectionHandler);
            new Thread(connectionHandler).start();
            try {
                wait(10);
            } catch (InterruptedException e) {
                Logger.getLogger().error(e);
            }

        }
        return true;
    }

    public void connectToPeers(Collection<Peer> peersToConnectTo) {

        Iterator<Peer> allPeers = peersToConnectTo.iterator();
        int numberOfConnections = 0;
        int maxNumberOfConnections = peersToConnectTo.size();

        while (numberOfConnections < maxNumberOfConnections) {
        do {
                Socket socket = null;
                Peer peer = allPeers.next();
                try {
                    Logger.getLogger().logMessage(Integer.toString(peer.getPeerId()), LogType.MAKESCONNECTION, 0);
                    socket = new Socket(peer.hostName, peer.getPortId());
                    boolean isconnected = establishConnections(new ConnectionHandler(this.peer.peerId, true, peer.getPeerId(),
                            socket, fileHandler, peerHandler));
                    if (isconnected) {
                        numberOfConnections++;
                        allPeers.remove();
                    }
                } catch (Exception ex) {
                    if (socket != null) {
                        try {
                            socket.close();
                        } catch (IOException ex1) {
                        }
                    }
                }
            }
            while (allPeers.hasNext());

            allPeers = peersToConnectTo.iterator();
            try {
                Thread.sleep(20);
            } catch (InterruptedException ex) {
            }
        }
    }

    @Override
    public synchronized void quitPeer(Boolean complete) {
        if(complete == true){
            logger.logMessage(null, LogType.COMPLETEDOWNLOAD, 0);
            fileCompleted.set(true);
        }
        else {
            logger.logMessage(null, LogType.COMPLETEDOWNLOAD, 0);
            peersFileCompleted.set(true);
        }

        if (fileCompleted.get() && peersFileCompleted.get()) {
            neighboursStatus.set(true);
            System.exit(0);
        }
    }

    @Override
    public synchronized void broadcastHaveMessage(int index) {
        for (ConnectionHandler connectionHandler : connectionHandlers) {
            connectionHandler.addMessageinQueue(new ActualMessage(MessageType.HAVE, Utils.getByteFromInt(index)));
            if (!peerHandler.isInteresting(connectionHandler.getClientRemotePeerId(), fileHandler.getReceivedParts())) {
                connectionHandler.addMessageinQueue(new ActualMessage(MessageType.NOT_INTERESTED,null));
            }
        }
    }

    @Override
    public synchronized void chockUnchockPeers(Collection<Integer> chokedPeersIds, Boolean isChoke) {
        MessageType messageType = isChoke == true ? MessageType.CHOKE : MessageType.UNCHOKE;
        for (ConnectionHandler connectionHandler : connectionHandlers) {
            if (chokedPeersIds.contains(connectionHandler.getClientRemotePeerId())) {
                connectionHandler.addMessageinQueue(new ActualMessage(messageType,null));
            }
        }
    }
}