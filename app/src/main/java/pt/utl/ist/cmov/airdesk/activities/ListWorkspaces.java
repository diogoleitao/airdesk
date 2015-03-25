package pt.utl.ist.cmov.airdesk.activities;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import pt.utl.ist.cmov.airdesk.R;
import pt.utl.ist.cmov.airdesk.domain.User;
import pt.utl.ist.cmov.airdesk.domain.Workspace;

public class ListWorkspaces extends ActionBarActivity {

    ArrayAdapter<Workspace> adapter;
    ListView workspaceListView;
    List<Workspace> workspaceList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_workspaces);

        workspaceList = new ArrayList<Workspace>();
        // TODO - get workspace list from the user that is logged in

        ListView workspaceListView = (ListView) findViewById(R.id.workspaceList);
        adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, workspaceList );
        workspaceListView.setAdapter(adapter);

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

    // TODO - HAVE A LOGGED IN USER, ADD WORKSPACE TO THAT USER

    // HACK TO TRY LIST ADAPTER
    User user = new User("Joao", "Jonny", "joao.jonny@badmails.com");
    EditText name = (EditText) findViewById(R.id.workspaceNameText);
    Workspace workspace = new Workspace(100, name.getText().toString(),"Jonny");
    workspaceList.add(workspace);
    adapter.notifyDataSetChanged();

}
}
