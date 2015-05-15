package pt.utl.ist.cmov.airdesk.domain.network;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.IBinder;
import android.os.Messenger;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import pt.inesc.termite.wifidirect.SimWifiP2pBroadcast;
import pt.inesc.termite.wifidirect.SimWifiP2pDevice;
import pt.inesc.termite.wifidirect.SimWifiP2pDeviceList;
import pt.inesc.termite.wifidirect.SimWifiP2pInfo;
import pt.inesc.termite.wifidirect.SimWifiP2pManager;
import pt.inesc.termite.wifidirect.service.SimWifiP2pService;
import pt.inesc.termite.wifidirect.sockets.SimWifiP2pSocketManager;
import pt.utl.ist.cmov.airdesk.domain.AirdeskManager;
import pt.utl.ist.cmov.airdesk.domain.BroadcastMessage;

public class GlobalService extends Service implements SimWifiP2pManager.PeerListListener, SimWifiP2pManager.GroupInfoListener{

    public static final String TAG = "airdesk";

    public static final int SERVICE_PORT = 10001;

    private SimWifiP2pManager mManager = null;

    private SimWifiP2pManager.Channel mChannel = null;

    private Messenger mService = null;

    public static List<String> ips;

    private ServerCommTask srvSocketTask;

    public List<IncomingServerClientCommTask> clientSocketTasks;

    public static GlobalService instance;

    @Override
    public IBinder onBind(Intent arg0) {
        throw new UnsupportedOperationException("it is not implemented");
    }

    @Override
    public void onPeersAvailable(SimWifiP2pDeviceList peers) {
        Log.d(TAG, "onPeersAvailable");

        StringBuilder peersStr = new StringBuilder();
        ips.clear();

        // compile list of devices in range
        for (SimWifiP2pDevice device : peers.getDeviceList()) {
            String devstr = "" + device.deviceName + " (" + device.getVirtIp() + "); ";
            peersStr.append(devstr);
            ips.add(device.getVirtIp());
        }
        // TODO: optimization: only the new peers online need to send this message. this is all peers that are online, even if there is only 1 new peer
        if (AirdeskManager.getInstance(this).getLoggedUser() != null){
            BroadcastMessage message = new BroadcastMessage(BroadcastMessage.MessageTypes.I_AM_USER, AirdeskManager.getInstance(this).getLoggedUser().getEmail());
            GlobalService.broadcastMessage(message);

            BroadcastMessage messageTopics = new BroadcastMessage(BroadcastMessage.MessageTypes.WORKSPACE_TOPICS_REQUEST, AirdeskManager.getInstance(this).getLoggedUser().getEmail());
            messageTopics.setTopics(AirdeskManager.getInstance(this).getLoggedUser().getTopics());
            GlobalService.broadcastMessage(messageTopics);
        }

        Log.d(TAG, "Peer list: " + peersStr);
    }

    @Override
    public void onGroupInfoAvailable(SimWifiP2pDeviceList devices,
                                     SimWifiP2pInfo groupInfo) {

        // compile list of network members
        StringBuilder peersStr = new StringBuilder();
        for (String deviceName : groupInfo.getDevicesInNetwork()) {
            SimWifiP2pDevice device = devices.getByName(deviceName);
            String devstr = "" + deviceName + " (" +
                    ((device == null)?"??":device.getVirtIp()) + ")\n";
            peersStr.append(devstr);
        }
        Log.d(TAG, "Peer list: " + peersStr);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        ips = new ArrayList<String>();
        instance = this;

        // register broadcast receiver
        // initialize the WDSim API
        SimWifiP2pSocketManager.Init(getApplicationContext());

        // register broadcast receiver
        IntentFilter filter = new IntentFilter();
        filter.addAction(SimWifiP2pBroadcast.WIFI_P2P_STATE_CHANGED_ACTION);
        filter.addAction(SimWifiP2pBroadcast.WIFI_P2P_PEERS_CHANGED_ACTION);
        filter.addAction(SimWifiP2pBroadcast.WIFI_P2P_NETWORK_MEMBERSHIP_CHANGED_ACTION);
        filter.addAction(SimWifiP2pBroadcast.WIFI_P2P_GROUP_OWNERSHIP_CHANGED_ACTION);
        AirdeskBroadcastReceiver receiver = new AirdeskBroadcastReceiver(this);
        registerReceiver(receiver, filter);

        Intent intent = new Intent(getApplicationContext(), SimWifiP2pService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);

        clientSocketTasks = new ArrayList<IncomingServerClientCommTask>();
        // spawn the chat server background task
        srvSocketTask = new ServerCommTask(this);
        srvSocketTask.executeOnExecutor(
                AsyncTask.THREAD_POOL_EXECUTOR);
    }

    @Override
    public void onDestroy() {
        srvSocketTask.cancel(true);
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    private ServiceConnection mConnection = new ServiceConnection() {
        // callbacks for service binding, passed to bindService()

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            mService = new Messenger(service);
            mManager = new SimWifiP2pManager(mService);
            mChannel = mManager.initialize(getApplication(), getMainLooper(), null);
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mService = null;
            mManager = null;
            mChannel = null;
        }
    };

    public void registerDeviceListCallback(){
        mManager.requestPeers(mChannel, this);
    }

    public SimWifiP2pManager getManager() {
        return mManager;
    }

    public SimWifiP2pManager.Channel getChannel() {
        return mChannel;
    }

    public static void broadcastMessage(BroadcastMessage message){
        for(String ip : ips) {
            message.setIp(ip);
            OutgoingServerClientCommTask task = new OutgoingServerClientCommTask(GlobalService.instance);
            task.executeOnExecutor(
                    AsyncTask.THREAD_POOL_EXECUTOR, message);
        }
    }
}