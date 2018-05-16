package org.ufl.cise.cn.handler;

import org.ufl.cise.cn.exceptions.BitTorrentPrototypeException;
import org.ufl.cise.cn.log.LogType;
import org.ufl.cise.cn.messages.*;
import org.ufl.cise.cn.log.Logger;
import org.ufl.cise.cn.utilities.Utils;
import sun.rmi.runtime.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.BitSet;

public class MessageHandler {

    private boolean isChocked;
    private final int remotePeerId;
    private final FileHandler fileHandler;
    private final PeerHandler peerHandler;
    private final Logger logger;

    public MessageHandler(int remotePeerId, FileHandler fileMgr, PeerHandler peerMgr, Logger logger) {
        this.remotePeerId = remotePeerId;
        this.fileHandler = fileMgr;
        this.peerHandler = peerMgr;
        this.logger = logger;
        this.isChocked = true;
    }

    public ActualMessage generateBitFieldsMessages(HandShakeMessage handshake) {
        BitSet bitset = fileHandler.getReceivedParts();
        if (!bitset.isEmpty()) {
            return new ActualMessage(MessageType.BITFIELD, bitset.toByteArray());
        }
        return null;
    }

    public ActualMessage generateMessages(ActualMessage actualMessage) throws BitTorrentPrototypeException {

        if(actualMessage.getType().equals(MessageType.CHOKE)){
            return chockMessage();
        }
        else if(actualMessage.getType().equals(MessageType.UNCHOKE)){
            return unChockMessage();
        }
        else if(actualMessage.getType().equals(MessageType.INTERESTED)){
            return interestedMessage();
        }
        else if(actualMessage.getType().equals(MessageType.NOT_INTERESTED)){
            return notInterestedMessage();
        }
        else if(actualMessage.getType().equals(MessageType.HAVE)){
            return haveMessage(actualMessage);
        }
        else if(actualMessage.getType().equals(MessageType.BITFIELD)){
            return bitFieldMessage(actualMessage);
        }
        else if(actualMessage.getType().equals(MessageType.REQUEST)){
            return requestMessage(actualMessage);
        }
        else if(actualMessage.getType().equals(MessageType.PIECE)){
            return pieceMessage(actualMessage);
        }
        else
            return null;

    }

    private ActualMessage chockMessage(){
        isChocked = true;
        logger.logMessage(Integer.toString(remotePeerId), LogType.CHOKE, 0);
        return null;
    }

    private ActualMessage unChockMessage(){
        isChocked = false;
        logger.logMessage(Integer.toString(remotePeerId), LogType.UNCHOKE, 0);
        return requestPiece();
    }

    private ActualMessage interestedMessage(){
        logger.logMessage(Integer.toString(remotePeerId), LogType.INTERESTED, 0);
        peerHandler.updateInterestPeers(remotePeerId,true);
        return null;
    }
    private ActualMessage notInterestedMessage(){
        logger.logMessage(Integer.toString(remotePeerId),LogType.NOTINTERESTED, 0);
        peerHandler.updateInterestPeers(remotePeerId,false);
        return null;
    }

    private ActualMessage haveMessage(ActualMessage have){
        final int index = Utils.getIntFromByte(have.getMessagePayload(),0,4);//ByteBuffer.wrap(Arrays.copyOfRange(have.getMessagePayload(), 0, 4)).order(ByteOrder.BIG_ENDIAN).getInt();
        logger.logMessage(Integer.toString(remotePeerId), LogType.HAVE, index);
        peerHandler.updateBitField(remotePeerId, index);

        if (fileHandler.getReceivedParts().get(index)) {
            return new ActualMessage(MessageType.NOT_INTERESTED,null);
        } else {
            return new ActualMessage(MessageType.INTERESTED,null);
        }
    }

    private ActualMessage pieceMessage(ActualMessage piece) throws BitTorrentPrototypeException {
        int pieceSize = Utils.getIntFromByte(piece.getMessagePayload(),0,4);//ByteBuffer.wrap(Arrays.copyOfRange(piece.getMessagePayload(), 0, 4)).order(ByteOrder.BIG_ENDIAN).getInt();
        byte [] piecePayload = ((piece.getMessagePayload() == null) || (piece.getMessagePayload().length <= 4)) ? null : (Arrays.copyOfRange(piece.getMessagePayload(), 4, piece.getMessagePayload().length));
        fileHandler.checkAndSendFileParts(pieceSize , piecePayload);
        fileHandler.checkFileCompleted();
        peerHandler.updateFileDownloadSize(remotePeerId, piecePayload.length);
        logger.logMessage(new String(remotePeerId + "|" + fileHandler.getNumberOfReceivedParts()), LogType.DOWNLOAD, pieceSize);

        return requestPiece();
    }

    private ActualMessage requestMessage(ActualMessage request){
        if (peerHandler.isValidPeer(remotePeerId)) {
            byte[] piece = fileHandler.getPiece(ByteBuffer.wrap(Arrays.copyOfRange(request.getMessagePayload(), 0, 4)).order(ByteOrder.BIG_ENDIAN).getInt());
            if (piece != null) {
                int index = Utils.getIntFromByte(request.getMessagePayload(),0,4);//ByteBuffer.wrap(Arrays.copyOfRange(request.getMessagePayload(), 0, 4)).order(ByteOrder.BIG_ENDIAN).getInt();
                return new ActualMessage(MessageType.PIECE, Utils.mergePieces(index, piece));
            }
        }
        return null;
    }
    private ActualMessage bitFieldMessage(ActualMessage bitfield){
        BitSet bitset = BitSet.valueOf(bitfield.getMessagePayload());
        peerHandler.updateBitField(remotePeerId, bitset);

        bitset.andNot(fileHandler.getReceivedParts());
        if (bitset.isEmpty()) {
            return new ActualMessage(MessageType.NOT_INTERESTED,null);
        } else {
            return new ActualMessage(MessageType.INTERESTED, null);
        }
    }


    private ActualMessage requestPiece() {
        if (!isChocked) {
            int partId = fileHandler.getPartToRequest(peerHandler.getReceivedBits(remotePeerId));
            if (partId >= 0) {
                return new ActualMessage(MessageType.REQUEST,ByteBuffer.allocate(4).order(ByteOrder.BIG_ENDIAN).putInt(partId).array());
            }
            else {
            }
        } 
        return null;
    }
}