package pt.utl.ist.cmov.airdesk.domain;

import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;

import pt.utl.ist.cmov.airdesk.domain.exceptions.UserAlreadyExistsException;
import pt.utl.ist.cmov.airdesk.domain.exceptions.WorkspaceAlreadyExistsException;

public class AirdeskManager {

	/**
	 * This class' singleton
	 */
	private static AirdeskManager instance = null;

	/**
	 * Mapping between users' nicknames and User objects
	 */
	private static HashMap<String, User> registeredUsers = new HashMap<String, User>();

	/**
	 * Mapping between workspaces' names and Workspace objects
	 */
	private static HashMap<String, Workspace> existingWorkspaces = new HashMap<String, Workspace>();

	/**
	 * Mapping between files' names and File objects
	 */
	private static HashMap<String, File> createdFiles = new HashMap<String, File>();

	/**
	 * The currently logged in user nickname
	 */
	private String loggedUser = "";

	/**
	 * The currently opened workspace name
	 */
	private String currentWorkspace = "";

	/**
	 * The currently opened file name
	 */
	private String currentFile = "";

	private AirdeskManager() {}

	public static AirdeskManager getInstance() {
		if (instance == null) {
			instance = new AirdeskManager();
			populateAirdesk();
		}
		return instance;
	}

    public User getLoggedUser() {
        return registeredUsers.get(loggedUser);
    }

	public static void populateAirdesk() {
		ArrayList<String> newUsers = new ArrayList<String>();
		ArrayList<String> newWorkspaces = new ArrayList<String>();
		ArrayList<String> newFiles = new ArrayList<String>();
		ArrayList<String> newFileContents = new ArrayList<String>();

		// CREATE SOME GIBBERISH DATA TO FILL THE "DATABASE"
		for (int i = 0; i < 10; i ++) {
			newUsers.add("user" + i);
			newWorkspaces.add("workspace" + i);
			newFiles.add("file" + i);
			if (i % 2 == 0) {
				newFileContents.add("I have some content " + i);
			}
		}

		// ADD THE GIBBERISH DATA TO "DATABASE";
		// ALL DATA CREATION AND MANIPULATION IS DONE MANUALLY
		// TO PREVENT (AND LATER, TO DETECT) ERRORS
		for (int i = 0; i < 10; i++) {
			// CREATE USER
			User newUser = new User(newUsers.get(i), newUsers.get(i), newUsers.get(i));
			registeredUsers.put(newUsers.get(i), newUser);

			// CREATE WORKSPACE AND ADD TO PREVIOUSLY CREATED USER'S WORKSPACE SET
			Workspace newWorkspace = new Workspace(50, newWorkspaces.get(i), newUsers.get(i));
			existingWorkspaces.put(newWorkspaces.get(i), newWorkspace);
			newUser.getOwnedWorkspaces().put(newWorkspaces.get(i), newWorkspace);

			// CREATE FILE, ADD CONTENT AND ADD TO PREVIOUSLY CREATED WORKSPACE'S FILE SET
			File newFile = new File(newFiles.get(i));
			if (i % 2 == 0)
				newFile.setContent(newFileContents.get(i/2));
			createdFiles.put(newFiles.get(i), newFile);
			newWorkspace.getFiles().put(newFiles.get(i), newFile);
		}
	}

	public ArrayList<String> login(String nickname) {
		ArrayList<String> workspaceNames = new ArrayList<String>();

		loggedUser = nickname;
		if (registeredUsers.get(loggedUser) != null) {
			workspaceNames.addAll(registeredUsers.get(loggedUser).getOwnedWorkspaces().keySet());
			workspaceNames.addAll(registeredUsers.get(loggedUser).getForeignWorkspaces().keySet());
			return workspaceNames;
		} else {
			loggedUser = null;
			return null;
		}
	}

	public void registerUser(String name, String nickname, String email) throws UserAlreadyExistsException {
        User user = new User(name, nickname, email);
        if (getUserByNickname(nickname) == null) {
            registeredUsers.put(nickname, user);
		} else {
			throw new UserAlreadyExistsException();
		}
        loggedUser = nickname;
	}

