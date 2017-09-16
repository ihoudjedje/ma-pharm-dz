package com.example.farouk.mapharmconsdz;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.SearchView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.SphericalUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.Serializable;
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
public class SmPharmacieProcheList extends Fragment implements SwipeRefreshLayout.OnRefreshListener, RecyclerItemClickListener.OnItemClickListener, Serializable {
    RecyclerView list;
    public static Context C;
    public static Activity A;
    private SwipeRefreshLayout swipeContainer;
    static AdpCustomAdptListPharmProche adapter;
    static AdpCustomAdptListPharmProche adapterFilter;
    static Exception exceptionToBeThrown;
    String Url ="http://192.168.137.1/InfoPharmecy.php";
    static List<Pharmacie> listPharmacie = new ArrayList<>();
    Double lat, lang;
    static LatLng myPosition;
    LatLng pharmacyPosition;
    public static ArrayList<LatLng> listDesPosition = new ArrayList<>();
    ImageButton SwitcherToMap;
    static double distance;
    private static final int REQUEST_CODE_PERMISSION = 2;
    int range;

    ArrayList <Pharmacie> FilterPharmacy =new ArrayList<>();
    Button All;
    Button near;
    boolean witchList = true;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        C = getContext().getApplicationContext();
        A = getActivity();


        //.................................Setting the Shared preferences..........................................//
        SharedPreferences p = PreferenceManager.getDefaultSharedPreferences(getContext());
        String value =getContext().getResources().getString(R.string.search_distance_default_value);
        String b = p.getString(getString(R.string.search_distance_pref), value);
        range =Integer.parseInt(b);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        String A  = prefs.getString(getString(R.string.search_distance_pref), "");
         if (A != "")
            range=Integer.parseInt(A);

        //..............................................Permission Check ...........................................//
        if (ActivityCompat.checkSelfPermission(SmPharmacieProcheList.C, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(SmPharmacieProcheList.C, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(SmPharmacieProcheList.A,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION)&& ActivityCompat.shouldShowRequestPermissionRationale(SmPharmacieProcheList.A,
                    android.Manifest.permission.ACCESS_FINE_LOCATION)) {

            } else {
                ActivityCompat.requestPermissions(SmPharmacieProcheList.A,
                        new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION, android.Manifest.permission.ACCESS_FINE_LOCATION},
                        REQUEST_CODE_PERMISSION);
            }


        }
        final View fragment = inflater.inflate(R.layout.sm_pharm_proche, container, false);

        //..........................................Getting user Location................................................//

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
        //.....................................................init firstImage and text......................................//
      /*  firstImage = (ImageView) fragment.findViewById(R.id.firstImage);
        firstImage.setVisibility(View.GONE);
        firstText = (TextView) fragment.findViewById(R.id.firstText);
        firstText.setVisibility(View.GONE);*/

