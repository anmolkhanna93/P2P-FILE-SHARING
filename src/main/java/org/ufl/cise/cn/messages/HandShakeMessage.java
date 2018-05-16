package org.ufl.cise.cn.messages;

import org.ufl.cise.cn.model.Message;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ProtocolException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

public class HandShakeMessage implements Message {
    private static final String HEADER = "P2PFILESHARINGPROJ";
    private final byte[] ZERO_BITS = new byte[10];
    private byte[] peerId = new byte[4];

    public HandShakeMessage() {
    }

    private HandShakeMessage(byte[] peerId) {
        this.peerId = peerId;
    }

    public HandShakeMessage(int peerId) {
        this.peerId = ByteBuffer.allocate(4).order(ByteOrder.BIG_ENDIAN).putInt(peerId).array();
    }

    public int getPeerId() {
        return ByteBuffer.wrap(peerId).order(ByteOrder.BIG_ENDIAN).getInt();
    }

    public void setPeerId(byte[] peerId) {
        this.peerId = peerId;
    }

    @Override
    public void writeMessageAsByte(DataOutputStream out) throws IOException {
        out.write(ByteBuffer.allocate(32).put(HEADER.getBytes()).put(ZERO_BITS).put(peerId).array());
    }

    @Override
    public void readMessageAsByte(DataInputStream in) throws IOException {

        byte[] receivedHandShakeMessage = new byte[32];
        in.read(receivedHandShakeMessage);
        peerId = Arrays.copyOfRange(receivedHandShakeMessage, 28, 32);

        if (!validateReceivedHandShakeMessage(receivedHandShakeMessage))
            throw new ProtocolException("[Error] :: Handshake Message : " + new String(receivedHandShakeMessage)+ " could not be validated ");

    }

    private boolean validateReceivedHandShakeMessage(byte[] receivedHandShakeMessage) {
        return new String(Arrays.copyOfRange(receivedHandShakeMessage, 0, 18)).equals(HEADER)
                && byteArrayCheck(Arrays.copyOfRange(receivedHandShakeMessage, 18, 28))
                && ByteBuffer.wrap(Arrays.copyOfRange(receivedHandShakeMessage, 28, 32))
                .getInt() == this.getPeerId();
    }

    private boolean byteArrayCheck(byte[] zeroBits) {
        int sum = 0;
        for (byte b : zeroBits) {
            sum |= b;
        }
        return (sum == 0);
    }
}