	public void addWorkspace(String workspaceName, int quota) throws WorkspaceAlreadyExistsException {
		for (String w : existingWorkspaces.keySet()) {
			if (w.equals(workspaceName)) {
				throw new WorkspaceAlreadyExistsException();
			}
		}
        Workspace workspace = new Workspace(quota, workspaceName, loggedUser);
        HashMap<String, Privileges> accessList = new HashMap<String, Privileges>();
        accessList.put(loggedUser, new Privileges(true,true,true,true));
        workspace.setAccessLists(accessList);
        existingWorkspaces.put(workspaceName, workspace);
		User user = getUserByNickname(loggedUser);
		user.createWorkspace(quota, workspaceName);
	}

	public User getUserByNickname(String nickname) {
		if (registeredUsers.keySet().contains(nickname)) {
			return registeredUsers.get(nickname);
		} else {
			return null;
		}
	}

    public ArrayList<String> getUsersFromWorkspace() {
        return existingWorkspaces.get(currentWorkspace).getUsers();
    }

	// TODO
	public void mountWorkspace(ArrayList<String> nicknames, String workspace) {
		for (String nickname : nicknames) {

		}
	}

	public ArrayList<String> getFilesFromWorkspace(String workspace) {
		ArrayList<String> fileNames = new ArrayList<String>();

		if (registeredUsers.get(loggedUser).getOwnedWorkspaces().get(workspace) != null)
			fileNames.addAll(registeredUsers.get(loggedUser).getOwnedWorkspaces().get(workspace).getFiles().keySet());
		if (registeredUsers.get(loggedUser).getForeignWorkspaces().get(workspace) != null)
			fileNames.addAll(registeredUsers.get(loggedUser).getForeignWorkspaces().get(workspace).getFiles().keySet());

		return fileNames;
	}

    public void addNewFile(String fileName) {
        File newFile = new File(fileName);
        existingWorkspaces.get(currentWorkspace).getFiles().put(fileName, newFile);
        getLoggedUser().getOwnedWorkspaces().get(currentWorkspace).getFiles().put(fileName, newFile);
    }

	public File getFile(String name) {
        currentFile = name;
		return existingWorkspaces.get(currentWorkspace).getFiles().get(name);
	}

    public void saveFile(String content) {
        existingWorkspaces.get(currentWorkspace).getFiles().get(currentFile).save(content);
        existingWorkspaces.get(currentWorkspace).updateQuotaOccupied(content.length());
    }

    public void changeUserPrivileges(String nickname, boolean[] privileges) {
        existingWorkspaces.get(currentWorkspace).getAccessLists().get(nickname).setAll(privileges);
    }

    public void applyGlobalPrivileges(String workspaceName, boolean[] choices) {
        //TODO
        Log.d("applyGlobalPrivileges","applyGlobalPrivileges");
    }

    public void inviteUser(String workspaceName, String username) {
        //TODO
        Log.d("inviteUser","inviteUser");
    }

    public void deleteWorkspace(String workspaceName) {
        //TODO
        Log.d("deleteWorkspace", "deleteWorkspace");
    }

    public int getTotalQuota(String workspaceName) {
        return existingWorkspaces.get(workspaceName).getQuota();
    }

    public int getUsedQuota(String workspaceName) {
        return existingWorkspaces.get(workspaceName).getQuotaOccupied();
    }

    public boolean[] getUserPrivileges(String nickname) {
        if (existingWorkspaces.get(currentWorkspace).getAccessLists().get(nickname) == null)
            return new boolean[] {false,false,false,false};
        return existingWorkspaces.get(currentWorkspace).getAccessLists().get(nickname).getAll();
    }

    public void logout() {
        loggedUser="";
    }

    public void deleteFile(String filename) {
    }

    public void renameFile(String filename, String newfilename) {

    }

    public String getCurrentFile() {
        return currentFile;
    }

    public String getCurrentWorkspace() {
        return currentWorkspace;
    }

    public void setCurrentWorkspace(String currentWorkspace) {
        this.currentWorkspace = currentWorkspace;
    }

    public void setCurrentFile(String currentFile) {
        this.currentFile = currentFile;
    }
}
