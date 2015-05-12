package pt.utl.ist.cmov.airdesk.domain.network;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.p2p.WifiP2pDevice;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import java.util.Collection;
import java.util.HashMap;

import pt.inesc.termite.wifidirect.SimWifiP2pBroadcast;
import pt.inesc.termite.wifidirect.SimWifiP2pDevice;
import pt.inesc.termite.wifidirect.SimWifiP2pDeviceList;
import pt.inesc.termite.wifidirect.SimWifiP2pInfo;
import pt.utl.ist.cmov.airdesk.activities.ListWorkspaces;
import pt.utl.ist.cmov.airdesk.domain.AirdeskManager;
import pt.utl.ist.cmov.airdesk.domain.BroadcastMessages;
import pt.utl.ist.cmov.airdesk.domain.WifiManager;
import pt.utl.ist.cmov.airdesk.domain.exceptions.FileAlreadyExistsException;
import pt.utl.ist.cmov.airdesk.domain.exceptions.UserDoesNotHavePermissionsToCreateFilesException;
import pt.utl.ist.cmov.airdesk.domain.exceptions.UserDoesNotHavePermissionsToDeleteFileException;
import pt.utl.ist.cmov.airdesk.domain.exceptions.UserDoesNotHavePermissionsToDeleteWorkspaceException;

public class AirdeskBroadcastReceiver extends BroadcastReceiver {

    private AirdeskManager manager;
    private Context context;
    private WifiManager wifiManager;

    public AirdeskBroadcastReceiver(Context _context) {
        super();
        manager = AirdeskManager.getInstance(null);
        context = _context;
        wifiManager = WifiManager.getInstance();
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (SimWifiP2pBroadcast.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {

            // This action is triggered when the WDSim Activity changes state:
            // - creating the Activity generates the WIFI_P2P_STATE_ENABLED event
            // - destroying the Activity generates the WIFI_P2P_STATE_DISABLED event

            int state = intent.getIntExtra(SimWifiP2pBroadcast.EXTRA_WIFI_STATE, -1);
            if (state == SimWifiP2pBroadcast.WIFI_P2P_STATE_ENABLED) {
                Toast.makeText(context.getApplicationContext(), "WiFi Direct enabled", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(context.getApplicationContext(), "WiFi Direct disabled", Toast.LENGTH_SHORT).show();
            }

        } else if (SimWifiP2pBroadcast.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {

            // Request available peers from the wifi p2p manager. This is an
            // asynchronous call and the calling activity is notified with a
            // callback on PeerListListener.onPeersAvailable()

            Toast.makeText(context.getApplicationContext(), "Peer list changed", Toast.LENGTH_SHORT).show();

        } else if (SimWifiP2pBroadcast.WIFI_P2P_NETWORK_MEMBERSHIP_CHANGED_ACTION.equals(action)) {

            SimWifiP2pInfo ginfo = (SimWifiP2pInfo) intent.getSerializableExtra(SimWifiP2pBroadcast.EXTRA_GROUP_INFO);
            ginfo.print();
            Toast.makeText(context.getApplicationContext(), "Network membership changed", Toast.LENGTH_SHORT).show();

        } else if (SimWifiP2pBroadcast.WIFI_P2P_GROUP_OWNERSHIP_CHANGED_ACTION.equals(action)) {
            SimWifiP2pInfo ginfo = (SimWifiP2pInfo) intent.getSerializableExtra(SimWifiP2pBroadcast.EXTRA_GROUP_INFO);
            ginfo.print();
            Toast.makeText(context.getApplicationContext(), "Group ownership changed", Toast.LENGTH_SHORT).show();
        } else if (BroadcastMessages.FILE_ADDED_TO_WORKSPACE.equals(action)) {

            String workspaceName = intent.getStringExtra("workspaceName");
            String fileName = intent.getStringExtra("fileName");
            try {
                manager.newFileAdded(workspaceName, fileName);
            } catch (FileAlreadyExistsException e) {
                e.printStackTrace();
            } catch (UserDoesNotHavePermissionsToCreateFilesException e) {
                e.printStackTrace();
            }

        } else if (BroadcastMessages.FILE_CHANGED.equals(action)) {

            String workspaceName = intent.getStringExtra("workspaceName");
            String fileName = intent.getStringExtra("fileName");
           // ((ListFiles)mActivity).fileChanged(workspaceName, fileName);

            try {
                manager.newFileAdded(workspaceName, fileName);
            } catch (FileAlreadyExistsException e) {
                e.printStackTrace();
            } catch (UserDoesNotHavePermissionsToCreateFilesException e) {
                e.printStackTrace();
            }

        } else if (BroadcastMessages.FILE_DELETED.equals(action)) {

            String workspaceName = intent.getStringExtra("workspaceName");
            String fileName = intent.getStringExtra("fileName");

            try {
                manager.fileDeleted(workspaceName, fileName);
            } catch (UserDoesNotHavePermissionsToDeleteFileException e) {
                e.printStackTrace();
            }

        } else if (BroadcastMessages.INVITATION_TO_WORKSPACE.equals(action)) {

            String workspaceName = intent.getStringExtra("workspaceName");
            String username = intent.getStringExtra("username");

            // TODO: manager.inviteUser();

        } else if (BroadcastMessages.WORKSPACE_DELETED.equals(action)) {

            String workspaceName = intent.getStringExtra("workspaceName");

            try {
                manager.workspaceDeleted(workspaceName);
            } catch (UserDoesNotHavePermissionsToDeleteWorkspaceException e) {
                e.printStackTrace();
            }

        } else if (BroadcastMessages.WORKSPACE_TOPIC_MATCH.equals(action)) {
            // TODO: manager update
            String workspaceName = intent.getStringExtra("workspaceName");
            //((ListWorkspaces)mActivity).workspaceTopicMatch(workspaceName);
        }
    }
}
