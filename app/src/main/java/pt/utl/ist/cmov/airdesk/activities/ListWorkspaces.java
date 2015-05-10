package pt.utl.ist.cmov.airdesk.activities;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.regex.Pattern;

import pt.inesc.termite.wifidirect.SimWifiP2pBroadcast;
import pt.inesc.termite.wifidirect.sockets.SimWifiP2pSocketManager;
import pt.utl.ist.cmov.airdesk.R;
import pt.utl.ist.cmov.airdesk.domain.AirdeskManager;
import pt.utl.ist.cmov.airdesk.domain.exceptions.WorkspaceAlreadyExistsException;
import pt.utl.ist.cmov.airdesk.domain.network.AirdeskBroadcastReceiver;

public class ListWorkspaces extends ActionBarActivity {

    ArrayAdapter<String> adapter;
    ArrayAdapter<String> adapterForeign;
    ListView workspaceListView;
    ListView foreignWorkspaceListView;
    ArrayList<String> workspaceList;
    ArrayList<String> foreignWorkspaceList;
    AirdeskManager manager;

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

        workspaceList = new ArrayList<String>();
        foreignWorkspaceList = new ArrayList<String>();

        String email = manager.getLoggedUser();

        workspaceList = manager.getWorkspaces(email);
        foreignWorkspaceList = manager.getForeignWorkspaces(email);

        workspaceListView = (ListView) findViewById(R.id.workspaceList);
        foreignWorkspaceListView = (ListView) findViewById(R.id.foreignWorkspaceList);
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, workspaceList );
        adapterForeign = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, foreignWorkspaceList );
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

        // what View ??
        View wifiView = new View(getApplicationContext());
        manager.WifiOn(this, wifiView);

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
}
