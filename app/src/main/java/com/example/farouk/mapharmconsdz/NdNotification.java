package com.example.farouk.mapharmconsdz;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import Classes.Pharmacie;
import Listeners.RecyclerItemClickListener;

public class NdNotification extends AppCompatActivity implements RecyclerItemClickListener.OnItemClickListener{
    RecyclerView listNotification;
   public static Context context;
    SwipeRefreshLayout swiperNotification;
    RecyclerView.LayoutManager mLayoutManager;
    public static AdptNotificatios adapter;
    TextView NotificationView;
    ImageView NotificationImageView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);
        context = getApplicationContext();
        NotificationImageView = (ImageView) findViewById( R.id.NotificationImageView);
        NotificationView = (TextView) findViewById( R.id.NotificationTextView);
        NotificationImageView.setVisibility(View.GONE);
        NotificationView.setVisibility(View.GONE);

        if (StartUpActivity.lesNotification.size() == 0) {
            NotificationImageView.setVisibility(View.VISIBLE);
            NotificationView.setVisibility(View.VISIBLE);
        }
        if(StartUpActivity.exceptionToBeThrown != null)
        {
            Toast.makeText(this, "Server unreachable", Toast.LENGTH_SHORT).show();
        }


        listNotification = (RecyclerView) findViewById( R.id.notifiaction);
        mLayoutManager = new LinearLayoutManager(getApplicationContext());
        listNotification.setLayoutManager(mLayoutManager);
        listNotification.addOnItemTouchListener(new RecyclerItemClickListener(getApplicationContext(), this));
        swiperNotification = (SwipeRefreshLayout) findViewById( R.id.swiper_list_notification);
        swiperNotification.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                adapter.clear();
                new StartUpActivity().new BackgroundTask().execute(StartUpActivity.url);
                adapter.addAll(StartUpActivity.lesNotification);
                swiperNotification.setRefreshing(false);

            }
        });
        swiperNotification.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);

        adapter = new AdptNotificatios(StartUpActivity.lesNotification,getApplicationContext());
        listNotification.setAdapter(adapter);
    }

    @Override
    public void onItemClick(View childView, int position) {
        Pharmacie p = StartUpActivity.lesNotification.get(position).getPharmacie();
        Intent i = new Intent(this, PharmacieLocal.class);
        i.putExtra("InfoPharmacy", p);
        startActivity(i);
    }

    @Override
    public void onItemLongPress(View childView, int position) {

    }
}
