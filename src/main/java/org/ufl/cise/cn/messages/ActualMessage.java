package org.ufl.cise.cn.messages;

import org.ufl.cise.cn.model.Message;

import java.io.IOException;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.nio.ByteBuffer;

public class ActualMessage implements Message {

    private int messageLength;
    private final MessageType messageType;
    protected byte[] messagePayload;

    public ActualMessage(MessageType type, byte[] payload) {
        if(payload == null)
            this.messageLength = 1;
        else
            this.messageLength = payload.length + 1;

        this.messageType = type;
        this.messagePayload = payload;
    }

    public MessageType getType() {
        return messageType;
    }

    public byte[] getMessagePayload() {
        return messagePayload;
    }

    @Override
    public void readMessageAsByte(DataInputStream in) throws IOException {
        if ((messagePayload != null) && (messagePayload.length) > 0) {

                in.readFully(messagePayload, 0, messagePayload.length);

        }
    }

    @Override
    public void writeMessageAsByte(DataOutputStream out) throws IOException {


            out.write(getMessageAsByte());

    }

    public byte[] getMessageAsByte() {
        if(messagePayload == null || messagePayload.length  == 0){
            return ByteBuffer.allocate(5).putInt(messageLength).put(messageType.getValue()).array();
        }
        else
            return ByteBuffer.allocate(5+messagePayload.length).putInt(messagePayload.length+1).put(messageType.getValue()).put(messagePayload).array();
    }

    public static ActualMessage getActualMessage(int length, MessageType messageType) throws ClassNotFoundException, IOException {

        byte [] defaultByte = new byte[length];
        if(messageType.equals(MessageType.CHOKE)){
            return new ActualMessage(MessageType.CHOKE,null);
        }
        else if(messageType.equals(MessageType.UNCHOKE)){
            return new ActualMessage(MessageType.UNCHOKE,null);
        }
        else if(messageType.equals(MessageType.INTERESTED)){
            return new ActualMessage(MessageType.INTERESTED,null);
        }
        else if(messageType.equals(MessageType.NOT_INTERESTED)){
            return new ActualMessage(MessageType.NOT_INTERESTED,null);
        }
        else if(messageType.equals(MessageType.HAVE)) {
            return new ActualMessage(MessageType.HAVE,defaultByte);
        }
        else if(messageType.equals(MessageType.BITFIELD)) {
            return new ActualMessage(MessageType.BITFIELD,defaultByte);
        }
        else if(messageType.equals(MessageType.REQUEST)) {
            return new ActualMessage(MessageType.REQUEST,defaultByte);
        }
        else if(messageType.equals(MessageType.PIECE)) {
            return new ActualMessage(MessageType.PIECE,defaultByte);
        }
        else {
            throw new IOException ("[Error] :: Message Type : " + messageType.getMessageType() + " is not supported in the protocol ");
        }
    }
}