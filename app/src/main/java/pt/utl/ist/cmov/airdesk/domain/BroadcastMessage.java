package pt.utl.ist.cmov.airdesk.domain;

/**
 * Created by Tiago on 06/05/2015.
 */
public class BroadcastMessage {

    private String ip;
    private MessageTypes messageType;
    private String arg1, arg2;

    public BroadcastMessage(MessageTypes _messageType, String _arg1, String _ip){
        messageType = _messageType;
        arg1 = _arg1;
        ip = _ip;
    }

    public BroadcastMessage(MessageTypes _messageType, String _arg1, String _arg2, String _ip){
        messageType = _messageType;
        arg1 = _arg1;
        arg2 = _arg2;
        ip = _ip;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public MessageTypes getMessageType() {
        return messageType;
    }

    public void setMessageType(MessageTypes messageType) {
        this.messageType = messageType;
    }

    public String getArg1() {
        return arg1;
    }

    public void setArg1(String arg1) {
        this.arg1 = arg1;
    }

    public String getArg2() {
        return arg2;
    }

    public void setArg2(String arg2) {
        this.arg2 = arg2;
    }

    public enum MessageTypes {
        FILE_CHANGED, FILE_DELETED, FILE_ADDED_TO_WORKSPACE, WORKSPACE_DELETED,
        WORKSPACE_TOPIC_MATCH, INVITATION_TO_WORKSPACE
    }
}