package org.ufl.cise.cn;

import org.ufl.cise.cn.conf.CommonConfig;
import org.ufl.cise.cn.conf.PeerInfoConfig;
import org.ufl.cise.cn.exceptions.BitTorrentPrototypeException;
import org.ufl.cise.cn.model.Peer;
import org.ufl.cise.cn.handler.ClientMessageHandler;
import org.ufl.cise.cn.log.Logger;

import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;

public class peerProcess {

    private static void startHostServer(Peer currentPeer,Collection<Peer> peersToConnectTo) throws BitTorrentPrototypeException {
        ClientMessageHandler clientMessageHandler = null;
        try {
            clientMessageHandler = new ClientMessageHandler(currentPeer);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if(currentPeer.hasFile)
            clientMessageHandler.splitFile();
        clientMessageHandler.startPeerConnection();
        new Thread (clientMessageHandler).start();

        clientMessageHandler.connectToPeers (peersToConnectTo);
        try {
            Thread.sleep(50);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static void  fetchRemotePeers(int peerId)
            throws NumberFormatException, BitTorrentPrototypeException {
        Peer currentPeer = null;
        Collection<Peer> peersToConnectTo = new LinkedList<Peer>();
        try {
            CommonConfig.loadProperty();

            for (Peer peer : PeerInfoConfig.loadProperty()) {
                if (peerId == peer.getPeerId()) {
                    currentPeer = peer;
                    break;
                }
                else {
                    peersToConnectTo.add (peer);
                }
            }
        }
        catch (Exception ex) {
            Logger.getLogger().error (ex);
            System.exit(-1);
        }
        startHostServer(currentPeer, peersToConnectTo );
    }


    public static void main (String[] args) throws Exception {
        final int peerId = Integer.parseInt(args[0]);
        Logger.buildLogger(peerId);
        fetchRemotePeers(peerId);
    }
}