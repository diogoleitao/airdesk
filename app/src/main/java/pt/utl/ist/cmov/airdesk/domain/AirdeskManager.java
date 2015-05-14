package pt.utl.ist.cmov.airdesk.domain;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Message;

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

import pt.utl.ist.cmov.airdesk.activities.Updatable;
import pt.utl.ist.cmov.airdesk.domain.exceptions.FileAlreadyExistsException;
import pt.utl.ist.cmov.airdesk.domain.exceptions.TopicAlreadyAddedException;
import pt.utl.ist.cmov.airdesk.domain.exceptions.UserAlreadyHasPermissionsInWorkspaceException;
import pt.utl.ist.cmov.airdesk.domain.exceptions.UserDoesNotHavePermissionsToChangePrivilegesException;
import pt.utl.ist.cmov.airdesk.domain.exceptions.UserDoesNotHavePermissionsToCreateFilesException;
import pt.utl.ist.cmov.airdesk.domain.exceptions.UserDoesNotHavePermissionsToDeleteFileException;
import pt.utl.ist.cmov.airdesk.domain.exceptions.UserDoesNotHavePermissionsToDeleteWorkspaceException;
import pt.utl.ist.cmov.airdesk.domain.exceptions.WorkspaceAlreadyExistsException;
import pt.utl.ist.cmov.airdesk.domain.exceptions.WorkspaceQuotaReachedException;
import pt.utl.ist.cmov.airdesk.domain.network.GlobalService;

public class AirdeskManager implements Serializable {

    static String filenameSaveApp = "AirdeskState";

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
    private static Activity currentActivity;
    private GlobalService globalService;

    public Handler getServiceHandler() {
        return serviceHandler;
    }

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

            if(context == null)
                return instance;

