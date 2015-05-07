package pt.utl.ist.cmov.airdesk.activities;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.regex.Pattern;

import pt.utl.ist.cmov.airdesk.R;
import pt.utl.ist.cmov.airdesk.domain.AirdeskManager;
import pt.utl.ist.cmov.airdesk.domain.exceptions.FileAlreadyExistsException;
import pt.utl.ist.cmov.airdesk.domain.exceptions.UserDoesNotHavePermissionsToCreateFilesException;
import pt.utl.ist.cmov.airdesk.domain.exceptions.UserDoesNotHavePermissionsToDeleteFileException;

public class ListFiles extends ActionBarActivity {

    ArrayAdapter<String> adapter;
    ListView fileListView;
    ArrayList<String> fileNameList;
    String workspaceName;
    AirdeskManager manager;

    @Override
    protected void onPause() {
        manager.saveAppState(getApplicationContext());
        super.onPause();
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, ListWorkspaces.class);
        startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_files);

        manager = AirdeskManager.getInstance(getApplicationContext());
        workspaceName = manager.getCurrentWorkspace();

        fileNameList = manager.getFilesFromWorkspace(workspaceName);

        fileListView = (ListView) findViewById(R.id.filelist);
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, fileNameList);
        fileListView.setAdapter(adapter);
        final Context that = this;

        fileListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                boolean[] privileges = manager.getUserPrivileges(manager.getLoggedUser());
                if (!privileges[0]) { // read privilege
                    Context context = getApplicationContext();
                    CharSequence text = "You don't have privileges to read files!";
                    int duration = Toast.LENGTH_SHORT;
                    Toast toast = Toast.makeText(context, text, duration);
                    toast.show();
                } else {
                    Intent intent = new Intent(that, EditFile.class);
                    manager.getFile(fileNameList.get(position));
                    startActivity(intent);
                }
            }
        });

        this.fileListView.setLongClickable(true);
        this.fileListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            public boolean onItemLongClick(AdapterView<?> parent, View v, final int position, long id) {
                new AlertDialog.Builder(that)
                        .setTitle("Delete " + fileNameList.get(position) + "?")
                        .setMessage("This action is irreversible. This file uses " + manager.getFile(fileNameList.get(position)).getSize() + "kB of space.")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                try {
                                    manager.deleteFile(fileNameList.get(position));
                                    fileNameList.remove(position);
                                    adapter.notifyDataSetChanged();
                                } catch (UserDoesNotHavePermissionsToDeleteFileException e) {
                                    Context context = getApplicationContext();
                                    CharSequence text = e.getMessage();
                                    int duration = Toast.LENGTH_SHORT;

                                    Toast toast = Toast.makeText(context, text, duration);
                                    toast.show();
                                }
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

    public void addFile(View v) {
        EditText filenameView = (EditText) findViewById(R.id.fileNameText);
        String name = filenameView.getText().toString();

        if (Pattern.compile("^\\s+$").matcher(name).matches() || name.equals("") || name.equals(" ")) {
            Context context = getApplicationContext();
            CharSequence text = "File name must contain at least one meaningful character!";
            int duration = Toast.LENGTH_SHORT;
            Toast toast = Toast.makeText(context, text, duration);
            toast.show();
            return;
        }

        if (name.contains("\n")) {
            Context context = getApplicationContext();
            CharSequence text = "No line breaks allowed!";
            int duration = Toast.LENGTH_SHORT;
            Toast toast = Toast.makeText(context, text, duration);
            toast.show();
            return;
        }

        try {
            manager.addNewFile(name);
            filenameView.setText("");
            fileNameList.add(name);
            adapter.notifyDataSetChanged();
        } catch (FileAlreadyExistsException | UserDoesNotHavePermissionsToCreateFilesException e) {
            Context context = getApplicationContext();
            CharSequence text = e.getMessage();
            int duration = Toast.LENGTH_SHORT;
            Toast toast = Toast.makeText(context, text, duration);
            toast.show();
        }
    }

    public void fileAdded(String workspace, String fileName) {

        try {
            manager.updateWorkspaceFileList(workspace, fileName);
            if( workspaceName.equals(workspace)) {
                fileNameList = manager.getFilesFromWorkspace(workspaceName);
                adapter.notifyDataSetChanged();
            }
        } catch (FileAlreadyExistsException | UserDoesNotHavePermissionsToCreateFilesException e) {
            e.printStackTrace();
        }

    }

    public void fileDeleted(String workspace, String fileName) {

        try {
            manager.updateWorkspaceFileList(workspace, fileName);
            if( workspaceName.equals(workspace)) {
                fileNameList = manager.getFilesFromWorkspace(workspaceName);
                adapter.notifyDataSetChanged();
            }
        } catch (FileAlreadyExistsException | UserDoesNotHavePermissionsToCreateFilesException e) {
            e.printStackTrace();
        }
    }
}
