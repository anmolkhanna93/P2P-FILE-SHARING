package org.ufl.cise.cn;

import com.jcraft.jsch.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ufl.cise.cn.conf.CommonConfig;
import org.ufl.cise.cn.conf.PeerInfoConfig;
import org.ufl.cise.cn.exceptions.BitTorrentPrototypeException;
import org.ufl.cise.cn.log.StartPeerLogManager;
import org.ufl.cise.cn.model.Peer;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Properties;

public class StartPeer {

    static {
        System.setProperty("logfile_name", "startpeer.log");
    }
    static String USER;
    static String PASSWORD;
    static String path;


    private static Log LOG = LogFactory.getLog(StartPeer.class);

    public static void main(String[] args) throws IOException {


        startPeers(PeerInfoConfig.loadProperty());
    }

    public static void startPeers(Collection<Peer> peers) {
        for (final Peer peer : peers) {
            try {
                JSch jsch = new JSch();
                if(peer.getHostName().equals("localhost")){
                    USER = args[0];
                    PASSWORD = args[1];
                    path = "";
                }
                else {
                    USER = args[0];
                    PASSWORD = args[1];
                    path = "";
                }
                Session session = jsch.getSession(USER, peer.getHostName(), 22);
                session.setPassword(PASSWORD);
                Properties config = new Properties();
                config.put("StrictHostKeyChecking", "no");
                session.setConfig(config);
                session.connect();
                Channel channel = session.openChannel("exec");
         
                String exectionCommand = String.format("cd %s; java -Dlogfile_name=log_peer_%s.log -jar peerProcess.jar %s",path,String.valueOf(peer.getPeerId()), String.valueOf(peer.getPeerId()));
                ((ChannelExec) channel).setCommand(exectionCommand);

                channel.setInputStream(null);
                ((ChannelExec) channel).setErrStream(System.err);

                InputStream input = channel.getInputStream();
                channel.connect();

                Thread startPeerLogManager = new Thread(new StartPeerLogManager(channel, session, input),
                        "PeerId: " + peer.getPeerId());
                startPeerLogManager.start();
                LOG.info(String.format("%d: %s", peer.getPeerId(), "Started successfully"));
            } catch (JSchException jschException) {
                LOG.error(String.format("%d: %s", peer.getPeerId(), jschException.getMessage()));
                jschException.printStackTrace();
            } catch (IOException ioException) {
                LOG.error(String.format("%d: %s", peer.getPeerId(), ioException.getMessage()));
                ioException.printStackTrace();
            }

        }
    }
}
