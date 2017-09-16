package com.example.farouk.mapharmconsdz;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;

import ListeMedicaments.ListeMedAntiInflam;


public class ListeMedActivityScrollableTabs extends FragmentActivity {
    ViewPager viewPager = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_med_activity_scrollable_tabs);
        viewPager = (ViewPager) findViewById(R.id.pager_scrollTabs_liste_med);
        FragmentManager fragmentManager = getSupportFragmentManager();
        viewPager.setAdapter(new MyAdapter(fragmentManager));
    }

}

class MyAdapter extends FragmentPagerAdapter{

    public MyAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
    Fragment fragment = null;
        if(position == 0){
            fragment = new ListeMedAntiInflam();
        }
        if(position == 1){
            //fragment = new ListeMedDermatologie();
            fragment = new ListeMedAntiInflam();

        }
        if(position == 2){
            //fragment = new ListeMedGastro_Enterologie();
            fragment = new ListeMedAntiInflam();

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
