package com.example.farouk.mapharmconsdz;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.NotificationCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.oguzdev.circularfloatingactionmenu.library.FloatingActionMenu;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class NdAccueil extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private static final int REQUEST_CODE_PERMISSION = 2;
    Toolbar toolbar;
    private SQLiteHandler db;
    private SessionManager session;
    String currentLocale;
    private String type, fullname, pseudo;
    static FloatingActionButton fab = null;
    Exception exceptionToBeThrown;
    String url = "http://192.168.137.1/notificationSetHistory.php";

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Is Google play services Installed on the device
        if (isAvailable()) {
            // language Setting
            setLocaleFromPreferences();
            String language = getSelectedLocale();
            currentLocale = language;

            if (ActivityCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                        android.Manifest.permission.ACCESS_COARSE_LOCATION) && ActivityCompat.shouldShowRequestPermissionRationale(this,
                        android.Manifest.permission.ACCESS_FINE_LOCATION)) {
                    // Show an expanation to the user *asynchronously* -- don't block
                    // this thread waiting for the user's response! After the user
                    // sees the explanation, try again to request the permission.

                } else {

                    // No explanation needed, we can request the permission.
                    ActivityCompat.requestPermissions(this,
                            new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION, android.Manifest.permission.ACCESS_FINE_LOCATION},
                            REQUEST_CODE_PERMISSION);

                    // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                    // app-defined int constant. The callback method gets the
                    // result of the request.
                }


            }
            //Getting details from SQLite
            db = new SQLiteHandler(getApplicationContext());
            type = db.getUserDetails().get("type");
            fullname = db.getUserDetails().get("name");
            pseudo = db.getUserDetails().get("email");
            Log.i("Test", "NdAccueil ====== details from SQLite  : type = " + type + " fullname = " + fullname + " pseudo = " + pseudo);
            ///
            //db = new SQLiteHandler(getApplicationContext());
            ///
            setContentView(R.layout.activity_nd_accueil);
            toolbar = (Toolbar) findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);
            //Create TappedCoordinatorLayout
            setupToolbar();
            setupViewPager();
            setupCollapsingToolbar();
            //create floating and subFloatingActionButton ....... ;)
            fab = (FloatingActionButton) findViewById(R.id.fab);
            ImageView itemSort1 = new ImageView(NdAccueil.this);
            itemSort1.setImageResource(R.drawable.ratingpng);
            ImageView itemSort2 = new ImageView(NdAccueil.this);
            itemSort2.setImageResource(R.drawable.share);
            SubActionButton.Builder itemBuilder = new SubActionButton.Builder(NdAccueil.this);
            final SubActionButton button1 = itemBuilder.setContentView(itemSort1).build();
            final SubActionButton button2 = itemBuilder.setContentView(itemSort2).build();
            //OnClick
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    FloatingActionMenu actionMenu = new FloatingActionMenu.Builder(NdAccueil.this)
                            .addSubActionView(button1)
                            .addSubActionView(button2)
                            .attachTo(fab)
                            .build();
                }
            });

            DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
            ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                    this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
            drawer.setDrawerListener(toggle);
            toggle.syncState();
            NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
            navigationView.setNavigationItemSelectedListener(this);

        }


        // SqLite database handler
        Toast.makeText(getApplicationContext(), db.getUserDetails().get("uid"), Toast.LENGTH_SHORT).show();

        // session manager
        session = new SessionManager(getApplicationContext());

        if (!session.isLoggedIn()) {
            logoutUser();
        }

        for (int i = 0; i < StartUpActivity.lesNotificationNonLus.size(); i++) {
            Intent intent1 = new Intent(getApplicationContext(), NdNotification.class);
            TaskStackBuilder stackBuilder = TaskStackBuilder.create(getApplicationContext());
            stackBuilder.addParentStack(NdNotification.class);
            stackBuilder.addNextIntent(intent1);

            PendingIntent pendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

            NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext());

            Notification notification2 = builder.setContentTitle("Accepted Demand !!")
                    .setContentText("the Pharmacy " + StartUpActivity.lesNotificationNonLus.get(i).getPharmacie().getNomPharmacie() + " Accepted your demand")
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentIntent(pendingIntent)
                    .build();

            NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.notify(i, notification2);
            notification2.defaults |= Notification.DEFAULT_SOUND;
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
        getMenuInflater().inflate(R.menu.nd_accueil, menu);
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
            Intent i = new Intent(this, MyPreferenceActivity.class);
            startActivity(i);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {
            new BackgroundTaskToSetHistory().execute(url);
            Intent i = new Intent(this, NdNotification.class);
            startActivity(i);
        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {
            Intent i = new Intent(this, MyPreferenceActivity.class);
            startActivity(i);
        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        } else if (id == R.id.nav_disconnect) {
            logoutUser();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


    //TappedCoordinatorLayout Stuffs
    private void setupCollapsingToolbar() {
        final CollapsingToolbarLayout collapsingToolbar = (CollapsingToolbarLayout) findViewById(
                R.id.collapse_toolbar);

        collapsingToolbar.setTitleEnabled(false);
    }

    private void setupViewPager() {
        final ViewPager viewPager = (ViewPager) findViewById(R.id.viewpager);
        viewPager.setOffscreenPageLimit(2);
        setupViewPager(viewPager);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);
    }

    private void setupToolbar() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Ma Pharmacie 25");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFrag(new SmPharmacieProcheList(), "Pharmacie Proche");
        adapter.addFrag(new SmRechercheMed(), "RechercheMed");
        if (type.equals("SimpleUser")) {
            adapter.addFrag(new SmDemandeMed(), "Demande");
        }
        if (type.equals("pharmasist")) {
            adapter.addFrag(new SmEspaceMaPharmacie(), "MonEspace");
        }

        viewPager.setAdapter(adapter);
    }

    private String getSelectedLocale() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        return preferences.getString(getString(R.string.app_language_pref), getString(R.string.language_default_value));
    }


    /* private void setSharedPreference(String key,String value)
    {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        preferences.edit().putString(key, value);
        preferences.edit().apply();
    }*/
    private String setLocaleFromPreferences() {
        String language = getSelectedLocale();

        Resources res = this.getResources();
        // Change locale settings in the app.
        DisplayMetrics dm = res.getDisplayMetrics();
        android.content.res.Configuration conf = res.getConfiguration();
        conf.locale = new Locale(language);
        res.updateConfiguration(conf, dm);
        return language;
    }

    static class ViewPagerAdapter extends FragmentPagerAdapter {

        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFrag(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }
    }

    public boolean isAvailable() {
        GoogleApiAvailability api = GoogleApiAvailability.getInstance();
        int isAvailable = api.isGooglePlayServicesAvailable(this);
        if (isAvailable == ConnectionResult.SUCCESS)
            return true;
        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_PERMISSION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        String language = getSelectedLocale();
        if (!language.equals(currentLocale))
            recreate();
    }

    private void logoutUser() {
        session.setLogin(false);

        db.deleteUsers();

        // Launching the login activity
        Intent intent = new Intent(NdAccueil.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    public class BackgroundTaskToSetHistory extends AsyncTask<String, Void, String> {

            protected String doInBackground(String... params) {
                HttpURLConnection httpURLConnection = null;
                int tmp;
                String data = "";
                try {
                    URL url = new URL(params[0]);
                    String s = "id_user="+StartUpActivity.UserId;
                    httpURLConnection = (HttpURLConnection) url.openConnection();
                    httpURLConnection.setDoInput(true);
                    httpURLConnection.setRequestMethod("POST");
                    OutputStream os =httpURLConnection.getOutputStream();
                    os.write(s.getBytes());
                    os.flush();
                    os.close();
                    InputStream is = httpURLConnection.getInputStream();
                    while ( (tmp = is.read() )== -1)
                        data +=(char) tmp;
                    httpURLConnection.connect();
                    int mStatusCode =  httpURLConnection.getResponseCode();
                    switch (mStatusCode) {
                        case 200:
                            BufferedReader br = new BufferedReader(new InputStreamReader( httpURLConnection.getInputStream(),"utf-8"),8);
                            StringBuilder sb = new StringBuilder();
                            String line;
                            while ((line = br.readLine()) != null) {
                                sb.append(line).append("\n");
                            }
                            br.close();
                            return sb.toString();
                    }
                    return "";

                } catch (Exception ex) {
                    // Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
                    exceptionToBeThrown = ex;
                } finally {
                    if ( httpURLConnection != null) {
                        try {
                            httpURLConnection.disconnect();
                        } catch (Exception ex) {
                            //Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
                            exceptionToBeThrown = ex;
                        }
                    }

                }
                return null;
            }

            @TargetApi(Build.VERSION_CODES.M)
            @Override
            protected void onPostExecute(String s) {
                if (exceptionToBeThrown != null) {
                    if (NdNotification.context != null)
                        Toast.makeText(getApplicationContext(), "error", Toast.LENGTH_SHORT).show();
                    exceptionToBeThrown = null;
                }
            }

        }

}
