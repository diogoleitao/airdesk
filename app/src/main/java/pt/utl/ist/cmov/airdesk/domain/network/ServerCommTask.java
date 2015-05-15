package pt.utl.ist.cmov.airdesk.domain.network;

import android.app.Service;
import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import pt.inesc.termite.wifidirect.sockets.SimWifiP2pSocket;
import pt.inesc.termite.wifidirect.sockets.SimWifiP2pSocketServer;

public class ServerCommTask extends AsyncTask<Void, SimWifiP2pSocket, Void> {
    private SimWifiP2pSocketServer mSrvSocket = null;
    private static final String TAG = "ServerCommTask";
    private Service service;
    private List<IncomingServerClientCommTask> clientSocketTasks;

    public ServerCommTask(Service _service){
        super();
        clientSocketTasks = new ArrayList<IncomingServerClientCommTask>();
        service = _service;
    }

    @Override
    protected Void doInBackground(Void... params) {
        Log.d(TAG, "ServerCommTask started (" + this.hashCode() + ").");

        try {
            mSrvSocket = new SimWifiP2pSocketServer(GlobalService.SERVICE_PORT);
        } catch (IOException e) {
            e.printStackTrace();
        }
        while (!isCancelled()) {
            try {
                SimWifiP2pSocket sock = mSrvSocket.accept();
                publishProgress(sock);
            } catch (IOException e) {
                Log.d("Error accepting socket:", e.getMessage());
                break;
                //e.printStackTrace();
            }
        }
        try {
            mSrvSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onProgressUpdate(SimWifiP2pSocket... values) {
        SimWifiP2pSocket srvClientSocket = values[0];
        IncomingServerClientCommTask mComm = new IncomingServerClientCommTask(service);
        clientSocketTasks.add(mComm);
        mComm.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, srvClientSocket);
    }

    @Override
    protected void onCancelled(){
        for(AsyncTask a : clientSocketTasks){
            a.cancel(true);
        }
    }
}
