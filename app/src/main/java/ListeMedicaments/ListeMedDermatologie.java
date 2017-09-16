package ListeMedicaments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.farouk.mapharmconsdz.AdpListeMed;
import com.example.farouk.mapharmconsdz.R;

import java.util.ArrayList;
import java.util.List;

import Classes.Medicament;
import Listeners.RecyclerItemClickListener;

/**
 * Created by hp on 08/05/2016.
 */
public class ListeMedDermatologie extends Fragment implements SwipeRefreshLayout.OnRefreshListener, RecyclerItemClickListener.OnItemClickListener {
    RecyclerView myList;
    public static Context C;
    private SwipeRefreshLayout swipeContainer;
    private SwipeRefreshLayout EmptyswipeContainer;
    private String Url = "http://192.168.43.168/android_login_maphdz/gestion_medicaments/medicament_Dermatologie.php";
    static Exception exceptionToBeThrown;
    static AdpListeMed adapter;
    static List<Medicament> listMedicament = new ArrayList<>();
    //public String type = "Dermatologie";






    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View fragment = inflater.inflate(R.layout.liste_med_layout_all_types, container, false);

        // Configuring SwipeRefresh Layout
        myList = (RecyclerView) fragment.findViewById(R.id.RecyclerView_Medicament);
        RecyclerView.LayoutManager mLayoutManager;
        mLayoutManager = new LinearLayoutManager(C);
        myList.setLayoutManager(mLayoutManager);
        myList.addOnItemTouchListener(new RecyclerItemClickListener(C, this));
        swipeContainer = (SwipeRefreshLayout) fragment.findViewById(R.id.swiper_list_med);
        swipeContainer.setOnRefreshListener(this);
        ListeMedAntiInflam.Url = Url;
        new ListeMedAntiInflam().new BackgroundTask().execute(Url);

        if (isEmpty(myList)) {

            EmptyswipeContainer = (SwipeRefreshLayout) fragment.findViewById(R.id.swiper_empty_liste_med);
            EmptyswipeContainer.setOnRefreshListener(this);
            EmptyswipeContainer.setVisibility(View.GONE);
        }
        return fragment;
    }

    @Override
    public void onRefresh() {
        ListeMedAntiInflam.Url = Url;
        refreshing(Url);

    }
    public void refreshing(String s) {
        new ListeMedAntiInflam().new BackgroundTask().execute(s);
        EmptyswipeContainer.setRefreshing(false);
        swipeContainer.setRefreshing(false);
        if (!isEmpty(myList))
            EmptyswipeContainer.setVisibility(View.GONE);


    }

    @Override
    public void onItemClick(View childView, int position) {

    }

    @Override
    public void onItemLongPress(View childView, int position) {

    }
/*
// AsyncTask Liste Médicaments

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

                adapter = new AdpListeMed(listMedicament);
                adapter.clear();
                try {
                    JSONObject jsonResponse = new JSONObject(s);
                    JSONArray jsonList = jsonResponse.getJSONArray("Medicament");
                    for (int i = 0; i < jsonList.length(); i++) {
                        Medicament med = new Medicament();
                        JSONObject obj = jsonList.getJSONObject(i);
                        med.setNomMed(obj.getString("nom_med"));
                        med.setType(obj.getString("type"));
                        med.setReduction(obj.getInt("reduction"));

                        listMedicament.add(med);

                    }

/*                    Collections.sort(listMedicament, new Comparator<Medicament>() {
                        @Override
                        public int compare(Object e1, Object e2) {
                            return e1.nom_medicament.compareTo(e2.nom_medicament);                        }
                    });
                    myList.setAdapter(adapter);
                    adapter.notifyDataSetChanged();
                    EmptyswipeContainer.setVisibility(View.GONE);

                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

            }
        }

    }*/

    public boolean isEmpty(RecyclerView list) {
        if (list.getChildCount() == 0)
            return true;
        return false;
    }
}