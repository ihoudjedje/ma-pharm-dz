package ListeMedicaments;

/**
 * Created by hp on 12/05/2016.
 */

import android.annotation.TargetApi;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.farouk.mapharmconsdz.AdpListeConsultMed;
import com.example.farouk.mapharmconsdz.R;
import com.example.farouk.mapharmconsdz.SQLiteHandler;

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
import java.util.List;

import Classes.Medicament;

public class ListeMedConsultAntiInflam extends Fragment implements SwipeRefreshLayout.OnRefreshListener{

private RecyclerView myList;
public static Context C;
private SwipeRefreshLayout swipeContainer;
private SwipeRefreshLayout EmptyswipeContainer;
private static String Url = "http://192.168.43.168/android_login_maphdz/gestion_medicaments/medicament_consultation_Anti_inflammatoires.php";
private static Exception exceptionToBeThrown;
private static AdpListeConsultMed adapter;
public static List<Medicament> listMedicament = new ArrayList<>();
private String IdActualPharmacy;
public SQLiteHandler db;



    @Nullable
@Override
public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View fragment = inflater.inflate(R.layout.liste_med_layout_all_types, container, false);

        // Configuring SwipeRefresh Layout
        myList = (RecyclerView) fragment.findViewById(R.id.RecyclerView_Medicament);
        RecyclerView.LayoutManager mLayoutManager;
        mLayoutManager = new LinearLayoutManager(C);
        myList.setLayoutManager(mLayoutManager);
        //myList.addOnItemTouchListener(new RecyclerItemClickListener(C, this));
        swipeContainer = (SwipeRefreshLayout) fragment.findViewById(R.id.swiper_list_med);
        swipeContainer.setOnRefreshListener(this);
        C = getContext();
        //Getting the sharedPreference
     //   SharedPreferences sp = RegisterActivityPlusInfo.C.getSharedPreferences("namapassword" , RegisterActivityPlusInfo.C.MODE_PRIVATE ); // here in the recovering of sharedPreferences always use the
       // IdActualPharmacy = sp.getString("unique_id_phar", null);//context of the contxt where you have set the sharedValue

    //Getting the Pharmacy' ID from SQLite
    db = new SQLiteHandler(C);
    IdActualPharmacy = db.getUserDetails().get("id_phar");

        Log.i("Test","here is the id of the actual pharmacy before passing it to url : "+IdActualPharmacy);
        new BackgroundTask().execute(Url);

        if (isEmpty(myList)) {

        EmptyswipeContainer = (SwipeRefreshLayout) fragment.findViewById(R.id.swiper_empty_liste_med);
        EmptyswipeContainer.setOnRefreshListener(this);
        EmptyswipeContainer.setVisibility(View.GONE);
        }

        return fragment;
        }
@Override
public void onRefresh() {
        refreshing(Url);

        }
public void refreshing(String s) {
        new BackgroundTask().execute(s);
        EmptyswipeContainer.setRefreshing(false);
        swipeContainer.setRefreshing(false);
        if (!isEmpty(myList))
        EmptyswipeContainer.setVisibility(View.GONE);


        }



// AsyncTask Liste Médicaments

public class BackgroundTask extends AsyncTask<String, Void, String> {



    protected String doInBackground(String... params) {
        HttpURLConnection c = null;
        int tmp;
        String data="";
        try {
            String urlString = params[0];
            String s = "id_phar="+IdActualPharmacy;
            URL url = new URL(urlString);
            c = (HttpURLConnection) url.openConnection();
            c.setRequestMethod("POST");
            //  c.setConnectTimeout(15000);
            c.setDoInput(true);
            OutputStream os = c.getOutputStream();
            os.write(s.getBytes());
            os.flush();
            os.close();

            InputStream is = c.getInputStream();
            while ((tmp = is.read()) == -1){
                data +=(char) tmp;
            }
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
            Snackbar.make(getView(), "Check your internet connexion! ", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
            EmptyswipeContainer.setVisibility(View.VISIBLE);
            exceptionToBeThrown = null;

        } else {

            adapter = new AdpListeConsultMed(listMedicament);
            adapter.clear();
            try {
                JSONArray listeJson = new JSONArray(s);

                for (int i = 0; i < listeJson.length(); i++) {
                    Medicament med = new Medicament();
                    JSONObject obj = listeJson.getJSONObject(i);
                    med.setNomMed(obj.getString("nom_med"));
                    med.setType(obj.getString("type"));
                    med.setReduction(obj.getInt("reduction"));
                    med.setIdMed(obj.getInt("id_med"));

                    listMedicament.add(med);

                }

/*                    Collections.sort(listMedicament, new Comparator<Medicament>() {
                        @Override
                        public int compare(Object e1, Object e2) {
                            return e1.nom_medicament.compareTo(e2.nom_medicament);                        }
                    });*/
                myList.setAdapter(adapter);
                adapter.notifyDataSetChanged();
                EmptyswipeContainer.setVisibility(View.GONE);

            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        }

    }


}


    public boolean isEmpty(RecyclerView list) {
        if (list.getChildCount() == 0)
            return true;
        return false;
    }
}



