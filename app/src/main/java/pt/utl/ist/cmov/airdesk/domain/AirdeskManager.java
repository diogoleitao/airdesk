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
import pt.utl.ist.cmov.airdesk.domain.exceptions.UserAlreadyHasPermissionsInWorkspaceException;
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
    private static HashMap<String, String> namesToHashes = new HashMap<String, String>();

    /**
     *
     */
    private static WifiManager wifiManager;

    /**
     * The currently logged in user email
	 */
	private static User loggedUser;

    public void setCurrentFile(String currentFile) {
        this.currentFile = currentFile;
    }

    /**
	 * The currently opened file name
	 */
	private String currentFile = "";

    public void setCurrentWorkspace(String currentWorkspace) {
        this.currentWorkspace = namesToHashes.get(currentWorkspace);
    }

    private String currentWorkspace;

    private AirdeskManager() {}

	public static AirdeskManager getInstance(Context context) {
		if (instance == null) {
			instance = new AirdeskManager();
            wifiManager = new WifiManager();

            try {
                FileInputStream fileInputStream = context.openFileInput(filename);
                ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);

                loggedUser = (User) objectInputStream.readObject();
                namesToHashes = (HashMap<String, String>) objectInputStream.readObject();

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
            objectOutputStream.writeObject(namesToHashes);

            objectOutputStream.close();
            fileOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /////////////////////////////
    ////////// GETTERS //////////
    /////////////////////////////
    public User getLoggedUser() {
        return loggedUser;
    }
    
    public String getCurrentFile() {
        return currentFile;
    }

    public File getFile(String workspaceHash, String name) {
        return loggedUser.getAllWorkspaces().get(workspaceHash).getFiles().get(name);
    }

    public int getTotalQuota(String workspaceHash) {
        return loggedUser.getAllWorkspaces().get(workspaceHash).getQuota();
    }

    public int getUsedQuota(String workspaceHash) {
        return loggedUser.getAllWorkspaces().get(workspaceHash).getQuotaOccupied();
    }

    public boolean[] getUserPrivileges(String workspaceHash, String email) {
        return loggedUser.getAllWorkspaces().get(workspaceHash).getAccessLists().get(email).getAll();
    }

    public ArrayList<String> getUsersFromWorkspace(String workspaceHash) {
        return new ArrayList<String>(loggedUser.getAllWorkspaces().get(workspaceHash).getAccessLists().keySet());
    }

    public HashMap<String, Workspace> getOwnedWorkspaces(){
        return loggedUser.getOwnedWorkspaces();
    }

    public HashMap<String, Workspace> getForeignWorkspaces(){
        return loggedUser.getForeignWorkspaces();
    }

    public ArrayList<String> getFilesFromWorkspace(String workspaceHash) {

        ArrayList<String> fileNames = new ArrayList<String>();
        if (loggedUser.getWorkspace(workspaceHash) != null)
            fileNames.addAll(loggedUser.getWorkspace(workspaceHash).getFiles().keySet());

        return fileNames;
    }


    public boolean getWorkspacePrivacy(String workspaceHash) {
        return loggedUser.getAllWorkspaces().get(workspaceHash).isPrivate();
    }

    public ArrayList<String> getTopics(String workspaceHash) {
        return loggedUser.getAllWorkspaces().get(workspaceHash).getTopics();
    }

    public boolean[] getAllPrivilegesFromWorkspace(String workspaceHash) {
        boolean[] privileges = {true, true, true, true};
        for (Privileges p : loggedUser.getAllWorkspaces().get(workspaceHash).getAccessLists().values()) {
            if (!p.canRead()) {
                privileges[0] = false;
                break;
            }
        }
        for (Privileges p : loggedUser.getAllWorkspaces().get(workspaceHash).getAccessLists().values()) {
            if (!p.canWrite()) {
                privileges[1] = false;
                break;
            }
        }
        for (Privileges p : loggedUser.getAllWorkspaces().get(workspaceHash).getAccessLists().values()) {
            if (!p.canCreate()) {
                privileges[2] = false;
                break;
            }
        }
        for (Privileges p : loggedUser.getAllWorkspaces().get(workspaceHash).getAccessLists().values()) {
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
            if (w.equals(namesToHashes.get(workspaceName))) {
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

            loggedUser.getAllWorkspaces().put(namesToHashes.get(workspaceName), loggedUser.createWorkspace(quota * 1024, workspaceName, sb.toString()));
            namesToHashes.put(workspaceName, sb.toString());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        getLoggedUser();
    }

    public void deleteWorkspace(String workspaceHash) throws UserDoesNotHavePermissionsToDeleteWorkspaceException {
        loggedUser.deleteWorkspace(workspaceHash);
        namesToHashes.remove(workspaceHash);
    }

    public void addTopicToWorkspace(String workspaceHash, String topic) throws TopicAlreadyAddedException {
        loggedUser.getWorkspace(workspaceHash).addTopic(topic);
    }

    public void addNewFile(String workspaceHash, String fileName) throws FileAlreadyExistsException, UserDoesNotHavePermissionsToCreateFilesException {
        loggedUser.getAllWorkspaces().get(workspaceHash).getFiles().put(fileName, loggedUser.createFile(workspaceHash, fileName));
    }

    public void saveFile(String workspaceHash, String filename, String content) throws WorkspaceQuotaReachedException {
        loggedUser.getWorkspace(workspaceHash).saveFile(filename, content);
    }

    public void deleteFile(String workspaceHash, String fileName) throws UserDoesNotHavePermissionsToDeleteFileException {
        loggedUser.deleteFile(workspaceHash, fileName);
    }

    public void changeUserPrivileges(String workspaceHash, String email, boolean[] privileges) throws UserDoesNotHavePermissionsToChangePrivilegesException {
        loggedUser.changeUserPrivileges(email, privileges, workspaceHash);
    }

    public void applyGlobalPrivileges(String workspaceHash, boolean[] choices) throws UserDoesNotHavePermissionsToChangePrivilegesException {
        loggedUser.applyGlobalPrivileges(workspaceHash, choices);
    }

    public void setWorkspacePrivacy(String workspaceHash, boolean isPrivate) {
        loggedUser.getWorkspace(workspaceHash).setPrivacy(isPrivate);
    }

    // TODO ?? SEND  MESSAGE
    public void inviteUser(String workspaceHash, String email) throws UserAlreadyHasPermissionsInWorkspaceException, UserDoesNotHavePermissionsToChangePrivilegesException {
        if(loggedUser.getWorkspace(workspaceHash).getOwner() == loggedUser.getEmail())
            loggedUser.getWorkspace(workspaceHash).getAccessLists().put(email, new Privileges());
        else
            throw new UserDoesNotHavePermissionsToChangePrivilegesException();
    }


    ///////////////////////////////////////////////
    ////////// NETWORK INTERFACE METHODS //////////
    ///////////////////////////////////////////////

    public void addForeignWorkspace(Workspace workspace){
        loggedUser.getForeignWorkspaces().put(workspace.getHash(), workspace);
        namesToHashes.put(workspace.getName(),workspace.getHash());
    }

    public void deleteWorkspaceBC(String workspaceHash) throws UserDoesNotHavePermissionsToDeleteWorkspaceException {
        loggedUser.deleteWorkspace(workspaceHash);
    }

    public void addTopicToWorkspaceBC(String workspaceHash, String topic) throws TopicAlreadyAddedException {
        loggedUser.getWorkspace(workspaceHash).addTopic(topic);
    }

    public void addNewFileBC(String workspaceHash, String fileName) throws FileAlreadyExistsException, UserDoesNotHavePermissionsToCreateFilesException {
        loggedUser.getAllWorkspaces().get(workspaceHash).getFiles().put(fileName, loggedUser.createFile(workspaceHash, fileName));
    }

    public void saveFileBC(String workspaceHash, String fileName, String content) throws WorkspaceQuotaReachedException {
        loggedUser.getWorkspace(workspaceHash).saveFile(fileName, content);
    }

    public void deleteFileBC(String workspaceHash, String fileName) throws UserDoesNotHavePermissionsToDeleteFileException {
        loggedUser.deleteFile(workspaceHash, fileName);
    }

    public void applyGlobalPrivilegesBC(String workspaceHash, boolean[] choices) throws UserDoesNotHavePermissionsToChangePrivilegesException {
        loggedUser.applyGlobalPrivileges(workspaceHash, choices);
        //loggedUser.getAllWorkspaces().get(workspaceName).setPrivacy(isPrivate);
    }

    public void setWorkspacePrivacyBC(String workspaceHash, boolean isPrivate) {
        loggedUser.getWorkspace(workspaceHash).setPrivacy(isPrivate);
    }

    public void updateWorkspaceFileList(String workspaceHash, String fileName) throws FileAlreadyExistsException, UserDoesNotHavePermissionsToCreateFilesException {
        addNewFileBC(workspaceHash, fileName);
    }

    public void matchWorkspaceTopicsBC(String workspaceHash, ArrayList<String> topics) {
        for (String topic : topics) {
            if (loggedUser.getWorkspace(workspaceHash).getTopics().contains(topic)) {
                loggedUser.mountWorkspace(loggedUser.getAllWorkspaces().get(workspaceHash));
                return;
            }
        }
    }

    public Workspace getCurrentWorkspace() {
        return loggedUser.getWorkspace(currentWorkspace);
    }
}
