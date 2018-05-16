package org.ufl.cise.cn.messages;

import org.ufl.cise.cn.exceptions.InvalidMessageTypeException;

public enum MessageType {
    CHOKE	 ("choke",0),
    UNCHOKE ("unchoke",1),
    INTERESTED ("interested",2),
    NOT_INTERESTED ("not interested",3),
    HAVE ("have",4),
    BITFIELD ("bitfield",5),
    REQUEST ("request",6),
    PIECE ("piece",7);

    private String messageType;
    private int value;

    private MessageType(String messageType, int value) {
        this.messageType = messageType;
        this.value = value;
    }

    public MessageType getMessageType(byte value) throws InvalidMessageTypeException {
        for (MessageType mType : MessageType.values()) {
            if (mType.value == value) {
                return mType;
            }

        }
        throw new InvalidMessageTypeException(String.format("No message type with value %d", value));
    }

    public String getMessageType() {
        return messageType;
    }

    public byte getValue() {
        return (byte) value;
    }

    public static MessageType valueOf (byte b) {
        for (MessageType mt : MessageType.values()) {
            if (mt.value == b) {
                return mt;
            }
        }
        throw new IllegalArgumentException();
    }
}