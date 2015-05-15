package pt.utl.ist.cmov.airdesk.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

import pt.utl.ist.cmov.airdesk.domain.exceptions.FileAlreadyExistsException;
import pt.utl.ist.cmov.airdesk.domain.exceptions.UserAlreadyHasPermissionsInWorkspaceException;
import pt.utl.ist.cmov.airdesk.domain.exceptions.UserDoesNotHavePermissionsToChangePrivilegesException;
import pt.utl.ist.cmov.airdesk.domain.exceptions.UserDoesNotHavePermissionsToCreateFilesException;
import pt.utl.ist.cmov.airdesk.domain.exceptions.UserDoesNotHavePermissionsToDeleteFileException;
import pt.utl.ist.cmov.airdesk.domain.exceptions.UserDoesNotHavePermissionsToDeleteWorkspaceException;
import pt.utl.ist.cmov.airdesk.domain.network.GlobalService;

public class User implements Serializable {

    private String email;

    private HashMap<String, Workspace> ownedWorkspaces = new HashMap<String, Workspace>();

    private HashMap<String, Workspace> foreignWorkspaces = new HashMap<String, Workspace>();

    private ArrayList<String> topics = new ArrayList<String>();

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
        return workspace;
    }

    public void deleteWorkspace(String hash) throws UserDoesNotHavePermissionsToDeleteWorkspaceException {
        if (this.getOwnedWorkspaces().containsKey(hash)) {
            this.getOwnedWorkspaces().remove(hash);
        } else {
            if (getForeignWorkspaces().containsKey(hash)) {
                if (this.getForeignWorkspaces().get(hash).getAccessLists().get(this.getEmail()).canDelete()) {}
                else {
                    throw new UserDoesNotHavePermissionsToDeleteWorkspaceException();
                }
            }
        }
    }

    public void deleteForeignWorkspace(String hash) {
        if (getForeignWorkspaces().containsKey(hash))
            this.getForeignWorkspaces().remove(hash);
    }

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
            workspace.removeFile(filename);
        } else {
            throw new UserDoesNotHavePermissionsToDeleteFileException();
        }
    }

    public void deleteForeignFile(String workspaceHash, String filename) {
        Workspace workspace = getWorkspace(workspaceHash);
        workspace.removeFile(filename);
    }

    public File createFile(String currentWorkspace, String fileName) throws FileAlreadyExistsException, UserDoesNotHavePermissionsToCreateFilesException {
        Workspace workspace = getWorkspace(currentWorkspace);
        if (workspace.getAccessLists().get(getEmail()).canCreate()) {
            if (workspace.getFiles().containsKey(fileName))
                throw new FileAlreadyExistsException();
            else {
                File file = new File(fileName);
                workspace.addFile(fileName, file);
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
            workspace.getAccessLists().get(getEmail()).setAll(new boolean[]{true, true, true, true});
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

    public ArrayList<String> getTopics() {
        return topics;
    }

    public boolean addTopic(String topic) {
        boolean result = false;
        if(!topics.contains(topic)) {
            topics.add(topic);
            result = true;
            BroadcastMessage messageTopics = new BroadcastMessage(BroadcastMessage.MessageTypes.WORKSPACE_TOPICS_REQUEST, getEmail());
            messageTopics.setTopics(getTopics());
            GlobalService.broadcastMessage(messageTopics);
        }
        return result;
    }

    public void removeTopic(String s) {
        topics.remove(s);
    }
}
