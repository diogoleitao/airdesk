package pt.utl.ist.cmov.airdesk.activities;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import pt.inesc.termite.wifidirect.SimWifiP2pDevice;
import pt.inesc.termite.wifidirect.SimWifiP2pDeviceList;
import pt.inesc.termite.wifidirect.SimWifiP2pManager;
import pt.utl.ist.cmov.airdesk.R;
import pt.utl.ist.cmov.airdesk.domain.AirdeskManager;
import pt.utl.ist.cmov.airdesk.domain.WifiManager;
import pt.utl.ist.cmov.airdesk.domain.exceptions.UserAlreadyExistsException;
import pt.utl.ist.cmov.airdesk.domain.network.GlobalService;

public class MainActivity extends ActionBarActivity {

    String email;
    AirdeskManager manager;


    @Override
    protected void onPause() {
        manager.saveAppState(getApplicationContext());
        super.onPause();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        manager = AirdeskManager.getInstance(getApplicationContext());


        // Start GlobalService
        Intent intent = new Intent(this, GlobalService.class);
        startService(intent);

        if(manager.getLoggedUser() != null){
            intent = new Intent(this, ListWorkspaces.class);
            startActivity(intent);
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }


    public void login(View v) {
        EditText emailView = (EditText) findViewById(R.id.emailEditText);
        email = emailView.getText().toString();

        Context context = getApplicationContext();
        int duration = Toast.LENGTH_SHORT;
        Toast toast;
        CharSequence text;

        if (email.contains(" ") || email.contains("\n")) {
            text = "No spaces or line breaks allowed!";
            toast = Toast.makeText(context, text, duration);
            toast.show();
            return;
        }

        if (email.equals("")) {
            text = "Please fill in all fields!";
            toast = Toast.makeText(context, text, duration);
            toast.show();
            return;
        }

        manager.login(email);

        Intent intent = new Intent(this, ListWorkspaces.class);
        startActivity(intent);
    }
}
