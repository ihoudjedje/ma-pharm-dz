package com.example.farouk.mapharmconsdz;

import android.annotation.TargetApi;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

import Classes.Demande;


public class AdpConsultationDemande extends RecyclerView.Adapter<AdpConsultationDemande.MyViewHolder> {
    List<Demande> mListData;
    private int focusedItem = 0;
    static Exception exceptionToBeThrown;
    //public Context context;
    String IdUser;
    private String IdActualPharmacy;
    public static String UrlInsertStoque = "http://192.168.43.219/AcceptDemande.php";
    public SQLiteHandler db;


    public AdpConsultationDemande(List<Demande> mListData) {
       // this.context =context;
        this.mListData = mListData;
    }



    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.adpt_list_consultation_demande,
                viewGroup, false);


        return new MyViewHolder(view);
    }


    @Override
    public void onBindViewHolder(AdpConsultationDemande.MyViewHolder myViewHolder, int i) {


        myViewHolder.nomMedicament.setText(mListData.get(i).getMed().getNomMed());
        myViewHolder.date.setText(mListData.get(i).getDate());
        myViewHolder.icon_entry.setText(""+mListData.get(i).getMed().getNomMed().charAt(0));


    }

    @Override
    public int getItemCount() {
        return mListData == null ? 0 : mListData.size();
    }



    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        public CardView cardView;
        public TextView nomMedicament;
        public TextView date;
        public TextView icon_entry;
        public ImageButton btn_add;


        public MyViewHolder(final View itemView) {
            super(itemView);
            cardView = (CardView) itemView.findViewById(R.id.user_layout);
            nomMedicament = (TextView) itemView.findViewById(R.id.NomMedConsultation);
            date = (TextView) itemView.findViewById(R.id.dateDemande);
            icon_entry = (TextView) itemView.findViewById(R.id.consultationLettre);
            btn_add = (ImageButton) itemView.findViewById(R.id.envoyeeNotification);

            btn_add.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (v.getId() == btn_add.getId()){

                IdUser = mListData.get(getAdapterPosition()).getUser().getFullName();
                Log.i("Test","here is the IdMedOfPosition  : "+IdUser);
                db = new SQLiteHandler(ConsultationDemande.context);
                IdActualPharmacy = db.getUserDetails().get("id_phar");
                Log.i("Test","here is the IdActualPharmacy  : "+IdActualPharmacy);
                new BackgroundTask().execute(UrlInsertStoque);
                 btn_add.setColorFilter(Color.rgb(66,134,245));
                btn_add.setClickable(false);
                Log.i("Test","hhhhhhhhhhhhhhhhhhhhhhh");
            }
        }

        @Override
        public boolean onLongClick(View v) {
            return false;
        }
    }


    @Override
    public void onAttachedToRecyclerView(final RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }


    public void clear() {
        mListData.clear();
        notifyDataSetChanged();
    }

    public void addAll(List<Demande> list) {
        mListData.addAll(list);
        notifyDataSetChanged();
    }



    // AsyncTask Liste Médicaments
    public class BackgroundTask extends AsyncTask<String, Void, String> {

        protected String doInBackground(String... params) {
            HttpURLConnection httpURLConnection = null;
            int tmp;
            String data = "";
            try {
                URL url = new URL(params[0]);
                String s = "id_util=" + IdUser + "&id_phar_actuel=" + IdActualPharmacy;
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
                        BufferedReader br = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream(),"utf-8"),8);
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
                if (httpURLConnection != null) {
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
                Toast.makeText(ConsultationDemande.context, "Ereur de connexion", Toast.LENGTH_SHORT).show();
                exceptionToBeThrown = null;
            } else
                try {
                    JSONObject jObj = new JSONObject(s);
                    boolean error = jObj.getBoolean("error");
                    if (!error)
                        Toast.makeText(ConsultationDemande.context, jObj.getString("message"), Toast.LENGTH_SHORT).show();
                    else
                        Toast.makeText(ConsultationDemande.context, jObj.getString("message"), Toast.LENGTH_SHORT).show();
                } catch (JSONException e) {
                    // JSON error
                    e.printStackTrace();
                    Toast.makeText(ConsultationDemande.context, "Json error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }
        }
    }
}






