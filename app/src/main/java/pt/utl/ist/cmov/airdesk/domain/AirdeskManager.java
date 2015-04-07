package pt.utl.ist.cmov.airdesk.domain;

import java.util.ArrayList;
import java.util.HashMap;

import pt.utl.ist.cmov.airdesk.domain.exceptions.FileAlreadyExistsException;
import pt.utl.ist.cmov.airdesk.domain.exceptions.UserAlreadyExistsException;
import pt.utl.ist.cmov.airdesk.domain.exceptions.UserDoesNotExistException;
import pt.utl.ist.cmov.airdesk.domain.exceptions.UserDoesNotHavePermissionsToCreateFilesException;
import pt.utl.ist.cmov.airdesk.domain.exceptions.UserDoesNotHavePermissionsToDeleteFileException;
import pt.utl.ist.cmov.airdesk.domain.exceptions.UserDoesNotHavePermissionsToDeleteWorkspaceException;
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
			newWorkspace.getFiles().put(newFiles.get(i), newFile);
		}
	}

	public ArrayList<String> login(String nickname) {
		ArrayList<String> workspaceNames = new ArrayList<String>();

		loggedUser = nickname;
		if (registeredUsers.keySet().contains(loggedUser)) {
			workspaceNames.addAll(registeredUsers.get(loggedUser).getOwnedWorkspaces().keySet());
			workspaceNames.addAll(registeredUsers.get(loggedUser).getForeignWorkspaces().keySet());
			return workspaceNames;
		} else {
			loggedUser = "";
			return null;
		}
	}

    public void logout() {
        loggedUser = "";
        currentWorkspace = "";
        currentFile = "";
    }

	public void registerUser(String name, String nickname, String email) throws UserAlreadyExistsException {
		if (getUserByNickname(nickname) == null) {
			registeredUsers.put(nickname, new User(name, nickname, email));
            loggedUser = nickname;
		} else {
			throw new UserAlreadyExistsException();
		}
	}

	public void addWorkspace(String workspaceName, int quota) throws WorkspaceAlreadyExistsException {
		for (String w : existingWorkspaces.keySet()) {
			if (w.equals(workspaceName)) {
				throw new WorkspaceAlreadyExistsException();
			}
		}

        User user = getUserByNickname(loggedUser);
		existingWorkspaces.put(workspaceName, user.createWorkspace(quota, workspaceName));
	}

    public void deleteWorkspace(String workspaceName) throws UserDoesNotHavePermissionsToDeleteWorkspaceException {
        registeredUsers.get(loggedUser).deleteWorkspace(workspaceName);
        existingWorkspaces.remove(workspaceName);
    }

    public ArrayList<String> getFilesFromWorkspace(String workspace) {
        ArrayList<String> fileNames = new ArrayList<String>();

        currentWorkspace = workspace;

        if (registeredUsers.get(loggedUser).getOwnedWorkspaces().get(currentWorkspace) != null)
            fileNames.addAll(registeredUsers.get(loggedUser).getOwnedWorkspaces().get(currentWorkspace).getFiles().keySet());
        if (registeredUsers.get(loggedUser).getForeignWorkspaces().get(currentWorkspace) != null)
            fileNames.addAll(registeredUsers.get(loggedUser).getForeignWorkspaces().get(currentWorkspace).getFiles().keySet());

        return fileNames;
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

	public void addNewFile(String fileName) throws FileAlreadyExistsException, UserDoesNotHavePermissionsToCreateFilesException {
		existingWorkspaces.get(currentWorkspace).getFiles().put(fileName, registeredUsers.get(loggedUser).createFile(currentWorkspace, fileName));
	}

	public File getFile(String name) {
		currentFile = name;
		return existingWorkspaces.get(currentWorkspace).getFiles().get(name);
	}

	public void saveFile(String content) {
		existingWorkspaces.get(currentWorkspace).getFiles().get(currentFile).save(content);
		existingWorkspaces.get(currentWorkspace).updateQuotaOccupied(content.length());
	}

    public void deleteFile(String fileName) throws UserDoesNotHavePermissionsToDeleteFileException {
        registeredUsers.get(loggedUser).deleteFile(currentWorkspace, fileName);
        existingWorkspaces.get(currentWorkspace).getFiles().remove(currentFile);
    }

	public void changeUserPrivileges(String nickname, boolean[] privileges) {
        // TODO: change this to be apllied from a user's perspective
		existingWorkspaces.get(currentWorkspace).getAccessLists().get(nickname).setAll(privileges);
	}

	public void applyGlobalPrivileges(String workspaceName, boolean[] choices) {
        // TODO: change this to be apllied from a user's perspective
		for (Privileges userPrivileges : existingWorkspaces.get(workspaceName).getAccessLists().values())
            userPrivileges.setAll(choices);
	}

	public void inviteUser(String workspaceName, String username) throws UserDoesNotExistException {
        if (!registeredUsers.containsKey(username))
            throw new UserDoesNotExistException();
        else {
            registeredUsers.get(loggedUser).addUserToWorkspace(username, workspaceName);
            registeredUsers.get(username).mountWorkspace(existingWorkspaces.get(workspaceName));
            existingWorkspaces.get(workspaceName).getUsers().add(username);
        }
	}

	public int getTotalQuota(String workspaceName) {
		return existingWorkspaces.get(workspaceName).getQuota();
	}

	public int getUsedQuota(String workspaceName) {
		return existingWorkspaces.get(workspaceName).getQuotaOccupied();
	}

	public boolean[] getUserPrivileges(String nickname) {
		return existingWorkspaces.get(currentWorkspace).getAccessLists().get(nickname).getAll();
	}

    public String getLoggedUser() {
        return loggedUser;
    }

	public String getCurrentWorkspace() {
		return currentWorkspace;
	}

	public String getCurrentFile() {
		return currentFile;
	}

    public void setCurrentWorkspace(String currentWorkspace) {
        this.currentWorkspace = currentWorkspace;
    }
}
