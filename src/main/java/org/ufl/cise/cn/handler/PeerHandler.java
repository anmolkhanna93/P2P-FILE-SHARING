package org.ufl.cise.cn.handler;

import org.ufl.cise.cn.conf.CommonConfig;
import org.ufl.cise.cn.exceptions.BitTorrentPrototypeException;
import org.ufl.cise.cn.log.LogType;
import org.ufl.cise.cn.model.Peer;
import org.ufl.cise.cn.model.PeerListener;
import org.ufl.cise.cn.log.Logger;
import org.ufl.cise.cn.utilities.Utils;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class PeerHandler implements Runnable {


    class OptimisticUnchoker extends Thread {

        OptimisticUnchoker() throws BitTorrentPrototypeException {
            super();
        }

        @Override
        public void run() {
            while (true) {
                try {
                    Thread.sleep(optimisticUnchokingInterval);
                } catch (InterruptedException ex) {
                }

                synchronized (this) {
                    if (!chokedNeighbors.isEmpty()) {
                        Collections.shuffle(chokedNeighbors);
                        optmisticallyUnchokedPeers.clear();
                        optmisticallyUnchokedPeers.addAll(chokedNeighbors.subList(0,
                                Math.min(numberOfOptimisticallyUnchokedNeighbors, chokedNeighbors.size())));
                    }
                }

                if (chokedNeighbors.size() > 0) {
                    logger.logMessage(Utils.getString (optmisticallyUnchokedPeers),LogType.OPTIMISTICCHOKEDPEERS,0);
                }
                for (PeerListener listener : peerListeners) {
                    listener.chockUnchockPeers(Utils.getPeerId(optmisticallyUnchokedPeers), false);
                }
            }
        }
    }

    private final int numberOfPreferredNeighbors;
    private final int unchokingInterval;
    private final int splitSize;
    private final Logger logger;
    private final List<Peer> peers = new ArrayList<Peer>();
    private final Collection<Peer> preferredPeers = new HashSet<Peer>();
    private final OptimisticUnchoker optimisticUnchoker;
    private final Collection<PeerListener> peerListeners = new LinkedList<PeerListener>();
    private final int numberOfOptimisticallyUnchokedNeighbors;
    private final int optimisticUnchokingInterval;
    private final List<Peer> chokedNeighbors = new ArrayList<Peer>();
    private final Map<Integer,Peer> peerMap = new HashMap<Integer, Peer>();
    final Collection<Peer> optmisticallyUnchokedPeers =
            Collections.newSetFromMap(new ConcurrentHashMap<Peer, Boolean>());

    public PeerHandler(int peerId, Collection<Peer> peers, int splitSize) throws BitTorrentPrototypeException {
        this.peers.addAll(peers);
        setPeerMap(peers);
        numberOfPreferredNeighbors = Integer.parseInt(
                CommonConfig.getProperty(CommonConfig.NumberOfPreferredNeighbors.toString()));
        unchokingInterval = Integer.parseInt(
                CommonConfig.getProperty(CommonConfig.UnchokingInterval.toString())) * 1000;
        optimisticUnchoker = new OptimisticUnchoker();
        this.splitSize = splitSize;
        logger = new Logger (peerId);
        numberOfOptimisticallyUnchokedNeighbors = 1;
        optimisticUnchokingInterval = Integer.parseInt(
                CommonConfig.getProperty(CommonConfig.OptimisticUnchokingInterval.toString())) * 1000;

    }

    public void setPeerMap(Collection<Peer> peers){
        for(Peer p : peers){
            peerMap.put(p.peerId,p);
        }

    }

    public long getUnchokingInterval() {
        return unchokingInterval;
    }

    synchronized private Peer findPeer(int peerId) {
        for (Peer p : peers) {
            if (p.getPeerId() == peerId) {
                return p;
            }
        }
        return null;
    }

    public synchronized void updateInterestPeers(int remotePeerId, boolean addInterestPeers) {
        Peer peer = peerMap.get(remotePeerId);
        if (peer != null) {
            if(addInterestPeers)
                peer.setInterested();
            else
                peer.setNotInterested();
        }
    }

    public synchronized boolean isInteresting(int remotePeerId, BitSet bitset) {
        Peer peer = peerMap.get(remotePeerId);
        if(peer == null){
            return false;
        }
        else {
            return Utils.intersectBitSet(bitset, (BitSet) peer.bitSet.clone());
        }
    }

    synchronized List<Peer> getInterestedPeers() {
        ArrayList<Peer> interestedPeers = new ArrayList<Peer>();
        for (Peer peer : peers){
            if(peer.isInterested()){
                interestedPeers.add(peer);
            }
        }
        if(interestedPeers.size() <= 1)
            return interestedPeers;

        Collections.sort(interestedPeers, new Comparator<Peer>() {
            @Override
            public int compare(Peer o1, Peer o2) {
                return o2.fileDownloadSize.get() - o1.fileDownloadSize.get();
            }
        });

        return interestedPeers;
    }

    public synchronized void updateFileDownloadSize(int remotePeerId, int size) {
        Peer peer = peerMap.get(remotePeerId);
        if (peer != null) {
            peer.fileDownloadSize.addAndGet(size);
        }
    }

    public synchronized boolean isValidPeer(int peerId) {
        Peer defaultPeer = Peer.getDefaultPeer(peerId);

        if(optmisticallyUnchokedPeers.contains(defaultPeer))
            return true;
        else if(preferredPeers.contains(defaultPeer))
            return true;
        else
            return true;
    }

    public synchronized void updateBitField(int remotePeerId, BitSet bitfield) {
        Peer peer = peerMap.get(remotePeerId);
        if (peer != null) {
            peer.bitSet = bitfield;
        }
        if(allPeersDone())
            initiateQuitPeer();
    }

    public synchronized void updateBitField(int remotePeerId, int bitIndex) {
        Peer peer = peerMap.get(remotePeerId);
        if (peer != null) {
            peer.bitSet.set(bitIndex);
        }
        if(allPeersDone())
            initiateQuitPeer();
    }

    public synchronized BitSet getReceivedBits(int remotePeerId) {
        Peer peer = peerMap.get(remotePeerId);
        if (peer != null) {
            return (BitSet) peer.bitSet.clone();
        }
        else
            return new BitSet();
    }

    synchronized public void initiateQuitPeer(){
        for (PeerListener listener : peerListeners) {
            listener.quitPeer(false);
        }
    }

    synchronized public boolean allPeersDone() {
        for (Peer currentPeer : peers) {
            if (!currentPeer.hasFile && currentPeer.bitSet.cardinality() < splitSize) {
                return false;
            }
        }
        return true;
    }

    synchronized void resetChokedNeighbors(Collection<Peer> chokedNeighbors) {
        this.chokedNeighbors.clear();
        this.chokedNeighbors.addAll(chokedNeighbors);
    }

    synchronized void resetDownloadedBytes(){
        for(Peer p : peers){
            p.fileDownloadSize.set(0);
        }
    }

    synchronized void resetPreferedNeigbhour(List<Peer> interestedPeers){
        preferredPeers.clear();
        preferredPeers.addAll(interestedPeers.subList(0, Math.min(numberOfPreferredNeighbors, interestedPeers.size())));
        if (preferredPeers.size() > 0) {
            logger.logMessage(Utils.getString (preferredPeers), LogType.PREFERNEIGHBOURCHANGED, 0);
        }
    }

    synchronized Set<Integer> getChockedPeers(){
        Set<Integer> chokedPeerIDs = new HashSet<Integer>();
        for(Peer p : peers){
            if(!preferredPeers.contains(p))
                chokedPeerIDs.add(p.peerId);
        }
        return chokedPeerIDs;
    }

    public synchronized void addClientConnections(PeerListener listener) {
        peerListeners.add(listener);
    }

    @Override
    public void run() {

        optimisticUnchoker.start();

        while (true) {

            try {
                Thread.sleep(unchokingInterval);
            } catch (InterruptedException ex) {
            }

            Set<Integer> chokePeerIdList;
            Set<Integer> validNeighbourIdList = new HashSet<Integer>();
            List<Peer> interestedPeers = getInterestedPeers();
            Collection<Peer> optimisticUnchokPeersList;

            synchronized (this) {
                resetDownloadedBytes();
                resetPreferedNeigbhour(interestedPeers);
                chokePeerIdList = getChockedPeers();
                validNeighbourIdList.addAll (Utils.getPeerId(preferredPeers));
                optimisticUnchokPeersList = numberOfPreferredNeighbors < interestedPeers.size() ? interestedPeers.subList(numberOfPreferredNeighbors,interestedPeers.size()) : new ArrayList<Peer>();
            }

            if (optimisticUnchokPeersList != null) {
                resetChokedNeighbors(optimisticUnchokPeersList);
            }

            for (PeerListener listener : peerListeners) {
                listener.chockUnchockPeers(chokePeerIdList, true);
                listener.chockUnchockPeers(validNeighbourIdList , false);
            }

        }
        
    }
}