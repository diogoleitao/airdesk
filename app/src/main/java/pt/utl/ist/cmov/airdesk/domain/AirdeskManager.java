package pt.utl.ist.cmov.airdesk.domain;

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

	public ArrayList<String> login(String nickname, String email) {
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
		login(nickname, email);
	}

	public void addWorkspace(String nickname, String workspace) throws WorkspaceAlreadyExistsException {
		for (String workspaceName : existingWorkspaces.keySet()) {
			if (workspaceName.equals(workspace)) {
				throw new WorkspaceAlreadyExistsException();
			}
		}
		User user = getUserByNickname(nickname);
		user.createWorkspace(50, workspace);
	}

	public User getUserByNickname(String nickname) {
		if (registeredUsers.keySet().contains(nickname)) {
			return registeredUsers.get(nickname);
		} else {
			return null;
		}
	}

	// TODO
	public void mountWorkspace(ArrayList<String> nicknames, String workspace) {
		for (String nickname : nicknames) {

		}
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

	public File getFile(String name) {
		return existingWorkspaces.get(currentWorkspace).getFiles().get(name);
	}


    public void changeUserPrivileges(String workspaceName, String nickname, boolean[] choices) {
        /// TODO
    }
}
