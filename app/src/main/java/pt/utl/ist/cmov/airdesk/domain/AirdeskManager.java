package pt.utl.ist.cmov.airdesk.domain;

import java.util.ArrayList;
import java.util.HashMap;

public class AirdeskManager {

    private static AirdeskManager instance = null;
    private HashMap<String, User> registeredUsers = new HashMap<String, User>();
    private HashMap<String, Workspace> workspaces = new HashMap<String, Workspace>();
<<<<<<< HEAD

    public AirdeskManager() {
        if (instance == null) {
            instance = new AirdeskManager();
        }
=======
    private HashMap<String, ArrayList<File>> workspaceFiles = new HashMap<String, ArrayList<File>>();

    private AirdeskManager() {}

    public AirdeskManager getInstance() {
        if (instance == null) {
            instance = new AirdeskManager();
        }
        return  instance;
>>>>>>> 1bb8446a7c49437e4d3e14b55dd6b3d7a9526f6f
    }

    public void addUser(String name, String nickname, String email) throws Exception {
        User user = new User(name, nickname, email);
<<<<<<< HEAD
        if (getUserByNickname(nickname).equals("")) {
=======
        if (getUserByNickname(nickname).equals(null)) {
>>>>>>> 1bb8446a7c49437e4d3e14b55dd6b3d7a9526f6f
            registeredUsers.put(nickname, user);
        } else {
            throw new Exception();
        }
    }

<<<<<<< HEAD
    public String getUserByNickname(String nickname) {
        if (registeredUsers.keySet().contains(nickname)) {
            return nickname;
=======
    public void addWorkspace(String nickname, String workspace) {
        User user = getUserByNickname(nickname);
        user.createWorkspace(0, workspace);
    }

    public User getUserByNickname(String nickname) {
        if (registeredUsers.keySet().contains(nickname)) {
            return registeredUsers.get(nickname);
>>>>>>> 1bb8446a7c49437e4d3e14b55dd6b3d7a9526f6f
        } else {
            return null;
        }
    }

    public void mountWorkspace(ArrayList<String> nicknames, String workspace) {
        for (String nickname : nicknames) {

        }
    }
}
