package com.example.farouk.mapharmconsdz;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.SphericalUtil;

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

import Classes.Demande;
import Classes.Medicament;
import Classes.Utilsateur;

public class ConsultationDemande extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener {
    RecyclerView listConsult;
    SwipeRefreshLayout swiperConsult;
    SwipeRefreshLayout swiperConsultEmpty;
    String url ="http://192.168.43.168/android_login_maphdz/demande/Consultation.php";
    String urlP ="http://192.168.43.168/android_login_maphdz/demande/positionPharmacy.php";
    Exception exceptionToBeThrown;
    ProgressBar barPrg;
    AdpConsultationDemande adapter;
    ArrayList<Demande> listDesDemandes= new ArrayList();
    String id_pharm;
    LatLng position_pharmacy;
    SQLiteHandler db;
    TextView pasDeDemande ;
    public static Context context;
    ImageView AucuneDemande;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getApplicationContext();
        setContentView(R.layout.activity_consultation_demande);
        db = new SQLiteHandler(getApplicationContext());
        id_pharm = db.getUserDetails().get("id_phar");

        pasDeDemande = (TextView)findViewById(R.id.pasDeDemande);
        AucuneDemande = (ImageView) findViewById(R.id.AcuneDemande);
        //ListView;
        listConsult  = (RecyclerView) findViewById(R.id.listConsultationDemande);
        RecyclerView.LayoutManager mLayoutManager;
        mLayoutManager = new LinearLayoutManager(getApplicationContext());
        listConsult.setLayoutManager(mLayoutManager);
        ///// Swipers
        swiperConsult = (SwipeRefreshLayout) findViewById(R.id.swiperConsultationDemande);
        swiperConsult.setOnRefreshListener(this);
        swiperConsult.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);

        ///// Swiper
        swiperConsultEmpty= (SwipeRefreshLayout) findViewById(R.id.swiperConsultationDemande_empty);
        swiperConsultEmpty.setOnRefreshListener(this);
        swiperConsultEmpty.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);
        swiperConsultEmpty.setVisibility(View.GONE);


        // progress bar
        barPrg = (ProgressBar)findViewById(R.id.listCosultPrgBar);
        barPrg.setVisibility(View.GONE);
        //Async
        new BackgroundTaskPharmPosition().execute(urlP);

    }

    @Override
    public void onRefresh() {
        refreshing(url);

    }
    public void refreshing(String url) {
        barPrg.setVisibility(View.VISIBLE);
        new BackgroundTask().execute(url);
        swiperConsultEmpty.setRefreshing(false);
        swiperConsult.setRefreshing(false);
        if (!SmPharmacieProcheList.isEmpty(listConsult))
            swiperConsultEmpty.setVisibility(View.GONE);
    }


    public class BackgroundTask extends AsyncTask<String, Void, String> {


        protected String doInBackground(String... params) {
            HttpURLConnection c = null;
            try {
                String urlString = params[0];
                URL url = new URL(urlString);
                c = (HttpURLConnection) url.openConnection();
                c.setRequestMethod("GET");
                c.setConnectTimeout(15000);
                c.setDoInput(true);
                c.connect();
                int mStatusCode = c.getResponseCode();
                switch (mStatusCode) {
                    case 200:
                        BufferedReader br = new BufferedReader(new InputStreamReader(c.getInputStream()));
                        StringBuilder sb = new StringBuilder();
                        String line;
                        while ((line = br.readLine()) != null) {
                            sb.append(line).append("\n");
                        }
                        br.close();
                        String result = sb.toString();
                        return result;
                    default:
                        return "error";
                }

            } catch (Exception ex) {
                // Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
                exceptionToBeThrown = ex;
            } finally {
                if (c != null) {
                    try {
                        c.disconnect();
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
                barPrg.setVisibility(View.GONE);
                Snackbar snackbar = Snackbar
                        .make(getCurrentFocus(), "no connexion", Snackbar.LENGTH_LONG)
                        .setAction("Refresh", new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                new BackgroundTask().execute(url);
                            }
                        });

                snackbar.show();
                swiperConsultEmpty.setVisibility(View.VISIBLE);
                exceptionToBeThrown = null;

            }else{
                adapter = new AdpConsultationDemande(listDesDemandes);
                adapter.clear();
                try {
                    JSONArray listeJson = new JSONArray(s);
                    for (int i = 0; i < listeJson.length(); i++) {
                        JSONObject obj = listeJson.getJSONObject(i);
                        Demande demande = new Demande();
                        double lat = Double.parseDouble(obj.getString("latitude"));
                        double lang = Double.parseDouble(obj.getString("longitude"));
                        LatLng position = new LatLng(lat,lang);
                        Double distance = SphericalUtil.computeDistanceBetween(position, position_pharmacy);
                            if (distance <20000) {
                                Medicament med = new Medicament();
                                Utilsateur user = new Utilsateur();
                                user.setFullName(obj.getString("id_util"));
                                med.setNomMed(obj.getString("nom_med"));
                                demande.setMed(med);
                                demande.setDate(obj.getString("date_demande"));
                                demande.setUser(user);
                                listDesDemandes.add(demande);
                            }

                    }

                    listConsult.setAdapter(adapter);
                    adapter.notifyDataSetChanged();
                    swiperConsultEmpty.setVisibility(View.GONE);
                    barPrg.setVisibility(View.GONE);
                    if (listDesDemandes.size()==0){
                        AucuneDemande.setImageResource(R.drawable.list);
                        pasDeDemande.setVisibility(View.VISIBLE);
                        pasDeDemande.setText("aucune demande n'a été trouvé !");
                        swiperConsultEmpty.setVisibility(View.VISIBLE);
                        barPrg.setVisibility(View.GONE);
                    }

                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

            }
        }

    }
    public class BackgroundTaskPharmPosition extends AsyncTask<String, Void, String> {


        protected String doInBackground(String... params) {
            HttpURLConnection httpURLConnection = null;
            int tmp;
            String data = "";
            try {
                URL url = new URL(params[0]);
                String s = "id_pharm="+id_pharm;
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
                barPrg.setVisibility(View.GONE);
                Snackbar snackbar = Snackbar
                        .make(getCurrentFocus(), "no connexion", Snackbar.LENGTH_LONG)
                        .setAction("Refresh", new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                new BackgroundTaskPharmPosition().execute(urlP);
                            }
                        });

                snackbar.show();
                swiperConsultEmpty.setVisibility(View.VISIBLE);
                exceptionToBeThrown = null;
            }else{
                try {
                    JSONArray listeJson = new JSONArray(s);
                    for (int i = 0; i < listeJson.length(); i++) {
                        JSONObject obj = listeJson.getJSONObject(i);
                        position_pharmacy = new LatLng(Double.parseDouble(obj.getString("latitude")),Double.parseDouble(obj.getString("longitude")));
                    }
                    new BackgroundTask().execute(url);


                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

            }
        }

    }


}



