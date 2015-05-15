package pt.utl.ist.cmov.airdesk.domain.network;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import pt.inesc.termite.wifidirect.SimWifiP2pBroadcast;
import pt.inesc.termite.wifidirect.SimWifiP2pInfo;

public class AirdeskBroadcastReceiver extends BroadcastReceiver {

    private static final String TAG = "BroadcastReceiver";
    private GlobalService service;

    public AirdeskBroadcastReceiver(GlobalService _context) {
        super();
        service = _context;
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

            Log.d(TAG, "Peer list changed");
            service.registerDeviceListCallback();

        } else if (SimWifiP2pBroadcast.WIFI_P2P_NETWORK_MEMBERSHIP_CHANGED_ACTION.equals(action)) {

            SimWifiP2pInfo ginfo = (SimWifiP2pInfo) intent.getSerializableExtra(SimWifiP2pBroadcast.EXTRA_GROUP_INFO);
            ginfo.print();
            Log.d(TAG, "Network membership changed");

        } else if (SimWifiP2pBroadcast.WIFI_P2P_GROUP_OWNERSHIP_CHANGED_ACTION.equals(action)) {
            SimWifiP2pInfo ginfo = (SimWifiP2pInfo) intent.getSerializableExtra(SimWifiP2pBroadcast.EXTRA_GROUP_INFO);
            ginfo.print();
            Log.d(TAG, "Group ownership changed");
        }
    }
}
