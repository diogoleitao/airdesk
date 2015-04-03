package pt.utl.ist.cmov.airdesk.activities;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

import pt.utl.ist.cmov.airdesk.R;
import pt.utl.ist.cmov.airdesk.domain.AirdeskManager;
import pt.utl.ist.cmov.airdesk.domain.File;
import pt.utl.ist.cmov.airdesk.domain.User;

public class UserPrivileges extends ActionBarActivity {

    ArrayAdapter<User> adapter;
    ListView userListView;
    ArrayList<String> userNameList;
    String workspaceName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_privileges);

        workspaceName = getIntent().getExtras().getString("workspaceName");

        TextView workspaceNameView = (TextView)findViewById(R.id.workspaceNameText);
        workspaceNameView.setText(workspaceName);

        userNameList = AirdeskManager.getInstance().getUsersFromWorkspace(workspaceName);

        userListView = (ListView) findViewById(R.id.userListView);
        adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, userNameList );
        userListView.setAdapter(adapter);
        final Context that = this;
        final boolean[] choices = new boolean[4];

        userListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    final int position, long id) {
                new AlertDialog.Builder(that)
                        .setTitle("Edit " + userNameList.get(position) + "'s Privileges")
                        .setMultiChoiceItems(new CharSequence[]{ "Read", "Write", "Create", "Delete"},  AirdeskManager.getInstance().getUserPrivileges(workspaceName, userNameList.get(position)), new DialogInterface.OnMultiChoiceClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which,
                                                boolean isChecked) {
                                    // If the user checked the item, add it to the selected items
                                choices[which] = isChecked;
                            }

                        })
                        .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                AirdeskManager.getInstance().changeUserPrivileges(workspaceName, userNameList.get(position), choices);

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


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_user_privileges, menu);
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
}
