package pt.utl.ist.cmov.airdesk.domain;

import java.util.ArrayList;
import java.util.HashMap;

import pt.utl.ist.cmov.airdesk.domain.exceptions.UserAlreadyExistsException;

public class AirdeskManager {

    private static AirdeskManager instance = null;
    private HashMap<String, User> registeredUsers = new HashMap<String, User>();
    private HashMap<String, Workspace> existingWorkspaces = new HashMap<String, Workspace>();

    private String loggedUser = "";
    private String currentWorkspace = "";
    private String currentFile = "";

    private AirdeskManager() {}

    public static AirdeskManager getInstance() {
        if (instance == null) {
            instance = new AirdeskManager();
        }
        return  instance;
    }

    // TODO: refactor for loop
    public ArrayList<String> login(String username, String email) {
        loggedUser = username;
        ArrayList<String> workspaceNames = new ArrayList<String>();
        for (String workspace : registeredUsers.get(loggedUser).getOwnedWorkspaces().keySet()) {
            workspaceNames.add(workspace);
        }
        for (String workspace : registeredUsers.get(loggedUser).getForeignWorkspaces().keySet()) {
            workspaceNames.add(workspace);
        }
        return workspaceNames;
    }

    public void registerUser(String name, String nickname, String email) throws Exception {
        User user = new User(name, nickname, email);
        if (getUserByNickname(nickname).equals(null)) {
            registeredUsers.put(nickname, user);
        } else {
            throw new UserAlreadyExistsException();
        }
    }

    public void addWorkspace(String nickname, String workspace) {
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

    // TODO: refactor for loop
    public ArrayList<String> getFilesFromWorkspace(String workspace) {
        currentWorkspace = workspace;
        ArrayList<String> fileNames = new ArrayList<String>();
        for (File f : registeredUsers.get(loggedUser).getOwnedWorkspaces().get(currentWorkspace).getFiles()) {
            fileNames.add(f.getName());
        }
        for (File f : registeredUsers.get(loggedUser).getForeignWorkspaces().get(currentWorkspace).getFiles()) {
            fileNames.add(f.getName());
        }
        return fileNames;
    }

    public File getFile(String name) {
        for (File f: existingWorkspaces.get(currentWorkspace).getFiles()) {
            if (f.getName().equals(name)) {
                return f;
            }
        }
        return null;
    }
}
