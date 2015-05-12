package pt.utl.ist.cmov.airdesk.domain;

import android.app.Activity;
import android.content.Context;
import android.view.View;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;

import pt.utl.ist.cmov.airdesk.activities.ListWorkspaces;
import pt.utl.ist.cmov.airdesk.activities.MainActivity;
import pt.utl.ist.cmov.airdesk.domain.exceptions.FileAlreadyExistsException;
import pt.utl.ist.cmov.airdesk.domain.exceptions.TopicAlreadyAddedException;
import pt.utl.ist.cmov.airdesk.domain.exceptions.UserAlreadyExistsException;
import pt.utl.ist.cmov.airdesk.domain.exceptions.UserAlreadyHasPermissionsInWorkspaceException;
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
     *
     */
    private static WifiManager wifiManager;

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

    private Activity currentActivity = null;

	private AirdeskManager() {}

	public static AirdeskManager getInstance(Context context) {
		if (instance == null) {
			instance = new AirdeskManager();
            wifiManager = new WifiManager();

            try {
                FileInputStream fileInputStream = context.openFileInput(filename);
                ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);

                existingWorkspaces = (HashMap<String, Workspace>)objectInputStream.readObject();
                registeredUsers = (HashMap<String, User>)objectInputStream.readObject();

                objectInputStream.close();
                fileInputStream.close();
            } catch (FileNotFoundException e) {
                //populateAirdesk();    this method crashes the app!
            }  catch (ClassNotFoundException | IOException e) {
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
        for (int i = 0; i < 10; i++) {
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
            Workspace newWorkspace = new Workspace(5000, newWorkspaces.get(i), newUsers.get(i), "");
            existingWorkspaces.put(newWorkspaces.get(i), newWorkspace);
            newUser.getOwnedWorkspaces().put(newWorkspaces.get(i), newWorkspace);

            // CREATE FILE, ADD CONTENT AND ADD TO PREVIOUSLY CREATED WORKSPACE'S FILE SET
            File newFile = new File(newFiles.get(i));

            // TODO:: APP CRASHES WHEN THE FILE DOESN'T EXIST. CAN'T RUN ON NEW EMULATORS

            if (i % 2 == 0)
                newFile.save(newFileContents.get(i / 2));
            newWorkspace.getFiles().put(newFiles.get(i), newFile);
            newWorkspace.updateQuotaOccupied(newFile.getSize());
        }
    }

    public void saveAppState(Context context) {
        try {
            FileOutputStream fileOutputStream = context.openFileOutput(filename, Context.MODE_PRIVATE);
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);

            objectOutputStream.writeObject(existingWorkspaces);
            objectOutputStream.writeObject(registeredUsers);

            objectOutputStream.close();
            fileOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setCurrentWorkspace(String currentWorkspace) {
        this.currentWorkspace = currentWorkspace;
    }

    public void WifiOn(MainActivity a, View v) {
        wifiManager.WifiOn(a, v);
    }

    public void WifiOff() {
        wifiManager.WifiOff();
    }


    /////////////////////////////
    ////////// GETTERS //////////
    /////////////////////////////
    public String getLoggedUser() {
        return loggedUser;
    }

    public String getCurrentWorkspace() {
        return currentWorkspace;
    }

    public String getCurrentFile() {
        return currentFile;
    }

    public File getFile(String name) {
        currentFile = name;
        return existingWorkspaces.get(currentWorkspace).getFiles().get(name);
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

    public ArrayList<String> getUsersFromWorkspace() {
        return existingWorkspaces.get(currentWorkspace).getUsers();
    }

    // TODO CHANGE SO FOREIGN WORKSPACES ARE RETRIEVED FROM THE OTHER USER VIA WIFI
    public ArrayList<String> getForeignWorkspaces(String email){
        ArrayList<String> workspaceNames = new ArrayList<String>();
        if (registeredUsers.keySet().contains(email)) {
            workspaceNames.addAll(registeredUsers.get(email).getForeignWorkspaces().keySet());
            return workspaceNames;
        } else {
            return null;
        }
    }

    public ArrayList<String> getFilesFromWorkspace(String workspace) {
        currentWorkspace = workspace;

        ArrayList<String> fileNames = new ArrayList<String>();
        if (registeredUsers.get(loggedUser).getWorkspace(workspace) != null)
            fileNames.addAll(registeredUsers.get(loggedUser).getWorkspace(workspace).getFiles().keySet());
        // if (registeredUsers.get(loggedUser).getForeignWorkspaces().get(currentWorkspace) != null)
        //   fileNames.addAll(registeredUsers.get(loggedUser).getForeignWorkspaces().get(currentWorkspace).getFiles().keySet());

        return fileNames;
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

    public boolean getWorkspacePrivacy(String workspaceName) {
        return existingWorkspaces.get(workspaceName).isPrivate();
    }

    public ArrayList<String> getTopics() {
        return existingWorkspaces.get(currentWorkspace).getTopics();
    }

    public User getUserByEmail(String email) {
        if (registeredUsers.keySet().contains(email)) {
            return registeredUsers.get(email);
        } else {
            return null;
        }
    }

    public boolean[] getAllPrivilegesFromWorkspace() {
        boolean[] privileges = {true, true, true, true};
        for (Privileges p : existingWorkspaces.get(currentWorkspace).getAccessLists().values()) {
            if (!p.canRead()) {
                privileges[0] = false;
                break;
            }
        }
        for (Privileges p : existingWorkspaces.get(currentWorkspace).getAccessLists().values()) {
            if (!p.canWrite()) {
                privileges[1] = false;
                break;
            }
        }
        for (Privileges p : existingWorkspaces.get(currentWorkspace).getAccessLists().values()) {
            if (!p.canCreate()) {
                privileges[2] = false;
                break;
            }
        }
        for (Privileges p : existingWorkspaces.get(currentWorkspace).getAccessLists().values()) {
            if (!p.canDelete()) {
                privileges[3] = false;
                break;
            }
        }
        return privileges;
    }


    //////////////////////////////////
    ////////// DOMAIN LOGIC //////////
    //////////////////////////////////
    public boolean login(String email) {
        loggedUser = email;
        return registeredUsers.keySet().contains(email);
    }

    public void logout() {
        loggedUser = "";
        currentWorkspace = "";
        currentFile = "";
    }

    // TODO BECOME DISCOVERABLE IN WIFI, SO OTHERS CAN DOWNLOAD OUR FILES
    public void registerUser(String name, String email) throws UserAlreadyExistsException {
        if (getUserByEmail(email) == null) {
            registeredUsers.put(email, new User(name, email));
            loggedUser = email;
        } else {
            throw new UserAlreadyExistsException();
        }
    }

    // TODO FORWARD CHANGES TO USERS SUBSCRIBED
    public void addWorkspace(String workspaceName, int quota) throws WorkspaceAlreadyExistsException {
        for (String w : existingWorkspaces.keySet()) {
            if (w.equals(workspaceName)) {
                throw new WorkspaceAlreadyExistsException();
            }
        }

        String finalWorkspaceName = workspaceName + loggedUser;
        MessageDigest md;

        try {
            md = MessageDigest.getInstance("SHA-512");
            byte[] digestion = md.digest(finalWorkspaceName.getBytes());

            StringBuilder sb = new StringBuilder();
            for (int i : digestion)
                sb.append(Byte.toString(digestion[i]));

            existingWorkspaces.put(sb.toString(), getUserByEmail(loggedUser).createWorkspace(quota * 1024, workspaceName, sb.toString()));
            existingWorkspaces.get(workspaceName).notifyObservers();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    public void deleteWorkspace(String workspaceName) throws UserDoesNotHavePermissionsToDeleteWorkspaceException {
        registeredUsers.get(loggedUser).deleteWorkspace(workspaceName);
        existingWorkspaces.remove(workspaceName).notifyObservers();
    }

    public void addTopicToWorkspace(String topic) throws TopicAlreadyAddedException {
        registeredUsers.get(loggedUser).getWorkspace(currentWorkspace).addTopic(topic);
        registeredUsers.get(loggedUser).getWorkspace(currentWorkspace).notifyObservers();
    }

    public void addNewFile(String fileName) throws FileAlreadyExistsException, UserDoesNotHavePermissionsToCreateFilesException {
        existingWorkspaces.get(currentWorkspace).getFiles().put(fileName, registeredUsers.get(loggedUser).createFile(currentWorkspace, fileName));
        existingWorkspaces.get(currentWorkspace).getFiles().get(fileName).notifyObservers();
    }

    public void saveFile(String content) throws WorkspaceQuotaReachedException {
        registeredUsers.get(loggedUser).getWorkspace(currentWorkspace).saveFile(currentFile, content);
        registeredUsers.get(loggedUser).getWorkspace(currentWorkspace).notifyObservers();
    }

    public void deleteFile(String fileName) throws UserDoesNotHavePermissionsToDeleteFileException {
        registeredUsers.get(loggedUser).deleteFile(currentWorkspace, fileName);
        registeredUsers.get(loggedUser).getWorkspace(currentWorkspace).notifyObservers();
    }

    public void changeUserPrivileges(String email, boolean[] privileges) throws UserDoesNotHavePermissionsToChangePrivilegesException {
        registeredUsers.get(loggedUser).changeUserPrivileges(email, privileges, currentWorkspace);
    }

    public void applyGlobalPrivileges(String workspaceName, boolean[] choices) throws UserDoesNotHavePermissionsToChangePrivilegesException {
        registeredUsers.get(loggedUser).applyGlobalPrivileges(workspaceName, choices);
    }

    public void setWorkspacePrivacy(String workspaceName, boolean isPrivate) {
        registeredUsers.get(loggedUser).getWorkspace(workspaceName).setPrivacy(isPrivate);
    }

    // TODO ?? SEND THE WORKSPACE
    public void inviteUser(String username) throws UserDoesNotExistException, UserAlreadyHasPermissionsInWorkspaceException, UserDoesNotHavePermissionsToChangePrivilegesException {
        if (!existingWorkspaces.get(currentWorkspace).getOwner().equals(loggedUser))
            throw new UserDoesNotHavePermissionsToChangePrivilegesException();
        if (!registeredUsers.containsKey(username))
            throw new UserDoesNotExistException();

        registeredUsers.get(loggedUser).addUserToWorkspace(username, currentWorkspace);
        registeredUsers.get(username).mountWorkspace(existingWorkspaces.get(currentWorkspace));
        existingWorkspaces.get(currentWorkspace).addUser(username);
    }


    ///////////////////////////////////////////////
    ////////// NETWORK INTERFACE METHODS //////////
    ///////////////////////////////////////////////
    public void deleteWorkspaceBC(String workspaceName) throws UserDoesNotHavePermissionsToDeleteWorkspaceException {
        registeredUsers.get(loggedUser).deleteWorkspace(workspaceName);
    }

    public void addTopicToWorkspaceBC(String workspaceName, String topic) throws TopicAlreadyAddedException {
        registeredUsers.get(loggedUser).getWorkspace(workspaceName).addTopic(topic);
    }

    public void addNewFileBC(String workspaceName, String fileName) throws FileAlreadyExistsException, UserDoesNotHavePermissionsToCreateFilesException {
        existingWorkspaces.get(workspaceName).getFiles().put(fileName, registeredUsers.get(loggedUser).createFile(currentWorkspace, fileName));
    }

    public void saveFileBC(String fileName, String content) throws WorkspaceQuotaReachedException {
        registeredUsers.get(loggedUser).getWorkspace(currentWorkspace).saveFile(fileName, content);
    }

    public void deleteFileBC(String workspaceName, String fileName) throws UserDoesNotHavePermissionsToDeleteFileException {
        registeredUsers.get(loggedUser).deleteFile(workspaceName, fileName);
    }

    public void applyGlobalPrivilegesBC(String workspaceName, boolean[] choices) throws UserDoesNotHavePermissionsToChangePrivilegesException {
        registeredUsers.get(loggedUser).applyGlobalPrivileges(workspaceName, choices);
        //existingWorkspaces.get(workspaceName).setPrivacy(isPrivate);
    }

    public void setWorkspacePrivacyBC(String workspaceName, boolean isPrivate) {
        registeredUsers.get(loggedUser).getWorkspace(workspaceName).setPrivacy(isPrivate);
    }

    public void updateWorkspaceFileList(String workspaceName, String fileName) throws FileAlreadyExistsException, UserDoesNotHavePermissionsToCreateFilesException {
        addNewFileBC(workspaceName, fileName);
    }

    public void matchWorkspaceTopicsBC(String workspaceName, ArrayList<String> topics) {
        for (String topic : topics) {
            if (registeredUsers.get(loggedUser).getWorkspace(workspaceName).getTopics().contains(topic)) {
                registeredUsers.get(loggedUser).mountWorkspace(existingWorkspaces.get(workspaceName));
                return;
            }
        }
    }
}
