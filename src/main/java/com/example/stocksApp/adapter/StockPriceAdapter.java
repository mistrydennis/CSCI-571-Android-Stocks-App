package com.example.stocksApp.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.stocksApp.NewsArticle;
import com.example.stocksApp.R;
import com.example.stocksApp.StockPrice;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

public class StockPriceAdapter extends BaseAdapter {

    private static final String TAG = "StockPriceAdapter";
    private List<StockPrice> stockPrice;
    private Context context;
    LayoutInflater layoutInflater;

    public StockPriceAdapter(List<StockPrice> stocks, Context context) {
        this.stockPrice= stocks;
        this.context = context;
    }
    @Override
    public int getCount() {
        return stockPrice.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        StockPrice currentstock = stockPrice.get(position);
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View gridView;

        if(convertView==null)
        {
            gridView = new View(context);
            gridView = inflater.inflate(R.layout.stats,null);
            TextView first = (TextView)gridView.findViewById(R.id.first);
            first.setText(currentstock.company_latest_price);
            TextView second = (TextView)gridView.findViewById(R.id.second);
            second.setText(currentstock.low);
            TextView third = (TextView)gridView.findViewById(R.id.third);
            third.setText(currentstock.bid_Price);
            TextView fourth = (TextView)gridView.findViewById(R.id.fourth);
            fourth.setText(currentstock.open_Price);
            TextView fifth = (TextView)gridView.findViewById(R.id.fifth);
            fifth.setText(currentstock.mid);
            TextView sixth = (TextView)gridView.findViewById(R.id.sixth);
            sixth.setText(currentstock.high);
            TextView seven = (TextView)gridView.findViewById(R.id.seventh);
            seven.setText(currentstock.Volume);
        }
        else
        {
            gridView = (View)convertView;
        }


        return gridView;

    }
}
