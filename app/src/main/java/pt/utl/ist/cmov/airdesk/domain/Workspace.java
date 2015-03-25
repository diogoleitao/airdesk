package pt.utl.ist.cmov.airdesk.domain;

import java.util.ArrayList;
import java.util.HashMap;

public class Workspace {
    /**
     * The maximum size of the workspace (in MB)
     */
    private int quota;

    /**
     * The name of the workspace
     */
    private String name;

    /**
     * The owner's/creator's nickname
     */
    private String owner;

    /**
     * Mapping between user's nickname and their privileges regarding the workspace
     */
    private HashMap<String, Privileges> accessLists;

    /**
     * A list of the topics that the workspace covers
     */
    private ArrayList<String> topics;

    /**
     * If true, the workspace is private; false, if it is public
     */
    private boolean isPrivate;

    private ArrayList<File> files;

    public Workspace() {}

    public Workspace(int quota, String name, String owner) {
        this.setQuota(quota);
        this.setName(name);
        this.setOwner(owner);
    }

    public int getQuota() {
        return quota;
    }

    public void setQuota(int quota) {
        this.quota = quota;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public HashMap<String, Privileges> getAccessLists() {
        return accessLists;
    }

    public void setAccessLists(HashMap<String, Privileges> accessLists) {
        this.accessLists = accessLists;
    }

    public ArrayList<String> getTopics() {
        return topics;
    }

    public void setTopics(ArrayList<String> topics) {
        this.topics = topics;
    }

    public boolean isPrivate() {
        return isPrivate;
    }

    public void setPrivacy(boolean isPrivate) {
        this.isPrivate = isPrivate;
    }

    public ArrayList<File> getFiles() {
        return files;
    }

    public void setFiles(ArrayList<File> files) {
        this.files = files;
    }

    @Override
    public String toString(){
        return getName();
    }
}
