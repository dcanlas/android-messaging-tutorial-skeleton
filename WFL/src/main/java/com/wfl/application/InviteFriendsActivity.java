package com.wfl.application;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.waffle.wfl.R;

import java.util.ArrayList;
import java.util.List;

public class InviteFriendsActivity extends Activity implements View.OnClickListener{

    ArrayList<String> names = new ArrayList<String>();

    private Button findButton;
    private EditText friendSearch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invite_friends);

        findButton = (Button) findViewById(R.id.findButton);
        friendSearch = (EditText) findViewById(R.id.friendSearch);

        findButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        if (view == findButton) {
            findUsers();
        }
    }

    public void findUsers() {
        //Clear the names array first
        names.clear();
        String name = friendSearch.getText().toString();
        ParseQuery<ParseUser> query = ParseUser.getQuery();
        query.whereStartsWith("username", name);
        query.findInBackground(new FindCallback<ParseUser>() {
            @Override
            public void done(List<ParseUser> userList, ParseException e) {
                if (e == null) {
                    for (int i = 0; i < userList.size(); i++) {
                        names.add(userList.get(i).getUsername().toString());
                    }
                    ListView inviteableListView = (ListView) findViewById(R.id.inviteableListView);
                    //Todo: The below is not using the proper list item, use custom list adapter to do it.
                    ArrayAdapter<String> namesArrayAdapter = new ArrayAdapter<String>(getApplicationContext(),
                            R.layout.user_list_item, names);
                    inviteableListView.setAdapter(namesArrayAdapter);
                }
                else {
                    Toast.makeText(getApplicationContext(),
                            "Cannot find matching user",
                            Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}
