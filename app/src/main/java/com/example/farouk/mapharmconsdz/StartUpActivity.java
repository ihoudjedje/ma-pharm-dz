package com.example.farouk.mapharmconsdz;

import android.annotation.TargetApi;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import Classes.Medicament;
import Classes.Pharmacie;


public class StartUpActivity extends AppCompatActivity {
    public  static  Exception exceptionToBeThrown;
    SQLiteHandler db ;
    public static  String UserId;
    static  String url ="http://192.168.137.1/notification.php";
   public static ArrayList<Classes.Notification> lesNotification = new ArrayList<>();
   public static ArrayList<Classes.Notification> lesNotificationNonLus = new ArrayList<>();
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = new SQLiteHandler(this);
        UserId = db.getUserDetails().get("uid");
        setContentView(R.layout.activity_start_up);
        new BackgroundTask ().execute(url);
        final Handler handler = new Handler();
        final Intent intent = new Intent(this, LoginActivity.class);
        final Runnable doNextActivity = new Runnable() {
            @Override
            public void run() {
                // Intent to jump to the next activity
                startActivity(intent);
                finish(); // so the splash activity goes away
            }
        };

        new Thread() {
            @Override
            public void run() {
                SystemClock.sleep(2500);
                handler.post(doNextActivity);
            }
        }.start();
    }
    public class BackgroundTask extends AsyncTask<String, Void, String> {
        protected String doInBackground(String... params) {
            HttpURLConnection httpURLConnection = null;
            int tmp;
            String data = "";
            try {
                URL url = new URL(params[0]);
                String s = "id_user="+UserId;
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

                exceptionToBeThrown = null;
            }else{
                if (NdNotification.adapter != null)
                    NdNotification.adapter.clear();
                try {
                    JSONArray listeJson = new JSONArray(s);
                    for (int i = 0; i < listeJson.length(); i++) {
                        JSONObject obj = listeJson.getJSONObject(i);
                        Classes.Notification notification = new Classes.Notification();
                        Pharmacie pharmacie = new Pharmacie();
                        pharmacie.lat = obj.getString("latitude");
                        pharmacie.lang = obj.getString("longitude");
                        pharmacie.setNomPharmacie(obj.getString("nom_phar"));
                        pharmacie.setAddressPharmacie(obj.getString("adresse"));
//                        pharmacie.setActivation(obj.getInt("activation"));
                        pharmacie.setVillePharmacie(obj.getString("ville"));
                        pharmacie.setCodePostalPharmacie(obj.getString("codepostal"));
                        pharmacie.setHeureOuverture(obj.getString("horaire_ouverture"));
                        pharmacie.setHeureFermeture(obj.getString("horaire_fermeture"));
                        pharmacie.setTelephonPharmacie(obj.getString("telephone"));
                        Medicament  medicament = new Medicament();
                        medicament.setNomMed(obj.getString("nom_med"));
                        //notification.setHistory(obj.getInt("history"));
                        notification.setDate(obj.getString("date_dem"));
                        notification.setMedicament(medicament);
                        notification.setPharmacie(pharmacie);
                        notification.setHistory(obj.getInt("history"));
                        lesNotification.add(notification);
                        if (notification.getHistory()==0)
                            lesNotificationNonLus.add(notification);

                    }


                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

            }
        }

    }

}