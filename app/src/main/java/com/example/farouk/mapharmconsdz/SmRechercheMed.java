package com.example.farouk.mapharmconsdz;

import android.annotation.TargetApi;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.MatrixCursor;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.CursorAdapter;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
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
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import Classes.Pharmacie;
import Listeners.RecyclerItemClickListener;

/**
 * Created by farouk on 07-04-2016.
 */


public class SmRechercheMed extends Fragment implements SwipeRefreshLayout.OnRefreshListener, RecyclerItemClickListener.OnItemClickListener{
    private SearchView searchMed;
    private AdpCustomAdptListPharmProche adapterList;

    Exception exceptionToBeThrown;
    public  static ArrayList<String> listDesMedicament = new ArrayList<>();
    String Url= "http://192.168.137.1/PharmacyFilter.php";
    public static List<Pharmacie> listPharmacie = new ArrayList<>();
    public static ArrayList<LatLng> listDesPosition = new ArrayList<>();
    public static String nomMed = "";
    RecyclerView listPharmMed ;
    SwipeRefreshLayout swipeListPharmSearch;
    SwipeRefreshLayout  EmptyswipeContainer;
    ProgressBar  barPrg ;
    ImageButton  SwitcherToMap;
    ImageButton  search;
    ImageView secondImage;
    TextView secondText;
    TextView presentationText;
    public static LatLng myPosition;
    public static LatLng pharmacyPosition;
    TextView noPharmacyFound;
    double lat;
    double lang;
    double distance;
    int range;
    String[] columnNames = {"_id","text"};
    String[] temp = new String[2];
    String[] from = {"text"};
    int[] to = {R.id.med_prop};
    int id = 0;
    MatrixCursor cursor;
    CursorAdapter cursorAdapter;
    SearchView.SearchAutoComplete searchSrcTextView;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.sm_recherch_med_pharm, container, false);
        //imag && text View
        secondImage = (ImageView ) v.findViewById(R.id.SecondImage);
        secondText = (TextView)  v.findViewById(R.id.SecondText);
        //Presentation Text
        presentationText = (TextView) v.findViewById(R.id.presentationText);
        //Auto complete handler
        new BackgroundTaskAdepter().execute("http://192.168.137.1/MedList.php");

        searchMed = (SearchView) v.findViewById(R.id.SearchViwMed);
        searchSrcTextView = (SearchView.SearchAutoComplete) v.findViewById(android.support.v7.appcompat.R.id.search_src_text);
        searchSrcTextView.setThreshold(1);
        searchMed.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                new BackgroundTask().execute(Url);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                cursor = new MatrixCursor(columnNames);
                for(int i = 0 ;i<listDesMedicament.size();i++){
                    temp[0] = Integer.toString(id++);
                    temp[1] = listDesMedicament.get(i).toString();
                    cursor.addRow(temp);
                }
                cursorAdapter = new SimpleCursorAdapter(getContext(), R.layout.adpt_list_med_propositon, cursor, from, to);
                searchMed.setSuggestionsAdapter(cursorAdapter);
                return true;
            }
        });
       /* searchSrcTextView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                if (listDesMedicament.size() == 0 ){
                    new BackgroundTaskAdepter().execute("http://192.168.137.1/MedList.php");
                }

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                new BackgroundTask().execute(Url);
            }
        });*/

        // getting range
        SharedPreferences p = PreferenceManager.getDefaultSharedPreferences(getContext());
        String value =getContext().getResources().getString(R.string.search_distance_default_value);
        String b = p.getString(getString(R.string.search_distance_pref), value);
        range =Integer.parseInt(b);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        String A  = prefs.getString(getString(R.string.search_distance_pref), "");
        if (A != "")
            range=Integer.parseInt(A);
        //Init Recycle
        listPharmMed = (RecyclerView) v.findViewById(R.id.ListPharmMed);
        RecyclerView.LayoutManager mLayoutManager;
        mLayoutManager = new LinearLayoutManager(getContext());
        listPharmMed.setLayoutManager(mLayoutManager);
        listPharmMed.addOnItemTouchListener(new RecyclerItemClickListener(getContext(), this));
        //init swipe refresh
        swipeListPharmSearch = (SwipeRefreshLayout) v.findViewById(R.id.swiper_list_pharm_search);
        swipeListPharmSearch.setOnRefreshListener(this);
        swipeListPharmSearch.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);

        // init empty Swiper
        noPharmacyFound = (TextView) v.findViewById(R.id.empty_Text_Med);
        EmptyswipeContainer = (SwipeRefreshLayout) v.findViewById(R.id.empty_swiper_list_pharm_search);
        EmptyswipeContainer.setOnRefreshListener(this);
        EmptyswipeContainer.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);
        EmptyswipeContainer.setVisibility(View.VISIBLE);
        //init Progress bar
        barPrg= (ProgressBar) v.findViewById(R.id.progressBarMedSearch);
        barPrg.setVisibility(View.GONE);
        //init progress bar 2


        //init button Switch
        SwitcherToMap = (ImageButton)v.findViewById(R.id.SwitcherToMapMed);
        SwitcherToMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Fragment fragment =  new SmPharmacieProcheMedMap();
                FragmentManager fm = getFragmentManager();
                FragmentTransaction transaction = fm.beginTransaction();
                transaction.replace(R.id.ListPharmMedLayout, fragment);
                transaction.commit();
            }
        });
        SwitcherToMap.setEnabled(false);
        //getting position if null
        if (myPosition == null) {
            UserLocation.LocationResult locationResult = new UserLocation.LocationResult() {
                @Override
                public void gotLocation(Location location) {
                    lat = location.getLatitude();
                    lang = location.getLongitude();
                    myPosition = new LatLng(lat, lang);
                }
            };
            UserLocation myLocation = new UserLocation();
            myLocation.getLocation(getContext(), locationResult);

        }
        if (!nomMed.equals("") ){
            new BackgroundTask().execute(Url);
            presentationText.setText("");
        }

        return v;
    }
    @Override
    public void onItemClick(View childView, int position) {
        Pharmacie obj = listPharmacie.get(position);
        Intent i = new Intent(getActivity(), PharmacieLocal.class);
        i.putExtra("InfoPharmacy", obj);
        startActivity(i);
    }

    @Override
    public void onItemLongPress(View childView, int position) {

    }

    @Override
    public void onRefresh() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        String A  = prefs.getString(getString(R.string.search_distance_pref), "");
        if (A != "")
            range=Integer.parseInt(A);

        if (myPosition == null)
        {
            UserLocation.LocationResult locationResult = new UserLocation.LocationResult(){
                @Override
                public void gotLocation(Location location){
                    lat = location.getLatitude();
                    lang = location.getLongitude();
                    myPosition = new LatLng(lat, lang);
                }
            };
            UserLocation myLocation = new UserLocation();
            myLocation.getLocation(getContext(), locationResult);
        }
        refreshing(Url);

    }

    public void refreshing(String s) {
        barPrg.setVisibility(View.VISIBLE);
        new BackgroundTask().execute(s);
        EmptyswipeContainer.setRefreshing(false);
        swipeListPharmSearch.setRefreshing(false);
        if (!SmPharmacieProcheList.isEmpty(listPharmMed))
            EmptyswipeContainer.setVisibility(View.GONE);

    }


    public class BackgroundTaskAdepter extends AsyncTask<String, Void, String> {

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
                exceptionToBeThrown = ex;
            } finally {
                if (c != null) {
                    try {
                        c.disconnect();
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
                exceptionToBeThrown = null;
            }else{
            try {
                JSONArray listeJson = new JSONArray(s);
                for (int i = 0; i < listeJson.length(); i++) {
                    JSONObject obj = listeJson.getJSONObject(i);
                    String nom = obj.getString("nom");
                    listDesMedicament.add(i, nom);
                }
               cursor = new MatrixCursor(columnNames);
                for(int i = 0 ;i<listDesMedicament.size();i++){
                    temp[0] = Integer.toString(id++);
                    temp[1] = listDesMedicament.get(i).toString();
                    cursor.addRow(temp);
                }
                cursorAdapter = new SimpleCursorAdapter(getContext(), R.layout.adpt_list_med_propositon, cursor, from, to);
                searchMed.setSuggestionsAdapter(cursorAdapter);

            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

         }
        }
    }


    public class BackgroundTask extends AsyncTask<String, Void, String> {


        protected String doInBackground(String... params) {
            HttpURLConnection httpURLConnection = null;
            int tmp;
            String data = "";
            try {
               // String urlString=params[0];
                URL url = new URL(params[0]);
                String s = "nom_med="+nomMed;
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
                Snackbar.make(getView(), "Server unreachable ", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                EmptyswipeContainer.setVisibility(View.VISIBLE);
                exceptionToBeThrown = null;
                SwitcherToMap.setEnabled(false);
                //secondText.setText("essayée d'actualiser la page");
                //secondImage.setImageResource(R.drawable.ic_refresh_black_24dp);
                searchSrcTextView.setAdapter(new SuggestionAdapter<String>(getContext(), android.R.layout.simple_dropdown_item_1line, listDesMedicament));


            } else if (myPosition == null) {
                barPrg.setVisibility(View.GONE);
                Snackbar.make(getView(), "Check your internet connexion and GPS ", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                EmptyswipeContainer.setVisibility(View.VISIBLE);
                SwitcherToMap.setEnabled(false);
              //  secondText.setText("essayée d'actualiser pour determine \n \t\t\t\t\t\t\t\t\t votre position ");
                //secondImage.setImageResource(R.drawable.ic_location_on_black_24dp);


            }else{
                secondText.setVisibility(View.GONE);
                secondImage.setVisibility(View.GONE);
                SwitcherToMap.setEnabled(true);
                adapterList = new AdpCustomAdptListPharmProche(listPharmacie,getContext());
                adapterList.clear();
                listDesPosition.clear();
                try {
                    JSONArray listeJson = new JSONArray(s);
                    for (int i = 0; i < listeJson.length(); i++) {
                        Log . i ("Size",listeJson.length()+"");
                        JSONObject obj = listeJson.getJSONObject(i);
                        Pharmacie pharm = new Pharmacie();
                        pharm.lat = obj.getString("latitude");
                        pharm.lang = obj.getString("longitude");
                        Double latPharm =  Double.parseDouble( pharm.lat);
                        Double longPharm =  Double.parseDouble( pharm.lang);
                        pharmacyPosition = new LatLng(latPharm,longPharm);
                        distance = SphericalUtil.computeDistanceBetween(myPosition, pharmacyPosition);
                        if (distance <range) {
                            pharm.setDistance(distance);
                            pharm.setNomPharmacie(obj.getString("nom_phar"));
                            pharm.setAddressPharmacie(obj.getString("adresse"));
                            pharm.setHeureOuverture(obj.getString("horaire_ouverture"));
                            pharm.setHeureFermeture(obj.getString("horaire_fermeture"));
                            pharm.setTelephonPharmacie(obj.getString("telephone"));
//                                pharm.setRating(obj.getInt("rating"));
                            listPharmacie.add(pharm);
                            listDesPosition.add(pharmacyPosition);
                        }
                    }
                    //
                    Collections.sort(listPharmacie, new Comparator() {
                        @Override
                        public int compare(Object e1, Object e2) {
                            Double Distance1 = ((Pharmacie) e1).getDistance();
                            Double distance2 = ((Pharmacie) e2).getDistance();
                            return Distance1.compareTo(distance2);
                        }
                    });
                    listPharmMed.setAdapter(adapterList);
                    EmptyswipeContainer.setVisibility(View.GONE);
                    barPrg.setVisibility(View.GONE);
                   // searchSrcTextView.setAdapter(new SuggestionAdapter<String>(getContext(), android.R.layout.simple_dropdown_item_1line, listDesMedicament));
                    if (listPharmacie.size()==0){
                        presentationText.setText("aucune pharmacie n'a été trouvé !");
                        EmptyswipeContainer.setVisibility(View.VISIBLE);
                        SwitcherToMap.setEnabled(false);
                        barPrg.setVisibility(View.GONE);
                    }

                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

            }
        }

    }


}



