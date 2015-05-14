package pt.utl.ist.cmov.airdesk.activities;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import pt.utl.ist.cmov.airdesk.R;
import pt.utl.ist.cmov.airdesk.domain.AirdeskManager;
import pt.utl.ist.cmov.airdesk.domain.Workspace;
import pt.utl.ist.cmov.airdesk.domain.exceptions.UserDoesNotHavePermissionsToChangePrivilegesException;

public class UserPrivileges extends ActionBarActivity implements Updatable{

    ArrayAdapter<String> adapter;
    ListView userListView;
    ArrayList<String> userNameList;
    Workspace workspace;
    AirdeskManager manager;

    @Override
    protected void onPause() {
        manager.saveAppState(getApplicationContext());
        super.onPause();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_privileges);

        manager = AirdeskManager.getInstance(getApplicationContext());
        manager.setCurrentActivity(this);
        updateUI();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_user_privileges, menu);
        return true;
    }

    @Override
    public void updateUI() {
        workspace = manager.getCurrentWorkspace();

        TextView workspaceView = (TextView)findViewById(R.id.topicText);
        workspaceView.setText("Workspace name: " + workspace.getName());

        userNameList = new ArrayList<String>(manager.getUsersFromWorkspace(workspace.getHash()));
        userNameList.remove(workspace.getOwner());

        userListView = (ListView) findViewById(R.id.userListView);
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, userNameList );
        userListView.setAdapter(adapter);
        final Context that = this;

        userListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                final boolean[] choices;
                choices = manager.getUserPrivileges(workspace.getHash(),userNameList.get(position));
                new AlertDialog.Builder(that)
                        .setTitle("Edit " + userNameList.get(position) + "'s Privileges")
                        .setMultiChoiceItems(new CharSequence[] { "Read", "Write", "Create", "Delete"}, choices, new DialogInterface.OnMultiChoiceClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                                choices[which] = isChecked;
                            }
                        })
                        .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                try {
                                    manager.changeUserPrivileges(workspace.getHash(),userNameList.get(position), choices);
                                } catch (UserDoesNotHavePermissionsToChangePrivilegesException e) {
                                    Context context = getApplicationContext();
                                    CharSequence text = e.getMessage();
                                    int duration = Toast.LENGTH_SHORT;
                                    Toast toast = Toast.makeText(context, text, duration);
                                    toast.show();
                                }
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // do nothing
                            }
                        })
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
            }
        });
    }
}
