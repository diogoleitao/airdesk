package pt.utl.ist.cmov.airdesk.domain;

import java.io.Serializable;
import java.util.HashMap;

import pt.utl.ist.cmov.airdesk.domain.exceptions.FileAlreadyExistsException;
import pt.utl.ist.cmov.airdesk.domain.exceptions.UserAlreadyHasPermissionsInWorkspaceException;
import pt.utl.ist.cmov.airdesk.domain.exceptions.UserDoesNotHavePermissionsToChangePrivilegesException;
import pt.utl.ist.cmov.airdesk.domain.exceptions.UserDoesNotHavePermissionsToCreateFilesException;
import pt.utl.ist.cmov.airdesk.domain.exceptions.UserDoesNotHavePermissionsToDeleteFileException;
import pt.utl.ist.cmov.airdesk.domain.exceptions.UserDoesNotHavePermissionsToDeleteWorkspaceException;

public class User implements Serializable, Observer {


    /**
     * The user's email, used as a GUID
     */
	private String email;


    private HashMap<String, Workspace> ownedWorkspaces = new HashMap<String, Workspace>();


    private HashMap<String, Workspace> foreignWorkspaces = new HashMap<String, Workspace>();

    public User(String email) {
		this.setEmail(email);
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

    public HashMap<String, Workspace> getAllWorkspaces() {
        HashMap<String, Workspace> result =  new HashMap<String, Workspace>();
        result.putAll(foreignWorkspaces);
        result.putAll(ownedWorkspaces);
        return result;
    }


	public Workspace createWorkspace(int quota, String name, String hash) {
		Workspace workspace = new Workspace(quota, name, getEmail(), hash);
        getOwnedWorkspaces().put(hash, workspace);
        getWorkspace(hash).register(this);
        return workspace;
	}


	public void deleteWorkspace(String hash) throws UserDoesNotHavePermissionsToDeleteWorkspaceException {
        if (this.getOwnedWorkspaces().containsKey(hash)) {
            this.getOwnedWorkspaces().remove(hash).unregister(this);
        } else {
            if (getForeignWorkspaces().containsKey(hash)) {
                if (this.getForeignWorkspaces().get(hash).getAccessLists().get(this.getEmail()).canDelete()) {
                    this.getForeignWorkspaces().remove(hash).unregister(this);
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
			getForeignWorkspaces().put(workspace.getHash(), workspace);
		}
	}

    public void unmountWorkspace(Workspace workspace) {
        if (!workspace.getOwner().equals(getEmail())) {
            getForeignWorkspaces().remove(workspace.getHash());
        }
    }

    public Workspace getWorkspace(String workspaceHash) {
        Workspace workspace;

        if (getOwnedWorkspaces().containsKey(workspaceHash))
            workspace = getOwnedWorkspaces().get(workspaceHash);
        else if (getForeignWorkspaces().containsKey(workspaceHash))
            workspace = getForeignWorkspaces().get(workspaceHash);
        else
            workspace = null;

        return workspace;
    }

    public void deleteFile(String workspaceHash, String filename) throws UserDoesNotHavePermissionsToDeleteFileException {
        Workspace workspace = getWorkspace(workspaceHash);
        if (workspace.getAccessLists().get(getEmail()).canDelete()) {
            workspace.updateQuotaOccupied(-workspace.getFiles().get(filename).getSize());
            workspace.getFiles().remove(filename).unregister(this);
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
                file.register(this);
                return file;
            }
        } else {
            throw new UserDoesNotHavePermissionsToCreateFilesException();
        }
    }

    public void applyGlobalPrivileges(String workspaceHash, boolean[] choices) throws UserDoesNotHavePermissionsToChangePrivilegesException {
        Workspace workspace = getWorkspace(workspaceHash);
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
        if (getOwnedWorkspaces().get(workspace) != null) {
            getOwnedWorkspaces().get(workspace).getAccessLists().get(email).setAll(privileges);
        } else
            throw new UserDoesNotHavePermissionsToChangePrivilegesException();
    }
}
