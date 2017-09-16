package com.example.farouk.mapharmconsdz;


import android.annotation.TargetApi;
import android.content.Context;
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

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

import Classes.Medicament;
import ListeMedicaments.ListeMedAntiInflam;


public class AdpListeMed extends RecyclerView.Adapter<AdpListeMed.MyViewHolder> {
    List<Medicament> mListData;
    private int focusedItem = 0;
    static Exception exceptionToBeThrown;
    public Context context;
    int IdMedOfPosition;
    private String IdActualPharmacy;
    public static String UrlInsertStoque = "http://192.168.43.168/android_login_maphdz/gestion_medicaments/medicament_insert_stoque.php";
    public SQLiteHandler db;




    public AdpListeMed(List<Medicament> mListData) {

        this.mListData = mListData;
    }



    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.adp_custom_liste_med,
                viewGroup, false);


        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MyViewHolder myViewHolder, int i) {


        myViewHolder.nomMedicament.setText(mListData.get(i).getNomMed());
        myViewHolder.reduction.setText("- "+mListData.get(i).getReduction()+" %");

        if(mListData.get(i).getReduction() <= 0){
            myViewHolder.reduction.setTextColor(Color.RED);
        }else{
            myViewHolder.reduction.setTextColor(Color.GREEN);
        }
        myViewHolder.icon_entry.setText(""+mListData.get(i).getNomMed().charAt(0));


    }

    @Override
    public int getItemCount() {
        return mListData == null ? 0 : mListData.size();
    }



    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        public CardView cardView;
        public TextView nomMedicament;
        public TextView reduction;
        public TextView icon_entry;
        public ImageButton btn_add;


        public MyViewHolder(final View itemView) {
            super(itemView);
            cardView = (CardView) itemView.findViewById(R.id.user_layout);
            nomMedicament = (TextView) itemView.findViewById(R.id.nom_medicament);
            reduction = (TextView) itemView.findViewById(R.id.reduction_med);
            icon_entry = (TextView) itemView.findViewById(R.id.icon_entry);
            btn_add = (ImageButton) itemView.findViewById(R.id.button_add);

            btn_add.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (v.getId() == btn_add.getId()){
          //      Toast.makeText(v.getContext(), "Le médicament été ajouté! = ", Toast.LENGTH_SHORT).show();
                IdMedOfPosition = ListeMedAntiInflam.listMedicament.get(getAdapterPosition()).getIdMed();
                Log.i("Test","here is the IdMedOfPosition  : "+IdMedOfPosition);
                //Getting the sharedPreference
               // SharedPreferences sp = RegisterActivityPlusInfo.C.getSharedPreferences("namapassword" , RegisterActivityPlusInfo.C.MODE_PRIVATE ); // here in the recovering of sharedPreferences always use the
               // IdActualPharmacy = sp.getString("unique_id_phar", null);                                                //context of the contxt where you have set the sharedValue
                //Getting the Pharmacy' ID from SQLite
                db = new SQLiteHandler(ListeMedAntiInflam.C);
                IdActualPharmacy = db.getUserDetails().get("id_phar");

                Log.i("Test","here is the IdActualPharmacy  : "+IdActualPharmacy);
                new BackgroundTask().execute(UrlInsertStoque);
                btn_add.setImageResource(R.drawable.checked);
                btn_add.setClickable(false);
            }
        }

        @Override
        public boolean onLongClick(View v) {
 /*           final AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
            builder.setTitle ("Hello Dialog")
                    .setMessage ("LONG CLICK DIALOG WINDOW FOR ICON " + String.valueOf(getAdapterPosition()))
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Toast.makeText("")
                        }
                    });

            builder.create().show();*/

            return false;
        }
    }


    @Override
    public void onAttachedToRecyclerView(final RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);

        // Handle key up and key down and attempt to move selection
    /*    recyclerView.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                RecyclerView.LayoutManager lm = recyclerView.getLayoutManager();

                // Return false if scrolled to the bounds and allow focus to move off the list
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
                        return tryMoveSelection(lm, 1);
                    } else if (keyCode == KeyEvent.KEYCODE_DPAD_UP) {
                       return tryMoveSelection(lm, -1);
                   }
              }

                return false;
            }
        });  */
    }

   /* private boolean tryMoveSelection(RecyclerView.LayoutManager lm, int direction) {
        int tryFocusItem = focusedItem + direction;

        // If still within valid bounds, move the selection, notify to redraw, and scroll
        if (tryFocusItem >= 0 && tryFocusItem < getItemCount()) {
            notifyItemChanged(focusedItem);
            focusedItem = tryFocusItem;
            notifyItemChanged(focusedItem);
            lm.scrollToPosition(focusedItem);
            return true;
        }

        return false;
    }*/



    public void clear() {
        mListData.clear();
        notifyDataSetChanged();
    }
    public void addAll(List<Medicament> list) {
        mListData.addAll(list);
        notifyDataSetChanged();
    }



    // AsyncTask Liste Médicaments

    public class BackgroundTask extends AsyncTask<String, Void, String> {



        protected String doInBackground(String... params) {
            HttpURLConnection c = null;
            int tmp;
            String data="";
            try {
                String urlString = params[0];
                String s = "id_med_position="+IdMedOfPosition+"&id_phar_actuel=" + IdActualPharmacy;
                URL url = new URL(urlString);
                c = (HttpURLConnection) url.openConnection();
                c.setRequestMethod("POST");
            //    c.setConnectTimeout(15000);
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
                Toast.makeText(context,"Verifier votre connexion!",Toast.LENGTH_LONG).show();
                exceptionToBeThrown = null;

            } else {

                try {

                    Toast.makeText(context,"Modifier avec succes!",Toast.LENGTH_LONG).show();
/*                    Collections.sort(listMedicament, new Comparator<Medicament>() {
                        @Override
                        public int compare(Object e1, Object e2) {
                            return e1.nom_medicament.compareTo(e2.nom_medicament);                        }
                    });*/

                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

            }

        }


    }
}

