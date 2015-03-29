package pt.utl.ist.cmov.airdesk.activities;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import pt.utl.ist.cmov.airdesk.R;
import pt.utl.ist.cmov.airdesk.domain.AirdeskManager;
import pt.utl.ist.cmov.airdesk.domain.File;

public class EditFile extends ActionBarActivity {

    String filename;
    File file;
    String workspaceName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_file);

        workspaceName = getIntent().getExtras().getString("workspaceName");
        filename = getIntent().getExtras().getString("filename");
        file = AirdeskManager.getInstance().getFile(filename);

        TextView textView = (TextView)findViewById(R.id.fileText);
        textView.setText(file.getContent());
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_edit_file, menu);
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

    public void save(View v) {

        AirdeskManager manager = AirdeskManager.getInstance();

        String text = ((EditText) findViewById(R.id.fileText)).getText().toString();

        //file.delete(); ??
        file.edit(text);

        Intent intent = new Intent(this, ListFiles.class);
        intent.putExtra("workspaceName", workspaceName);
        startActivity(intent);
    }

    public void cancelFileEdit(View v) {

        Intent intent = new Intent(this, ListFiles.class);
        intent.putExtra("workspaceName", workspaceName);
        startActivity(intent);
    }
}
