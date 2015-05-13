package pt.utl.ist.cmov.airdesk.domain;

import java.io.Serializable;

/**
 * Created by Tiago on 06/05/2015.
 */
public class BroadcastMessage implements Serializable{

    private String ip;
    private MessageTypes messageType;
    private String arg1, arg2;
    private File file;
    private Workspace workspace;

    public BroadcastMessage(MessageTypes _messageType, String _arg1){
        messageType = _messageType;
        arg1 = _arg1;
    }

    public BroadcastMessage(MessageTypes _messageType, String _arg1, String _arg2){
        messageType = _messageType;
        arg1 = _arg1;
        arg2 = _arg2;
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

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public Workspace getWorkspace() {
        return workspace;
    }

    public void setWorkspace(Workspace workspace) {
        this.workspace = workspace;
    }

    public enum MessageTypes {
        FILE_CHANGED, FILE_DELETED, FILE_ADDED_TO_WORKSPACE, WORKSPACE_DELETED,
        INVITATION_TO_WORKSPACE, REQUEST_FILE, REQUEST_WORKSPACE
    }
}