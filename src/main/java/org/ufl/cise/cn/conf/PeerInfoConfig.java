package org.ufl.cise.cn.conf;

import org.ufl.cise.cn.model.Peer;

import java.io.*;
import java.util.Collection;
import java.util.LinkedList;

public class PeerInfoConfig {

    public static final String CONFIG_FILE = "PeerInfo.cfg";

    public static Collection<Peer> loadProperty () throws IOException {
        Collection<Peer> peers = new LinkedList<Peer>();
        BufferedReader in = new BufferedReader(new FileReader (CONFIG_FILE));
        for (String line; (line = in.readLine()) != null;) {
            String[] tokens = line.split("\\s+");
            if (tokens.length != 4) {
                throw new IOException ();
            }
            peers.add (new Peer(Integer.parseInt(tokens[0]), tokens[1],
                    Integer.parseInt(tokens[2]), (Integer.parseInt(tokens[3]) == 1)));
        }
        return peers;
    }
}