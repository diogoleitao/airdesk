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
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_workspaces);

        AirdeskManager manager = AirdeskManager.getInstance();

        workspaceList = new ArrayList<String>();

        String nickname = getIntent().getExtras().getString("nickname");
        String email = getIntent().getExtras().getString("email");
        workspaceList = manager.login(nickname, email);

        workspaceListView = (ListView) findViewById(R.id.workspaceList);
        adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, workspaceList );
        workspaceListView.setAdapter(adapter);
        final Context that = this;

        workspaceListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                Intent intent = new Intent(that, ListFiles.class);
                intent.putExtra("workspaceName", workspaceList.get(position));
                startActivity(intent);
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
        User user = manager.getLoggedUser();
        EditText name = (EditText) findViewById(R.id.workspaceNameText);
        try {
            manager.addWorkspace(user.getNickname(), name.getText().toString());
            workspaceList.add(name.getText().toString());
            adapter.notifyDataSetChanged();
        } catch (WorkspaceAlreadyExistsException e) {
            Context context = getApplicationContext();
            CharSequence text = "Workspace Already Exists!";
            int duration = Toast.LENGTH_SHORT;

            Toast toast = Toast.makeText(context, text, duration);
            toast.show();
        }
    }
}
