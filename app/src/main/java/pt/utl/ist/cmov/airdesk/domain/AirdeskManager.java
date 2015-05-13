package pt.utl.ist.cmov.airdesk.domain;

import android.app.Activity;
import android.content.Context;

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
     * Mapping between workspaces' hashes and Workspace names
     */
    private static HashMap<String, String> hashesToNames = new HashMap<String, String>();

    /**
     *
     */
    private static WifiManager wifiManager;

    /**
     * The currently logged in user email
	 */
	private static User loggedUser;

    /**
	 * The currently opened workspace name
	 */
	private String currentWorkspace = "";
	private String currentWorkspaceHash = "";

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

                loggedUser = (User) objectInputStream.readObject();

                objectInputStream.close();
                fileInputStream.close();
            } catch (FileNotFoundException e) {
                //it's okay
            }  catch (ClassNotFoundException | IOException e) {
                e.printStackTrace();
            }
        }
		return instance;
	}

    public void saveAppState(Context context) {
        try {
            FileOutputStream fileOutputStream = context.openFileOutput(filename, Context.MODE_PRIVATE);
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);

            objectOutputStream.writeObject(loggedUser);

            objectOutputStream.close();
            fileOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setCurrentWorkspace(String currentWorkspace) {
        this.currentWorkspaceHash = loggedUser.getAllWorkspaces().get(currentWorkspace).getHash();
        this.currentWorkspace = currentWorkspace;
    }

    /////////////////////////////
    ////////// GETTERS //////////
    /////////////////////////////
    public User getLoggedUser() {
        return loggedUser;
    }

    public String getCurrentWorkspace() {
        return currentWorkspace;
    }

    public String getCurrentWorkspaceHash() {
        return currentWorkspaceHash;
    }

    public String getCurrentFile() {
        return currentFile;
    }

    public File getFile(String name) {
        currentFile = name;
        return loggedUser.getAllWorkspaces().get(currentWorkspace).getFiles().get(name);
    }

    public int getTotalQuota(String workspaceName) {
        return loggedUser.getAllWorkspaces().get(workspaceName).getQuota();
    }

    public int getUsedQuota(String workspaceName) {
        return loggedUser.getAllWorkspaces().get(workspaceName).getQuotaOccupied();
    }

    public boolean[] getUserPrivileges(String email) {
        return loggedUser.getAllWorkspaces().get(currentWorkspace).getAccessLists().get(email).getAll();
    }

    public ArrayList<String> getUsersFromWorkspace() {
        return new ArrayList<String>(loggedUser.getAllWorkspaces().get(currentWorkspace).getAccessLists().keySet());
    }

    public HashMap<String, Workspace> getOwnedWorkspaces(){
        return loggedUser.getOwnedWorkspaces();
    }

    public HashMap<String, Workspace> getForeignWorkspaces(){
        return loggedUser.getForeignWorkspaces();
    }

    public ArrayList<String> getFilesFromWorkspace(String workspace) {
        currentWorkspace = workspace;

        ArrayList<String> fileNames = new ArrayList<String>();
        if (loggedUser.getWorkspace(workspace) != null)
            fileNames.addAll(loggedUser.getWorkspace(workspace).getFiles().keySet());
        // if (loggedUser.getForeignWorkspaces().get(currentWorkspace) != null)
        //   fileNames.addAll(loggedUser.getForeignWorkspaces().get(currentWorkspace).getFiles().keySet());

        return fileNames;
    }


    public boolean getWorkspacePrivacy(String workspaceName) {
        return loggedUser.getAllWorkspaces().get(workspaceName).isPrivate();
    }

    public ArrayList<String> getTopics() {
        return loggedUser.getAllWorkspaces().get(currentWorkspace).getTopics();
    }

    public boolean[] getAllPrivilegesFromWorkspace() {
        boolean[] privileges = {true, true, true, true};
        for (Privileges p : loggedUser.getAllWorkspaces().get(currentWorkspace).getAccessLists().values()) {
            if (!p.canRead()) {
                privileges[0] = false;
                break;
            }
        }
        for (Privileges p : loggedUser.getAllWorkspaces().get(currentWorkspace).getAccessLists().values()) {
            if (!p.canWrite()) {
                privileges[1] = false;
                break;
            }
        }
        for (Privileges p : loggedUser.getAllWorkspaces().get(currentWorkspace).getAccessLists().values()) {
            if (!p.canCreate()) {
                privileges[2] = false;
                break;
            }
        }
        for (Privileges p : loggedUser.getAllWorkspaces().get(currentWorkspace).getAccessLists().values()) {
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
    public void login(String email) {
        loggedUser = new User(email);

    }

    // TODO FORWARD CHANGES TO USERS SUBSCRIBED
    public void addWorkspace(String workspaceName, int quota) throws WorkspaceAlreadyExistsException {
        for (String w : loggedUser.getAllWorkspaces().keySet()) {
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
            for (byte i : digestion)
                sb.append(Byte.toString(i));

            loggedUser.getAllWorkspaces().put(workspaceName, loggedUser.createWorkspace(quota * 1024, workspaceName, sb.toString()));
            hashesToNames.put(sb.toString(), workspaceName);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    public void deleteWorkspace(String workspaceName) throws UserDoesNotHavePermissionsToDeleteWorkspaceException {
        hashesToNames.remove(loggedUser.getAllWorkspaces().get(workspaceName).getHash());
        loggedUser.deleteWorkspace(workspaceName);
    }

    public void addTopicToWorkspace(String topic) throws TopicAlreadyAddedException {
        loggedUser.getWorkspace(currentWorkspace).addTopic(topic);
    }

    public void addNewFile(String fileName) throws FileAlreadyExistsException, UserDoesNotHavePermissionsToCreateFilesException {
        loggedUser.getAllWorkspaces().get(currentWorkspace).getFiles().put(fileName, loggedUser.createFile(currentWorkspace, fileName));
    }

    public void saveFile(String content) throws WorkspaceQuotaReachedException {
        loggedUser.getWorkspace(currentWorkspace).saveFile(currentFile, content);
    }

    public void deleteFile(String fileName) throws UserDoesNotHavePermissionsToDeleteFileException {
        loggedUser.deleteFile(currentWorkspace, fileName);
    }

    public void changeUserPrivileges(String email, boolean[] privileges) throws UserDoesNotHavePermissionsToChangePrivilegesException {
        loggedUser.changeUserPrivileges(email, privileges, currentWorkspace);
    }

    public void applyGlobalPrivileges(String workspaceName, boolean[] choices) throws UserDoesNotHavePermissionsToChangePrivilegesException {
        loggedUser.applyGlobalPrivileges(workspaceName, choices);
    }

    public void setWorkspacePrivacy(String workspaceName, boolean isPrivate) {
        loggedUser.getWorkspace(workspaceName).setPrivacy(isPrivate);
    }

    // TODO ?? SEND  MESSAGE
    public void inviteUser(String email) throws UserAlreadyHasPermissionsInWorkspaceException, UserDoesNotHavePermissionsToChangePrivilegesException {
        if(loggedUser.getWorkspace(currentWorkspace).getOwner() == loggedUser.getEmail())
            loggedUser.getWorkspace(currentWorkspace).getAccessLists().put(email, new Privileges());
        else
            throw new UserDoesNotHavePermissionsToChangePrivilegesException();
    }


    ///////////////////////////////////////////////
    ////////// NETWORK INTERFACE METHODS //////////
    ///////////////////////////////////////////////
    public void deleteWorkspaceBC(String workspaceName) throws UserDoesNotHavePermissionsToDeleteWorkspaceException {
        loggedUser.deleteWorkspace(workspaceName);
    }

    public void addTopicToWorkspaceBC(String workspaceName, String topic) throws TopicAlreadyAddedException {
        loggedUser.getWorkspace(workspaceName).addTopic(topic);
    }

    public void addNewFileBC(String workspaceName, String fileName) throws FileAlreadyExistsException, UserDoesNotHavePermissionsToCreateFilesException {
        loggedUser.getAllWorkspaces().get(workspaceName).getFiles().put(fileName, loggedUser.createFile(currentWorkspace, fileName));
    }

    public void saveFileBC(String workspaceName, String fileName, String content) throws WorkspaceQuotaReachedException {
        loggedUser.getWorkspace(workspaceName).saveFile(fileName, content);
    }

    public void deleteFileBC(String workspaceName, String fileName) throws UserDoesNotHavePermissionsToDeleteFileException {
        loggedUser.deleteFile(workspaceName, fileName);
    }

    public void applyGlobalPrivilegesBC(String workspaceName, boolean[] choices) throws UserDoesNotHavePermissionsToChangePrivilegesException {
        loggedUser.applyGlobalPrivileges(workspaceName, choices);
        //loggedUser.getAllWorkspaces().get(workspaceName).setPrivacy(isPrivate);
    }

    public void setWorkspacePrivacyBC(String workspaceName, boolean isPrivate) {
        loggedUser.getWorkspace(workspaceName).setPrivacy(isPrivate);
    }

    public void updateWorkspaceFileList(String workspaceName, String fileName) throws FileAlreadyExistsException, UserDoesNotHavePermissionsToCreateFilesException {
        addNewFileBC(workspaceName, fileName);
    }

    public void matchWorkspaceTopicsBC(String workspaceName, ArrayList<String> topics) {
        for (String topic : topics) {
            if (loggedUser.getWorkspace(workspaceName).getTopics().contains(topic)) {
                loggedUser.mountWorkspace(loggedUser.getAllWorkspaces().get(workspaceName));
                return;
            }
        }
    }
}
