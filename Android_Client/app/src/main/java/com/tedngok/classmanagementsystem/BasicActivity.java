package com.tedngok.classmanagementsystem;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import general.Common;
import general.WebSocketIO;
import io.socket.emitter.Emitter;

public class BasicActivity extends AppCompatActivity
        implements  ClassScheduleFragment.OnFragmentInteractionListener,  SMSLogFragment.OnFragmentInteractionListener, NavigationView.OnNavigationItemSelectedListener, SearchAttachmentFragment.OnFragmentInteractionListener, SearchClassFragment.OnFragmentInteractionListener, SearchStudentFragment.OnFragmentInteractionListener {

    private FragmentManager _fm;
    private FragmentTransaction _ft;
    private DrawerLayout _drawer;
    private NavigationView nav_view;
    private Common common;
    private WebSocketIO webSocketIO;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_basic);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        _fm = getSupportFragmentManager();
        _ft = _fm.beginTransaction();
        _drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, _drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        _drawer.setDrawerListener(toggle);
        toggle.syncState();

        

        nav_view = (NavigationView) findViewById(R.id.nav_view);
        nav_view.setNavigationItemSelectedListener(this);

        common = new Common(getBaseContext());
        webSocketIO = new WebSocketIO(getBaseContext());

        WebSocketIO._socket.off("getProfile2");
        WebSocketIO._socket.on("getProfile2", new Emitter.Listener() {
            @Override
            public void call(final Object... args) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        String firstname = "", lastname = "", email = "", img = null;

                        try {
                            JSONObject json = (JSONObject) args[0];
                            if (json.has(getString(R.string.jsonUser))) {
                                JSONObject jsonUser = json.getJSONObject(getString(R.string.jsonUser));

                                if (jsonUser.has(getString(R.string.jsonFirstName))) {
                                    firstname = jsonUser.getString(getString(R.string.jsonFirstName));
                                }
                                if (jsonUser.has(getString(R.string.jsonLastName))) {
                                    lastname = jsonUser.getString(getString(R.string.jsonLastName));
                                }
                                if (jsonUser.has(getString(R.string.jsonEmail))) {
                                    email = jsonUser.getString(getString(R.string.jsonEmail));
                                }
                                if (jsonUser.has(getString(R.string.jsonImage))) {
                                    img = jsonUser.getString(getString(R.string.jsonImage));

                                    ImageView imgUser = (ImageView) findViewById(R.id.imgUser);

                                    if (img != null && imgUser != null)
                                        imgUser.setImageBitmap(common.base64ToBitmap(img));
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        TextView txtNavEmail = (TextView) findViewById(R.id.email);
                        TextView txtNavName = (TextView) findViewById(R.id.name);
                        if (txtNavEmail != null && txtNavName != null){
                            txtNavName.setText("Hello,  " + lastname + " " + firstname + "!");
                            txtNavEmail.setText(email);
                        }


                    }
                });
            }
        });

        _ft.replace(R.id.content_main, new ClassScheduleFragment());
        _ft.commit();

        updateDrawerInfo();

    }

    @Override
    public void onBackPressed() {
        _drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (_drawer.isDrawerOpen(GravityCompat.START)) {
            _drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        updateDrawerInfo();
        return true;
    }

    private void updateDrawerInfo() {
//        final SharedPreferences sp = getSharedPreferences(getString(R.string.login_preference_file), Context.MODE_PRIVATE);
//        String firstname = sp.getString(getString(R.string.jsonFirstName), "");
//        String lastname = sp.getString(getString(R.string.jsonLastName), "");
//        String email = sp.getString(getString(R.string.jsonEmail), "");
//        TextView txtNavEmail = (TextView) findViewById(R.id.email);
//        TextView txtNavName = (TextView) findViewById(R.id.name);
//        txtNavName.setText("Hello,  " + lastname + " " + firstname + "!");
//        txtNavEmail.setText(email);
        try {
            JSONObject json = new JSONObject();
            json.put(getString(R.string.jsonToken), common.getToken());
            new LoadProfileTask().execute(json);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        Intent i;
        switch (id) {
            case R.id.action_logout:
                SharedPreferences sp = getSharedPreferences(getString(R.string.login_preference_file), 0);
                SharedPreferences.Editor editor = sp.edit();
                editor.remove(getString(R.string.token));
                editor.commit();
                i = new Intent(this, LoginActivity.class);
                startActivity(i);
                finish();
                return true;
            case R.id.action_profile:
                i = new Intent(this, ProfileActivity.class);
                startActivity(i);
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        _ft = _fm.beginTransaction();
        if (id == R.id.nav_class_schedulas) {
            _ft.replace(R.id.content_main, new ClassScheduleFragment());
            getSupportActionBar().setTitle("Class Schedule");
        } else if (id == R.id.nav_classes) {
            _ft.replace(R.id.content_main, new SearchClassFragment());
            getSupportActionBar().setTitle("Classes");
        } else if (id == R.id.nav_gallery) {
            _ft.replace(R.id.content_main, new SearchAttachmentFragment());
            getSupportActionBar().setTitle("Gallery");
        } else if (id == R.id.nav_sms_log) {
            _ft.replace(R.id.content_main, new SMSLogFragment());
            getSupportActionBar().setTitle("SMS Log");
        } else if (id == R.id.nav_students) {
            _ft.replace(R.id.content_main, new SearchStudentFragment());
            getSupportActionBar().setTitle("Student");
        }
        _ft.commit();
        _drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
    }

    public class LoadProfileTask extends AsyncTask<JSONObject, Void, Boolean> {

        @Override
        protected Boolean doInBackground(JSONObject... params) {
            JSONObject json = (JSONObject) params[0];
            webSocketIO._socket.emit("getProfile", json);
            return null;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
        }
    }


}
