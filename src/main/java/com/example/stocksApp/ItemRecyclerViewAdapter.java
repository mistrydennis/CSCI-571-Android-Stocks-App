package com.example.stocksApp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.DrawableRes;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

/**
 * Created by dennis on 11/7/2020.
 */

public class ItemRecyclerViewAdapter extends RecyclerView.Adapter<ItemRecyclerViewAdapter.ItemViewHolder> {

    class ItemViewHolder extends RecyclerView.ViewHolder {
        private TextView itemLabel;
        private ImageView imgsrc;

        public ItemViewHolder(View itemView) {
            super(itemView);
//            itemLabel = (TextView) itemView.findViewById(R.id.item_label);
            imgsrc = (ImageView)itemView.findViewById((R.id.image_change));
        }
    }

    private Context context;
    private ArrayList<String> arrayList;

    public ItemRecyclerViewAdapter(Context context, ArrayList<String> arrayList) {
        this.context = context;
        this.arrayList = arrayList;
    }

    @Override
    public ItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_custom_row_layout, parent, false);
        return new ItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ItemViewHolder holder, int position) {
        holder.itemLabel.setText(arrayList.get(position));
//        holder.imgsrc.setImageDrawable('');
    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }


}
