package pt.utl.ist.cmov.airdesk.domain.network;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import pt.inesc.termite.wifidirect.sockets.SimWifiP2pSocket;
import pt.utl.ist.cmov.airdesk.domain.AirdeskManager;
import pt.utl.ist.cmov.airdesk.domain.BroadcastMessage;
import pt.utl.ist.cmov.airdesk.domain.File;
import pt.utl.ist.cmov.airdesk.domain.Privileges;
import pt.utl.ist.cmov.airdesk.domain.Workspace;
import pt.utl.ist.cmov.airdesk.domain.exceptions.WorkspaceQuotaReachedException;
import pt.utl.ist.cmov.airdesk.domain.exceptions.UserAlreadyHasPermissionsInWorkspaceException;
import pt.utl.ist.cmov.airdesk.domain.exceptions.UserDoesNotHavePermissionsToDeleteWorkspaceException;

public class IncomingServerClientCommTask extends AsyncTask<SimWifiP2pSocket, String, Void> {
    SimWifiP2pSocket s;
    Context context;
    private Handler handler;

    public IncomingServerClientCommTask(Context _context){
        super();
        context = _context;
    }

    @Override
    protected Void doInBackground(SimWifiP2pSocket... params) {
        ObjectInputStream sockIn = null;
        String st;
        BroadcastMessage message = null;

        s = params[0];
        try {
            sockIn = new ObjectInputStream(s.getInputStream());
            message = (BroadcastMessage)sockIn.readObject();
            dispatchMessage(message, s);
            publishProgress(message.getMessageType().toString());
        } catch (IOException e) {
            Log.d("IncomingTask", "Got :" + e.getMessage());
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            try {
                if(sockIn != null){
                    sockIn.close();
                }
                s.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    @Override
    protected void onPreExecute() {
        /*CharSequence text = "Connecting...";
        int duration = Toast.LENGTH_SHORT;
        Toast toast = Toast.makeText(context, text, duration);
        toast.show();*/
    }

    @Override
    protected void onProgressUpdate(String... messages) {
        Toast toast = Toast.makeText(context, "GOT MESSAGE! " + messages[0], Toast.LENGTH_SHORT);
        toast.show();
    }

    @Override
    protected void onPostExecute(Void result) {
        if (!s.isClosed()) {
            try {
                s.close();
            } catch (Exception e) {
                Log.d("Error closing socket:", e.getMessage());
            }
        }
        s = null;
    }

    public void registerHandler(Handler serviceHandler) {
        handler = serviceHandler;
    }

    protected void dispatchMessage(BroadcastMessage message, SimWifiP2pSocket socket){

        BroadcastMessage messageOutput;
        String workspaceHash, fileName, user;
        Message msg;
        AirdeskManager manager = AirdeskManager.getInstance(context);
        if(manager.getLoggedUser() == null)
            return;
        registerHandler(manager.getServiceHandler());
        switch(message.getMessageType()){
            case I_AM_USER:
                // a user just connected to our device
                String email = message.getArg1();
                for(Workspace w : manager.getForeignWorkspaces().values()){
                    if(w.getOwner().equals(email)){
                        // I have a foreign workspace of this user, I need to know how recent is his version
                        messageOutput = new BroadcastMessage(BroadcastMessage.MessageTypes.WORKSPACE_TIMESTAMP, email, w.getHash());
                        messageOutput.setWorkspaceTimestamp(w.getTimestamp());
                        messageOutput.setWorkspace(w);
                        GlobalService.broadcastMessage(messageOutput);
                        // TODO: possible optimization, send all shared workspaces and timespams in one broadcast. dont do 1 message per workspace
                    }
                }
                break;
            case WORKSPACE_TIMESTAMP:
                if(! manager.getLoggedUser().getEmail().equals(message.getArg1()))
                    break; // this message is not for me. it's for the owner of the workspace
                workspaceHash = message.getArg2();
                Workspace w = manager.getOwnedWorkspaces().get(workspaceHash);
                if(w == null){
                    // I deleted that workspace you are asking for. I'll return the hash you gave me and tell everyone to delete it.
                    messageOutput = new BroadcastMessage(BroadcastMessage.MessageTypes.WORKSPACE_DELETED, workspaceHash);
                    GlobalService.broadcastMessage(messageOutput);
                } else if(!message.getWorkspace().getTimestamp().equals(message.getWorkspace().getLastOnlineTimestamp())){
                    // The peer changed the workspace while disconnected
                    if(w.getTimestamp().equals(message.getWorkspace().getLastOnlineTimestamp())) {
                        // I didn't change my version while this peer was disconnected. I accept the peer's version.
                        manager.changeWorkspace(w.getHash(), message.getWorkspace());
                        msg = handler.obtainMessage();
                        handler.sendMessage(msg);
                    } else {
                        // I changed my version too. There is a conflict, I'll send my version and let the peer handle it.
                        messageOutput = new BroadcastMessage(BroadcastMessage.MessageTypes.WORKSPACE_UPDATED, workspaceHash);
                        messageOutput.setWorkspace(w);
                        messageOutput.setWorkspaceTimestamp(w.getTimestamp());
                        GlobalService.broadcastMessage(messageOutput);
                    }
                } else if(w.getTimestamp().after(message.getWorkspaceTimestamp())){
                    // I changed the workspace while you were disconnected. Here is my current version.
                    messageOutput = new BroadcastMessage(BroadcastMessage.MessageTypes.WORKSPACE_UPDATED, workspaceHash);
                    messageOutput.setWorkspace(w);
                    messageOutput.setWorkspaceTimestamp(w.getTimestamp());
                    GlobalService.broadcastMessage(messageOutput);
                } // none of us changed anything while disconnected, we have the same version
                break;
            case WORKSPACE_UPDATED:
                // I must make sure this message was intended for me. I might not have access to this workspace.
                workspaceHash = message.getArg1();
                w = manager.getForeignWorkspaces().get(workspaceHash);
                if(w != null) {
                    if( w.getTimestamp().equals(w.getLastOnlineTimestamp())) {
                        manager.updateWorkspace(workspaceHash, message.getWorkspace(), message.getWorkspaceTimestamp());
                    } else {
                        manager.conflict(workspaceHash, message.getWorkspace(), message.getWorkspaceTimestamp());
                        msg = handler.obtainMessage();
                        handler.sendMessage(msg);
                    }
                }
                break;
            case FILE_CHANGED:
                workspaceHash = message.getArg1();
                fileName = message.getArg2();
                File file = manager.getFile(workspaceHash, fileName);

                // If the file is null, we don't need it
                if(file != null){
                    if(file.getTimestamp().compareTo(message.getFile().getTimestamp()) < 0){
                        try {
                            manager.saveForeignFile(workspaceHash, fileName, message.getFile().getContent(), message.getWorkspaceTimestamp());
                        } catch (WorkspaceQuotaReachedException e) {
                            e.printStackTrace();//should never happen, if owner had space, we have space
                        }
                        if(manager.getLoggedUser().getEmail().equals(manager.getLoggedUser().getWorkspace(workspaceHash).getOwner())){
                            messageOutput = new BroadcastMessage(BroadcastMessage.MessageTypes.OWNERS_VERSION, workspaceHash);
                            messageOutput.setWorkspaceTimestamp(manager.getLoggedUser().getWorkspace(workspaceHash).getTimestamp());
                            GlobalService.broadcastMessage(messageOutput);
                        }
                        msg = handler.obtainMessage();
                        handler.sendMessage(msg);
                    }
                }
                break;
            case OWNERS_VERSION:
                workspaceHash = message.getArg1();
                w = manager.getForeignWorkspaces().get(workspaceHash);
                if( w!= null){
                    w.setTimestamp(message.getWorkspaceTimestamp());
                    w.setLastOnlineTimestamp(message.getWorkspaceTimestamp());
                    // my changes for sure reached the owner, we have the same versions/timestamps
                }
                break;
            case FILE_OPEN:
                workspaceHash = message.getArg1();
                fileName = message.getArg2();
                file = manager.getFile(workspaceHash, fileName);
                if( file!= null)
                    file.open();
                break;
            case FILE_CLOSE:
                workspaceHash = message.getArg1();
                fileName = message.getArg2();
                file = manager.getFile(workspaceHash, fileName);
                if( file!= null)
                    file.close();
                break;
            case FILE_DELETED:
                workspaceHash = message.getArg1();
                fileName = message.getArg2();
                manager.deleteForeignFile(workspaceHash, fileName, message.getWorkspaceTimestamp());

                msg = handler.obtainMessage();
                handler.sendMessage(msg);
                break;
            case FILE_ADDED_TO_WORKSPACE:
                workspaceHash = message.getArg1();
                fileName = message.getArg2();
                Workspace workspace = manager.getLoggedUser().getForeignWorkspaces().get(workspaceHash);
                // We have the workspace and we should update it
                if(workspace != null){
                    manager.addForeignNewFile(workspaceHash, fileName, message.getWorkspaceTimestamp());
                    msg = handler.obtainMessage();
                    handler.sendMessage(msg);
                }
                break;
            case WORKSPACE_DELETED:
                workspaceHash = message.getArg1();
                manager.deleteForeignWorkspace(workspaceHash);
                msg = handler.obtainMessage();
                handler.sendMessage(msg);
                break;
            case INVITATION_TO_WORKSPACE:
                workspaceHash = message.getArg1();
                user = message.getArg2();
                Log.d("MessageDispatch", "GOT INVITATION TO WORKSPACE " + workspaceHash);
                if(manager.getLoggedUser().getEmail().equals(user)){
                    manager.addForeignWorkspace(message.getWorkspace());
                    msg = handler.obtainMessage();
                    handler.sendMessage(msg);
                }
                break;
            case WORKSPACE_PRIVILEGES_CHANGED:
                workspaceHash = message.getArg1();
                user = message.getArg2();
                workspace = manager.getLoggedUser().getWorkspace(workspaceHash);
                // We have the workspace mounted and its our privileges
                if(workspace != null && user.equals(manager.getLoggedUser().getEmail())) {
                    workspace.getAccessLists().get(user).setAll(message.getPrivileges().getAll());
                    msg = handler.obtainMessage();
                    handler.sendMessage(msg);
                }
                break;
            case REQUEST_FILE:
                workspaceHash = message.getArg1();
                fileName = message.getArg2();
                messageOutput = new BroadcastMessage(null, fileName);
                messageOutput.setWorkspaceTimestamp(manager.getLoggedUser().getWorkspace(workspaceHash).getTimestamp());
                messageOutput.setFile(manager.getFile(workspaceHash, fileName));
                try {
                    (new ObjectOutputStream(s.getOutputStream())).writeObject(messageOutput);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            case REQUEST_WORKSPACE:
                workspaceHash = message.getArg1();
                messageOutput = new BroadcastMessage(null, workspaceHash);
                messageOutput.setWorkspaceTimestamp(manager.getLoggedUser().getWorkspace(workspaceHash).getTimestamp());
                messageOutput.setWorkspace(manager.getLoggedUser().getWorkspace(workspaceHash));
                try {
                    (new ObjectOutputStream(s.getOutputStream())).writeObject(messageOutput);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            case WORKSPACE_TOPICS_REQUEST:
                List<String> topics = message.getTopics();
                user = message.getArg1();
                List<Workspace> matchingWorkspaces = new ArrayList<Workspace>();
                for(String topic : topics){
                    for(Map.Entry<String, Workspace> workspaceEntry : manager.getOwnedWorkspaces().entrySet()){
                        if(workspaceEntry.getValue().getTopics().contains(topic)){
                            matchingWorkspaces.add(workspaceEntry.getValue());
                        }
                    }
                }
                for(Workspace workspace1 : matchingWorkspaces){
                    try {
                        manager.getLoggedUser().addUserToWorkspace(user, workspace1.getHash());
                        workspace1.getAccessLists().put(user, new Privileges());
                        BroadcastMessage messageInvite = new BroadcastMessage(BroadcastMessage.MessageTypes.INVITATION_TO_WORKSPACE, workspace1.getHash(), user);
                        messageInvite.setWorkspace(workspace1);
                        GlobalService.broadcastMessage(messageInvite);
                    } catch (UserAlreadyHasPermissionsInWorkspaceException e) {
                        // He has already asked us for it before
                    }
                }
                break;
            case WORKSPACE_TOPICS_CHANGED:
                // There was a topic change, let's flood the network with requests for workspace with topics
                BroadcastMessage messageTopics = new BroadcastMessage(BroadcastMessage.MessageTypes.WORKSPACE_TOPICS_REQUEST, AirdeskManager.getInstance(context).getLoggedUser().getEmail());
                messageTopics.setTopics(AirdeskManager.getInstance(context).getLoggedUser().getTopics());
                GlobalService.broadcastMessage(messageTopics);
                break;
        }
    }
}
