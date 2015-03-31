package pt.utl.ist.cmov.airdesk.activities;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import pt.utl.ist.cmov.airdesk.R;
import pt.utl.ist.cmov.airdesk.domain.AirdeskManager;

public class workspaceSettings extends ActionBarActivity {
    String workspaceName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_workspace_settings);

        workspaceName = getIntent().getExtras().getString("workspaceName");

        TextView workspaceNameView = (TextView) findViewById(R.id.workspaceNameText);
        workspaceNameView.setText(workspaceName);

        TextView QuotaView = (TextView) findViewById(R.id.quotaText);
        //fix me: get real values from manager
        QuotaView.setText("Quota used/total: " + "100/1000");
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
        intent.putExtra("workspaceName", workspaceName);
        startActivity(intent);
    }
}
