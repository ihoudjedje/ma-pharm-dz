package com.example.farouk.mapharmconsdz;

/**
 * Created by hp on 12/05/2016.
 */

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;

import ListeMedicaments.ListeMedConsultAntiInflam;

public class ListeMedConsultActivityScrollableTabs extends FragmentActivity {
    ViewPager viewPager = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_med_consult_activity_scrollable_tabs);
        viewPager = (ViewPager) findViewById(R.id.pager_scrollTabs_liste_med_consult);
        FragmentManager fragmentManager = getSupportFragmentManager();
        viewPager.setAdapter(new MyAdapter2(fragmentManager));
    }
}

 class MyAdapter2 extends FragmentPagerAdapter{

    public MyAdapter2(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        Fragment fragment = null;
        if(position == 0){
            fragment = new ListeMedConsultAntiInflam();
        }
        if(position == 1){
            //fragment = new ListeMedDermatologie();
            fragment = new ListeMedConsultAntiInflam();

        }
        if(position == 2){
            //fragment = new ListeMedGastro_Enterologie();
            fragment = new ListeMedConsultAntiInflam();

        }
        return fragment;
    }

    @Override
    public int getCount() {
        return 3 ;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        String title = new String();
        if(position == 0){
            return "          Anti-inflammatoires          ";
        }

        if(position == 1){
            return "           Dermatologie          ";
        }

        if(position == 2){
            return "          Gastro-Entérologie          ";
        }
        return null;
    }
}

