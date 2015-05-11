package pt.utl.ist.cmov.airdesk.activities;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Messenger;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.regex.Pattern;

import pt.inesc.termite.wifidirect.SimWifiP2pBroadcast;
import pt.inesc.termite.wifidirect.SimWifiP2pDevice;
import pt.inesc.termite.wifidirect.SimWifiP2pDeviceList;
import pt.inesc.termite.wifidirect.SimWifiP2pInfo;
import pt.inesc.termite.wifidirect.SimWifiP2pManager;
import pt.inesc.termite.wifidirect.service.SimWifiP2pService;
import pt.inesc.termite.wifidirect.sockets.SimWifiP2pSocket;
import pt.inesc.termite.wifidirect.sockets.SimWifiP2pSocketManager;
import pt.inesc.termite.wifidirect.sockets.SimWifiP2pSocketServer;
import pt.utl.ist.cmov.airdesk.R;
import pt.utl.ist.cmov.airdesk.domain.AirdeskManager;
import pt.utl.ist.cmov.airdesk.domain.WifiManager;
import pt.utl.ist.cmov.airdesk.domain.exceptions.WorkspaceAlreadyExistsException;
import pt.utl.ist.cmov.airdesk.domain.network.AirdeskBroadcastReceiver;

public class ListWorkspaces extends ActionBarActivity implements
        SimWifiP2pManager.PeerListListener, SimWifiP2pManager.GroupInfoListener {

    ArrayAdapter<String> adapter;
    ArrayAdapter<String> adapterForeign;
    ListView workspaceListView;
    ListView foreignWorkspaceListView;
    ArrayList<String> workspaceList;
    ArrayList<String> foreignWorkspaceList;
    AirdeskManager manager;
    private WifiManager wifiManager;

    @Override
    protected void onPause() {
        manager.saveAppState(getApplicationContext());
        super.onPause();
    }

    @Override
    public void onBackPressed() {
        logout(findViewById(R.id.bt_logout));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_workspaces);

        manager = AirdeskManager.getInstance(getApplicationContext());

        workspaceList = new ArrayList<>();
        foreignWorkspaceList = new ArrayList<>();

        String email = manager.getLoggedUser();

        workspaceList = manager.getWorkspaces(email);
        foreignWorkspaceList = manager.getForeignWorkspaces(email);

        workspaceListView = (ListView) findViewById(R.id.workspaceList);
        foreignWorkspaceListView = (ListView) findViewById(R.id.foreignWorkspaceList);
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, workspaceList);
        adapterForeign = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, foreignWorkspaceList);
        workspaceListView.setAdapter(adapter);
        foreignWorkspaceListView.setAdapter(adapterForeign);
        final Context that = this;

        workspaceListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(that, ListFiles.class);
                manager.setCurrentWorkspace(workspaceList.get(position));
                startActivity(intent);
            }
        });

        foreignWorkspaceListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(that, ListFiles.class);
                manager.setCurrentWorkspace(foreignWorkspaceList.get(position));
                startActivity(intent);
            }
        });

        this.workspaceListView.setLongClickable(true);
        this.workspaceListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            public boolean onItemLongClick(AdapterView<?> parent, View v, int position, long id) {
                Intent intent = new Intent(that, workspaceSettings.class);
                manager.setCurrentWorkspace(workspaceList.get(position));
                startActivity(intent);
                return true;
            }
        });

        this.foreignWorkspaceListView.setLongClickable(true);
        this.foreignWorkspaceListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            public boolean onItemLongClick(AdapterView<?> parent, View v, int position, long id) {
                Intent intent = new Intent(that, workspaceSettings.class);
                manager.setCurrentWorkspace(foreignWorkspaceList.get(position));
                startActivity(intent);
                return true;
            }
        });


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

        wifiManager = WifiManager.getInstance();

        wifiOn();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_list_workspaces, menu);
        return true;
    }

    public void addWorkspace(View v) {
        EditText name = (EditText) findViewById(R.id.workspaceNameText);
        String workspaceName = name.getText().toString();
        EditText quotaView = (EditText) findViewById(R.id.quotaText);
        String quotaText = quotaView.getText().toString();
        int quota;

        if (quotaText.equals("") || workspaceName.equals("")) {
            Context context = getApplicationContext();
            CharSequence text = "Please fill in all fields!";
            int duration = Toast.LENGTH_SHORT;
            Toast toast = Toast.makeText(context, text, duration);
            toast.show();
            return;
        }

        quota = Integer.parseInt(quotaText);

        if (Pattern.compile("^\\s+$").matcher(workspaceName).matches()) {
            Context context = getApplicationContext();
            CharSequence text = "Workspace name must contain at least one meaningful character!";
            int duration = Toast.LENGTH_SHORT;
            Toast toast = Toast.makeText(context, text, duration);
            toast.show();
            return;
        }

        if (workspaceName.contains("\n")) {
            Context context = getApplicationContext();
            CharSequence text = "No line breaks allowed!";
            int duration = Toast.LENGTH_SHORT;
            Toast toast = Toast.makeText(context, text, duration);
            toast.show();
            return;
        }

        try {
            manager.addWorkspace(workspaceName, quota);
            workspaceList.add(name.getText().toString());
            adapter.notifyDataSetChanged();
            name.setText("");
            quotaView.setText("");
        } catch (WorkspaceAlreadyExistsException e) {
            Context context = getApplicationContext();
            CharSequence text = e.getMessage();
            int duration = Toast.LENGTH_SHORT;
            Toast toast = Toast.makeText(context, text, duration);
            toast.show();
        }
    }

    public void logout(View v) {
        manager.logout();
        manager.WifiOff();
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    // Workspace was deleted by another user
    public void workspaceRemoved(String workspaceName) {
        foreignWorkspaceList = manager.getForeignWorkspaces(manager.getLoggedUser());
        adapterForeign.notifyDataSetChanged();
    }

    public void invitationToWorkspace(String workspaceName, String username) {
        foreignWorkspaceList = manager.getForeignWorkspaces(manager.getLoggedUser());
        adapterForeign.notifyDataSetChanged();
    }


    /*
	 * Classes implementing chat message exchange
	 */

    public static final String TAG = "simplechat";

    private SimWifiP2pManager mManager = null;
    private SimWifiP2pManager.Channel mChannel = null;
    private Messenger mService = null;
    private boolean mBound = false;
    private SimWifiP2pSocketServer mSrvSocket = null;
    private ReceiveCommTask mComm = null;
    private SimWifiP2pSocket mCliSocket = null;


    public class IncommingCommTask extends AsyncTask<Void, SimWifiP2pSocket, Void> {

        @Override
        protected Void doInBackground(Void... params) {

            Log.d(TAG, "IncommingCommTask started (" + this.hashCode() + ").");

            try {
                mSrvSocket = new SimWifiP2pSocketServer(
                        Integer.parseInt(getString(R.string.port)));
            } catch (IOException e) {
                e.printStackTrace();
            }
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    SimWifiP2pSocket sock = mSrvSocket.accept();
                    if (mCliSocket != null && mCliSocket.isClosed()) {
                        mCliSocket = null;
                    }
                    if (mCliSocket != null) {
                        Log.d(TAG, "Closing accepted socket because mCliSocket still active.");
                        sock.close();
                    } else {
                        publishProgress(sock);
                    }
                } catch (IOException e) {
                    Log.d("Error accepting socket:", e.getMessage());
                    break;
                    //e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(SimWifiP2pSocket... values) {
            mCliSocket = values[0];
            mComm = new ReceiveCommTask();

            mComm.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, mCliSocket);
        }
    }

    public class OutgoingCommTask extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
            Context context = getApplicationContext();
            CharSequence text = "Connecting...";
            int duration = Toast.LENGTH_SHORT;
            Toast toast = Toast.makeText(context, text, duration);
            toast.show();
        }

        @Override
        protected String doInBackground(String... params) {
            try {
                mCliSocket = new SimWifiP2pSocket(params[0],
                        Integer.parseInt(getString(R.string.port)));
            } catch (UnknownHostException e) {
                return "Unknown Host:" + e.getMessage();
            } catch (IOException e) {
                return "IO error:" + e.getMessage();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            if (result != null) {
                Context context = getApplicationContext();
                int duration = Toast.LENGTH_SHORT;
                Toast toast = Toast.makeText(context, result, duration);
                toast.show();
            } else {
                mComm = new ReceiveCommTask();
                mComm.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, mCliSocket);
            }
        }
    }

    public class ReceiveCommTask extends AsyncTask<SimWifiP2pSocket, String, Void> {
        SimWifiP2pSocket s;

        @Override
        protected Void doInBackground(SimWifiP2pSocket... params) {
            BufferedReader sockIn;
            String st;

            s = params[0];
            try {
                sockIn = new BufferedReader(new InputStreamReader(s.getInputStream()));

                while ((st = sockIn.readLine()) != null) {
                    publishProgress(st);
                }
            } catch (IOException e) {
                Log.d("Error reading socket:", e.getMessage());
            }
            return null;
        }

        @Override
        protected void onPreExecute() {
            Context context = getApplicationContext();
            CharSequence text = "Connecting...";
            int duration = Toast.LENGTH_SHORT;
            Toast toast = Toast.makeText(context, text, duration);
            toast.show();

        }

        @Override
        protected void onProgressUpdate(String... values) {
            EditText output = (EditText) findViewById(R.id.workspaceNameText);
            output.append(values[0] + "\n");
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
            if (mBound) {

            } else {

            }
        }
    }

	/*
	 * Listeners associated to WDSim
	 */

    @Override
    public void onPeersAvailable(SimWifiP2pDeviceList peers) {
        Context context = getApplicationContext();
        CharSequence text = "AWSETDRTYOJIKDRYFCTVYGBHUNJIMKDRYCTFVYGBHUNJIDCYFUVGBHNOJXSTDYCFUVGBHNOJXDYCFTUVSBUNJIXCTFYGVBUHNJIM";
        int duration = Toast.LENGTH_SHORT;
        Toast toast = Toast.makeText(context, text, duration);
        toast.show();

        SimWifiP2pDeviceList alreadyConnected = wifiManager.getIPS();
        wifiManager.setIPS(peers);

        // compile list of devices in range
        for (SimWifiP2pDevice d : peers.getDeviceList()) {
            if(alreadyConnected.get(d.getVirtIp()) == null) { // if not connected yet
                connect(d.getVirtIp());
                send();
            }
        }

    }

    public void connect(SimWifiP2pDeviceList peers){
        SimWifiP2pDeviceList alreadyConnected = wifiManager.getIPS();
        wifiManager.setIPS(peers);

        // compile list of devices in range
        for (SimWifiP2pDevice d : peers.getDeviceList()) {
            if(alreadyConnected.get(d.getVirtIp()) == null) { // if not connected yet
                connect(d.getVirtIp());
                send();
            }
        }

    }

    @Override
    public void onGroupInfoAvailable(SimWifiP2pDeviceList devices,
                                     SimWifiP2pInfo groupInfo) {

        // compile list of network members
        StringBuilder peersStr = new StringBuilder();
        for (String deviceName : groupInfo.getDevicesInNetwork()) {
            SimWifiP2pDevice device = devices.getByName(deviceName);
            String devstr = "" + deviceName + " (" +
                    ((device == null) ? "??" : device.getVirtIp()) + ")\n";
            peersStr.append(devstr);
        }

        // display list of network members
        new AlertDialog.Builder(this)
                .setTitle("Devices in WiFi Network")
                .setMessage(peersStr.toString())
                .setNeutralButton("Dismiss", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
                .show();
    }

    public void send(){
        findViewById(R.id.bt_addworkspace).setEnabled(false);
        try {
            mCliSocket.getOutputStream().write((manager.getLoggedUser()).getBytes());
            Log.d("send", "send");
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    /*
	 * Listeners associated to buttons
	 */

    public  void wifiOn(){

        Intent intent = new Intent(getApplicationContext(), SimWifiP2pService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        mBound = true;

        // spawn the chat server background task
        new IncommingCommTask().executeOnExecutor(
                AsyncTask.THREAD_POOL_EXECUTOR);
    }


    public void wifiOff(){
        if (mBound) {
            unbindService(mConnection);
            mBound = false;
        }
    }


    public void connect(String ip){
        Log.d("connnect", "connect");
        new OutgoingCommTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, ip);
    }

   public void disconnect(){

        if (mCliSocket != null) {
            try {
                mCliSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        mCliSocket = null;
    }

    private ServiceConnection mConnection = new ServiceConnection() {
        // callbacks for service binding, passed to bindService()

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            mService = new Messenger(service);
            mManager = new SimWifiP2pManager(mService);
            mChannel = mManager.initialize(getApplication(), getMainLooper(), null);
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
}