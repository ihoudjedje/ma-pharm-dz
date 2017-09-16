package com.example.farouk.mapharmconsdz;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;

import com.android.volley.Request.Method;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;


public class RegisterActivity extends Activity {
    private static final String TAG = RegisterActivity.class.getSimpleName();
    private Button btnRegister;
    private Button btnLinkToLoginScreen;
    private EditText fullname_reg;
    private EditText pseudo_reg;
    private EditText password_reg;
    private ProgressDialog pDialog;
    private SessionManager session;
    private SQLiteHandler db;
    private boolean radioButtonClient = false;
    private boolean radioButtonPharmacien = false;
    public static Context context;
    public static LatLng Position ;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        context = getApplicationContext();
        fullname_reg = (EditText) findViewById(R.id.fullname_reg);
        pseudo_reg = (EditText) findViewById(R.id.pseudo_reg);
        password_reg = (EditText) findViewById(R.id.password_reg);
        btnRegister = (Button) findViewById(R.id.btnRegister);
        btnLinkToLoginScreen = (Button) findViewById(R.id.btnLinkToLoginScreen);


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
            Intent intent = new Intent(RegisterActivity.this,
                    NdAccueil.class);
            startActivity(intent);
            finish();
        }

        // Register Button Click event
        btnRegister.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                String fullname = fullname_reg.getText().toString().trim();
                String pseudo = pseudo_reg.getText().toString().trim();
                String password = password_reg.getText().toString().trim();

                if (!fullname.isEmpty() && !pseudo.isEmpty() && !password.isEmpty()) {
                    if (radioButtonClient && !radioButtonPharmacien) {
                        registerUser(fullname,pseudo,password);
                    }else {
                        if (!radioButtonClient && radioButtonPharmacien) {
                            Intent i=new Intent(RegisterActivity.this, RegisterActivityPlusInfo.class);

                            i.putExtra("fullname", fullname);
                            i.putExtra("pseudo", pseudo);
                            i.putExtra("password", password);
                            startActivity(i);
                            finish();
                        }else{
                            Toast.makeText(getApplicationContext(),
                                    "Entrer votre type de compte SVP!", Toast.LENGTH_LONG)
                                    .show();
                        }
                    }
                }
                    else {
                        Toast.makeText(getApplicationContext(),
                                "Entrer vos détails SVP!", Toast.LENGTH_LONG)
                                .show();
                    }
                }

        });


        // Link to Login Screen
        btnLinkToLoginScreen.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                Intent i = new Intent(getApplicationContext(),
                        LoginActivity.class);
                startActivity(i);
                finish();
            }
        });

        //radioButtons clicked
        RadioButton rbph = (RadioButton) findViewById(R.id.radioButtonPharmacien);
        RadioButton rbc = (RadioButton) findViewById(R.id.radioButtonClient);

        rbph.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnRegister.setText("Plus d'information..");
                radioButtonPharmacien = true;
                radioButtonClient = false;
            }
        });

        rbc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnRegister.setText("ENREGISTRER");
                radioButtonPharmacien = false;
                radioButtonClient = true;
            }
        });

        }
    /**
     * Function to store user in MySQL database will post params(tag, name,
     * pseudo, password) to register url
     * */
    public void registerUser(final String fullname, final String pseudo,
                              final String password) {
        // Tag used to cancel the request
        String tag_string_req = "req_register";

        pDialog.setMessage("Registering ...");
        showDialog();

        StringRequest strReq = new StringRequest(Method.POST,
                AppConfig.URL_REGISTER, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Log.i("Test", "Register Response: " + response.toString());
                hideDialog();

                try {
                    JSONObject jObj = new JSONObject(response);
                    boolean error = jObj.getBoolean("error");
                    if (!error) {
                        // User successfully stored in MySQL
                        // Now store the user in sqlite
                        String uid = jObj.getString("uid");
                        String type = jObj.getString("SimpleUser_util");
                        JSONObject user = jObj.getJSONObject("user");
                        String name = user.getString("name");
                        String pseudo = user.getString("pseudo");
                        String created_at = user
                                .getString("created_at");

                        // Inserting row in users table
                        db.addUser(name, pseudo, uid, created_at, type);

                        Toast.makeText(getApplicationContext(), "User successfully registered. Try login now!", Toast.LENGTH_LONG).show();
             //           LoginActivity.isSimpleUser = true;
                        // Launch login activity
                        Intent intent = new Intent(
                                RegisterActivity.this,
                                LoginActivity.class);
                        startActivity(intent);
                        finish();
                    } else {

                        // Error occurred in registration. Get the error
                        // message
                        String errorMsg = jObj.getString("error_msg");
                        Toast.makeText(getApplicationContext(), errorMsg, Toast.LENGTH_LONG).show();
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

}