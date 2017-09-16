package com.example.farouk.mapharmconsdz;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import Classes.Pharmacie;


public class SmPharmacieProcheMedMap extends Fragment implements OnMapReadyCallback
{
    GoogleMap mMap;
    ImageButton SwitcheToList ;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.sm_pharmacie_proche_med_map,container,false);
        SwitcheToList = (ImageButton)v.findViewById(R.id.SwitcherToListMed);
        SwitcheToList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    SwitcheToList.setVisibility(View.GONE);
                    Fragment fragment = new SmRechercheMed();
                    FragmentManager fm = getFragmentManager();
                    FragmentTransaction transaction = fm.beginTransaction();
                    transaction.replace(R.id.pharmacieProcheMed, fragment);
                    transaction.commit();
            }
        });
        SupportMapFragment mapFragment = (SupportMapFragment) this.getChildFragmentManager()
                .findFragmentById(R.id.pharmacieProcheMed);
        mapFragment.getMapAsync(this);
        return v;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;


        mMap.addMarker(new MarkerOptions()
                .position(SmPharmacieProcheList.myPosition)
                .title("Current Location").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
        CameraUpdate center=
                CameraUpdateFactory.newLatLng(SmRechercheMed.myPosition);
        CameraUpdate zoom=CameraUpdateFactory.zoomTo(14);
        mMap.moveCamera(center);
        mMap.animateCamera(zoom);

            for (int i = 0; i<SmRechercheMed.listDesPosition.size() ;i++) {
                MarkerOptions marker = new MarkerOptions();
                mMap.addMarker(marker
                        .position(SmRechercheMed.listDesPosition.get(i))
                        .title(SmRechercheMed.listPharmacie.get(i).getNomPharmacie()));
            }
        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                String pharmacyName = marker.getTitle();
                for (int i = 0; i < SmRechercheMed.listPharmacie.size(); i++)
                    if (pharmacyName.equals(SmRechercheMed.listPharmacie.get(i).getNomPharmacie())) {
                        Pharmacie obj = SmRechercheMed.listPharmacie.get(i);
                        Intent intent = new Intent(getActivity(), PharmacieLocal.class);
                        intent.putExtra("InfoPharmacy", obj);
                        startActivity(intent);
                        return true;
                    }
                return false;
            }
        });




    }



}