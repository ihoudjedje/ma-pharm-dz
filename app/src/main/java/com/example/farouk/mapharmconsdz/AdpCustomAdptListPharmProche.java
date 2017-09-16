package com.example.farouk.mapharmconsdz;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import Classes.Pharmacie;


public class AdpCustomAdptListPharmProche extends RecyclerView.Adapter<AdpCustomAdptListPharmProche.MyViewHolder> {
       PharmacieLocal A = new PharmacieLocal();
        List<Pharmacie> mListData;
        Context context;


        public AdpCustomAdptListPharmProche(List<Pharmacie> mListData,Context context) {
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
                myViewHolder.pharmacieNom.setText(mListData.get(i).getNomPharmacie());
                myViewHolder.address.setText(mListData.get(i).getAddressPharmacie());
                myViewHolder.distance.setText(A.convert(mListData.get(i).getDistance()));
                myViewHolder.firstLetter.setText(mListData.get(i).getNomPharmacie().charAt(0)+"".toUpperCase());
                animate(myViewHolder);

        }

        @Override
        public int getItemCount() {
                return mListData == null ? 0 : mListData.size();
        }

        class MyViewHolder extends RecyclerView.ViewHolder {

                TextView pharmacieNom;
                TextView address;
                TextView  distance;
                TextView  firstLetter;


                public MyViewHolder(View itemView) {
                        super(itemView);


                        pharmacieNom = (TextView) itemView.findViewById(R.id.NomPharmacie);
                        address = (TextView) itemView.findViewById(R.id.Adress);
                        distance = (TextView) itemView.findViewById(R.id.Distance);
                        firstLetter = (TextView) itemView.findViewById(R.id.letter);
                }
        }



     public void clear() {
                mListData.clear();
                notifyDataSetChanged();
        }
        public void addAll(List<Pharmacie> list) {
                mListData.addAll(list);
                notifyDataSetChanged();
        }
    public void animate(RecyclerView.ViewHolder viewHolder) {
        final Animation animAnticipateOvershoot = AnimationUtils.loadAnimation(context, R.anim.bounce_interpolator);
        viewHolder.itemView.setAnimation(animAnticipateOvershoot);
    }
    public void setModels(List<Pharmacie> models) {
        mListData = new ArrayList<>(models);
    }
    public Pharmacie removeItem(int position) {
        final Pharmacie model = mListData.remove(position);
        notifyItemRemoved(position);
        return model;
    }

    public void addItem(int position, Pharmacie model) {
        mListData.add(position, model);
        notifyItemInserted(position);
    }

    public void moveItem(int fromPosition, int toPosition) {
        final Pharmacie model = mListData.remove(fromPosition);
        mListData.add(toPosition, model);
        notifyItemMoved(fromPosition, toPosition);
    }
    public void animateTo(List<Pharmacie> models) {
        applyAndAnimateRemovals(models);
        applyAndAnimateAdditions(models);
        applyAndAnimateMovedItems(models);
    }

    private void applyAndAnimateRemovals(List<Pharmacie> newModels) {
        for (int i = mListData.size() - 1; i >= 0; i--) {
            final Pharmacie model = mListData.get(i);
            if (!newModels.contains(model)) {
                removeItem(i);
            }
        }
    }
    private void applyAndAnimateAdditions(List<Pharmacie> newModels) {
        for (int i = 0, count = newModels.size(); i < count; i++) {
            final Pharmacie model = newModels.get(i);
            if (!mListData.contains(model)) {
                addItem(i, model);
            }
        }
    }
    private void applyAndAnimateMovedItems(List<Pharmacie> newModels) {
        for (int toPosition = newModels.size() - 1; toPosition >= 0; toPosition--) {
            final Pharmacie model = newModels.get(toPosition);
            final int fromPosition = mListData.indexOf(model);
            if (fromPosition >= 0 && fromPosition != toPosition) {
                moveItem(fromPosition, toPosition);
            }
        }
    }
}

