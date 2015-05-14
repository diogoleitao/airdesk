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

import pt.inesc.termite.wifidirect.sockets.SimWifiP2pSocket;
import pt.utl.ist.cmov.airdesk.domain.AirdeskManager;
import pt.utl.ist.cmov.airdesk.domain.BroadcastMessage;
import pt.utl.ist.cmov.airdesk.domain.File;
import pt.utl.ist.cmov.airdesk.domain.Workspace;

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
        CharSequence text = "Connecting...";
        int duration = Toast.LENGTH_SHORT;
        Toast toast = Toast.makeText(context, text, duration);
        toast.show();
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
        switch(message.getMessageType()){
            case FILE_CHANGED:
                workspaceHash = message.getArg1();
                fileName = message.getArg2();
                File file = manager.getFile(workspaceHash, fileName);

                // If the file is null, we don't need it
                if(file != null){
                    if(file.getTimestamp().compareTo(message.getFile().getTimestamp()) < 0){
                        file.setContent(message.getFile().getContent());
                        registerHandler(manager.getServiceHandler());
                        msg = handler.obtainMessage();
                        handler.sendMessage(msg);
                    }
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
                manager.deleteForeignFile(workspaceHash, fileName);
                registerHandler(manager.getServiceHandler());
                msg = handler.obtainMessage();
                handler.sendMessage(msg);
                break;
            case FILE_ADDED_TO_WORKSPACE:
                workspaceHash = message.getArg1();
                fileName = message.getArg2();
                Workspace workspace = manager.getLoggedUser().getWorkspace(workspaceHash);
                // We have the workspace and we should update it
                if(workspace != null){
                    manager.addForeignNewFile(workspaceHash, fileName);
                    registerHandler(manager.getServiceHandler());
                    msg = handler.obtainMessage();
                    handler.sendMessage(msg);
                }
                break;
            case WORKSPACE_DELETED:
                workspaceHash = message.getArg1();
                manager.deleteForeignWorkspace(workspaceHash);
                registerHandler(manager.getServiceHandler());
                msg = handler.obtainMessage();
                handler.sendMessage(msg);
                break;
            case INVITATION_TO_WORKSPACE:
                workspaceHash = message.getArg1();
                user = message.getArg2();
                Log.d("MessageDispatch", "GOT INVITATION TO WORKSPACE " + workspaceHash);
                if(manager.getLoggedUser().getEmail().equals(user)){
                    manager.addForeignWorkspace(message.getWorkspace());
                    registerHandler(manager.getServiceHandler());
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
                    registerHandler(manager.getServiceHandler());
                    msg = handler.obtainMessage();
                    handler.sendMessage(msg);
                }
                break;
            case REQUEST_FILE:
                workspaceHash = message.getArg1();
                fileName = message.getArg2();
                messageOutput = new BroadcastMessage(null, fileName);
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
                messageOutput.setWorkspace(manager.getLoggedUser().getWorkspace(workspaceHash));
                try {
                    (new ObjectOutputStream(s.getOutputStream())).writeObject(messageOutput);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
        }
    }
}
