package pt.utl.ist.cmov.airdesk.activities;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import pt.utl.ist.cmov.airdesk.R;
import pt.utl.ist.cmov.airdesk.domain.AirdeskManager;
import pt.utl.ist.cmov.airdesk.domain.exceptions.UserAlreadyExistsException;

public class MainActivity extends ActionBarActivity {

    String name;
    String email;
    String registerEmail;
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
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    public void register(View v) {
        EditText nameView = (EditText) findViewById(R.id.nameEditText);
        EditText emailRegisterView = (EditText) findViewById(R.id.registerEmailText);
        EditText emailView = (EditText) findViewById(R.id.emailEditText);

        name = nameView.getText().toString();
        registerEmail = emailRegisterView.getText().toString();
        email = emailView.getText().toString();

        Context context = getApplicationContext();
        int duration = Toast.LENGTH_SHORT;
        Toast toast;
        CharSequence text;

        if (name.contains(" ") || registerEmail.contains(" ") || name.contains("\n") || registerEmail.contains("\n")) {
            text = "No spaces or line breaks allowed!";
            toast = Toast.makeText(context, text, duration);
            toast.show();
            return;
        }

        if (name.equals("") || registerEmail.equals("")) {
            text = "Please fill in all fields!";
            toast = Toast.makeText(context, text, duration);
            toast.show();
            return;
        }

        try {
            manager.registerUser(name, registerEmail);
        } catch (UserAlreadyExistsException e) {
            text = e.getMessage();
            toast = Toast.makeText(context, text, duration);
            toast.show();
            return;
        }

        Intent intent = new Intent(this, ListWorkspaces.class);
        startActivity(intent);
    }

    public void login(View v) {
        EditText emailView = (EditText) findViewById(R.id.emailEditText);
        email = emailView.getText().toString();

       if (!manager.login(email)) {
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