        //button Switcher to map
        SwitcherToMap = (ImageButton) fragment.findViewById(R.id.SwitcherToMap);
        SwitcherToMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Fragment fragment = new SmPharmacieProcheMap();
                FragmentManager fm = getFragmentManager();
                FragmentTransaction transaction = fm.beginTransaction();
                transaction.replace(R.id.fragmentList, fragment);
                transaction.commit();

            }
        });
        //.....................................................Configuring SwipeRefresh Layout ...................................//
        list = (RecyclerView) fragment.findViewById(R.id.Pharmacie);
        RecyclerView.LayoutManager mLayoutManager;
        mLayoutManager = new LinearLayoutManager(C);
        list.setLayoutManager(mLayoutManager);
        list.addOnItemTouchListener(new RecyclerItemClickListener(C, this));
        swipeContainer = (SwipeRefreshLayout) fragment.findViewById(R.id.swiper_list);
        swipeContainer.setOnRefreshListener(this);
        swipeContainer.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);

        //fields of sherch
        final SearchView pharmacySearch = (SearchView) fragment.findViewById(R.id.SearchButton);
        pharmacySearch.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String query) {
                Toast.makeText(getContext(), query, Toast.LENGTH_SHORT).show();
                final List<Pharmacie> filteredModelList = filter(FilterPharmacy, query);
                adapterFilter.animateTo(filteredModelList);
                list.scrollToPosition(0);
                return true;
            }
        });
        //Button
        near = (Button) fragment.findViewById(R.id.nearPharmacyButton);
        All = (Button) fragment.findViewById(R.id.allPharmacyButton);
        near.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listPharmacie.size()==0){
                    Snackbar snackbar = Snackbar
                            .make(getView(), "No pharmacy found", Snackbar.LENGTH_INDEFINITE)
                            .setAction("Refresh", new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    refreshing(Url);
                                }
                            });
                    snackbar.show();
                    SwitcherToMap.setEnabled(false);
                    swipeContainer.setRefreshing(false);
                    swipeContainer.setEnabled(true);
                }
                witchList = false;
                pharmacySearch.setEnabled(false);
                SwitcherToMap.setEnabled(true);
                list.setAdapter(adapter);
                pharmacySearch.setEnabled(false);
            }
        });
        All.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                witchList = true;
                pharmacySearch.setEnabled(true);
                SwitcherToMap.setEnabled(false);
                list.setAdapter(adapterFilter);

            }
        });


                //.......................................Async.......................................................//
        new BackgroundTask().execute(Url);

        return fragment;
    }



    @Override
    public void onRefresh() {
        refreshing(Url);
    }

    public void refreshing(String s) {
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
       swipeContainer.setRefreshing(true);
        new BackgroundTask().execute(s);
        swipeContainer.setRefreshing(false);
    }

    @Override
    public void onItemClick(View childView, int position) {
        Pharmacie obj;
        if (!witchList)
           obj = listPharmacie.get(position);
        else
            obj = FilterPharmacy.get(position);
        Intent i = new Intent(getActivity(), PharmacieLocal.class);
        i.putExtra("InfoPharmacy", obj);
        startActivity(i);
    }

    @Override
    public void onItemLongPress(View childView, int position) {

    }


    public class BackgroundTask extends AsyncTask<String, Void, String> {


        protected String doInBackground(String... params) {
            HttpURLConnection c = null;
            try {
                String urlString = params[0];
                URL url = new URL(urlString);
                c = (HttpURLConnection) url.openConnection();
                c.setRequestMethod("GET");
                c.setConnectTimeout(1500);
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
                NdAccueil.fab.setVisibility(View.GONE);
                swipeContainer.setRefreshing(false);
                swipeContainer.setEnabled(false);
                Snackbar snackbar = Snackbar
                        .make(getView(), "no connexion", Snackbar.LENGTH_INDEFINITE)
                        .setAction("Refresh", new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                               refreshing(Url);
                            }
                        });
                snackbar.show();
                exceptionToBeThrown = null;
                SwitcherToMap.setEnabled(false);
                NdAccueil.fab.setVisibility(View.GONE);

            } else if (myPosition == null) {
                swipeContainer.setEnabled(false);
                swipeContainer.setRefreshing(false);
                Snackbar snackbar = Snackbar
                        .make(getView(), "no connexion", Snackbar.LENGTH_INDEFINITE)
                        .setAction("Refresh", new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                refreshing(Url);
                            }
                        });

                snackbar.show();

                        SwitcherToMap.setEnabled(false);

                     }else{
                adapter = new AdpCustomAdptListPharmProche(listPharmacie, getContext());
                adapter.clear();
                adapterFilter = new AdpCustomAdptListPharmProche( FilterPharmacy,getContext());
                adapterFilter.clear();
                SwitcherToMap.setEnabled(true);
                    listDesPosition.clear();
                try {
                    JSONObject jsonResponse = new JSONObject(s);
                    JSONArray jsonList = jsonResponse.getJSONArray("Pharmacie");
                    for (int i = 0; i < jsonList.length(); i++) {
                        Pharmacie pharm = new Pharmacie();
                        JSONObject obj = jsonList.getJSONObject(i);
                        pharm.lat = obj.getString("latitude");
                        pharm.lang = obj.getString("longitude");
                        Double latPharm =  Double.parseDouble( pharm.lat);
                        Double longPharm =  Double.parseDouble( pharm.lang);
                        pharmacyPosition = new LatLng(latPharm,longPharm);
                        distance = SphericalUtil.computeDistanceBetween(myPosition, pharmacyPosition);
                        pharm.setDistance(distance);
                        pharm.setNomPharmacie(obj.getString("nom_phar"));
                        pharm.setAddressPharmacie(obj.getString("adresse"));
                        pharm.setActivation(obj.getInt("activation"));
                        pharm.setVillePharmacie(obj.getString("ville"));
                        pharm.setCodePostalPharmacie(obj.getString("codepostal"));
                        pharm.setHeureOuverture(obj.getString("horaire_ouverture"));
                        pharm.setHeureFermeture(obj.getString("horaire_fermeture"));
                        pharm.setTelephonPharmacie(obj.getString("telephone"));
//                      pharm.setRating(obj.getInt("rating"));
                        FilterPharmacy.add(pharm);
                        if (distance <range) {
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
                    if(!witchList) {
                        list.setAdapter(adapter);
                        adapter.notifyDataSetChanged();
                    }else{
                    list.setAdapter(adapterFilter);
                    swipeContainer.setRefreshing(false);
                    swipeContainer.setEnabled(true);
                    NdAccueil.fab.setVisibility(View.VISIBLE);
                    }

                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

            }
        }

    }

    public static  boolean isEmpty(RecyclerView list) {
        if (list.getChildCount() == 0)
            return true;
        return false;
    }



    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
    }
    
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_PERMISSION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
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
    private List<Pharmacie> filter(List<Pharmacie> models, String query) {
        query = query.toLowerCase();

        final List<Pharmacie> filteredModelList = new ArrayList<>();
        for (Pharmacie model : models) {
            final String text = model.getNomPharmacie().toLowerCase();
            if (text.contains(query)) {
                filteredModelList.add(model);
            }
        }
        return filteredModelList;
    }


}
