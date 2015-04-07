package pt.utl.ist.cmov.airdesk.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import pt.utl.ist.cmov.airdesk.R;
import pt.utl.ist.cmov.airdesk.domain.AirdeskManager;
import pt.utl.ist.cmov.airdesk.domain.exceptions.UserAlreadyExistsException;

public class MainActivity extends ActionBarActivity {

    EditText name;
    EditText email;
    EditText nickname;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SharedPreferences prefs = this.getSharedPreferences(
                "pt.utl.ist.cmov.airdesk", Context.MODE_PRIVATE);

        AirdeskManager.getInstance().populateAirdesk();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

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

    public void register(View v) {
        name = (EditText) findViewById(R.id.nameEditText);
        nickname = (EditText) findViewById(R.id.nicknameEditText);
        email = (EditText) findViewById(R.id.emailEditText);

        Context context = getApplicationContext();
        int duration = Toast.LENGTH_SHORT;
        Toast toast;
        CharSequence text;
        try {
            AirdeskManager.getInstance().registerUser( name.getText().toString(),  nickname.getText().toString(),  email.getText().toString());
        } catch (UserAlreadyExistsException e) {    // excepçoes para controlo ?? mudar
            text = "User Already Exists!";
            toast= Toast.makeText(context, text, duration);
            toast.show();
            return;
        }
        Intent intent = new Intent(this, ListWorkspaces.class);
        startActivity(intent);
    }


    public void login(View v) {
        nickname = (EditText) findViewById(R.id.nicknameEditText);

       if(null == AirdeskManager.getInstance().login(nickname.getText().toString())) {
            Context context = getApplicationContext();
            int duration = Toast.LENGTH_SHORT;
            CharSequence text = "Login failed. Please register.";
            Toast toast = Toast.makeText(context, text, duration);
            toast.show();
            return;
        }

        Intent intent = new Intent(this, ListWorkspaces.class);
        startActivity(intent);
    }
}
