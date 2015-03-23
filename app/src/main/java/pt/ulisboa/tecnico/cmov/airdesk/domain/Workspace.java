package pt.utl.ist.airdesk.domain;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Diogo on 18/03/2015.
 */
public class Workspace {
    private int quota;
    private String name;
    private String owner;
    private HashMap<String, Privileges> accessLists;
    private ArrayList<String> topics;
    private boolean isPrivate;

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

    public void setPrivate(boolean isPrivate) {
        this.isPrivate = isPrivate;
    }
}
