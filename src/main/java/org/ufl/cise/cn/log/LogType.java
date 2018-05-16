package org.ufl.cise.cn.log;

public enum LogType {

    MAKESCONNECTION     (" makes a connection to ",0),
    CONNECTED(" is connected from ",1),
    PREFERNEIGHBOURCHANGED(" has the preferred neighbours : ",2),
    OPTIMISTICCHOKEDPEERS (" has the optimistically unchoked neighbor ",3),
    CHOKE (" is chocked by ",4),
    UNCHOKE (" is unchocked by ",5),
    HAVE (" received the ‘have’ message from %s for the piece ",6),
    INTERESTED (" received the 'interested' message from ",7),
    NOTINTERESTED (" received the ‘not interested’ message from ",8),
    DOWNLOAD (" has downloaded the piece %s from %s. Now the number of pieces it has is %s",9),
    COMPLETEDOWNLOAD (" has downloaded the complete file",10),
    EXIT (" is terminated connection.",12)
    ;

    private String logType;
    private int value;

    private LogType(String logType, int value) {
        this.logType = logType;
        this.value = value;
    }

    public String getLogType() {
        return logType;
    }

    public int getValue() {
        return value;
    }

}