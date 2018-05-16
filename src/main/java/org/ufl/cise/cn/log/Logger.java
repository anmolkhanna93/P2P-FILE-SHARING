package org.ufl.cise.cn.log;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.logging.Formatter;
import java.util.Properties;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;

public class Logger {
    private static final String CONF = "/org/ufl/cise/cn/conf/logger.properties";
    private static final Logger logger = new Logger();
    private static final java.util.logging.Logger log = java.util.logging.Logger.getLogger("P2P-Project");
    private static int peerId;

    static {
        InputStream in = null;
        try{
            in = Logger.class.getResourceAsStream(CONF);
            LogManager.getLogManager().readConfiguration(in);
        }
        catch (IOException e) {
            e.printStackTrace();
            System.exit(-1);
        }
        finally {
            try {
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private Logger() {
    }

    public Logger(int peerId) {
        this.peerId = peerId;
    }

    public String getPeerFormat(String peerId){
        peerId = peerId == null ? "0" : peerId;
        return String.format("[Peer : %s]",peerId);
    }

    public static void buildLogger(int peerId)
            throws Exception {
        Properties properties = new Properties();
        String format = String.format("log_peer_%s.log",peerId);
        properties.load(Logger.class.getResourceAsStream(CONF));
        Handler handler = new FileHandler (format);
        Formatter formatter = (Formatter) Class.forName(properties.getProperty("java.util.logging.FileHandler.formatter")).newInstance();
        handler.setFormatter(formatter);
        log.addHandler(handler);
    }

    public static Logger getLogger () {
        return logger;
    }

    public synchronized void info (String msg) {
        log.log (Level.INFO, msg);
    }

    public synchronized void error(Throwable error){
        StringWriter errorMessage = new StringWriter();
        error.printStackTrace(new PrintWriter(errorMessage));
        log.log(Level.SEVERE, errorMessage.toString());
    }

    public synchronized void logMessage(String peerId,LogType logType,int index){
        if(logType.equals(LogType.MAKESCONNECTION) || logType.equals(LogType.CONNECTED) || logType.equals(logType.PREFERNEIGHBOURCHANGED) || logType.equals(logType.OPTIMISTICCHOKEDPEERS)
                || logType.equals(LogType.CHOKE) || logType.equals(LogType.UNCHOKE))
            logger.info(getPeerFormat(Integer.toString(this.peerId)) + logType.getLogType() + getPeerFormat(peerId));
        else if(logType.equals(LogType.INTERESTED)  || logType.equals(LogType.NOTINTERESTED) )
            logger.info(getPeerFormat(Integer.toString(this.peerId)) + logType.getLogType() + getPeerFormat(peerId));
        else if(logType.equals(LogType.DOWNLOAD)){
            String [] information = peerId.split("\\|");
            logger.info(getPeerFormat(Integer.toString(this.peerId)) + String.format(logType.getLogType(),index,getPeerFormat(information[0]),information[1]));
        }

        else if(logType.equals(LogType.HAVE))
            logger.info(getPeerFormat(Integer.toString(this.peerId)) + String.format(logType.getLogType(),getPeerFormat(peerId)) + index);
        else if(logType.equals(LogType.COMPLETEDOWNLOAD))
            logger.info(getPeerFormat(Integer.toString(this.peerId)) + logType.getLogType());
        else if(logType.equals(LogType.EXIT))
            logger.info(getPeerFormat(Integer.toString(this.peerId) + logType.getLogType()));

    }
}