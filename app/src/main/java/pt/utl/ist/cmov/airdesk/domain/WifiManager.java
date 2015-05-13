package pt.utl.ist.cmov.airdesk.domain;


import android.os.AsyncTask;
import android.os.Messenger;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

import pt.inesc.termite.wifidirect.SimWifiP2pDeviceList;
import pt.inesc.termite.wifidirect.SimWifiP2pManager;
import pt.inesc.termite.wifidirect.sockets.SimWifiP2pSocket;
import pt.inesc.termite.wifidirect.sockets.SimWifiP2pSocketServer;
import pt.utl.ist.cmov.airdesk.activities.MainActivity;
import pt.utl.ist.cmov.airdesk.domain.network.GlobalService;
import pt.utl.ist.cmov.airdesk.domain.network.OutgoingServerClientCommTask;

public class WifiManager implements Serializable {

    public static final String TAG = "airdesk";
    private static WifiManager instance;

    private SimWifiP2pManager mManager = null;
    private SimWifiP2pManager.Channel mChannel = null;
    private Messenger mService = null;
    private boolean mBound = false;
    private SimWifiP2pSocketServer mSrvSocket = null;
    private SimWifiP2pSocket mCliSocket = null;

    private String connectedUser;

    private MainActivity activityLW;
    private static ArrayList<String> IPs;

    public SimWifiP2pManager getManager() {
        return mManager;
    }

    public WifiManager() {
    }

    public static WifiManager getInstance() {
        if (instance == null) {
            instance = new WifiManager();
        }
        return instance;
    }

    public String getConnectedUser() {
        return connectedUser;
    }

    public void setConnectedUser(String connectedUser) {
        this.connectedUser = connectedUser;
    }

    public void setIPs(ArrayList<String> IPs) {
        this.IPs = IPs;
    }

    public void addIP(String virtIp) {
        IPs.add(virtIp);
    }

    public ArrayList<String> getIPs() {
        return IPs;
    }


    public static void broadcastWorkspaceUpdate(Workspace w) {
/*
        for (String ip : IPs){
        //create outgoing task for user
        //TODO new OutgoingServerClientCommTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, ip);

        }*/
    }

}
