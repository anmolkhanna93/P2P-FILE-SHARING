package org.ufl.cise.cn.utilities;

import javafx.beans.binding.StringBinding;
import org.ufl.cise.cn.model.Peer;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.*;

public class Utils {

    public static int getRandom(BitSet bitset) {

        if(!bitset.isEmpty()){
            String bitSetCopy = bitset.toString();
            String[] bitSetIndexes = bitSetCopy.substring(1, bitSetCopy.length()-1).split(",");
            int randomValue = (int) Math.random()*(bitSetIndexes.length-1);
            return Integer.parseInt(bitSetIndexes[randomValue].trim());
        }
        else
            throw new RuntimeException ("[Error] :: BitSet is Empty !!");

    }

    public static byte[] mergePieces (int index, byte[] piece) {
        int length = 4,i=0;
        if(piece != null)
            length += piece.length;

        byte[] mergedPiece = new byte[length];
        byte[] indexBytes = getByteFromInt(index);

        for(;i<4;i++)
            mergedPiece[i] = indexBytes[i];

        for(int j=0;j<piece.length;j++)
            mergedPiece[i++] = piece[j];

        return mergedPiece;
    }

    public static Collection<Integer> getPeerId(Collection<Peer> peers) {
        Set<Integer> peerIds = new HashSet<Integer>();
        for (Peer p : peers) {
            peerIds.add(p.getPeerId());
        }
        return peerIds;
    }

    public static int getIntFromByte(byte [] message,int start, int end){
        return ByteBuffer.wrap(Arrays.copyOfRange(message, start, end)).order(ByteOrder.BIG_ENDIAN).getInt();
    }

    public static byte[] getByteFromInt(int val){
        return ByteBuffer.allocate(4).order(ByteOrder.BIG_ENDIAN).putInt(val).array();

    }

    public static boolean intersectBitSet(BitSet first, BitSet second){
        second.andNot(first);
        return ! second.isEmpty();
    }

    public static String getString(Collection<Peer> peers){
        StringBuilder result = new StringBuilder();
        for(Peer p : peers)
            result.append(Integer.toString(p.peerId)).append(",");
        return result.substring(0,result.length()-1).toString();

    }
}