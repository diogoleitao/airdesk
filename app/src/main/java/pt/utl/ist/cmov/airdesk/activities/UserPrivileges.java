package pt.utl.ist.cmov.airdesk.activities;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import pt.utl.ist.cmov.airdesk.R;
import pt.utl.ist.cmov.airdesk.domain.AirdeskManager;

public class UserPrivileges extends ActionBarActivity {

    ArrayAdapter<String> adapter;
    ListView userListView;
    ArrayList<String> userNameList;
    String workspaceName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_privileges);

        AirdeskManager manager = AirdeskManager.getInstance();
        workspaceName = manager.getCurrentWorkspace();

        TextView workspaceNameView = (TextView)findViewById(R.id.workspaceNameText);
        workspaceNameView.setText(workspaceName);

        userNameList = new ArrayList<String>(AirdeskManager.getInstance().getUsersFromWorkspace());
        userNameList.remove(AirdeskManager.getInstance().getLoggedUser());

        userListView = (ListView) findViewById(R.id.userListView);
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, userNameList );
        userListView.setAdapter(adapter);
        final Context that = this;

        userListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    final int position, long id) {
                final boolean[] choices;
                choices = AirdeskManager.getInstance().getUserPrivileges(userNameList.get(position));
                new AlertDialog.Builder(that)
                        .setTitle("Edit " + userNameList.get(position) + "'s Privileges")
                        .setMultiChoiceItems(new CharSequence[]{ "Read", "Write", "Create", "Delete"}, choices, new DialogInterface.OnMultiChoiceClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which,
                                                boolean isChecked) {
                                choices[which] = isChecked;
                            }

                        })
                        .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                AirdeskManager.getInstance().changeUserPrivileges(userNameList.get(position), choices);

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

}
