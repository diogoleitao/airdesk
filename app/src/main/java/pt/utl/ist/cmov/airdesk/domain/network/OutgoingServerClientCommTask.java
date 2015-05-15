package pt.utl.ist.cmov.airdesk.domain.network;

import android.app.Service;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.UnknownHostException;

import pt.inesc.termite.wifidirect.sockets.SimWifiP2pSocket;
import pt.utl.ist.cmov.airdesk.R;
import pt.utl.ist.cmov.airdesk.domain.AirdeskManager;
import pt.utl.ist.cmov.airdesk.domain.BroadcastMessage;
import pt.utl.ist.cmov.airdesk.domain.Workspace;

/**
 * Created by theburdencarrier on 13/05/15.
 */
public class OutgoingServerClientCommTask extends AsyncTask<BroadcastMessage, Void, String> {

    private Service service;
    private SimWifiP2pSocket socket;

    public OutgoingServerClientCommTask(Service _service) {
        super();
        service = _service;
    }

    @Override
    protected void onPreExecute() {
        /*Context context = service;
        CharSequence text = "Connecting...";
        int duration = Toast.LENGTH_SHORT;
        Toast toast = Toast.makeText(context, text, duration);
        toast.show();*/
    }

    @Override
    protected String doInBackground(BroadcastMessage... params) {
        BroadcastMessage message = params[0];
        try {
            socket = new SimWifiP2pSocket(message.getIp(), GlobalService.SERVICE_PORT);
            ObjectOutputStream o = new ObjectOutputStream(socket.getOutputStream());
            o.writeObject(message);
            Log.d("MessageSend", "SENT MESSAGE " + message.getMessageType().toString());
            /*switch(message.getMessageType()){
                case WORKSPACE_TOPICS_REQUEST:
                    ObjectInputStream i = new ObjectInputStream(socket.getInputStream());
                    try {
                        BroadcastMessage messageResponse = (BroadcastMessage)i.readObject();
                        AirdeskManager manager = AirdeskManager.getInstance(service);
                        for(Workspace w : messageResponse.getWorkspaces()){
                            manager.addForeignWorkspace(w);
                            Message msg = manager.getServiceHandler().obtainMessage();
                            manager.getServiceHandler().sendMessage(msg);
                        }
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    } catch (EOFException e) {
                        // No message to read;
                    }
                    break;
            }*/
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (NullPointerException e) {
                e.printStackTrace();
                // Termite nulls sockets when groups change?
            }
        }
        return "Message sent: " + message.getMessageType() + " to: " + message.getIp();
    }

    @Override
    protected void onPostExecute(String result) {
        if (result != null) {
            Context context = service;
            int duration = Toast.LENGTH_SHORT;
            Toast toast = Toast.makeText(context, result, duration);
            toast.show();
        }
    }

    @Override
    protected void onCancelled() {
        if (!socket.isClosed()) {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}

