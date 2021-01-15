package com.example.stocksApp.adapter;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.stocksApp.R;
import com.example.stocksApp.Stock;
import com.example.stocksApp.StockDetailForLocal;
import com.example.stocksApp.StockDetails;
import com.example.stocksApp.interfaces.ClickListener;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.joining;

public class HomePageAdapter extends RecyclerView.Adapter<HomePageAdapter.StockViewHolder> implements ItemMoveCallback.ItemTouchHelperContract {
    private static final String TAG = "Adapter";
    private List<StockDetailForLocal> stocks;
    private Context context;
    private boolean isPortfolio = false;
    private ClickListener clickListener;

    public HomePageAdapter(List<StockDetailForLocal> stocks,Context context,boolean isPortfolio) {
        this.stocks = stocks;
        this.context = context;
        this.isPortfolio = isPortfolio;
    }

    public void setList(List<StockDetailForLocal> list) {
        this.stocks = new ArrayList<>(list);
        notifyDataSetChanged();
    }

    public void setClickListener(ClickListener clickListener) {
        this.clickListener = clickListener;
    }

    @Override
    public int getItemCount() {


        return stocks.size();
    }

    public void removeItem(int position) {
        stocks.remove(position);
        notifyItemRemoved(position);
    }

    public List<StockDetailForLocal> getData() {
        return stocks;
    }
    @NonNull
    @Override
    public StockViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.item_custom_row_layout, viewGroup, false);
        final StockViewHolder stockViewHolder = new StockViewHolder(v);
        return stockViewHolder;
    }

    @Override
    public void onBindViewHolder(StockViewHolder stockViewHolder, int i) {
//        SharedPreferences pref = context.getSharedPreferences(Constants.BOOKMARK, 0);
        StockDetailForLocal currentArticle = stocks.get(i);

        stockViewHolder.title.setText(currentArticle.ticker);
        Log.d("TAG","This is being called"+stocks.get(i).change);
        if(Double.parseDouble(stocks.get(i).quantity)>0)
        {
            if(Double.parseDouble(stocks.get(i).quantity)<=1.0)
            {
                stockViewHolder.description.setText(Math.round(Double.parseDouble(currentArticle.quantity)*100.0)/100.0 +" share");
            }
            else
            {
                stockViewHolder.description.setText(Math.round(Double.parseDouble(currentArticle.quantity)*100.0)/100.0 +" shares");
            }

        }
        else
        {
            stockViewHolder.description.setText(currentArticle.ticker_company_name);
        }
        DecimalFormat df = new DecimalFormat("###.##");
        stockViewHolder.currentPrice.setText(df.format(Double.parseDouble(currentArticle.latestPrice)));
        stockViewHolder.change_value_card.setText(currentArticle.change);
        if(Double.parseDouble(currentArticle.change)<0.0)
        {
            stockViewHolder.change_value_card.setTextColor(Color.parseColor("#af6a72"));
            double positive = Math.abs(Double.parseDouble(currentArticle.change));
            stockViewHolder.change_value_card.setText(Double.toString(positive));
        }

        else if(Double.parseDouble(currentArticle.change)==0.0)
        {
            stockViewHolder.change_value_card.setTextColor(Color.GRAY);
        }
        else
        {
            stockViewHolder.change_value_card.setTextColor(Color.parseColor("#73b78f"));
        }
        stockViewHolder.newsCardImage.setImageResource(
                Double.parseDouble(currentArticle.change)>0.0?R.drawable.ic_twotone_trending_up_24:(Double.parseDouble(currentArticle.change)==0.0?R.drawable.no_image:R.drawable.ic_baseline_trending_down_24));
//        articleViewHolder.timeDifference.setText(getDifference(currentArticle.publicationDate));
//        Picasso.with(context).load(currentArticle.thumbnail).into(articleViewHolder.newsCardImage);
//        articleViewHolder.bookmarkIcon.setImageResource(
//                pref.getString(articles.get(i).articleId, null) == null ?
//                        R.drawable.ic_bookmark : R.drawable.ic_bookmarked);
    }

    @Override
    public void onRowMoved(int fromPosition, int toPosition) {
        if (fromPosition < toPosition) {
            for (int i = fromPosition; i < toPosition; i++) {
                Collections.swap(stocks, i, i + 1);
            }
        } else {
            for (int i = fromPosition; i > toPosition; i--) {
                Collections.swap(stocks, i, i - 1);
            }
        }
        //Logic for preserving the order after drag and drop.
        StockDetailForLocal stock_from = stocks.get(fromPosition);
        StockDetailForLocal stock_to = stocks.get(toPosition);
        if(isPortfolio)
        {
            SharedPreferences order = this.context.getApplicationContext().getSharedPreferences("order",0);
            SharedPreferences.Editor edit = order.edit();
            String getticker = order.getString("order","");
            Map<String,Integer> port_order = new LinkedHashMap<>();

            if(getticker.length()>0) {
                String[] arr = getticker.split(",");
                int i = 0;
                for (String a : arr) {
                    port_order.put(a, i++);
                }
                int from = port_order.get(stock_from.ticker);
                int to = port_order.get(stock_to.ticker);
                port_order.put(stock_from.ticker, to);
                port_order.put(stock_to.ticker, from);
                String s = port_order.entrySet().stream().sorted(Map.Entry.comparingByValue()).map(e->e.getKey()).collect(joining(","));
                edit.putString("order",s);
                edit.apply();
            }

        }
        else
        {

            SharedPreferences forder = this.context.getApplicationContext().getSharedPreferences("forder",0);
            SharedPreferences.Editor fedit = forder.edit();
            Log.d("Home Page",forder+"");
            String getticker = forder.getString("forder","");
            Log.d("Home Page",getticker+"");
            Map<String,Integer> fav_order = new LinkedHashMap<>();


            if(getticker.length()>0)
            {
                String[] arr = getticker.split(",");
                int i=0;
                for(String a: arr)
                {
                    fav_order.put(a,i++);
                }
                int from = fav_order.get(stock_from.ticker);
                int to = fav_order.get(stock_to.ticker);
                fav_order.put(stock_from.ticker,to);
                fav_order.put(stock_to.ticker,from);
//

                Log.d("Home page",fav_order.values()+"");
                String s = fav_order.entrySet().stream().sorted(Map.Entry.comparingByValue()).map(e->e.getKey()).collect(joining(","));
                Log.d("Home page",fav_order.keySet()+" "+fav_order.values());
                fedit.putString("forder",s);

                fedit.apply();
            }
        }


        notifyItemMoved(fromPosition, toPosition);
    }

    @Override
    public void onRowSelected(StockViewHolder myViewHolder) {
        myViewHolder.cv.setBackgroundColor(Color.GRAY);
    }

    @Override
    public void onRowClear(StockViewHolder myViewHolder) {
        myViewHolder.cv.setBackgroundColor(Color.WHITE);

    }

    public class StockViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener {
        CardView cv;
        TextView title;
        TextView description, timeDifference;
        ImageView newsCardImage;
        TextView currentPrice;
        TextView change_value_card;
        Button onclickDetail;
        View rowView;
            StockViewHolder(View itemView) {
            super(itemView);
//            SharedPreferences pref = context.getSharedPreferences(Constants.BOOKMARK, 0);
//            SharedPreferences.Editor addToBookmark = pref.edit();
            rowView = itemView;
            cv = itemView.findViewById(R.id.card_id);
            title = itemView.findViewById(R.id.ticker_name);
            description = itemView.findViewById(R.id.ticker_description);
            currentPrice = itemView.findViewById(R.id.current_price_card);
            newsCardImage = itemView.findViewById(R.id.image_change);
            change_value_card = itemView.findViewById(R.id.change_value_card);
            onclickDetail = itemView.findViewById(R.id.onclickDetail);

            onclickDetail.setOnClickListener(v-> {
                StockDetailForLocal currstock = stocks.get(getAdapterPosition());
                Context context = v.getContext();
                String ticker = currstock.ticker;
                Intent stockIntent = new Intent(v.getContext(), StockDetails.class);
//                Intent stockIntent = new Intent(v.getContext(), StockDetails.class);

                stockIntent.putExtra("query", ticker);
               context.startActivity(stockIntent);
            });
//            description = itemView.findViewById(R.id.description);
//            newsCardImage = itemView.findViewById(R.id.newscardImage);
//            timeDifference = itemView.findViewById(R.id.time_difference);
//            bookmarkIcon = itemView.findViewById(R.id.bookmark_card_recycler_view);
//            bookmarkIcon.setOnClickListener(v -> {
//                NewsArticle article = articles.get(getAdapterPosition());
//                articleId = article.articleId;
//                String title = article.title;
//                if(pref.getString(articleId,null) == null){
//                    bookmarkIcon.setImageResource(R.drawable.ic_bookmarked);
//                    addToBookmark.putString(articleId, article.toMyString());
//                    Toast.makeText(itemView.getContext(),
//                            title+" added to Bookmarks",
//                            Toast.LENGTH_SHORT).show();
//                } else {
//                    bookmarkIcon.setImageResource(R.drawable.ic_bookmark);
//                    addToBookmark.remove(articleId);
//                    Toast.makeText(itemView.getContext(),
//                            title+" removed from Bookmarks",
//                            Toast.LENGTH_SHORT).show();
//                }
//                addToBookmark.apply();
//            });
//            onclickDetail.setOnClickListener(this);
            itemView.setOnClickListener(this);
//            itemView.setOnLongClickListener(this);
        }

        @Override
        public void onClick(View v) {
            clickListener.onClick(v, getAdapterPosition());
        }
    }
}
