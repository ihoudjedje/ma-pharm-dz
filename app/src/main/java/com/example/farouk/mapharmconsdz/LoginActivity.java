package com.example.farouk.mapharmconsdz;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Request.Method;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;


public class LoginActivity extends Activity {
    private static final String TAG = RegisterActivity.class.getSimpleName();
    private Button btnSignIn;
    private Button textViewRegisterLink;
    private EditText pseudo_login;
    private EditText password_login;
    private ProgressDialog pDialog;
    private SessionManager session;
    private SQLiteHandler db;
  //  public static boolean isSimpleUser = true;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        pseudo_login = (EditText) findViewById(R.id.pseudo_login);
        password_login = (EditText) findViewById(R.id.password_login);
        btnSignIn = (Button) findViewById(R.id.btnSignIn);
        textViewRegisterLink = (Button) findViewById(R.id.textViewRegisterLink);

        // Progress dialog
        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);

        // SQLite database handler
        db = new SQLiteHandler(getApplicationContext());

        // Session manager
        session = new SessionManager(getApplicationContext());

        // Check if user is already logged in or not
        if (session.isLoggedIn()) {
            // User is already logged in. Take him to main activity
            Intent intent = new Intent(LoginActivity.this, NdAccueil.class);
            startActivity(intent);
            finish();
        }

        // Login button Click Event
        btnSignIn.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                String pseudo = pseudo_login.getText().toString().trim();
                String password = password_login.getText().toString().trim();

                // Check for empty data in the form
                if (!pseudo.isEmpty() && !password.isEmpty()) {
                    // login user
                    checkLogin(pseudo, password);
                } else {
                    // Prompt user to enter credentials
                    Toast.makeText(getApplicationContext(),
                            "Please enter the credentials!", Toast.LENGTH_LONG)
                            .show();
                }
            }

        });

        // Link to Register Screen
        textViewRegisterLink.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                Intent i = new Intent(getApplicationContext(),
                        RegisterActivity.class);
                startActivity(i);
                finish();
            }
        });

    }

    /**
     * function to verify login details in mysql db
     * */
    private void checkLogin(final String pseudo, final String password) {
        // Tag used to cancel the request
        String tag_string_req = "req_login";

        pDialog.setMessage("Logging in ...");
        showDialog();


        StringRequest strReq = new StringRequest(Method.POST,
                AppConfig.URL_LOGIN, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Log.i("Test", "Login Response: " + response.toString());
                hideDialog();

                try {
                    JSONObject jObj = new JSONObject(response);
                    boolean error = jObj.getBoolean("error");

                    // Check for error node in json
                    if (!error) {
                        // user successfully logged in
                        // Create login session
                        session.setLogin(true);

                        // Now store the user in SQLite
                        String uid = jObj.getString("uid");
                        JSONObject user = jObj.getJSONObject("user");
                        String type = jObj.getString("type_user");
                        String name = user.getString("name");
                        String pseudo = user.getString("pseudo");
                        String created_at = user
                                .getString("created_at");
             //           Log.i("Test", "isSimpleUser = " + isSimpleUser);

                        // Inserting row in users table
                 //       if(isSimpleUser){
                  //      db.addUser(name, pseudo, uid, created_at, type);
                 //       }
                 //       if(!isSimpleUser){
                            String id_phar = jObj.getString("id_phar_final");
                            db.addUser2(name, pseudo, uid, created_at, id_phar, type);
                            Log.i("Test", "ID phar from Json = " + id_phar);

                 //       }


                        // Launch main activity
                        Intent intent = new Intent(LoginActivity.this,
                                NdAccueil.class);
                        startActivity(intent);
                        finish();
                    } else {
                        // Error in login. Get the error message
                        String errorMsg = jObj.getString("error_msg");
                        Toast.makeText(getApplicationContext(),
                                errorMsg, Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    // JSON error
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(), "Json error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }

            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.i("Test", "Login Error: " + error.getMessage());
                Toast.makeText(getApplicationContext(),
                        error.getMessage(), Toast.LENGTH_LONG).show();
                hideDialog();
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                // Posting parameters to login url
                Map<String, String> params = new HashMap<String, String>();
                params.put("pseudo_login", pseudo);
                params.put("password_login", password);

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