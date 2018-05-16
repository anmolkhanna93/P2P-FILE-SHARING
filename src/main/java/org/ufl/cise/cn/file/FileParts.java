package org.ufl.cise.cn.file;

import org.ufl.cise.cn.utilities.Utils;
import java.util.BitSet;

public class FileParts {
    private final BitSet fileBitSet;

    public FileParts(int numberOfFileParts) {
        fileBitSet = new BitSet (numberOfFileParts);
    }

    public synchronized int getPartToRequest(BitSet requestableParts) {
        requestableParts.andNot(fileBitSet);

        if (!requestableParts.isEmpty()) {
            final int partId = Utils.getRandom(requestableParts);
            fileBitSet.set(partId);
            return partId;
        }
        return -1;
    }
}