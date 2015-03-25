package pt.utl.ist.cmov.airdesk.domain;

import java.util.ArrayList;
import java.util.HashMap;

public class AirdeskManager {

    private static AirdeskManager instance = null;
    private HashMap<String, User> registeredUsers = new HashMap<String, User>();
    private HashMap<String, Workspace> workspaces = new HashMap<String, Workspace>();

    public AirdeskManager() {
        if (instance == null) {
            instance = new AirdeskManager();
        }
    }

    public void addUser(String name, String nickname, String email) throws Exception {
        User user = new User(name, nickname, email);
        if (getUserByNickname(nickname).equals("")) {
            registeredUsers.put(nickname, user);
        } else {
            throw new Exception();
        }
    }

    public String getUserByNickname(String nickname) {
        if (registeredUsers.keySet().contains(nickname)) {
            return nickname;
        } else {
            return null;
        }
    }

    public void mountWorkspace(ArrayList<String> nicknames, String workspace) {
        for (String nickname : nicknames) {

        }
    }
}
