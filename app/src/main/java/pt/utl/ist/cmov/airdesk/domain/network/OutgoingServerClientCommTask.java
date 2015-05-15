package pt.utl.ist.cmov.airdesk.domain.network;

import android.app.Service;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.io.ObjectOutputStream;

import pt.inesc.termite.wifidirect.sockets.SimWifiP2pSocket;
import pt.utl.ist.cmov.airdesk.domain.BroadcastMessage;

public class OutgoingServerClientCommTask extends AsyncTask<BroadcastMessage, Void, String> {

    private Service service;

    private SimWifiP2pSocket socket;

    public OutgoingServerClientCommTask(Service _service) {
        super();
        service = _service;
    }

    @Override
    protected void onPreExecute() {}

    @Override
    protected String doInBackground(BroadcastMessage... params) {
        BroadcastMessage message = params[0];
        try {
            socket = new SimWifiP2pSocket(message.getIp(), GlobalService.SERVICE_PORT);
            ObjectOutputStream o = new ObjectOutputStream(socket.getOutputStream());
            o.writeObject(message);
            Log.d("MessageSend", "SENT MESSAGE " + message.getMessageType().toString());
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

