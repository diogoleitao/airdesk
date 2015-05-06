package pt.utl.ist.cmov.airdesk.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import pt.utl.ist.cmov.airdesk.domain.exceptions.FileAlreadyExistsException;
import pt.utl.ist.cmov.airdesk.domain.exceptions.UserAlreadyHasPermissionsInWorkspaceException;
import pt.utl.ist.cmov.airdesk.domain.exceptions.UserDoesNotHavePermissionsToChangePrivilegesException;
import pt.utl.ist.cmov.airdesk.domain.exceptions.UserDoesNotHavePermissionsToCreateFilesException;
import pt.utl.ist.cmov.airdesk.domain.exceptions.UserDoesNotHavePermissionsToDeleteFileException;
import pt.utl.ist.cmov.airdesk.domain.exceptions.UserDoesNotHavePermissionsToDeleteWorkspaceException;

public class User implements Serializable, WorkspaceObserver {

	/**
	 * The user's name
	 */
	private String name;

    /**
     * The user's email, used as a GUID
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

    /**
     * List of workspaces that the user is "watching"
     */
    private ArrayList<Subject> subjects;

    public User(String name, String email) {
		this.setName(name);
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

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

    public HashMap<String, Workspace> getOwnedWorkspaces() {
		return ownedWorkspaces;
	}

	public HashMap<String, Workspace> getForeignWorkspaces() {
		return foreignWorkspaces;
	}

	public ArrayList<String> getSubscriptions() {
		return subscriptions;
	}

	public ArrayList<Subject> getSubjects() {
        return subjects;
    }

	/**
	 * Create AirdeskBroadcastReceiver workspace with AirdeskBroadcastReceiver given name and quota size
	 *
	 * @param quota the maximum size assigned to the workspace
	 * @param name the workspace's identifier
	 */
	public Workspace createWorkspace(int quota, String name, String hash) {
		Workspace workspace = new Workspace(quota, name, getEmail(), hash);
		getOwnedWorkspaces().put(name, workspace);
        return workspace;
	}

	/**
	 * Delete AirdeskBroadcastReceiver specific workspace
	 *
	 * @param name the workspace's identifier to be deleted from the user's set
	 */
	public void deleteWorkspace(String name) throws UserDoesNotHavePermissionsToDeleteWorkspaceException {
		if (getOwnedWorkspaces().containsKey(name)) {
            getOwnedWorkspaces().remove(name);
            getOwnedWorkspaces().get(name).notifyObservers();
        } else {
            if (getForeignWorkspaces().containsKey(name)) {
                if (getForeignWorkspaces().get(name).getAccessLists().get(getEmail()).canDelete()) {
                    getForeignWorkspaces().remove(name);
                    getForeignWorkspaces().get(name).notifyObservers();
                }
                else {
                    throw new UserDoesNotHavePermissionsToDeleteWorkspaceException();
                }
            }
        }


	}

	/**
	 * Add AirdeskBroadcastReceiver user to AirdeskBroadcastReceiver given workspace when the owner doesn't specify
	 * the user's privileges
	 *
	 * @param email
	 * @param workspace
	 */
	public void addUserToWorkspace(String email, String workspace) throws UserAlreadyHasPermissionsInWorkspaceException {
        if (email.equals(this.getEmail())) {
            Privileges privileges = new Privileges(true, true, true, true);
            getOwnedWorkspaces().get(workspace).getAccessLists().put(email, privileges);
            return;
        }

        if (getOwnedWorkspaces().get(workspace).getAccessLists().containsKey(email))
            throw new UserAlreadyHasPermissionsInWorkspaceException();
        Privileges privileges = new Privileges();
        getOwnedWorkspaces().get(workspace).getAccessLists().put(email, privileges);
	}

	/**
	 * Remove AirdeskBroadcastReceiver given user from AirdeskBroadcastReceiver given workspace
	 *
	 * @param email
	 * @param workspace
	 */
	public void removeUserFromWorkspace(String email, String workspace) {
		getOwnedWorkspaces().get(workspace).getAccessLists().remove(email);
	}

	/**
	 * Change AirdeskBroadcastReceiver given workspace's privacy
	 *
	 * @param workspace
	 * @param privacy
	 */
	public void changeWorkspacePrivacy(String workspace, boolean privacy) {
		getOwnedWorkspaces().get(workspace).setPrivacy(privacy);
	}

	public void mountWorkspace(Workspace workspace) {
		if (!workspace.getOwner().equals(getEmail())) {
			getForeignWorkspaces().put(workspace.getName(), workspace);
            getSubjects().add(workspace);
		}
	}

    public void unmountWorkspace(Workspace workspace) {
        if (!workspace.getOwner().equals(getEmail())) {
            getForeignWorkspaces().remove(workspace.getName());
            workspace.notifyObservers();
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
        if (workspace.getAccessLists().get(getEmail()).canDelete()) {
            workspace.updateQuotaOccupied(-workspace.getFiles().get(filename).getSize());
            workspace.getFiles().remove(filename);
            workspace.notifyObservers();
        } else {
            throw new UserDoesNotHavePermissionsToDeleteFileException();
        }
    }

    public File createFile(String currentWorkspace, String fileName) throws FileAlreadyExistsException, UserDoesNotHavePermissionsToCreateFilesException {
        Workspace workspace = getWorkspace(currentWorkspace);
        if (workspace.getAccessLists().get(getEmail()).canCreate()) {
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

    public void applyGlobalPrivileges(String workspaceName, boolean[] choices) throws UserDoesNotHavePermissionsToChangePrivilegesException {
        Workspace workspace = getWorkspace(workspaceName);
        if (workspace.getOwner().equals(this.getEmail())) {
            workspace.getAccessLists().values().remove(this.getEmail());
            for (Privileges userPrivileges : workspace.getAccessLists().values())
                userPrivileges.setAll(choices);
            workspace.getAccessLists().get(getEmail()).setAll(new boolean[]{ true,true,true,true });
        } else {
            throw new UserDoesNotHavePermissionsToChangePrivilegesException();
        }
    }

    public void changeUserPrivileges(String email, boolean[] privileges, String workspace) throws UserDoesNotHavePermissionsToChangePrivilegesException {
        if (getOwnedWorkspaces().get(workspace)!= null) {
            getOwnedWorkspaces().get(workspace).getAccessLists().get(email).setAll(privileges);
        } else
            throw new UserDoesNotHavePermissionsToChangePrivilegesException();
    }

    @Override
    public void update() {
        //TODO
    }

    @Override
    public void setSubject(Subject s) {
        this.getSubjects().add(s);
    }
}
