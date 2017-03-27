// Clean slate version


package com.project.sustain.controllers;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.project.sustain.R;
import com.project.sustain.model.UserProfile;


public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private FirebaseAuth auth;
    private FirebaseUser mUser;
    private FirebaseDatabase mDatabase;
    private Toolbar mToolbar;
    private UserProfile mUserProfile;
    private DatabaseReference mProfiles;

    private Button subWtrRep;
    private Button viewWtrRep;

    private NavigationView navigationView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Get Firebase auth instance
        auth = FirebaseAuth.getInstance();
        mUser = auth.getCurrentUser();

        setContentView(R.layout.activity_main);

        //add Toolbar as ActionBar with menu
        mToolbar = (Toolbar) findViewById(R.id.main_toolbar);
        mToolbar.setTitle("Main Screen");
        this.setSupportActionBar(mToolbar);

        if (mUser != null) {
            mDatabase = FirebaseDatabase.getInstance();
            mProfiles = mDatabase.getReference().child("userProfiles");
            mProfiles.child(mUser.getUid()).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    mUserProfile = dataSnapshot.getValue(UserProfile.class);
                    if (mUserProfile != null) {
                        if (mUserProfile.getUserName().equals("") == false) {
                            ((TextView) findViewById(R.id.nav_main_username)).setText(mUserProfile.getUserName());
                        } else {
                            ((TextView) findViewById(R.id.nav_main_username)).setText(mUser.getEmail());
                        }
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }

        subWtrRep = (Button) findViewById(R.id.subRep);

        subWtrRep.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String toPassIn = getToolbarTitle();
                Intent forWtrRptSubmit = new Intent(MainActivity.this, WaterRptSubmitActivity.class);
                forWtrRptSubmit.putExtra("nameRetrieval", toPassIn);
                startActivity(forWtrRptSubmit);
            }
        });

        viewWtrRep = (Button) findViewById(R.id.viewReportBut);

        viewWtrRep.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(new Intent(MainActivity.this, ViewReportsActivity.class), 5000);
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.main_activity_drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, mToolbar, 0, 0);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.main_activity_nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.setCheckedItem(R.id.nav_main);
    }

    //When returning to main screen make sure main screen is highlighted in nav view
    @Override
    protected void onStart(){
        super.onStart();
        navigationView.setCheckedItem(R.id.nav_main);
    }

    /**
     * Sets the text in the toolbar to the text provided.,
     * @param name The name to put into the toolbar title.
     */
    private void setToolbarTitle(String name) {
        ActionBar actionBar = this.getSupportActionBar();
        actionBar.setTitle(name);

    }

    private String getToolbarTitle() {
        ActionBar actionBar = this.getSupportActionBar();
        return (String) actionBar.getTitle();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                // User chose the "Settings" item, show the app settings UI...
                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }

    // Menu icons are inflated just as they were with actionbar
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_profile) {
            startActivity(new Intent(MainActivity.this, EditProfileActivity.class));
        } else if (id == R.id.nav_map) {
            startActivity(new Intent(MainActivity.this, MapsMarkerActivity.class));
        } else if (id == R.id.nav_water_source) {

        } else if (id == R.id.nav_water_quality) {

        } else if (id == R.id.nav_logout) {
            auth.signOut();
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            finish();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.main_activity_drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
