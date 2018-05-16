package org.ufl.cise.cn.model;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class Peer {

    public int peerId;
    public String hostName;
    public int portID;
    public boolean hasFile;
    private List<Peer> peer;
    public BitSet bitSet;
    private final AtomicBoolean interested;
    public AtomicInteger fileDownloadSize;

    public Peer(int peerId, String hostName, int portID, boolean hasFile) {
        this.peerId = peerId;
        this.hostName = hostName;
        this.portID = portID;
        this.hasFile = hasFile;

        this.peer = new LinkedList<Peer>();
        this.bitSet = new BitSet();
        this.fileDownloadSize = new AtomicInteger (0);
        this.interested = new AtomicBoolean (false);
    }

    public int getPeerId() {
        return this.peerId;
    }

    public void setPeerId(int peerId) {
        this.peerId = peerId;
    }

    public int getPortId() {
        return portID;
    }

    public void setPortId(int portId) {
        this.portID = portId;
    }

    public String getHostName() {
        return hostName;
    }

    public void setPeerAddress(String hostName) {
        this.hostName = hostName;
    }

    public String getPeerAddress() {
        return hostName;
    }

    public boolean hasFile() {
        return hasFile;
    }

    public boolean isInterested() {
        return interested.get();
    }

    public void setInterested() {
        interested.set (true);
    }

    public void setNotInterested() {
        interested.set (false);
    }

    public static Peer getDefaultPeer(int peerId){
        return new Peer(peerId, "127.0.0.1", 0, false);
    }
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Peer other = (Peer) obj;
        if (this.peerId != other.peerId)
            return false;
        return true;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + this.peerId;
        return result;
    }

    @Override
    public String toString() {
        return new StringBuilder ("peerId :").append(peerId)
                .append (" address:").append (hostName)
                .append(" port: ").append(portID).toString();
    }
}