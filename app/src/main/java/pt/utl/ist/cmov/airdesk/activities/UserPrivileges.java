package pt.utl.ist.cmov.airdesk.activities;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.AttributeSet;
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

        //fixe me get users from workspace
        userNameList = AirdeskManager.getInstance().getFilesFromWorkspace(workspaceName);

        userListView = (ListView) findViewById(R.id.userListView);
        adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, userNameList );
        userListView.setAdapter(adapter);
        final Context that = this;

        userListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                //fixme DIALOG NOT ACTIVITY
                Intent intent = new Intent(that, EditFile.class);
                intent.putExtra("workspaceName", workspaceName);
                intent.putExtra("username", userNameList.get(position));
                startActivity(intent);
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
