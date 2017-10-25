package com.example.senso.budgetracker;

import android.app.AlarmManager;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.identity.intents.Address;
import com.google.android.gms.instantapps.PackageManagerCompat;
import com.google.android.gms.internal.v;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.example.senso.budgetracker.onClickAwesomeHandler;

import android.Manifest;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import static android.content.pm.PackageManager.PERMISSION_DENIED;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, AdapterView.OnItemSelectedListener {

    public Button button;
    RelativeLayout introMessage;
    RelativeLayout appContent;
    String period= null;
    double tmp_budget = 0;
    Spinner spinner;
    ExpandableListView expandableListView;
    ExpandableListAdapter expandableListAdapter;
    List<String> expandableListTitle;
    LinkedHashMap<String, List<String>> expandableListDetail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);


        //Check if MainActivity is in the first-time Displayed mode (After installation or after Budget erease data), if so :
        // display welcome layouts, set preferences for isFirstTime and get basic variables: period and months. set default categories as well
        final SharedPreferences sharedPref = getSharedPreferences("FirstTime", Context.MODE_PRIVATE);

        if (sharedPref.getString("isFT", null) == null) {

            //first time handling

            setContentView(R.layout.welcome_layout);

            //set spinner
            spinner = (Spinner) findViewById(R.id.period_spinner);
            ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.period_array, android.R.layout.simple_spinner_item);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinner.setAdapter(adapter);
            spinner.setOnItemSelectedListener(this);

            introMessage = (RelativeLayout) findViewById(R.id.welcome_message_layout);
            appContent = (RelativeLayout) findViewById(R.id.app_content_layout);

            final Button startBtn = (Button) findViewById(R.id.start_btn);
            startBtn.setOnClickListener(new View.OnClickListener() {
                                          public void onClick(View v) {

                                              EditText tmp_budget_ET = (EditText) findViewById(R.id.budget_text);
                                              try {
                                                  tmp_budget = Integer.parseInt(tmp_budget_ET.getText().toString().trim());
                                              } catch(NumberFormatException nfe) {

                                                  tmp_budget_ET.setError("Il Budget è obbligatorio!");
                                                  tmp_budget = 0;
                                              }
                                              if (period.equals("null") || tmp_budget == 0) {
                                                  tmp_budget_ET.setError("Entrambi i campi sono obbligatori!");
                                              } else {

                                                  //lets save global project variables
                                                  SharedPreferences.Editor editor = sharedPref.edit();
                                                  editor.putString("isFT", "true");
                                                  editor.putString("period", period);
                                                  putDouble(editor, "currentBudget", tmp_budget);
                                                  putDouble(editor, "budget", tmp_budget);
                                                  editor.apply();


                                                  //creating an DBH instance
                                                  DBHelper dbh = new DBHelper(MainActivity.this);
                                                  if (dbh.addCategory("Prodotto unico") == -1 ||
                                                          dbh.addCategory("Sigarette") == -1 ||
                                                          dbh.addCategory("Bolletta") == -1 )
                                                  {

                                                      Context context = getApplicationContext();
                                                      CharSequence text = "Error writing in the database!";
                                                      int duration = Toast.LENGTH_SHORT;
                                                      Toast.makeText(context, text, duration).show();
                                                  }

                                                  Intent i = new Intent(MainActivity.this, MainActivity.class);
                                                  startActivity(i);
                                              }
                                          }
                                      });




        } else {

            //variables already setted

            setContentView(R.layout.activity_main);

            Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayShowTitleEnabled(false);

            TextView mTitle = (TextView) toolbar.findViewById(R.id.toolbar_title);

            //display remaining budget
            ;
            double tmp_budget = getDouble(sharedPref, "currentBudget", 0);
            if (tmp_budget < 0) { mTitle.setBackgroundColor(Color.parseColor("#FFFA5B53")); }
            else { mTitle.setBackgroundResource(R.color.colorPrimaryDark);}
            mTitle.setText(""+tmp_budget);

            if (sharedPref.getString("isFT", null) != null) {
                buildListExpandableExpense();
                floatingMenuHandler();
            }

        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences sharedPref = getSharedPreferences("FirstTime", Context.MODE_PRIVATE);

        //display remaining budget, builexpandablelist and floating menu
        if (sharedPref.getString("isFT", null) != null) {

            Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayShowTitleEnabled(false);

            TextView mTitle = (TextView) toolbar.findViewById(R.id.toolbar_title);
            double tmp_budget = getDouble(sharedPref, "currentBudget", 0);
            if (tmp_budget < 0) { mTitle.setBackgroundColor(Color.parseColor("#FFFA5B53")); }
            else { mTitle.setBackgroundResource(R.color.colorPrimaryDark);}
            mTitle.setText(""+tmp_budget);

            buildListExpandableExpense();
            floatingMenuHandler();
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }






    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //erase all data part
        if (id == R.id.action_settings) {

            final AlertDialog.Builder builder= new AlertDialog.Builder(MainActivity.this);
            builder.setTitle("Cancellazione Dati")
                    .setMessage("Cliccando OK tutti i dati andranno persi e non sarà possibile recuperarli")
                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //erase everything
                            MainActivity.this.getSharedPreferences("FirstTime", 0).edit().clear().commit();
                            MainActivity.this.deleteDatabase("Expenses.db");
                            Intent i = new Intent(MainActivity.this, MainActivity.class);
                            startActivity(i);
                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //do nothing
                        }
                    }).show();
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_globe) {

            Intent i = new Intent(this, Locations.class);
            i.putExtra("from", "globe");
            startActivity(i);

        } else if (id == R.id.nav_download) {
            Intent i = new Intent(this, reportActivity.class);
            startActivity(i);

        } else {

            Intent i = new Intent(this, Statistics.class);
            startActivity(i);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
    public void dismisWelcomeMessageBox(View view) {
        introMessage.setVisibility(View.INVISIBLE);
        appContent.setVisibility(View.VISIBLE);

    }

    //spinner callbacks
    public void onItemSelected(AdapterView<?> parent, View view,
                               int pos, long id) {
        period = parent.getItemAtPosition(pos).toString();
    }

    public void onNothingSelected(AdapterView<?> parent) {
        // Another interface callback
    }

    SharedPreferences.Editor putDouble(final SharedPreferences.Editor edit, final String key, final double value) {
        return edit.putLong(key, Double.doubleToRawLongBits(value));
    }

    double getDouble(final SharedPreferences prefs, final String key, final double defaultValue) {
        return Double.longBitsToDouble(prefs.getLong(key, Double.doubleToLongBits(defaultValue)));
    }



    // fetch data for expandable list
    public LinkedHashMap<String, List<String>> getLstData() {

        LinkedHashMap<String, List<String>> expandableListDetail = new LinkedHashMap<String, List<String>>();
        List<String> ne = new ArrayList<String>();
        List<String> ple = new ArrayList<String>();
        List<String> pee = new ArrayList<String>();



        DBHelper dbh = new DBHelper(this);

        List<expense> normalExp = dbh.retrieveNormal();
        List<expense> plannedExp= dbh.retrievePlanned();
        List<expense> periodicExp = dbh.retrievePeriodic();

        for (int j = 0; j < normalExp.size(); j++) {

            ne.add(normalExp.get(j).toString());
        }
        for (int j = 0; j < plannedExp.size(); j++) {

            ple.add(plannedExp.get(j).toString());
        }
        for (int j = 0; j < periodicExp.size(); j++) {

            pee.add(periodicExp.get(j).toString());
        }

        expandableListDetail.put("Spese Pianificate", ple);
        expandableListDetail.put("Spese Periodiche", pee)   ;
        expandableListDetail.put("Spese Normali", ne);
        return expandableListDetail;

    }



    public void buildListExpandableExpense() {

        // set expandable expense list view
        expandableListView = (ExpandableListView) findViewById(R.id.expandableListView);
        expandableListDetail = getLstData();
        expandableListTitle = new ArrayList<String>(expandableListDetail.keySet());

        expandableListAdapter = new CustomExpandableListAdapter(this, expandableListTitle, expandableListDetail);

        expandableListView.setAdapter(expandableListAdapter);
        expandableListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v,
                                        int groupPosition, int childPosition, long id) {
                Intent i = new Intent(MainActivity.this, ExpenseActivity.class);
                i.putExtra("type", groupPosition);
                i.putExtra("id", childPosition);
                startActivity(i);
                return false;
            }
        });
    }



    public void floatingMenuHandler () {
        //floating menu handling

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab1 = (FloatingActionButton) findViewById((R.id.material_design_floating_action_menu_item1));
        FloatingActionButton fab2 = (FloatingActionButton) findViewById((R.id.material_design_floating_action_menu_item2));
        FloatingActionButton fab3 = (FloatingActionButton) findViewById((R.id.material_design_floating_action_menu_item3));

        fab1.setOnClickListener(new onClickAwesomeHandler(this));
        fab2.setOnClickListener(new onClickAwesomeHandler(this));
        fab3.setOnClickListener(new onClickAwesomeHandler(this));


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

    }


}
