package pt.utl.ist.airdesk.domain;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Diogo on 18/03/2015.
 */
public class User {
    private String name;
    private String nickname;
    private String email;

    /**
     * Set of the user's workspaces that he created mapped by their name
     */
    private HashMap<String, Workspace> ownedWorkspaces;

    /**
     * Set of the user's workspaces that he joined mapped by their name
     */
    private HashMap<String, Workspace> foreignWorkspaces;

    /**
     * List of topics that the user is interested in regarding workspaces
     */
    private ArrayList<String> subscriptions;

    public User() {}

    public User(String name, String nickname, String email) {
        this.setName(name);
        this.setNickname(nickname);
        this.setEmail(email);
    }

    /**
     *
     * @return the user's name
     */
    public String getName() {
        return name;
    }

    /**
     *
     * @param name
     * Sets the user's name to 'name'
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     *
     * @return the user's nickname
     */
    public String getNickname() {
        return nickname;
    }

    /**
     *
     * @param nickname
     * Sets the user's nickname to 'nickname'
     */
    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    /**
     *
     * @return the user's email
     */
    public String getEmail() {
        return email;
    }

    /**
     *
     * @param email
     * Sets the user's email to 'email'
     */
    public void setEmail(String email) {
        this.email = email;
    }

    public HashMap<String, Workspace> getOwnedWorkspaces() {
        return ownedWorkspaces;
    }

    public void setOwnedWorkspaces(HashMap<String, Workspace> ownedWorkspaces) {
        this.ownedWorkspaces = ownedWorkspaces;
    }

    public HashMap<String, Workspace> getForeignWorkspaces() {
        return foreignWorkspaces;
    }

    public void setForeignWorkspaces(HashMap<String, Workspace> foreignWorkspaces) {
        this.foreignWorkspaces = foreignWorkspaces;
    }

    public ArrayList<String> getSubscriptions() {
        return subscriptions;
    }

    public void setSubscriptions(ArrayList<String> subscriptions) {
        this.subscriptions = subscriptions;
    }

    /**
     *
     * @param quota the maximum size assigned to the workspace
     * @param name the workspace's identifier
     * @return a new workspace object with size 'quota' and identifier 'name'
     */
    public String createWorkspace(int quota, String name) {
        if (!getOwnedWorkspaces().containsKey(name)) {
            Workspace workspace = new Workspace(quota, name, getNickname());
            ownedWorkspaces.put(name, workspace);
            return workspace.getName();
        }
        return new String();
    }

    /**
     *
     * @param name the workspace's identifier to be deleted from the user's set
     * @return the deleted workspace's identifier
     */
    public String deleteWorkspace(String name) {
        if (getOwnedWorkspaces().containsKey(name)) {
            Workspace workspace = getOwnedWorkspaces().get(name);
            ownedWorkspaces.remove(name);
            return workspace.getName();
        }
        return new String();
    }

    /**
     *
     * @param nickname
     * @param workspace
     */
    public void addUserToWorkspace(String nickname, String workspace) {

    }
}
