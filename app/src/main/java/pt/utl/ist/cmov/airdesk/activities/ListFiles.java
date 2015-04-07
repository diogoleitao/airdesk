package pt.utl.ist.cmov.airdesk.activities;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
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

        final AirdeskManager manager = AirdeskManager.getInstance();
        workspaceName = manager.getCurrentWorkspace();

        fileNameList = manager.getFilesFromWorkspace(workspaceName);

        fileListView = (ListView) findViewById(R.id.filelist);
        adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, fileNameList );
        fileListView.setAdapter(adapter);
        final Context that = this;

        fileListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {

                AirdeskManager manager = AirdeskManager.getInstance();
                boolean[] privileges = manager.getUserPrivileges(manager.getLoggedUser().getNickname());
                if(!privileges[1]) { // write privilege
                    Context context = getApplicationContext();
                    CharSequence text = "You don't have privilege to edit files!";
                    int duration = Toast.LENGTH_SHORT;
                    Toast toast = Toast.makeText(context, text, duration);
                    toast.show();
                } else {
                    Intent intent = new Intent(that, EditFile.class);
                    startActivity(intent);
                }
            }
        });

        this.fileListView.setLongClickable(true);
        this.fileListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            public boolean onItemLongClick(AdapterView<?> parent, View v, final int position, long id) {
                new AlertDialog.Builder(that)
                        .setTitle("Delete " + fileNameList.get(position) + "?")
                        .setMessage("This action is irreversible. This file uses "+ manager.getFile(fileNameList.get(position)).getSize() +" of space.")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                AirdeskManager.getInstance().deleteFile(fileNameList.get(position));
                                Intent intent = new Intent(that, ListFiles.class);
                                startActivity(intent);
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // do nothing
                            }
                        })
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
                return true;
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

        boolean[] privileges = manager.getUserPrivileges(manager.getLoggedUser().getNickname());

        if(!privileges[2]) { // create privilege
            Context context = getApplicationContext();
            CharSequence text = "You don't have privilege to add files!";
            int duration = Toast.LENGTH_SHORT;
            Toast toast = Toast.makeText(context, text, duration);
            toast.show();
        } else {
            String name = ((EditText) findViewById(R.id.fileNameText)).getText().toString();

            if(name.equals(""))
                return;

            if(manager.getFile(name) != null){
                Context context = getApplicationContext();
                CharSequence text = "File Already Exists!";
                int duration = Toast.LENGTH_SHORT;

                Toast toast = Toast.makeText(context, text, duration);
                toast.show();
            } else {
                manager.addNewFile(name);

                fileNameList.add(name);
                adapter.notifyDataSetChanged();
            }
        }
    }
}
