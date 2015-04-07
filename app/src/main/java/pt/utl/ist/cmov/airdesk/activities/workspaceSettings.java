package pt.utl.ist.cmov.airdesk.activities;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import pt.utl.ist.cmov.airdesk.R;
import pt.utl.ist.cmov.airdesk.domain.AirdeskManager;
import pt.utl.ist.cmov.airdesk.domain.User;
import pt.utl.ist.cmov.airdesk.domain.exceptions.UserDoesNotExistException;
import pt.utl.ist.cmov.airdesk.domain.exceptions.UserDoesNotHavePermissionsToDeleteWorkspaceException;

public class workspaceSettings extends ActionBarActivity {
    String workspaceName;

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, ListWorkspaces.class);
        startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_workspace_settings);

        AirdeskManager manager = AirdeskManager.getInstance();
        workspaceName = manager.getCurrentWorkspace();

        TextView workspaceNameView = (TextView) findViewById(R.id.workspaceNameText);
        workspaceNameView.setText(workspaceName);

        TextView QuotaView = (TextView) findViewById(R.id.quotaText);

        QuotaView.setText("Quota used/total: " + manager.getUsedQuota(workspaceName) + "/" + manager.getTotalQuota(workspaceName));
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_workspace_settings, menu);
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

    public void startUserPrivileges(View v){
        Intent intent = new Intent(this, UserPrivileges.class);
        startActivity(intent);
    }

    public void applyGlobalPrivileges(View v){
        boolean[] choices = new boolean[4];

        choices[0] = ((CheckBox) findViewById(R.id.writeFilesBox)).isChecked();
        choices[2] = ((CheckBox) findViewById(R.id.deleteFilesBox)).isChecked();
        choices[3] = ((CheckBox) findViewById(R.id.readFilesBox)).isChecked();
        choices[4] = ((CheckBox) findViewById(R.id.createFilesBox)).isChecked();

        AirdeskManager.getInstance().applyGlobalPrivileges(workspaceName, choices);
    }

    public void inviteUser(View v){

        String username = ((TextView)findViewById(R.id.inviteUserText)).getText().toString();
        try {
            AirdeskManager.getInstance().inviteUser(workspaceName, username);
            Context context = getApplicationContext();
            CharSequence text = "Invitation sent.";
            int duration = Toast.LENGTH_SHORT;

            Toast toast = Toast.makeText(context, text, duration);
            toast.show();
            ((TextView)findViewById(R.id.inviteUserText)).setText("");
        } catch (UserDoesNotExistException e) {
            Context context = getApplicationContext();
            CharSequence text = e.getMessage();
            int duration = Toast.LENGTH_SHORT;

            Toast toast = Toast.makeText(context, text, duration);
            toast.show();
        }
    }

    public void deleteThis(View v){
        final Context that = this;
        new AlertDialog.Builder(this)
                .setTitle("Delete " + workspaceName + "?")
                .setMessage("This action is irreversible.")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        try {
                            AirdeskManager.getInstance().deleteWorkspace(workspaceName);
                        } catch (UserDoesNotHavePermissionsToDeleteWorkspaceException e) {
                            Context context = getApplicationContext();
                            CharSequence text = e.getMessage();
                            int duration = Toast.LENGTH_SHORT;

                            Toast toast = Toast.makeText(context, text, duration);
                            toast.show();
                        }
                        Intent intent = new Intent(that, ListWorkspaces.class);
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
    }

    public void addTopic(View v){
        EditText topicView = ((EditText)findViewById(R.id.newTopicText));
        String topicname = topicView.getText().toString();
        AirdeskManager.getInstance().addTopicToWorkspace(topicname);
        topicView.setText("");
    }
}