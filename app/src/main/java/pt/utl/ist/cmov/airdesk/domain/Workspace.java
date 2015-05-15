package pt.utl.ist.cmov.airdesk.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import pt.utl.ist.cmov.airdesk.domain.exceptions.TopicAlreadyAddedException;
import pt.utl.ist.cmov.airdesk.domain.exceptions.WorkspaceQuotaReachedException;

public class Workspace implements Serializable {

	private int quota;

	private int quotaOccupied;

	private String name;

	private String owner;

	private String hash;

	public ArrayList<String> getConflicts() {
		return conflicts;
	}

	private ArrayList<String> conflicts;

	public boolean isOnline() {
		return online;
	}

	public void setOnline(boolean online) {
		this.online = online;
	}

	public boolean online = false;

	private Date timestamp;

	private Date lastOnlineTimestamp;

	private HashMap<String, Privileges> accessLists = new HashMap<String, Privileges>();

	private ArrayList<String> topics = new ArrayList<String>();

	private boolean isPrivate;

	private HashMap<String, File> files = new HashMap<String, File>();

	public Workspace(int quota, String name, String owner, String hash) {
		this.setQuota(quota);
		this.setName(name);
		this.setOwner(owner);
		this.setHash(hash);
		this.timestamp = Calendar.getInstance().getTime();
		this.lastOnlineTimestamp = timestamp;
		this.conflicts = new ArrayList<>();

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
		this.timestamp = Calendar.getInstance().getTime();
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
			this.timestamp = Calendar.getInstance().getTime();
			updateQuotaOccupied(size);
		}
	}

	public Date getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}

	public void update(Workspace workspace) {
		this.setQuota(workspace.getQuota());
		this.quotaOccupied = workspace.getQuotaOccupied();
		this.setName(workspace.getName());
		this.setOwner(workspace.getOwner());
		this.setAccessLists(workspace.getAccessLists());
		this.setTimestamp(workspace.getTimestamp());
		this.online = workspace.isOnline();
		this.files = workspace.getFiles();
		this.topics = workspace.getTopics();
		this.isPrivate = workspace.isPrivate();
		this.lastOnlineTimestamp = workspace.getLastOnlineTimestamp();
	}

	public void addFile(String fileName, File file) {
		this.timestamp = Calendar.getInstance().getTime();
		files.put(fileName,file);
		quotaOccupied = file.getSize();
	}

	public void removeFile(String fileName) {
		this.timestamp = Calendar.getInstance().getTime();
		quotaOccupied -= getFiles().get(fileName).getSize();
		files.remove(fileName);
	}

	public Date getLastOnlineTimestamp() {
		return lastOnlineTimestamp;
	}

	public void setLastOnlineTimestamp(Date lastOnlineTimestamp) {
		this.lastOnlineTimestamp = lastOnlineTimestamp;
	}

	public void addConflict(String name) {
		this.conflicts.add(name);
	}

	public void fixedConflict(String name){
		this.conflicts.remove(name);
	}


}
