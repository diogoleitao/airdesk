package pt.utl.ist.cmov.airdesk.domain;

import java.util.ArrayList;
import java.util.HashMap;

public class Workspace {
	/**
	 * The maximum size of the workspace (in kB)
	 */
	private int quota;

    /**
     *
     */
    private int quotaOccupied;

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
	private HashMap<String, Privileges> accessLists = new HashMap<String, Privileges>();

	/**
	 * A list of the topics that the workspace covers
	 */
	private ArrayList<String> topics = new ArrayList<String>();

	/**
	 * If true, the workspace is private; false, if it is public
	 */
	private boolean isPrivate;

	/**
	 * Mapping between files' names and File objects
	 */
	private HashMap<String, File> files = new HashMap<String, File>();

    /**
     *
     */
    private ArrayList<String> users = new ArrayList<String>();

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

	public HashMap<String, File> getFiles() {
		return files;
	}

	public void setFiles(HashMap<String, File> files) {
		this.files = files;
	}

    public ArrayList<String> getUsers() {
        return this.users;
    }

    public void setUsers(ArrayList<String> users) {
        this.users = users;
    }

    public void addUser(String user) {
        this.users.add(user);
    }

    public int getQuotaOccupied() {
        return this.quotaOccupied;
    }

    public void updateQuotaOccupied(int fileSize) {
        this.quota += fileSize;
    }
}
