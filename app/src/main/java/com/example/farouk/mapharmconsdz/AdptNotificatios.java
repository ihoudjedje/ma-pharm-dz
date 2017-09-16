package com.example.farouk.mapharmconsdz;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import java.util.List;

import Classes.Notification;

/**
 * Created by farouk on 19-05-2016.
 */
public class AdptNotificatios extends RecyclerView.Adapter<AdptNotificatios.MyViewHolder> {
    PharmacieLocal A = new PharmacieLocal();
    List<Notification> mListData;
    Context context;


    public AdptNotificatios (List<Notification> mListData,Context context) {
        this.mListData = mListData;
        this.context = context;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.adp_custom_adpt_list_pharm_proche,
                viewGroup, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MyViewHolder myViewHolder, int i) {
        myViewHolder.pharmacieNom.setText(mListData.get(i).getPharmacie().getNomPharmacie());
        myViewHolder.nomMed.setText(mListData.get(i).getMedicament().getNomMed());
        myViewHolder.date.setText(mListData.get(i).getDate());
        myViewHolder.firstLetter.setText(mListData.get(i).getPharmacie().getNomPharmacie().charAt(0)+"".toUpperCase());
        animate(myViewHolder);

    }

    @Override
    public int getItemCount() {
        return mListData == null ? 0 : mListData.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder {

        TextView pharmacieNom;
        TextView nomMed;
        TextView  date;
        TextView  firstLetter;


        public MyViewHolder(View itemView) {
            super(itemView);
            pharmacieNom = (TextView) itemView.findViewById(R.id.NomPharmacie);
            nomMed = (TextView) itemView.findViewById(R.id.Adress);
            date = (TextView) itemView.findViewById(R.id.Distance);
            firstLetter = (TextView) itemView.findViewById(R.id.letter);
        }
    }
    public void clear() {
        mListData.clear();
        notifyDataSetChanged();
    }
    public void addAll(List<Notification> list) {
        mListData.addAll(list);
        notifyDataSetChanged();
    }
    public void animate(RecyclerView.ViewHolder viewHolder) {
        final Animation animAnticipateOvershoot = AnimationUtils.loadAnimation(context, R.anim.bounce_interpolator);
        viewHolder.itemView.setAnimation(animAnticipateOvershoot);
    }

    }

