package org.ufl.cise.cn.handler;

import org.ufl.cise.cn.exceptions.BitTorrentPrototypeException;
import org.ufl.cise.cn.file.FileParts;
import org.ufl.cise.cn.conf.CommonConfig;
import org.ufl.cise.cn.file.FileSplit;
import org.ufl.cise.cn.model.PeerListener;

import java.util.BitSet;
import java.util.Collection;
import java.util.LinkedList;

public class FileHandler {

    private final double partSize;
    private final int numberOfSplits;
    private BitSet fileParts;
    private final FileParts requiredFileParts;
    private FileSplit fileSplit;
    private final Collection<PeerListener> clientConnections = new LinkedList<PeerListener>();


    public FileHandler(int peerId) throws BitTorrentPrototypeException {

        int fileSize = Integer.parseInt(CommonConfig.getProperty(CommonConfig.FileSize.toString()));
        String fileName = CommonConfig.getProperty (CommonConfig.FileName.toString());

        this.partSize = Integer.parseInt(CommonConfig.getProperty(CommonConfig.PieceSize.toString()));
        this.numberOfSplits = (int) Math.ceil (fileSize/ this.partSize);

        this.fileParts = new BitSet (numberOfSplits);
        this.requiredFileParts = new FileParts(numberOfSplits);
        this.fileSplit = new FileSplit(peerId, fileName);
    }

    public synchronized void checkAndSendFileParts(int index, byte[] payload) {
        if (!fileParts.get(index)) {
            fileParts.set (index);
            fileSplit.writeFilePart(payload, index);
            for (PeerListener listener : clientConnections) {
                listener.broadcastHaveMessage(index);
            }
        }
    }

    public synchronized void checkFileCompleted() throws BitTorrentPrototypeException {

        if (isFileCompleted()) {
            fileSplit.mergeParts(fileParts.cardinality());
            for (PeerListener listener : clientConnections) {
                listener.quitPeer(true);
            }
        }
    }

    public synchronized int getPartToRequest(BitSet availableParts) {
        availableParts.andNot(getReceivedParts());
        return requiredFileParts.getPartToRequest (availableParts);
    }

    public synchronized BitSet getReceivedParts () {
        return (BitSet) fileParts.clone();
    }

    synchronized public boolean hasPart(int pieceIndex) {
        return fileParts.get(pieceIndex);
    }


    public synchronized void setAllParts()
    {
        for (int i = 0; i < numberOfSplits; i++) {
            fileParts.set(i, true);
        }
    }

    public synchronized int getNumberOfReceivedParts() {
        return fileParts.cardinality();
    }

    public byte[] getPiece(int partId) {
        byte[] piece = fileSplit.convertPartToBytes(partId);
        return piece;
    }

    public void addClientConnections(PeerListener listener) {
        clientConnections.add (listener);
    }

    public void splitFile() throws BitTorrentPrototypeException {
        fileSplit.split((int) partSize);
    }

    public int getBitmapSize() {
        return numberOfSplits;
    }

    private boolean isFileCompleted() {
        for (int i = 0; i < numberOfSplits; i++) {
            if (!fileParts.get(i)) {
                return false;
            }
        }
        return true;
    }
}