            try {
                FileInputStream fileInputStream = context.openFileInput(filenameSaveApp);
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
            FileOutputStream fileOutputStream = context.openFileOutput(filenameSaveApp, Context.MODE_PRIVATE);
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
    public boolean openFile(String workspaceHash, String name) {
        boolean open = loggedUser.getAllWorkspaces().get(workspaceHash).getFiles().get(name).open();
        if(!open){
            BroadcastMessage message = new BroadcastMessage(BroadcastMessage.MessageTypes.FILE_OPEN, workspaceHash, name);
            message.setFile(getFile(workspaceHash, name));
            GlobalService.broadcastMessage(message);
        }
        return open;
    }
    public void closeFile(String workspaceHash, String name) {
        File file = loggedUser.getAllWorkspaces().get(workspaceHash).getFiles().get(name);
        if(file != null){
            file.close();
            BroadcastMessage message = new BroadcastMessage(BroadcastMessage.MessageTypes.FILE_CLOSE, workspaceHash, name);
            message.setFile(getFile(workspaceHash, name));
            GlobalService.broadcastMessage(message);
        }
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
        BroadcastMessage message = new BroadcastMessage(BroadcastMessage.MessageTypes.WORKSPACE_DELETED, workspaceHash);
        GlobalService.broadcastMessage(message);
    }

    public void addTopicToWorkspace(String workspaceHash, String topic) throws TopicAlreadyAddedException {
        loggedUser.getWorkspace(workspaceHash).addTopic(topic);
        // Send message to everyone informing
        BroadcastMessage messageTopics = new BroadcastMessage(BroadcastMessage.MessageTypes.WORKSPACE_TOPICS_CHANGED, getLoggedUser().getEmail());
        messageTopics.setTopics(loggedUser.getWorkspace(workspaceHash).getTopics());
        GlobalService.broadcastMessage(messageTopics);
    }

    public void addNewFile(String workspaceHash, String fileName) throws FileAlreadyExistsException, UserDoesNotHavePermissionsToCreateFilesException {
        loggedUser.getAllWorkspaces().get(workspaceHash).addFile(fileName, loggedUser.createFile(workspaceHash, fileName));
        BroadcastMessage message = new BroadcastMessage(BroadcastMessage.MessageTypes.FILE_ADDED_TO_WORKSPACE, workspaceHash, fileName);
        GlobalService.broadcastMessage(message);
    }

    public void saveFile(String workspaceHash, String filename, String content) throws WorkspaceQuotaReachedException {
        loggedUser.getWorkspace(workspaceHash).saveFile(filename, content);
        BroadcastMessage message = new BroadcastMessage(BroadcastMessage.MessageTypes.FILE_CHANGED, workspaceHash, filename);
        message.setFile(getFile(workspaceHash, filename));
        GlobalService.broadcastMessage(message);
    }

    public void deleteFile(String workspaceHash, String fileName) throws UserDoesNotHavePermissionsToDeleteFileException {
        loggedUser.deleteFile(workspaceHash, fileName);
        BroadcastMessage message = new BroadcastMessage(BroadcastMessage.MessageTypes.FILE_DELETED, workspaceHash, fileName);
        GlobalService.broadcastMessage(message);
    }

    public void changeUserPrivileges(String workspaceHash, String email, boolean[] privileges) throws UserDoesNotHavePermissionsToChangePrivilegesException {
        loggedUser.changeUserPrivileges(email, privileges, workspaceHash);
        BroadcastMessage message = new BroadcastMessage(BroadcastMessage.MessageTypes.WORKSPACE_PRIVILEGES_CHANGED, workspaceHash, email);
        Privileges privileges1 = new Privileges();
        privileges1.setAll(privileges);
        message.setPrivileges(privileges1);
        GlobalService.broadcastMessage(message);
    }

    public void applyGlobalPrivileges(String workspaceHash, boolean[] choices) throws UserDoesNotHavePermissionsToChangePrivilegesException {
        loggedUser.applyGlobalPrivileges(workspaceHash, choices);
    }

    public void setWorkspacePrivacy(String workspaceHash, boolean isPrivate) {
        loggedUser.getWorkspace(workspaceHash).setPrivacy(isPrivate);
    }


    public void inviteUser(String workspaceHash, String email) throws UserAlreadyHasPermissionsInWorkspaceException, UserDoesNotHavePermissionsToChangePrivilegesException {
        if (loggedUser.getWorkspace(workspaceHash).getOwner() == loggedUser.getEmail()){
            loggedUser.getWorkspace(workspaceHash).getAccessLists().put(email, new Privileges());
            BroadcastMessage message = new BroadcastMessage(BroadcastMessage.MessageTypes.INVITATION_TO_WORKSPACE, workspaceHash, email);
            message.setWorkspace(loggedUser.getWorkspace(workspaceHash));
            GlobalService.broadcastMessage(message);
        }
        else
            throw new UserDoesNotHavePermissionsToChangePrivilegesException();
    }


    ///////////////////////////////////////////////
    ////////// NETWORK INTERFACE METHODS //////////
    ///////////////////////////////////////////////

    public void addForeignWorkspace(Workspace workspace){
        loggedUser.getForeignWorkspaces().put(workspace.getHash(), workspace);
        namesToHashes.put(workspace.getName(), workspace.getHash());
    }

     private Handler serviceHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            updateUI();
        }
    };

    public void updateUI() {
        if(currentActivity instanceof Updatable)
            ((Updatable)currentActivity).updateUI();
    }

    public void deleteForeignFile(String workspaceHash, String filename){
        loggedUser.deleteForeignFile(workspaceHash, filename);
    }

    public void deleteForeignWorkspace(String workspaceHash) {
        loggedUser.deleteForeignWorkspace(workspaceHash);
        namesToHashes.remove(workspaceHash);
    }

    public void addForeignNewFile(String workspaceHash, String fileName){
        File file = new File(fileName);
        loggedUser.getAllWorkspaces().get(workspaceHash).addFile(fileName, file);
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

    public void setCurrentActivity(Activity currentActivity) {
        this.currentActivity = currentActivity;
    }

    public void setGlobalService(GlobalService globalService) {
        this.globalService = globalService;
    }

    public void changeWorkspace(String hash, Workspace workspace) {
        getLoggedUser().getOwnedWorkspaces().get(hash).update(workspace);
        BroadcastMessage message = new BroadcastMessage(BroadcastMessage.MessageTypes.WORKSPACE_UPDATED, hash);
        message.setWorkspace(workspace);
        GlobalService.broadcastMessage(message);
    }

    public void updateWorkspace(String hash, Workspace workspace) {
        getLoggedUser().getForeignWorkspaces().get(hash).update(workspace);
    }

}
