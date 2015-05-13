package pt.utl.ist.cmov.airdesk.domain.network;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.io.ObjectInputStream;

import pt.inesc.termite.wifidirect.sockets.SimWifiP2pSocket;
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
        switch(message.getMessageType()){
            case FILE_CHANGED:
                break;
            case FILE_DELETED:
                break;
            case FILE_ADDED_TO_WORKSPACE:
                break;
            case WORKSPACE_DELETED:
                break;
            case WORKSPACE_TOPIC_MATCH:
                break;
            case INVITATION_TO_WORKSPACE:
                break;
        }
    }
}
