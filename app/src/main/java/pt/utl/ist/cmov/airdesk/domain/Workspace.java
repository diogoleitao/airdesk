package pt.utl.ist.cmov.airdesk.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

import pt.utl.ist.cmov.airdesk.domain.exceptions.TopicAlreadyAddedException;
import pt.utl.ist.cmov.airdesk.domain.exceptions.WorkspaceQuotaReachedException;

public class Workspace implements Serializable, Observer, UserSubject {

	/**
	 * The maximum size of the workspace (in kB)
	 */
	private int quota;

    /**
	 * The total quota occupied (in kB)
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
	 * A hash that serves as an GUID
	 */
	private String hash;

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
	 * A list of Users observing the workspace
	 */
	private ArrayList<Observer> observers = new ArrayList<Observer>();

	public Workspace() {}

	public Workspace(int quota, String name, String owner, String hash) {
		this.setQuota(quota);
		this.setName(name);
		this.setOwner(owner);
		this.setHash(hash);

        Privileges p = new Privileges(true, true, true, true);
        HashMap<String, Privileges> al = new HashMap<String, Privileges>();
        al.put(owner, p);

        this.setAccessLists(al);
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

	public String getHash() {
		return hash;
	}

	public void setHash(String hash) {
		this.hash = hash;
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

	public boolean isPrivate() {
		return isPrivate;
	}

	public void setPrivacy(boolean isPrivate) {
		this.isPrivate = isPrivate;
	}

	public HashMap<String, File> getFiles() {
		return files;
	}

    public int getQuotaOccupied() {
        return this.quotaOccupied;
	}

	public ArrayList<Observer> getObservers() {
		return observers;
	}

    public void updateQuotaOccupied(int fileSize) {
        this.quotaOccupied += fileSize;
    }

    public void addTopic(String topic) throws TopicAlreadyAddedException {
		if (this.getTopics().contains(topic)) {
			throw new TopicAlreadyAddedException();
		} else {
			this.getTopics().add(topic);
		}
	}

    public void saveFile(String currentFile, String content) throws WorkspaceQuotaReachedException {
        int oldSize = getFiles().get(currentFile).getSize();
        int size = content.length() * 4 - oldSize;
        if ((getQuotaOccupied() + size) > getQuota()) {
            throw new WorkspaceQuotaReachedException();
        } else {
            getFiles().get(currentFile).save(content);
            updateQuotaOccupied(size);
		}
	}




	///////// USER SUBJECT METHODS /////////
	@Override
	public void register(Observer o) {
		this.getObservers().add(o);
	}

	@Override
	public void unregister(Observer o) {
		this.getObservers().remove(o);
	}

	@Override
	public void notifyObservers() {
		WifiManager.broadcastWorkspaceUpdate(this);
	}
}
