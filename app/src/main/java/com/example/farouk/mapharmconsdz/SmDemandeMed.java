package com.example.farouk.mapharmconsdz;


import android.annotation.TargetApi;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by farouk on 07-05-2016.
 */
public class SmDemandeMed extends Fragment {
    Button send;
    Exception exceptionToBeThrown;
    AutoCompleteTextView medDemendee;
    String nomMed;
    Double lang;
    Double lat;
    String idUtil;
    SQLiteHandler db;
    String Url= "http://192.168.137.1/Demande.php";
    ArrayAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.demand_med, container, false);
        db = new SQLiteHandler(getContext());
        send = (Button) v.findViewById(R.id.BtnEnvoyeeDemend);
        medDemendee = (AutoCompleteTextView) v.findViewById(R.id.medDemendee);
        medDemendee.setThreshold(1);
            adapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_list_item_1, SmRechercheMed.listDesMedicament);
            medDemendee.setAdapter(adapter);
            medDemendee.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {



                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    Toast.makeText(getContext(), s, Toast.LENGTH_SHORT).show();
                    if (SmRechercheMed.listDesMedicament.size() == 0) {
                        new SmRechercheMed().new BackgroundTaskAdepter().execute("http://192.168.137.1/MedList.php");
                        adapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_list_item_1, SmRechercheMed.listDesMedicament);
                        medDemendee.setAdapter(adapter);
                    }

                }

                @Override
                public void afterTextChanged(Editable s) {

                }
            });

        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nomMed = medDemendee.getText().toString();
                if (SmRechercheMed.myPosition != null){
                    lat = SmRechercheMed.myPosition.latitude;
                    lang = SmRechercheMed.myPosition.longitude;
                }
                idUtil = db.getUserDetails().get("uid");
                if (!nomMed.equals("")|| lat ==null||lang == null|| idUtil.equals(null))
                    new BackgroundTaskAdepter().execute(Url);
                else if (lat == null||lang ==null){
                    Toast.makeText(getContext(), "vérifier le GPS", Toast.LENGTH_SHORT).show();
                }else
                    Toast.makeText(getContext(),"le champs et vide ! ", Toast.LENGTH_SHORT).show();



            }
        });
        return v;
    }

    public class BackgroundTaskAdepter extends AsyncTask<String, Void, String> {

        protected String doInBackground(String... params) {
            HttpURLConnection httpURLConnection = null;
            int tmp;
            String data = "";
            try {
                // String urlString=params[0];
                URL url = new URL(params[0]);
                String s = "nom_med=" + nomMed + "&id_util=" + idUtil + "&latitude=" + lat + "&longitude=" + lang;
                httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.setDoInput(true);
                httpURLConnection.setRequestMethod("POST");
                OutputStream os = httpURLConnection.getOutputStream();
                os.write(s.getBytes());
                os.flush();
                os.close();

                InputStream is = httpURLConnection.getInputStream();
                while ((tmp = is.read()) == -1)
                    data += (char) tmp;
                httpURLConnection.connect();
                int mStatusCode = httpURLConnection.getResponseCode();
                switch (mStatusCode) {
                    case 200:
                        BufferedReader br = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream(), "utf-8"), 8);
                        StringBuilder sb = new StringBuilder();
                        String line;
                        while ((line = br.readLine()) != null) {
                            sb.append(line).append("\n");
                        }
                        br.close();
                        return sb.toString();
                }

            } catch (Exception ex) {
                exceptionToBeThrown = ex;
            } finally {
                if (httpURLConnection != null) {
                    try {
                        httpURLConnection.disconnect();
                    } catch (Exception ex) {
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
                Toast.makeText(getContext(), "Ereur de connexion", Toast.LENGTH_SHORT).show();
                exceptionToBeThrown = null;
            } else
                try {
                    JSONObject jObj = new JSONObject(s);
                    boolean error = jObj.getBoolean("error");
                        if(!error)
                            Toast.makeText(getContext(), jObj.getString("message"), Toast.LENGTH_SHORT).show();
                        else
                            Toast.makeText(getContext(), jObj.getString("message"), Toast.LENGTH_SHORT).show();
                } catch (JSONException e) {
                    // JSON error
                    e.printStackTrace();
                    Toast.makeText(getContext(), "Json error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }
        }
    }
}


