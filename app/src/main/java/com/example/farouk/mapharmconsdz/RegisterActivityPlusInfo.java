package com.example.farouk.mapharmconsdz;

import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.Rect;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Vibrator;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.util.Base64;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class RegisterActivityPlusInfo extends FragmentActivity implements View.OnClickListener,
        OnMapReadyCallback,
        GoogleMap.OnMarkerDragListener,
        GoogleMap.OnMapLongClickListener
{

    private static final String TAG = RegisterActivityPlusInfo.class.getSimpleName();
    private String fullname;
    private String pseudo;
    private String password;
    private EditText nom_pharm;
    private EditText addr_pharm;
    private EditText tel_pharm;
    private TextView editTextOuverture;
    private TextView editTextFermeture;
    private Button btn_register,btn_link_to_register;
    private ProgressDialog pDialog;
    private SessionManager session;
    private SQLiteHandler db;
    private int mHour, mMinute;
    private String heure_ouvre, heure_ferme;
    private int PICK_IMAGE_REQUEST = 1;
    GoogleMap mMap;
    double latitude, longitude;
    LatLng myPosition;
    public static Context C;
    private Button buttonUploadAttestation;
    private String stringImageAttestation,stringImageUser;
    private ImageView user_image;
    private boolean userImageClicked;
    private boolean btnAttestationClicked;


    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;
    private Location mLastLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_activity_plus_info);
        C = getApplicationContext();
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        MapFragment mapFragment = (MapFragment) getFragmentManager()
                .findFragmentById(R.id.localisation_fragment_map_pharmacien);
        mapFragment.getMapAsync(this);
        nom_pharm = (EditText) findViewById(R.id.nom_pharm_reg_plus);
        addr_pharm = (EditText) findViewById(R.id.adresse_pharm_reg_plus);
        tel_pharm = (EditText) findViewById(R.id.num_tel_pharm_reg_plus);
        editTextOuverture = (TextView) findViewById(R.id.editTextOuverture);
        editTextFermeture = (TextView) findViewById(R.id.editTextFermeture);
        btn_register = (Button) findViewById(R.id.btnRegister);
        btn_link_to_register = (Button) findViewById(R.id.btnLinkToRegisterScreen);
        buttonUploadAttestation = (Button) findViewById(R.id.buttonUploadAttestation);
        user_image = (ImageView) findViewById(R.id.imageViewUserLogo);

        editTextOuverture.setOnClickListener(this);
        editTextFermeture.setOnClickListener(this);

        // Progress dialog
        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);

        // Session manager
        session = new SessionManager(getApplicationContext());

        // SQLite database handler
        db = new SQLiteHandler(getApplicationContext());

        // Check if user is already logged in or not
        if (session.isLoggedIn()) {
            // User is already logged in. Take him to main activity
            Intent intent = new Intent(RegisterActivityPlusInfo.this,
                    NdAccueil.class);
            startActivity(intent);
            finish();
        }

        // Register Button Click event
        btn_register.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                String nom_pharmacie = nom_pharm.getText().toString().trim();
                String addr_pharmacie = addr_pharm.getText().toString().trim();
                String tel_pharmacie = tel_pharm.getText().toString().trim();
                // get data from previous activity
                Intent ii = getIntent();
                Bundle b = ii.getExtras();

                if (b != null) {
                    fullname = (String) b.get("fullname");
                    pseudo = (String) b.get("pseudo");
                    password = (String) b.get("password");
                }

                if (!nom_pharmacie.isEmpty() && !addr_pharmacie.isEmpty() && !tel_pharmacie.isEmpty()) {
                    // my function
                    registerUser(fullname, pseudo, password, nom_pharmacie, addr_pharmacie, tel_pharmacie, heure_ouvre, heure_ferme);//,stringImageAttestation, stringImageUser);


                } else {
                    Toast.makeText(getApplicationContext(),
                            "Entrer vos détails SVP!", Toast.LENGTH_LONG)
                            .show();
                }
            }

        });

        btn_link_to_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(RegisterActivityPlusInfo.this,RegisterActivity.class);
                startActivity(i);
            }
        });

        // disable scrollView vertical swipe when touches the Map
        final ScrollView mainScrollView = (ScrollView) findViewById(R.id.scrollView);
        ImageView transparentImageView = (ImageView) findViewById(R.id.transparent_image);

        transparentImageView.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int action = event.getAction();
                switch (action) {
                    case MotionEvent.ACTION_DOWN:
                        // Disallow ScrollView to intercept touch events.
                        mainScrollView.requestDisallowInterceptTouchEvent(true);
                        // Disable touch on transparent view
                        return false;

                    case MotionEvent.ACTION_UP:
                        // Allow ScrollView to intercept touch events.
                        mainScrollView.requestDisallowInterceptTouchEvent(false);
                        return true;

                    case MotionEvent.ACTION_MOVE:
                        mainScrollView.requestDisallowInterceptTouchEvent(true);
                        return false;

                    default:
                        return true;
                }
            }
        });

        buttonUploadAttestation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Load Image
                btnAttestationClicked = true;
                userImageClicked = false;
                Intent intent = new Intent();
                // Show only images, no videos or anything else
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);

                // Always show the chooser (if there are multiple options available)
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
            }
        });

        user_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Load Image
                userImageClicked = true;
                btnAttestationClicked = false;
                Intent intent = new Intent();
                // Show only images, no videos or anything else
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);

                // Always show the chooser (if there are multiple options available)
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
            }
        });
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }


    /**
     * Function to store user in MySQL database will post params(tag, name,
     * pseudo, password) to register url
     */
    public void registerUser(final String fullname, final String pseudo, final String password, final String nom_ph, final String addr_ph, final String tel_ph, final String heure_ouvre, final String heure_ferme ){//,final String stringImageAttestation, final String stringImageUser){
        // Tag used to cancel the request
        String tag_string_req = "req_register";

        pDialog.setMessage("Registering ...");
        showDialog();

        StringRequest strReq = new StringRequest(Request.Method.POST,
                AppConfig.URL_REGISTER_PLUS_INFO, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                hideDialog();

                try {
                    JSONObject jObj = new JSONObject(response);
                    boolean error = jObj.getBoolean("error");
                    if (!error) {
                        // User successfully stored in MySQL
                        // Now store the user in sqlite
                        String uid = jObj.getString("uid");
                        String type = jObj.getString("pharmasist_phar");
                        Log.i("Test","type =  : "+type);
                        String id_phar = jObj.getString("id_phar");
                        JSONObject user = jObj.getJSONObject("user");
                        String name = user.getString("name");
                        String pseudo = user.getString("pseudo");
                        String created_at = user
                                .getString("created_at");

                        // Inserting row in users table
                        db.addUser2(name, pseudo, uid, created_at,id_phar, type);
              //          LoginActivity.isSimpleUser = false;
                        Toast.makeText(getApplicationContext(), "Utilisateur enregistré avec succès, Essayez de connecter maintenant!", Toast.LENGTH_LONG).show();

                        Log.i("Test","here is the id_phar  : "+id_phar);

                        //        SharedPreferences sp = getSharedPreferences("namapassword" , C.MODE_PRIVATE );
                        //        SharedPreferences.Editor editor = sp.edit();
                        //       editor.clear();
                        //        editor.putString("unique_id_phar", id_phar);
                        //        editor.commit();
                        //        Log.i("Test","here is the editor  : "+editor);


                        // Launch LoginActivity
                        Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                        startActivity(intent);
                    } else {

                        // Error occurred in registration. Get the error
                        // message
                        String errorMsg = jObj.getString("error_msg");
                        Toast.makeText(getApplicationContext(),
                                errorMsg, Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.i("Test", "Registration Error: " + error.getMessage());
                Toast.makeText(getApplicationContext(),
                        error.getMessage(), Toast.LENGTH_LONG).show();
                hideDialog();
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                // Posting params to register url
                Map<String, String> params = new HashMap<String, String>();
                params.put("fullname_reg", fullname);
                params.put("pseudo_reg", pseudo);
                params.put("password_reg", password);
                params.put("nom_ph_reg", nom_ph);
                params.put("addr_ph_reg", addr_ph);
                params.put("tel_ph_reg", tel_ph);
                params.put("heure_ouvre", heure_ouvre);
                params.put("heure_ferme", heure_ferme);
                //     params.put("stringImageAttestation", stringImageAttestation);
                //   params.put("stringImageUser", stringImageUser);
                params.put("latitude_phar", latitude+"");
                params.put("longitude_phar", longitude+"");

                return params;
            }

        };

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
    }

    private void showDialog() {
        if (!pDialog.isShowing())
            pDialog.show();
    }

    private void hideDialog() {
        if (pDialog.isShowing())
            pDialog.dismiss();
    }

    @Override
    public void onClick(View v) {
        if (v == editTextOuverture) {
            // Get Current Time
            final Calendar c = Calendar.getInstance();
            mHour = c.get(Calendar.HOUR_OF_DAY);
            mMinute = c.get(Calendar.MINUTE);

            // Launch Time Picker Dialog
            TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                    new TimePickerDialog.OnTimeSetListener() {

                        @Override
                        public void onTimeSet(TimePicker view, int hourOfDay,
                                              int minute) {
                            heure_ouvre = hourOfDay + ":" + minute;
                            editTextOuverture.setText("De " + heure_ouvre);
                            editTextOuverture.setTextColor(getResources().getColor(R.color.btn_login_color));
                        }
                    }, mHour, mMinute, false);
            timePickerDialog.show();
        }
        if (v == editTextFermeture) {
            // Get Current Time
            final Calendar c = Calendar.getInstance();
            mHour = c.get(Calendar.HOUR_OF_DAY);
            mMinute = c.get(Calendar.MINUTE);

            // Launch Time Picker Dialog
            TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                    new TimePickerDialog.OnTimeSetListener() {

                        @Override
                        public void onTimeSet(TimePicker view, int hourOfDay,
                                              int minute) {
                            heure_ferme = hourOfDay + ":" + minute;
                            editTextFermeture.setText("à " + heure_ferme);
                            editTextFermeture.setTextColor(getResources().getColor(R.color.btn_login_color));
                        }
                    }, mHour, mMinute, false);
            timePickerDialog.show();
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnMapLongClickListener(this);
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mMap.setMyLocationEnabled(true);
        mMap.setIndoorEnabled(true);
        mMap.setTrafficEnabled(true);
        mMap.setContentDescription("Localiser votre pharmacie");

    }


    @Override
    public void onMapLongClick(LatLng latLng) {
        mMap.clear();
        mMap.addMarker(new MarkerOptions().position(latLng).draggable(true));
        latitude = latLng.latitude;
        longitude = latLng.longitude;
        Vibrator vibrator;
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        vibrator.vibrate(300);
        if (latitude != 0 && longitude != 0)
            Toast.makeText(RegisterActivityPlusInfo.this, "pharmacie localisé ", Toast.LENGTH_SHORT).show();
    }


    @Override
    public void onMarkerDragStart(Marker marker) {

    }

    @Override
    public void onMarkerDrag(Marker marker) {

    }

    @Override
    public void onMarkerDragEnd(Marker marker) {
        latitude = marker.getPosition().latitude;
        longitude = marker.getPosition().longitude;

    }


    // Image Upload Attestation
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {

            Uri uri = data.getData();

            try {
//                        Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                // Log.d(TAG, String.valueOf(bitmap));

//                        ImageView imageView = (ImageView) findViewById(R.id.imageViewAttestation);
//                        imageView.setImageBitmap(bitmap);
                if (btnAttestationClicked && !userImageClicked) {

                    buttonUploadAttestation.setText("Attestation sélectionné!");
                    buttonUploadAttestation.setTextColor(getResources().getColor(R.color.btn_upload_attestation_success));

                    InputStream imageStream = getContentResolver().openInputStream(uri);
                    ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();

                    int bufferSize = 1024;
                    byte[] buffer = new byte[bufferSize];
                    int len = 0;
                    while ((len = imageStream.read(buffer)) != -1) {
                        byteBuffer.write(buffer, 0, len);
                    }

                    byte[] imageArray = byteBuffer.toByteArray();

                    stringImageAttestation = Base64.encodeToString(imageArray, Base64.DEFAULT);
                    Log.i("ImageAttestation", stringImageAttestation);
                }
                if (userImageClicked && !btnAttestationClicked) {

                    InputStream imageStream = getContentResolver().openInputStream(uri);
                    ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();

                    int bufferSize = 1024;
                    byte[] buffer = new byte[bufferSize];
                    int len = 0;
                    while ((len = imageStream.read(buffer)) != -1) {
                        byteBuffer.write(buffer, 0, len);
                    }

                    byte[] imageArray = byteBuffer.toByteArray();

                    stringImageUser = Base64.encodeToString(imageArray, Base64.DEFAULT);

                    Bitmap bitmap = getRoundedShape(MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri));
                    user_image.setImageBitmap(bitmap);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }


    }
    // Circle shape Image User

    public Bitmap getRoundedShape(Bitmap scaleBitmapImage) {
        int targetWidth = 50;
        int targetHeight = 50;
        Bitmap targetBitmap = Bitmap.createBitmap(targetWidth,
                targetHeight, Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(targetBitmap);
        Path path = new Path();
        path.addCircle(((float) targetWidth - 1) / 2,
                ((float) targetHeight - 1) / 2,
                (Math.min(((float) targetWidth),
                        ((float) targetHeight)) / 2),
                Path.Direction.CCW);

        canvas.clipPath(path);
        Bitmap sourceBitmap = scaleBitmapImage;
        canvas.drawBitmap(sourceBitmap,
                new Rect(0, 0, sourceBitmap.getWidth(),
                        sourceBitmap.getHeight()),
                new Rect(0, 0, targetWidth, targetHeight), null);
        return targetBitmap;
    }


    @Override
    public void onStart() {
        super.onStart();
        client.connect();
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "RegisterActivityPlusInfo Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app URL is correct.
                Uri.parse("android-app://com.example.farouk.mapharmconsdz/http/host/path")
        );
        AppIndex.AppIndexApi.start(client, viewAction);
    }

    @Override
    public void onStop() {
        super.onStop();
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "RegisterActivityPlusInfo Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app URL is correct.
                Uri.parse("android-app://com.example.farouk.mapharmconsdz/http/host/path")
        );
        AppIndex.AppIndexApi.end(client, viewAction);
        client.disconnect();
    }


}