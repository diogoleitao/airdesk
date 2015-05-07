package pt.utl.ist.cmov.airdesk.domain.network;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import pt.inesc.termite.wifidirect.SimWifiP2pBroadcast;
import pt.inesc.termite.wifidirect.SimWifiP2pInfo;
import pt.utl.ist.cmov.airdesk.domain.BroadcastMessages;

public class AirdeskBroadcastReceiver extends BroadcastReceiver {

    private Service mService;

    public AirdeskBroadcastReceiver(Service service) {
        super();
        this.mService = service;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (SimWifiP2pBroadcast.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {

            // This action is triggered when the WDSim service changes state:
            // - creating the service generates the WIFI_P2P_STATE_ENABLED event
            // - destroying the service generates the WIFI_P2P_STATE_DISABLED event

            int state = intent.getIntExtra(SimWifiP2pBroadcast.EXTRA_WIFI_STATE, -1);
            if (state == SimWifiP2pBroadcast.WIFI_P2P_STATE_ENABLED) {
                Toast.makeText(mService, "WiFi Direct enabled", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(mService, "WiFi Direct disabled", Toast.LENGTH_SHORT).show();
            }

        } else if (SimWifiP2pBroadcast.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {

            // Request available peers from the wifi p2p manager. This is an
            // asynchronous call and the calling activity is notified with AirdeskBroadcastReceiver
            // callback on PeerListListener.onPeersAvailable()

            Toast.makeText(mService, "Peer list changed", Toast.LENGTH_SHORT).show();

        } else if (SimWifiP2pBroadcast.WIFI_P2P_NETWORK_MEMBERSHIP_CHANGED_ACTION.equals(action)) {

            SimWifiP2pInfo ginfo = (SimWifiP2pInfo) intent.getSerializableExtra(SimWifiP2pBroadcast.EXTRA_GROUP_INFO);
            ginfo.print();
            Toast.makeText(mService, "Network membership changed", Toast.LENGTH_SHORT).show();

        } else if (SimWifiP2pBroadcast.WIFI_P2P_GROUP_OWNERSHIP_CHANGED_ACTION.equals(action)) {
            SimWifiP2pInfo ginfo = (SimWifiP2pInfo) intent.getSerializableExtra(SimWifiP2pBroadcast.EXTRA_GROUP_INFO);
            ginfo.print();
            Toast.makeText(mService, "Group ownership changed", Toast.LENGTH_SHORT).show();
        } else if (BroadcastMessages.FILE_ADDED_TO_WORKSPACE.equals(action)) {
            // TODO: manager update
            String workspaceName = intent.getStringExtra("workspaceName");
            String fileName = intent.getStringExtra("fileName");

           // ((ListFiles)mService).fileAdded(workspaceName, fileName);

        } else if (BroadcastMessages.FILE_CHANGED.equals(action)) {
            // TODO: manager update
            String workspaceName = intent.getStringExtra("workspaceName");
            String fileName = intent.getStringExtra("fileName");
           // ((ListFiles)mService).fileChanged(workspaceName, fileName);

        } else if (BroadcastMessages.FILE_DELETED.equals(action)) {
            // TODO: manager update
            String workspaceName = intent.getStringExtra("workspaceName");
            String fileName = intent.getStringExtra("fileName");
           // ((ListFiles) mService).fileDeleted(workspaceName, fileName);

        } else if (BroadcastMessages.INVITATION_TO_WORKSPACE.equals(action)) {
            // TODO: manager update
            String workspaceName = intent.getStringExtra("workspaceName");
            String username = intent.getStringExtra("username");
            //((ListWorkspaces)mService).invitationToWorkspace(workspaceName, username);

        } else if (BroadcastMessages.WORKSPACE_DELETED.equals(action)) {
            // TODO: manager update
            String workspaceName = intent.getStringExtra("workspaceName");
            //((ListWorkspaces)mService).workspaceRemoved(workspaceName);

        } else if (BroadcastMessages.WORKSPACE_TOPIC_MATCH.equals(action)) {
            // TODO: manager update
            String workspaceName = intent.getStringExtra("workspaceName");
            //((ListWorkspaces)mService).workspaceTopicMatch(workspaceName);
        }
    }
}
