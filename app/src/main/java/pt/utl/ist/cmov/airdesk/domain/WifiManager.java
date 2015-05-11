package pt.utl.ist.cmov.airdesk.domain;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;


import android.os.AsyncTask;
import android.os.IBinder;
import android.os.Messenger;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import pt.inesc.termite.wifidirect.SimWifiP2pDevice;
import pt.inesc.termite.wifidirect.SimWifiP2pDeviceList;
import pt.inesc.termite.wifidirect.SimWifiP2pInfo;
import pt.inesc.termite.wifidirect.SimWifiP2pManager;
import pt.inesc.termite.wifidirect.service.SimWifiP2pService;
import pt.inesc.termite.wifidirect.sockets.SimWifiP2pSocket;
import pt.inesc.termite.wifidirect.sockets.SimWifiP2pSocketServer;
import pt.utl.ist.cmov.airdesk.R;
import pt.utl.ist.cmov.airdesk.activities.ListWorkspaces;
import pt.utl.ist.cmov.airdesk.activities.MainActivity;
import pt.utl.ist.cmov.airdesk.domain.network.GlobalService;

public class WifiManager implements Serializable {

    public static final String TAG = "airdesk";
    private static WifiManager instance;

    private SimWifiP2pManager mManager = null;
    private SimWifiP2pManager.Channel mChannel = null;
    private Messenger mService = null;
    private boolean mBound = false;
    private SimWifiP2pSocketServer mSrvSocket = null;
    private SimWifiP2pSocket mCliSocket = null;
    private TextView mTextInput;
    private TextView mTextOutput;

    //private HashMap<String, User> userToConnection = new HashMap<String, User>();

    private String connectedUser;

    private GlobalService globalService;

    private MainActivity activityLW;
    private SimWifiP2pDeviceList deviceList;

    public SimWifiP2pManager getManager() {
        return mManager;
    }

    public SimWifiP2pManager.Channel getChannel() {
        return mChannel;
    }
    private ServiceConnection mConnection = new ServiceConnection() {
        // callbacks for service binding, passed to bindService()

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            mService = new Messenger(service);
            mManager = new SimWifiP2pManager(mService);
            mChannel = mManager.initialize(activityLW.getApplication(), activityLW.getMainLooper(), null);
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mService = null;
            mManager = null;
            mChannel = null;
            mBound = false;
        }
    };

    public void WifiOn(MainActivity a, View v) {
        activityLW = a;

        // simple chat does: is it okay to bind to listworkspaces? TODO: ??
        Intent intent = new Intent(v.getContext(), SimWifiP2pService.class);
        activityLW.bindService(intent, mConnection, Context.BIND_AUTO_CREATE);

        // GlobalService.startService(); ? TODO: substitute SimWifiP2pService for our globalservice?

    }

    public void WifiOff() {
        if (mBound) {
            // simple chat does:
            activityLW.unbindService(mConnection);

            //GlobalService.stopService();
            mBound = false;

        }
    }

    public WifiManager (){}

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


    public void setIPS(SimWifiP2pDeviceList deviceList) {
        this.deviceList = deviceList;
    }

    public SimWifiP2pDeviceList getIPS() {
        return deviceList;
    }
}
