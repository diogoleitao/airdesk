package pt.utl.ist.cmov.airdesk.activities;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import pt.utl.ist.cmov.airdesk.R;
import pt.utl.ist.cmov.airdesk.domain.AirdeskManager;
import pt.utl.ist.cmov.airdesk.domain.File;
import pt.utl.ist.cmov.airdesk.domain.exceptions.WorkspaceQuotaReachedException;

public class EditFile extends ActionBarActivity {

    String filename;
    File file;
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
        setContentView(R.layout.activity_edit_file);

        manager = AirdeskManager.getInstance(getApplicationContext());
        workspaceName = manager.getCurrentWorkspace();
        filename = manager.getCurrentFile();
        file = manager.getFile(filename);

        TextView textView = (TextView)findViewById(R.id.fileText);
        textView.setText(file.getContent());
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_edit_file, menu);
        return true;
    }

    public void save(View v) {
        String content = ((EditText) findViewById(R.id.fileText)).getText().toString();

        boolean[] privileges = manager.getUserPrivileges(manager.getLoggedUser());

        if (!privileges[1]) { // read privilege
            Context context = getApplicationContext();
            CharSequence text = "You don't have privileges to edit files!";
            int duration = Toast.LENGTH_SHORT;
            Toast toast = Toast.makeText(context, text, duration);
            toast.show();
            return;
        }

        try {
            manager.saveFile(content);
            Intent intent = new Intent(this, ListFiles.class);
            startActivity(intent);
        } catch (WorkspaceQuotaReachedException e) {
            Context context = getApplicationContext();
            CharSequence text = e.getMessage();
            int duration = Toast.LENGTH_SHORT;
            Toast toast = Toast.makeText(context, text, duration);
            toast.show();
        }
    }

    public void cancelFileEdit(View v) {
        Intent intent = new Intent(this, ListFiles.class);
        startActivity(intent);
    }
}
