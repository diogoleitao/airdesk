package pt.utl.ist.cmov.airdesk.domain;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

import pt.utl.ist.cmov.airdesk.domain.exceptions.FileAlreadyExistsException;
import pt.utl.ist.cmov.airdesk.domain.exceptions.TopicAlreadyAddedException;
import pt.utl.ist.cmov.airdesk.domain.exceptions.UserAlreadyExistsException;
import pt.utl.ist.cmov.airdesk.domain.exceptions.UserDoesNotExistException;
import pt.utl.ist.cmov.airdesk.domain.exceptions.UserDoesNotHavePermissionsToChangePrivilegesException;
import pt.utl.ist.cmov.airdesk.domain.exceptions.UserDoesNotHavePermissionsToCreateFilesException;
import pt.utl.ist.cmov.airdesk.domain.exceptions.UserDoesNotHavePermissionsToDeleteFileException;
import pt.utl.ist.cmov.airdesk.domain.exceptions.UserDoesNotHavePermissionsToDeleteWorkspaceException;
import pt.utl.ist.cmov.airdesk.domain.exceptions.WorkspaceAlreadyExistsException;
import pt.utl.ist.cmov.airdesk.domain.exceptions.WorkspaceQuotaReachedException;

public class AirdeskManager implements Serializable {

    static String filename = "AirdeskState";

	/**
	 * This class' singleton
	 */
	private static AirdeskManager instance = null;

	/**
	 * Mapping between users' emails and User objects
	 */
	private static HashMap<String, User> registeredUsers = new HashMap<String, User>();

	/**
	 * Mapping between workspaces' names and Workspace objects
	 */
	private static HashMap<String, Workspace> existingWorkspaces = new HashMap<String, Workspace>();

	/**
	 * The currently logged in user email
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

	public static AirdeskManager getInstance(Context context) {
		if (instance == null) {
			instance = new AirdeskManager();

            try {

                FileInputStream fileInputStream = context.openFileInput(filename);

                ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);

                int duration = Toast.LENGTH_SHORT;

                existingWorkspaces = (HashMap<String, Workspace>)objectInputStream.readObject();
                registeredUsers = (HashMap<String, User>)objectInputStream.readObject();

                objectInputStream.close();
                fileInputStream.close();

            } catch (FileNotFoundException e) {
                populateAirdesk();
            }  catch (ClassNotFoundException e) {
                e.printStackTrace();
            }  catch (IOException e) {
                e.printStackTrace();
            }
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
			User newUser = new User(newUsers.get(i), newUsers.get(i));
			registeredUsers.put(newUsers.get(i), newUser);

			// CREATE WORKSPACE AND ADD TO PREVIOUSLY CREATED USER'S WORKSPACE SET
			Workspace newWorkspace = new Workspace(5000, newWorkspaces.get(i), newUsers.get(i));
			existingWorkspaces.put(newWorkspaces.get(i), newWorkspace);
			newUser.getOwnedWorkspaces().put(newWorkspaces.get(i), newWorkspace);

			// CREATE FILE, ADD CONTENT AND ADD TO PREVIOUSLY CREATED WORKSPACE'S FILE SET
			File newFile = new File(newFiles.get(i));
			if (i % 2 == 0)
				newFile.save(newFileContents.get(i/2));
			newWorkspace.getFiles().put(newFiles.get(i), newFile);
            newWorkspace.updateQuotaOccupied(newFile.getSize());
		}
	}

    public ArrayList<String> getWorkspaces(String email){
        ArrayList<String> workspaceNames = new ArrayList<String>();
        if (registeredUsers.keySet().contains(email)) {
            workspaceNames.addAll(registeredUsers.get(email).getOwnedWorkspaces().keySet());
            return workspaceNames;
        } else {
            return null;
        }
    }

    public ArrayList<String> getForeignWorkspaces(String email){
        ArrayList<String> workspaceNames = new ArrayList<String>();
        if (registeredUsers.keySet().contains(email)) {
            workspaceNames.addAll(registeredUsers.get(email).getForeignWorkspaces().keySet());
            return workspaceNames;
        } else {
            return null;
        }
    }

	public boolean login(String email) {
		loggedUser = email;
        return registeredUsers.keySet().contains(email);
	}

    public void logout() {
        loggedUser = "";
        currentWorkspace = "";
        currentFile = "";
    }

	public void registerUser(String name, String email) throws UserAlreadyExistsException {
		if (getUserByEmail(email) == null) {
			registeredUsers.put(email, new User(name, email));
            loggedUser = email;
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
		existingWorkspaces.put(workspaceName, getUserByEmail(loggedUser).createWorkspace(quota*1024, workspaceName));
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

    public void addTopicToWorkspace(String topic) throws TopicAlreadyAddedException {
        registeredUsers.get(loggedUser).getWorkspace(currentWorkspace).addTopic(topic);
    }

	public User getUserByEmail(String email) {
		if (registeredUsers.keySet().contains(email)) {
			return registeredUsers.get(email);
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

	public void saveFile(String content) throws WorkspaceQuotaReachedException {
        registeredUsers.get(loggedUser).getWorkspace(currentWorkspace).saveFile(currentFile, content);
	}

    public void deleteFile(String fileName) throws UserDoesNotHavePermissionsToDeleteFileException {
        registeredUsers.get(loggedUser).deleteFile(currentWorkspace, fileName);
        existingWorkspaces.get(currentWorkspace).getFiles().remove(currentFile);
    }

	public void changeUserPrivileges(String email, boolean[] privileges) throws UserDoesNotHavePermissionsToChangePrivilegesException {
        // TODO: change this to be applied from a user's perspective
        registeredUsers.get(loggedUser).changeUserPrivileges(email, privileges, currentWorkspace);
	}

	public void applyGlobalPrivileges(String workspaceName, boolean[] choices) throws UserDoesNotHavePermissionsToChangePrivilegesException {
        registeredUsers.get(loggedUser).applyGlobalPrivileges(workspaceName, choices);
	}

	public void inviteUser(String workspaceName, String username) throws UserDoesNotExistException {
        if (!registeredUsers.containsKey(username))
            throw new UserDoesNotExistException();
        else {
            registeredUsers.get(loggedUser).addUserToWorkspace(username, workspaceName);
            registeredUsers.get(username).mountWorkspace(existingWorkspaces.get(workspaceName));
            existingWorkspaces.get(workspaceName).addUser(username);
        }
	}

	public int getTotalQuota(String workspaceName) {
		return existingWorkspaces.get(workspaceName).getQuota();
	}

	public int getUsedQuota(String workspaceName) {
		return existingWorkspaces.get(workspaceName).getQuotaOccupied();
	}

	public boolean[] getUserPrivileges(String email) {
		return existingWorkspaces.get(currentWorkspace).getAccessLists().get(email).getAll();
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

    public void saveAppState(Context context) {

        try {
            FileOutputStream fileOutputStream = context.openFileOutput(filename, Context.MODE_PRIVATE);
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);

            objectOutputStream.writeObject(existingWorkspaces);
            objectOutputStream.writeObject(registeredUsers);

            objectOutputStream.close();
            fileOutputStream.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public ArrayList<String> getTopics() {
        return existingWorkspaces.get(currentWorkspace).getTopics();
    }

    public void setWorkspacePrivacy(String workspaceName, boolean isprivate) {
        existingWorkspaces.get(workspaceName).setPrivacy(isprivate);
    }

    public boolean getWorkspacePrivacy(String workspaceName) {
        return existingWorkspaces.get(workspaceName).isPrivate();
    }
}
