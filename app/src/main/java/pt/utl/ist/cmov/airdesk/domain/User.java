package pt.utl.ist.cmov.airdesk.domain;

import java.util.ArrayList;
import java.util.HashMap;

import pt.utl.ist.cmov.airdesk.domain.exceptions.FileAlreadyExistsException;
import pt.utl.ist.cmov.airdesk.domain.exceptions.UserDoesNotHavePermissionsToCreateFilesException;
import pt.utl.ist.cmov.airdesk.domain.exceptions.UserDoesNotHavePermissionsToDeleteFileException;
import pt.utl.ist.cmov.airdesk.domain.exceptions.UserDoesNotHavePermissionsToDeleteWorkspaceException;

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
	 */
	public Workspace createWorkspace(int quota, String name) {
		Workspace workspace = new Workspace(quota, name, getNickname());
		ownedWorkspaces.put(name, workspace);
        return workspace;
	}

	/**
	 * Delete a specific workspace
	 *
	 * @param name the workspace's identifier to be deleted from the user's set
	 */
	public void deleteWorkspace(String name) throws UserDoesNotHavePermissionsToDeleteWorkspaceException {
		if (getOwnedWorkspaces().containsKey(name))
            ownedWorkspaces.remove(name);
        else {
            if (getForeignWorkspaces().containsKey(name)) {
                if (getForeignWorkspaces().get(name).getAccessLists().get(getNickname()).canDelete()) {
                    foreignWorkspaces.remove(name);
                }
                else {
                    throw new UserDoesNotHavePermissionsToDeleteWorkspaceException();
                }
            }
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
	 * Remove a given user from a given workspace
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
		getOwnedWorkspaces().get(workspace).getAccessLists().get(nickname).setWritePrivilege(canWrite);
	}

	/**
	 * Set a user's create privileges for a given workspace
	 *
	 * @param nickname the user's nickname
	 * @param workspace the workspace identifier (its name)
	 * @param canCreate true if the user can create files on this workspace; false, otherwise
	 */
	public void setUserCreatePrivileges(String nickname, String workspace, boolean canCreate) {
		getOwnedWorkspaces().get(workspace).getAccessLists().get(nickname).setCreatePrivilege(canCreate);
	}

	/**
	 * Set a user's delete privilege for a given workspace
	 *
	 * @param nickname the user's nickname
	 * @param workspace the workspace identifier (its name)
	 * @param canDelete true if the user can delete files on this workspace; false, otherwise
	 */
	public void setUserDeletePrivileges(String nickname, String workspace, boolean canDelete) {
		getOwnedWorkspaces().get(workspace).getAccessLists().get(nickname).setDeletePrivilege(canDelete);
	}

	public void mountWorkspace(Workspace workspace) {
		if (!workspace.getOwner().equals(getNickname())) {
			foreignWorkspaces.put(workspace.getName(), workspace);
		}
	}

    public Workspace getWorkspace(String workspaceName) {
        Workspace workspace;

        if (getOwnedWorkspaces().containsKey(workspaceName))
            workspace = getOwnedWorkspaces().get(workspaceName);
        else if (getForeignWorkspaces().containsKey(workspaceName))
            workspace = getForeignWorkspaces().get(workspaceName);
        else
            workspace = null;

        return workspace;
    }

    public void deleteFile(String workspaceName, String filename) throws UserDoesNotHavePermissionsToDeleteFileException {
        Workspace workspace = getWorkspace(workspaceName);
        if (workspace.getAccessLists().get(getNickname()).canDelete()) {
            workspace.getFiles().remove(filename);
        } else {
            throw new UserDoesNotHavePermissionsToDeleteFileException();
        }
    }

    public File createFile(String currentWorkspace, String fileName) throws FileAlreadyExistsException, UserDoesNotHavePermissionsToCreateFilesException {
        Workspace workspace = getWorkspace(currentWorkspace);
        if (workspace.getAccessLists().get(getNickname()).canCreate()) {
            if (workspace.getFiles().containsKey(fileName))
                throw new FileAlreadyExistsException();
            else {
                File file = new File(fileName);
                workspace.getFiles().put(fileName, file);
                return file;
            }
        } else {
            throw new UserDoesNotHavePermissionsToCreateFilesException();
        }
    }
}
