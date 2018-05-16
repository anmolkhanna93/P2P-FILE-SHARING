package org.ufl.cise.cn.log;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.Session;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

public class StartPeerLogManager implements Runnable {
	private static final Log LOG = LogFactory.getLog(StartPeerLogManager.class);
	private final Channel channel;
	private final Session session;
	private final InputStream input;

	public StartPeerLogManager(Channel channel, Session session, InputStream input) {
		this.channel = channel;
		this.session = session;
		this.input = input;
	}

	public void run() {
		BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(input));
		String line = null;
		try {
			while ((line = bufferedReader.readLine()) != null) {
				LOG.info(line);
			}
			bufferedReader.close();
		} catch (Exception ex) {
			LOG.error(ex.getMessage());
			ex.printStackTrace();
		}

		channel.disconnect();
		session.disconnect();
	}
}