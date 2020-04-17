package com.example.tmedia.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tmedia.ItemModel;
import com.example.tmedia.OnClickedItem;
import com.example.tmedia.R;

import java.util.ArrayList;
import java.util.List;

public class AdapterOfRecyclerView extends RecyclerView.Adapter<AdapterOfRecyclerView.MyViewHolder> implements Filterable {

    private ArrayList<ItemModel> itemModelList, listfilter;
    private Context context;
    private OnClickedItem onClickedItem;
    private Animation ani;

    public AdapterOfRecyclerView(ArrayList<ItemModel> itemModelList, Context context, OnClickedItem onClickedItem) {
        this.itemModelList = itemModelList;
        this.context = context;
        this.onClickedItem = onClickedItem;
        this.listfilter = itemModelList;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View itemView = layoutInflater.inflate(R.layout.itemview, parent, false);

        ani = AnimationUtils.loadAnimation(context, R.anim.scale);
        itemView.startAnimation(ani);

        return new MyViewHolder(itemView);
    }

    @SuppressLint("ResourceAsColor")
    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        holder.txtTitle.setText(itemModelList.get(position).getTitle());
    }

    @Override
    public int getItemCount() {
        return itemModelList.size();
    }

    @Override
    public Filter getFilter() {
        Filter filter = new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults filterResults = new FilterResults();
                if(constraint.length() == 0 || constraint == null){
                    filterResults.values = listfilter;
                    filterResults.count = listfilter.size();
                } else {
                    String stFilter = constraint.toString().toLowerCase();
                    List<ItemModel> res = new ArrayList<>();
                    for (ItemModel itemModel:listfilter){
                        if(itemModel.getTitle().contains(stFilter)){
                            res.add(itemModel);
                        }
                        filterResults.count = res.size();
                        filterResults.values = res;
                    }

                }
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                itemModelList = (ArrayList<ItemModel>) results.values;
                notifyDataSetChanged();
            }
        };
        return filter;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        TextView txtTitle;

        @SuppressLint("ResourceAsColor")
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            txtTitle = (TextView) itemView.findViewById(R.id.txtitem);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int pos = getAdapterPosition();
                    onClickedItem.onClickedItem(pos);
                }
            });
        }
    }
}
