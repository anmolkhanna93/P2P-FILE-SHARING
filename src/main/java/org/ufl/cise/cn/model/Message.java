package org.ufl.cise.cn.model;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public interface Message {

    public void readMessageAsByte(DataInputStream in) throws IOException;
    public void writeMessageAsByte(DataOutputStream out) throws IOException;

}