package pt.utl.ist.cmov.airdesk.activities;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.regex.Pattern;

import pt.utl.ist.cmov.airdesk.R;
import pt.utl.ist.cmov.airdesk.domain.AirdeskManager;
import pt.utl.ist.cmov.airdesk.domain.exceptions.UserDoesNotHavePermissionsToDeleteFileException;

public class Topics extends ActionBarActivity implements Updatable {

    private AirdeskManager manager;
    ArrayList<String> topicList;
    ArrayAdapter<String> adapter;
    ListView topicListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_topics);

        manager = AirdeskManager.getInstance(getApplicationContext());

        manager.setCurrentActivity(this);
        updateUI();
    }

    @Override
    protected void onPause() {
        manager.saveAppState(getApplicationContext());
        super.onPause();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_list_workspaces, menu);
        return true;
    }

    public void addTopic(View v){
        EditText topicText = (EditText) findViewById(R.id.topicText);
        String topic = topicText.getText().toString();

        if (topic.equals("")) {
            Context context = getApplicationContext();
            CharSequence text = "Please fill in the field!";
            int duration = Toast.LENGTH_SHORT;
            Toast toast = Toast.makeText(context, text, duration);
            toast.show();
            return;
        }

        if (Pattern.compile("^\\s+$").matcher(topic).matches()) {
            Context context = getApplicationContext();
            CharSequence text = "Topic name must contain at least one meaningful character!";
            int duration = Toast.LENGTH_SHORT;
            Toast toast = Toast.makeText(context, text, duration);
            toast.show();
            return;
        }

        if (topic.contains("\n")) {
            Context context = getApplicationContext();
            CharSequence text = "No line breaks allowed!";
            int duration = Toast.LENGTH_SHORT;
            Toast toast = Toast.makeText(context, text, duration);
            toast.show();
            return;
        }

        manager.getLoggedUser().addTopic(topic);
        adapter.notifyDataSetChanged();
        topicText.setText("");

    }

    @Override
    public void updateUI() {
        topicList = manager.getLoggedUser().getTopics();

        topicListView = (ListView) findViewById(R.id.topicList);
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, topicList );
        topicListView.setAdapter(adapter);
        final Context that = this;

        this.topicListView.setLongClickable(true);
        this.topicListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            public boolean onItemLongClick(AdapterView<?> parent, View v, final int position, long id) {
                new AlertDialog.Builder(that)
                        .setTitle("Unsubscribe from " + topicList.get(position) + "?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                manager.getLoggedUser().removeTopic(topicList.get(position));
                                adapter.notifyDataSetChanged();
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
}