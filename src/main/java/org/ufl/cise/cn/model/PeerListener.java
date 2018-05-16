package org.ufl.cise.cn.model;

import java.util.Collection;

public interface PeerListener {
    void chockUnchockPeers(Collection<Integer> chokedPeersIds, Boolean isChoke);
    void quitPeer(Boolean complete);
    void broadcastHaveMessage(int partIdx);
}