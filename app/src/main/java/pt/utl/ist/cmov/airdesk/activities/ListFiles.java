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
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import pt.utl.ist.cmov.airdesk.R;
import pt.utl.ist.cmov.airdesk.domain.AirdeskManager;
import pt.utl.ist.cmov.airdesk.domain.File;
import pt.utl.ist.cmov.airdesk.domain.Workspace;

public class ListFiles extends ActionBarActivity {

    ArrayAdapter<File> adapter;
    ListView fileListView;
    ArrayList<String> fileNameList;
    String workspaceName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_files);
        workspaceName = getIntent().getExtras().getString("workspaceName");

        Log.d("name",workspaceName );

        fileNameList = AirdeskManager.getInstance().getFilesFromWorkspace(workspaceName);

        fileListView = (ListView) findViewById(R.id.filelist);
        adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, fileNameList );
        fileListView.setAdapter(adapter);
        final Context that = this;

        fileListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                Intent intent = new Intent(that, EditFile.class);
                intent.putExtra("workspaceName", workspaceName);
                intent.putExtra("filename", fileNameList.get(position));
                startActivity(intent);
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_list_files, menu);
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

    public void addFile(View v) {

        AirdeskManager manager = AirdeskManager.getInstance();

        String name = ((EditText) findViewById(R.id.fileNameText)).getText().toString();

        Log.d("name",workspaceName );

        // como é que getWorkspaceByName(workspaceName) dá null wtf...
        manager.getLoggedUser().getOwnedWorkspaces().get(workspaceName).getFiles().put(name, new File(name));

        fileNameList.add(name);
        adapter.notifyDataSetChanged();
    }
}
