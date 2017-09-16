package com.example.farouk.mapharmconsdz;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

/**
 * Created by farouk on 07-04-2016.
 */
public class SmEspaceMaPharmacie extends Fragment{
    private ImageButton btn_link_liste_med_activity_scrollTabs;
    private ImageButton btn_link_liste_med_activity_scrollTabs_2;
    private ImageButton btn_link_consultation_demande;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v =  inflater.inflate(R.layout.sm_espace_ma_pharmacie,container,false);
        btn_link_liste_med_activity_scrollTabs = (ImageButton) v.findViewById(R.id.btn_link_liste_med_activity_scrollTabs);
        btn_link_liste_med_activity_scrollTabs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getContext(),ListeMedActivityScrollableTabs.class);
                startActivity(i);
            }
        });
        btn_link_liste_med_activity_scrollTabs_2 = (ImageButton) v.findViewById(R.id.btn_link_liste_med_activity_scrollTabs_2);
        btn_link_liste_med_activity_scrollTabs_2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getContext(),ListeMedConsultActivityScrollableTabs.class);
                startActivity(i);
            }
        });
        btn_link_consultation_demande = (ImageButton) v.findViewById(R.id.btn_link_consultation_demande);
        btn_link_consultation_demande.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getContext(),ConsultationDemande.class);
                startActivity(i);
            }
        });

        return v;
    }

}
