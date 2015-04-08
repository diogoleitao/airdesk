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

    String name;
    String email;
    String nickname;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SharedPreferences prefs = this.getSharedPreferences(
                "pt.utl.ist.cmov.airdesk", Context.MODE_PRIVATE);

        AirdeskManager.getInstance();
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
        EditText nameView = (EditText) findViewById(R.id.nameEditText);
        EditText nicknameView = (EditText) findViewById(R.id.nicknameEditText);
        EditText emailView = (EditText) findViewById(R.id.emailEditText);

        name = nameView.getText().toString();
        nickname = nicknameView.getText().toString();
        email = emailView.getText().toString();

        Context context = getApplicationContext();
        int duration = Toast.LENGTH_SHORT;
        Toast toast;
        CharSequence text;

        if(name.contains(" ") || nickname.contains(" ") || email.contains(" ") || name.contains("\n") || nickname.contains("\n") || email.contains("\n")) {
            text = "No spaces or line breaks allowed!";
            toast= Toast.makeText(context, text, duration);
            toast.show();
            return;
        }

        if(name.equals("") || nickname.equals("") || email.equals("")) {
            text = "Please fill in all fields!";
            toast= Toast.makeText(context, text, duration);
            toast.show();
            return;
        }

        try {
            AirdeskManager.getInstance().registerUser( name,  nickname,  email);
        } catch (UserAlreadyExistsException e) {
            text = "User Already Exists!";
            toast= Toast.makeText(context, text, duration);
            toast.show();
            return;
        }
        Intent intent = new Intent(this, ListWorkspaces.class);
        startActivity(intent);
    }

    public void login(View v) {
        EditText nicknameView = (EditText) findViewById(R.id.nicknameEditText);
        nickname = nicknameView.getText().toString();

       if(! AirdeskManager.getInstance().login(nickname)) {
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
