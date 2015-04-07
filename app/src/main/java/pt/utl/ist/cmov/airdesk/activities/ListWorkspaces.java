package pt.utl.ist.cmov.airdesk.activities;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import pt.utl.ist.cmov.airdesk.R;
import pt.utl.ist.cmov.airdesk.domain.AirdeskManager;
import pt.utl.ist.cmov.airdesk.domain.User;
import pt.utl.ist.cmov.airdesk.domain.Workspace;
import pt.utl.ist.cmov.airdesk.domain.exceptions.WorkspaceAlreadyExistsException;

public class ListWorkspaces extends ActionBarActivity {

    ArrayAdapter<String> adapter;
    ListView workspaceListView;
    ArrayList<String> workspaceList;

    @Override
    public void onBackPressed() {
        logout(findViewById(R.id.bt_logout));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_workspaces);

        final AirdeskManager manager = AirdeskManager.getInstance();

        workspaceList = new ArrayList<String>();

        String nickname = manager.getLoggedUser();

        workspaceList = manager.login(nickname);

        workspaceListView = (ListView) findViewById(R.id.workspaceList);
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, workspaceList );
        workspaceListView.setAdapter(adapter);
        final Context that = this;

        workspaceListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                Intent intent = new Intent(that, ListFiles.class);
                manager.setCurrentWorkspace(workspaceList.get(position));
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
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_list_workspaces, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void addWorkspace(View v) {

        AirdeskManager manager = AirdeskManager.getInstance();
        EditText name = (EditText) findViewById(R.id.workspaceNameText);
        String workspaceName = name.getText().toString();
        EditText quotaView = (EditText) findViewById(R.id.quotaText);
        String quotaText = quotaView.getText().toString();
        int quota;
        if (!quotaText.equals("")) {
            quota = Integer.parseInt(quotaText);
        } else
            return;

        if(workspaceName.equals(""))
            return;

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
        AirdeskManager.getInstance().logout();
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }
}
