package pt.utl.ist.cmov.airdesk.domain;

import java.util.ArrayList;
import java.util.HashMap;

public class User {

	/**
	 * The user's name
	 */
	private String name;

	/**
	 * The user's nickname (derived from the email)
	 */
	private String nickname;

	/**
	 * The user's email
	 */
	private String email;

	/**
	 * Mapping between the user's workspaces' names that he created and the Workspace objects
	 */
	private HashMap<String, Workspace> ownedWorkspaces;

	/**
	 * Mapping between the user's workspaces' names that he joined/got invited to and the Workspace objects
	 */
	private HashMap<String, Workspace> foreignWorkspaces;

	/**
	 * List of topics that the user is interested in, regarding workspaces
	 */
	private ArrayList<String> subscriptions;

	public User() {}

	public User(String name, String nickname, String email) {
		this.setName(name);
		this.setNickname(nickname);
		this.setEmail(email);
		this.ownedWorkspaces = new HashMap<String, Workspace>();
		this.foreignWorkspaces = new HashMap<String, Workspace>();
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getNickname() {
		return nickname;
	}

	public void setNickname(String nickname) {
		this.nickname = nickname;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public HashMap<String, Workspace> getOwnedWorkspaces() {
		return ownedWorkspaces;
	}

	public void setOwnedWorkspaces(HashMap<String, Workspace> ownedWorkspaces) {
		this.ownedWorkspaces = ownedWorkspaces;
	}

	public HashMap<String, Workspace> getForeignWorkspaces() {
		return foreignWorkspaces;
	}

	public void setForeignWorkspaces(HashMap<String, Workspace> foreignWorkspaces) {
		this.foreignWorkspaces = foreignWorkspaces;
	}

	public ArrayList<String> getSubscriptions() {
		return subscriptions;
	}

	public void setSubscriptions(ArrayList<String> subscriptions) {
		this.subscriptions = subscriptions;
	}

	/**
	 * Create a workspace with a given name and quota size
	 *
	 * @param quota the maximum size assigned to the workspace
	 * @param name the workspace's identifier
	 * @return a new workspace object with size 'quota' and identifier 'name'
	 */
	public void createWorkspace(int quota, String name) {
		Workspace workspace = new Workspace(quota, name, getNickname());
		ownedWorkspaces.put(name, workspace);
	}

	/**
	 * Delete a specific workspace
	 *
	 * @param name the workspace's identifier to be deleted from the user's set
	 * @return the deleted workspace's identifier
	 */
	public void deleteWorkspace(String name) {
		if (getOwnedWorkspaces().containsKey(name)) {
			Workspace workspace = getOwnedWorkspaces().get(name);
			ownedWorkspaces.remove(name);
		}
	}

	/**
	 * Add a user to a given workspace when the owner doesn't specify
	 * the user's privileges
	 *
	 * @param nickname
	 * @param workspace
	 */
	public void addUserToWorkspace(String nickname, String workspace) {
		Privileges privileges = new Privileges();
		getOwnedWorkspaces().get(workspace).getAccessLists().put(nickname, privileges);
	}

	/**
	 * Remove a giver user from a given workspace
	 *
	 * @param nickname
	 * @param workspace
	 */
	public void removeUserFromWorkspace(String nickname, String workspace) {
		getOwnedWorkspaces().get(workspace).getAccessLists().remove(nickname);
	}

	/**
	 * Change a given workspace's privacy
	 *
	 * @param workspace
	 * @param privacy
	 */
	public void changeWorkspacePrivacy(String workspace, boolean privacy) {
		getOwnedWorkspaces().get(workspace).setPrivacy(privacy);
	}

	/**
	 * Set a user's read privileges for a given workspace
	 *
	 * @param nickname the user's nickname
	 * @param workspace the workspace identifier (its name)
	 * @param canRead true if the user can read files on this workspace; false, otherwise
	 */
	public void setUserReadPrivileges(String nickname, String workspace, boolean canRead) {
		getOwnedWorkspaces().get(workspace).getAccessLists().get(nickname).setReadPrivilege(canRead);
	}

	/**
	 * Set a user's read privileges for a given workspace
	 *
	 * @param nickname the user's nickname
	 * @param workspace the workspace identifier (its name)
	 * @param canWrite true if the user can edit files on this workspace; false, otherwise
	 */
	public void setUserWritePrivileges(String nickname, String workspace, boolean canWrite) {
		getOwnedWorkspaces().get(workspace).getAccessLists().get(nickname).setReadPrivilege(canWrite);
	}

	/**
	 * Set a user's create privileges for a given workspace
	 *
	 * @param nickname the user's nickname
	 * @param workspace the workspace identifier (its name)
	 * @param canCreate true if the user can create files on this workspace; false, otherwise
	 */
	public void setUserCreatePrivileges(String nickname, String workspace, boolean canCreate) {
		getOwnedWorkspaces().get(workspace).getAccessLists().get(nickname).setReadPrivilege(canCreate);
	}

	/**
	 * Set a user's delete privilege for a given workspace
	 *
	 * @param nickname the user's nickname
	 * @param workspace the workspace identifier (its name)
	 * @param canDelete true if the user can delete files on this workspace; false, otherwise
	 */
	public void setUserDeletePrivileges(String nickname, String workspace, boolean canDelete) {
		getOwnedWorkspaces().get(workspace).getAccessLists().get(nickname).setReadPrivilege(canDelete);
	}

	public void mountWorkspace(Workspace workspace) {
		if (!workspace.getOwner().equals(getNickname())) {
			foreignWorkspaces.put(workspace.getName(), workspace);
		}
	}
}
