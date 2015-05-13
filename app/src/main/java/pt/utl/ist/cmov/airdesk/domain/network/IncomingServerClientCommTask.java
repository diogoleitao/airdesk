package pt.utl.ist.cmov.airdesk.domain.network;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.io.ObjectInputStream;

import pt.inesc.termite.wifidirect.sockets.SimWifiP2pSocket;
import pt.utl.ist.cmov.airdesk.domain.AirdeskManager;
import pt.utl.ist.cmov.airdesk.domain.BroadcastMessage;

public class IncomingServerClientCommTask extends AsyncTask<SimWifiP2pSocket, String, Void> {
    SimWifiP2pSocket s;
    Context context;

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

            while (!isCancelled()) {
                message = (BroadcastMessage)sockIn.readObject();
                dispatchMessage(message);
                publishProgress("");
            }
        } catch (IOException e) {
            Log.d("Got :", e.getMessage());
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
        Toast toast = Toast.makeText(context, "GOT MESSAGE!", Toast.LENGTH_SHORT);
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

    protected void dispatchMessage(BroadcastMessage message){
        String workspaceHash, fileName, user;
        AirdeskManager manager = AirdeskManager.getInstance(context);
        switch(message.getMessageType()){
            case FILE_CHANGED:
                workspaceHash = message.getArg1();
                fileName = message.getArg2();


                break;
            case FILE_DELETED:
                workspaceHash = message.getArg1();
                fileName = message.getArg2();
                break;
            case FILE_ADDED_TO_WORKSPACE:
                workspaceHash = message.getArg1();
                fileName = message.getArg2();
                break;
            case WORKSPACE_DELETED:
                workspaceHash = message.getArg1();
                break;
            case NEW_WORKSPACE:
                workspaceHash = message.getArg1();
                break;
            case INVITATION_TO_WORKSPACE:
                workspaceHash = message.getArg1();
                user = message.getArg2();
                break;
            case REQUEST_FILE:
                workspaceHash = message.getArg1();
                fileName = message.getArg2();
                break;
            case REQUEST_WORKSPACE:
                workspaceHash = message.getArg1();
                break;
        }
    }
}
