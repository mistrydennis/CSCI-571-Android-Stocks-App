package com.example.stocksApp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.icu.number.Precision;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.InputFilter;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.stocksApp.adapter.NewsArticlesAdapter;
import com.example.stocksApp.adapter.StockPriceAdapter;
import com.example.stocksApp.interfaces.ClickListener;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.TreeMap;

import jp.wasabeef.picasso.transformations.RoundedCornersTransformation;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static jp.wasabeef.picasso.transformations.RoundedCornersTransformation.CornerType.BOTTOM_LEFT;

public class StockDetails extends AppCompatActivity implements ClickListener {
    private JSONObject searchResults, latestPriceResults;
    private JSONArray newsArticles;
    public static final String TAG = "StockDetails";
    private List<NewsArticle> articles;
    private List<StockPrice> stockPrices;
    private NewsArticlesAdapter adapter;
    boolean loading=false,dataReceived=false,latestPriceDataReceived=false,newsDataReceived= false;
    private String company_latest_price,ticker,company_name,latest_price,changeValue,quantity="0.0",market_value_calc,total_balance;
    final List<String> for_total_balance_check = new ArrayList<>();
    double total_qty_multiple_buy=0.0;
    StockDetailForLocal current_stock;
    ZoneId zoneId = ZoneId.of("PST");


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stock_details);
        Toolbar myToolbar = (Toolbar) findViewById(R.id.toolbar_details);
        setSupportActionBar(myToolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        ProgressBar spinner;
        loading = false;
        dataReceived = false;
        latestPriceDataReceived = false;
        newsDataReceived = false;
        spinner = findViewById(R.id.progressBar2);
        TextView txt = findViewById(R.id.fetching_text);
        Intent intent = getIntent();
        String query = intent.getStringExtra(Constants.KEYWORD);
        Log.d("Stock detail ticker on click",query);

        ticker = query;
        spinner.setVisibility(VISIBLE);
        txt.setVisibility(VISIBLE);
        getData(ticker);
        SharedPreferences total = this.getSharedPreferences(Constants.TOTAL_BALANCE,0);
        SharedPreferences.Editor edit = total.edit();
        total_balance = total.getString("total_balance","");
        Log.d("Total in Stock details",total_balance+"");
        edit.apply();
        final Handler handler = new Handler();
        Runnable proceedsRunnable = new Runnable() {
            @Override
            public void run() {
                if(newsDataReceived && dataReceived && latestPriceDataReceived) {
                    spinner.setVisibility(GONE);
                    txt.setVisibility(GONE);
                    findViewById(R.id.scroll_view).setVisibility(VISIBLE);
//                    findViewById(R.id.main_layout).setVisibility(VISIBLE);
                    displayStockDetails(ticker);
                    WebView webView = findViewById(R.id.highcharts_stock);
                    webView.getSettings().setJavaScriptEnabled(true);
                    webView.getSettings().setDomStorageEnabled(true);
                    webView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
                    webView.loadUrl("file:///android_asset/highcharts-data.html?data="+ticker);
                    webView.setWebViewClient(new WebViewClient());
                    webView.setBackgroundColor(Color.WHITE);
                    webView.setLayerType(View.LAYER_TYPE_SOFTWARE,null);
                    buyingShares(ticker);
                } else {
                    handler.postDelayed(this, 100);
                }
            }
        };
        handler.post(proceedsRunnable);


    }



    private void buyingShares(String ticker){
        Button trade = findViewById(R.id.trading_button);
        SharedPreferences pref = this.getSharedPreferences(Constants.PORTFOLIO, 0);
        SharedPreferences.Editor addToPortfolio = pref.edit();
        TextView info_portfolio_trading = findViewById(R.id.info_portfolio_trading);
        if(pref.getString(ticker,null)!=null)
        {
            try{
                current_stock = StockDetailForLocal.toStock(pref.getString(ticker," "));
                total_qty_multiple_buy = Double.parseDouble(current_stock.quantity);

                market_value_calc=current_stock.quantity;
            }
            catch (JSONException e){e.printStackTrace();}


        }
        else
        {

            market_value_calc="0.0";
            current_stock = new StockDetailForLocal(ticker,latest_price,changeValue,company_name,quantity,String.valueOf(new Date()));

        }
        DecimalFormat df = new DecimalFormat("###.##");
        DecimalFormat df1 = new DecimalFormat("###.####");
        if(Double.parseDouble(current_stock.quantity)==0.0)
        {
            info_portfolio_trading.setText("You have 0 shares of "+ current_stock.ticker +"\n"+"\t\t"+ " Start trading!");
        }
        else
        {
            info_portfolio_trading.setText("Shares Owned: " + current_stock.quantity+"\n"+"Market Value: $"+String.valueOf(df.format(Double.parseDouble(current_stock.quantity)*Double.parseDouble(latest_price))));
        }



            trade.setOnClickListener((View.OnClickListener) v -> {

                Dialog dialog = new Dialog(v.getContext());
                dialog.setContentView(R.layout.buy_sell_dialog);
                dialog.show();
                dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                try{
                    TextView dialog_title = dialog.findViewById(R.id.dialog_title);
                    dialog_title.setText("Trade "+searchResults.getString("name")+" shares");
                    TextView balance_text = dialog.findViewById(R.id.balance_text);
                    balance_text.setText("$"+total_balance+" available to buy "+ticker);


                }
                catch(JSONException e)
                {
                    e.printStackTrace();
                }

                Button buy_button = dialog.findViewById(R.id.buy_button);
                buy_button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Toast.makeText(v.getContext(),"Please enter a valid amount",Toast.LENGTH_SHORT).show();
                    }
                });
                Button sell = dialog.findViewById(R.id.sell_button);
                sell.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Toast.makeText(v.getContext(), "Please enter a valid amount", Toast.LENGTH_SHORT).show();
                    }
                });
                TextView tv = dialog.findViewById(R.id.per_share_calculator);
                tv.setText("X"+latest_price+"/share" + " = ");
                final EditText editText =(EditText) dialog.findViewById(R.id.transaction_editText);

                editText.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {

                        if(!TextUtils.isEmpty(editText.getText().toString()))
                        {


                            quantity = editText.getText().toString();

                            if(isNumeric(quantity))
                            {

                                double qty = Double.parseDouble(quantity);
                                TextView tv = dialog.findViewById(R.id.per_share_calculator);
                                DecimalFormat df = new DecimalFormat("###.####");
                                double calculation_per_share= qty*Double.parseDouble(latest_price);

                                TextView price = dialog.findViewById(R.id.per_stock_price);
                                quantity = TextUtils.isEmpty(editText.getText().toString())?"0":quantity;

                                TextView quantity_textview = dialog.findViewById(R.id.quantity_text);
                                quantity_textview.setText(editText.getText().toString());
                                tv.setText("X"+company_latest_price+"/share" + " = ");
                                if(editText.getText().toString().equals(""))
                                {
                                    quantity_textview.setText("0");
                                    price.setText("$0.0");
                                }
                                else
                                {
                                    price.setText("$"+String.valueOf(df.format(calculation_per_share)));
                                }





//                                TextView balance_text = dialog.findViewById(R.id.balance_text);
//                                balance_text.setText("$"+total_balance+" available to buy "+ticker);

                                Button buy_button = dialog.findViewById(R.id.buy_button);
                                buy_button.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {

                                        if(isNumeric(quantity))
                                        {
                                            if(Double.parseDouble(quantity)<=0) {

                                                Toast.makeText(v.getContext(),"Cannot buy less than 0 shares",Toast.LENGTH_SHORT).show();
                                            }

                                            else if(Double.parseDouble(total_balance)<calculation_per_share)
                                            {
                                                Toast.makeText(v.getContext(),"Not  enough  money  to  buy",Toast.LENGTH_SHORT).show();
                                            }
                                            else
                                            {
                                                //After successful buying of the stock
                                                Dialog success_dialog = new Dialog(v.getContext());
                                                success_dialog.setContentView(R.layout.success_buy_message);

                                                total_qty_multiple_buy += qty;
                                                Double update_balance = Double.parseDouble(total_balance)-calculation_per_share;
                                                total_balance = String.valueOf(df.format(update_balance));
                                                SharedPreferences total = v.getContext().getSharedPreferences(Constants.TOTAL_BALANCE,0);
                                                SharedPreferences.Editor edit = total.edit();
                                                edit.putString("total_balance",total_balance);
                                                edit.apply();
                                                SharedPreferences order = v.getContext().getSharedPreferences(Constants.ORDER,0);
                                                SharedPreferences.Editor editor = order.edit();
                                                if(order.getString("order",null)==null)
                                                {
                                                    editor.putString("order",ticker);
                                                }
                                                else
                                                {
                                                    String getticker = order.getString("order","");
                                                    if(!getticker.contains(ticker))
                                                    {
                                                        StringBuilder sb = new StringBuilder();
                                                        if(getticker.length()==0)
                                                        {
                                                            sb.append(ticker);
                                                        }
                                                        else
                                                        {
                                                            sb.append(getticker).append(",").append(ticker);
                                                        }

//

                                                        editor.putString("order",sb.toString());
                                                    }

                                                }

                                                editor.apply();
                                                TextView success_share_text = success_dialog.findViewById(R.id.success_share_text);
                                                success_share_text.setText("You have successfully bought "+quantity+" shares of "+ticker);


                                                DecimalFormat df = new DecimalFormat("###.####");
                                                double market_value = Double.parseDouble(market_value_calc) * Double.parseDouble(latest_price);
                                                market_value += calculation_per_share;
                                                if(market_value==0.0)
                                                {
                                                    info_portfolio_trading.setText("You have 0 shares of "+ ticker +"\n"+"\t\t"+" Start trading!");
                                                }
                                                else
                                                {
                                                    info_portfolio_trading.setText("Shares Owned: "+df.format(total_qty_multiple_buy)+"\n"+"Market Value: $"+df.format(market_value));
                                                }

                                                ZonedDateTime now = ZonedDateTime.now(zoneId);
                                                total_qty_multiple_buy = Math.round(total_qty_multiple_buy*10000.0)/10000.0;

                                                StockDetailForLocal sdfl = new StockDetailForLocal(ticker,latest_price,changeValue,company_name,String.valueOf(total_qty_multiple_buy),String.valueOf(now));


                                                addToPortfolio.putString(ticker,sdfl.toMyString());

//

                                                addToPortfolio.apply();

                                                SharedPreferences preffav = v.getContext().getSharedPreferences(Constants.FAVOURITES, 0);
                                                SharedPreferences.Editor addToFavourites = preffav.edit();
                                                if(preffav.getString(ticker,null)!=null)
                                                {
                                                    addToFavourites.putString(ticker,sdfl.toMyString());
                                                }
                                                addToFavourites.apply();
                                                dialog.dismiss();

                                                success_dialog.show();
                                                success_dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                                                success_dialog.findViewById(R.id.done).setOnClickListener(new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View v) {
                                                        success_dialog.dismiss();
                                                    }
                                                });
                                            }
                                        }
                                        else
                                        {
                                            Toast.makeText(v.getContext(),"Please enter valid amount",Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });

                                //SELLING LOGIC BEGINS HERE:
                                Button sell_button = dialog.findViewById(R.id.sell_button);
                                sell_button.setOnClickListener(v->{

                                    if(isNumeric(quantity)) {
                                        if (Float.parseFloat(quantity) <= 0) {

                                            Toast.makeText(v.getContext(), "Cannot sell less than 0 shares", Toast.LENGTH_SHORT).show();
                                        }

                                        else if(Double.parseDouble(quantity)>total_qty_multiple_buy)
                                        {
                                            Toast.makeText(v.getContext(),"Not  enough  shares  to  sell",Toast.LENGTH_SHORT).show();
                                        }

                                        else
                                        {
                                            Dialog success_dialog = new Dialog(v.getContext());
                                            success_dialog.setContentView(R.layout.success_sell_message);
                                            TextView success_share_text = success_dialog.findViewById(R.id.success_share_text);
                                            total_qty_multiple_buy -= qty;
                                            Double update_balance = Double.parseDouble(total_balance)+calculation_per_share;
                                            total_balance = String.valueOf(df.format(update_balance));
                                            SharedPreferences total = v.getContext().getSharedPreferences(Constants.TOTAL_BALANCE,0);
                                            SharedPreferences.Editor edit = total.edit();
                                            edit.putString("total_balance",total_balance);
                                            edit.apply();
                                            success_share_text.setText("You have successfully sold "+quantity+" shares of "+ticker);


//                                            DecimalFormat df = new DecimalFormat("###.####");
                                            double market_value = Double.parseDouble(market_value_calc) * Double.parseDouble(latest_price);
                                            market_value -= calculation_per_share;
                                            if(total_qty_multiple_buy==0.0)
                                            {
                                                info_portfolio_trading.setText("You have 0 shares of "+ticker+"\n"+"\t\t"+ " Start trading!");
                                            }
                                            else
                                            {
                                                info_portfolio_trading.setText("Shares Owned: "+df.format(total_qty_multiple_buy)+"\n"+"Market Value: $"+df.format(market_value));
                                            }

                                            ZonedDateTime now = ZonedDateTime.now(zoneId);
                                            total_qty_multiple_buy = Math.round(total_qty_multiple_buy*10000.0)/10000.0;
                                            StockDetailForLocal sdfl = new StockDetailForLocal(ticker,latest_price,changeValue,company_name,String.valueOf(total_qty_multiple_buy),String.valueOf(now));
                                            if(pref.getString(ticker,null)==null){

                                                Toast.makeText(v.getContext(),"Not enough shares to sell",Toast.LENGTH_SHORT).show();
//                                                addToPortfolio.putString(ticker,sdfl.toMyString());

                                            }
                                            else if(total_qty_multiple_buy==0.0)
                                            {
                                                addToPortfolio.remove(ticker);
                                                SharedPreferences order = v.getContext().getSharedPreferences(Constants.ORDER,0);
                                                SharedPreferences.Editor editor = order.edit();
                                                String getticker = order.getString("order","");

                                                if(getticker.startsWith(ticker))
                                                {
                                                    StringBuilder sb = new StringBuilder();
                                                    sb.append(getticker);
                                                    sb.delete(0,ticker.length()+1);
                                                    editor.putString("order",sb.toString());
                                                }
                                                else
                                                {
                                                    StringBuilder sb = new StringBuilder();
                                                    sb.append(getticker);
                                                    int first = sb.indexOf(ticker);
                                                    sb.delete(first-1,first+ticker.length());
                                                    editor.putString("order",sb.toString());
                                                }
                                                editor.apply();

                                            }
                                            else
                                            {
                                                addToPortfolio.putString(ticker,sdfl.toMyString());
                                            }

                                            addToPortfolio.apply();
                                            SharedPreferences preffav = v.getContext().getSharedPreferences(Constants.FAVOURITES, 0);
                                            SharedPreferences.Editor addToFavourites = preffav.edit();
                                            if(preffav.getString(ticker,null)!=null)
                                            {
                                                addToFavourites.putString(ticker,sdfl.toMyString());
                                            }
                                            addToFavourites.apply();
                                            dialog.dismiss();

                                            success_dialog.show();
                                            success_dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                                            success_dialog.findViewById(R.id.done).setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    success_dialog.dismiss();
                                                }
                                            });

                                        }
                                    }
                                    else
                                    {
                                        Toast.makeText(v.getContext(),"Please enter valid amount",Toast.LENGTH_SHORT).show();
                                    }

                                });
                            }

                        }
                        else
                        {
                            TextView quantity_textview = dialog.findViewById(R.id.quantity_text);
                            quantity_textview.setText(String.valueOf(editText.getText().toString()));
                            Button buy_button = dialog.findViewById(R.id.buy_button);
                            buy_button.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Toast.makeText(v.getContext(),"Please enter valid amount",Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }
                    int count = -1;
                    @Override
                    public void afterTextChanged(Editable arg0) {
                        if(arg0.length()==0)
                        {
                            TextView pricetext = dialog.findViewById(R.id.per_stock_price);
                            pricetext.setText("$0.0");
                            TextView quantity_text = dialog.findViewById(R.id.quantity_text);
                            quantity_text.setText("0");
                        }
                        if (arg0.length() > 0) {
                            String str = editText.getText().toString();
                            editText.setOnKeyListener(new View.OnKeyListener() {
                                public boolean onKey(View v, int keyCode, KeyEvent event) {
                                    if (keyCode == KeyEvent.KEYCODE_DEL) {
                                        count--;
                                        InputFilter[] fArray = new InputFilter[1];
                                        fArray[0] = new InputFilter.LengthFilter(100);
                                        editText.setFilters(fArray);
                                        //change the edittext's maximum length to 100.
                                        //If we didn't change this the edittext's maximum length will
                                        //be number of digits we previously entered.
                                    }
                                    return false;
                                }
                            });
                            char t = str.charAt(arg0.length() - 1);
                            if (t == '.') {
                                count = 0;
                            }
                            if (count >= 0) {
                                if (count == 4) {
                                    InputFilter[] fArray = new InputFilter[1];
                                    fArray[0] = new InputFilter.LengthFilter(arg0.length());
                                    editText.setFilters(fArray);
                                    //prevent the edittext from accessing digits
                                    //by setting maximum length as total number of digits we typed till now.
                                }
                                count++;
                            }
                        }


                    }
                });




            });





    }
    private void getData(final String query) {
        String url = Constants.STOCK_DETAILS_ENDPOINT + query;
        RequestQueue que = Volley.newRequestQueue(getBaseContext());
        JsonObjectRequest jsonRequest = new JsonObjectRequest(Request.Method.GET, url,
                null, response -> {
            try {
//                Log.d("JSON:",response.toString());
                searchResults = response.getJSONObject("data");
//                Log.d("SearchStocksDescription",searchResults+"");
                dataReceived = true;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }, error -> Log.e(TAG, error.toString()));
        que.add(jsonRequest);

        //Second request to request for the company latest_price_data:

        url = Constants.LATEST_PRICE_ENDPOINT + query;
        jsonRequest = new JsonObjectRequest(Request.Method.GET, url,
                null, response -> {
            try {
                latestPriceResults = (JSONObject) response.getJSONArray("data").get(0);
                latestPriceDataReceived = true;
                Log.d(TAG, latestPriceResults + "");

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }, error -> Log.e(TAG, error.toString()));
        que.add(jsonRequest);

         url = Constants.NEWS_API_ENDPOINT + query;
        que = Volley.newRequestQueue(getBaseContext());
        jsonRequest = new JsonObjectRequest(Request.Method.GET, url,
                null, response -> {
            try {

                newsArticles = response.getJSONObject("data")
                        .getJSONArray("articles");
                Log.d(TAG,newsArticles+"");
                newsDataReceived=true;

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }, error -> Log.e(TAG, error.toString()));
        que.add(jsonRequest);

//        if(dataReceived && latestPriceDataReceived && newsDataReceived)
//            loading=true;


    }




    private void displayStockDetails(String ticker)
    {
        try {


            TextView ticker_name = findViewById((R.id.detail_title));
            ticker_name.setText(ticker);
            TextView name_ticker_detail = findViewById((R.id.name_ticker_detail));
            if(searchResults!=null)
            {
                name_ticker_detail.setText(searchResults.getString("name"));
                company_name = searchResults.getString("name");
                TextView about_description = findViewById((R.id.expandable_description));
                about_description.setText(searchResults.getString("description"));
                if (about_description.getLineCount() > 2) {
                    makeTextViewResizable(about_description, 2, "Show More...", true);
                }
            }



            //Get the latest price data
            if(latestPriceResults!=null)
            {
                TextView current_price = findViewById(R.id.current_price);
                //Setting this for local Storage.
                double latest = Double.parseDouble(latestPriceResults.getString("last"));
                company_latest_price = "$"+latestPriceResults.getString("last");
                DecimalFormat df = new DecimalFormat("###.##");
                latest_price = latestPriceResults.getString("last");

                current_price.setText("$"+String.valueOf(df.format(latest)));



                double change = Double.parseDouble(latestPriceResults.getString("last")) - Double.parseDouble(latestPriceResults.getString("prevClose"));
                changeValue = String.valueOf(df.format(change));
                TextView change_value = findViewById(R.id.change_value);
                change_value.setText("$" + String.valueOf(df.format(change)));
                if (change < 0.0) {
                    change_value.setTextColor(Color.parseColor("#af6a72"));
                    change_value.setText("-"+"$"+df.format(Math.abs(change)));
                } else if (change > 0.0) {
                    change_value.setTextColor(Color.parseColor("#73b78f"));
                }
                else
                {
                    change_value.setTextColor(Color.GRAY);
                }
                getPerStockPriceDetails();
            }
            if(newsArticles!=null)
            {

                getHomePageNews();
                generateFirstNewscard();
            }

        }
        catch(JSONException e)
        {
            e.printStackTrace();
        }
    }



    private void generateFirstNewscard()  {
        try{
            JSONObject article = newsArticles.getJSONObject(0);
            View v = findViewById(R.id.firstnews_card);
            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        Uri uri = Uri.parse(article.getString("url")); // missing 'http://' will cause crashed
                        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                        startActivity(intent);
                    }
                    catch(JSONException e){}

                }


            });
            v.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    try {
                        Dialog dialog = new Dialog(v.getContext());
                        dialog.setContentView(R.layout.dialog);
                        ImageView gcIcon = dialog.findViewById(R.id.dialog_google_chrome_icon);
                        gcIcon.setImageResource(R.drawable.ic_chrome);
                        ImageView twitter = dialog.findViewById(R.id.twitter);
                        twitter.setImageResource(R.drawable.ic_twitter_logo_dialog);
                        ImageView img = dialog.findViewById(R.id.long_press_image);
                        TextView dialogText = dialog.findViewById(R.id.dialog_text);
                        dialogText.setText(article.getString("title"));
                        twitter.setOnClickListener(view -> {
                            try {
                                String shareText = Constants.SHARE_TWITTER_TEXT
                                        .replace("__link__", article.getString("url"));
                                Intent twitterIntent = new Intent("android.intent.action.VIEW",
                                        Uri.parse(Constants.TWITTER_INTENT.replace("__shareText__", shareText)));
                                startActivity(twitterIntent);
                            }
                            catch(JSONException e){}

                        });
                        gcIcon.setOnClickListener(view-> {
                            try {
                                Uri uri = Uri.parse(article.getString("url")); // missing 'http://' will cause crashed
                                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                                startActivity(intent);
                            }
                            catch (JSONException e){}

                        });
                        Picasso.with(v.getContext()).load(article.getString("urlToImage")).into(img);
//        Picasso.with(this).load(R.drawable.ic_twitter_logo).into(twitter);
                        dialog.show();

                    }
                    catch(JSONException e){}

                    return false;
                }
            });
        }
        catch(JSONException e){}




        findViewById(R.id.firstnews_card).setVisibility(VISIBLE);
        ImageView firstNewsCardImage = findViewById(R.id.news_card_image);
        TextView news_card_section = findViewById(R.id.news_card_section);
        TextView news_card_date = findViewById(R.id.news_card_date);
        TextView news_card_title = findViewById(R.id.news_card_title);

        try {
            JSONObject article = newsArticles.getJSONObject(0);
            String thumbnail;
            try {
                thumbnail = article.getString("urlToImage");
            } catch (Exception e){
                thumbnail = String.valueOf(R.drawable.no_image);
            }

            Picasso.with(this).load(thumbnail).transform(new RoundedCornersTransformation(30,0)).into(firstNewsCardImage);
            String title = article.getString("title");
            news_card_title.setText(title);
            ZonedDateTime publicationDate = ZonedDateTime.parse(
                    article.getString("publishedAt"))
                    .withZoneSameLocal(ZoneId.of(Constants.ZONEID_GMT));

            news_card_date.setText(getDifference(publicationDate));
            news_card_section.setText(article.getJSONObject("source").getString("name"));
            String webUrl = article.getString("url");
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    private void getHomePageNews() {
        RecyclerView rv = findViewById(R.id.news_recycler_view);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        rv.setLayoutManager(llm);
        articles = new ArrayList<>();
        for(int i = 1; i < newsArticles.length(); i ++){
            try {
                JSONObject article = newsArticles.getJSONObject(i);
                String title = article.getString("title");
                String section = article.getJSONObject("source").getString("name");
                String thumbnail = "";
//                String thumbnail = article.getString("urlToImage");
                if(article.getString("urlToImage")=="null")
                {

                    thumbnail = getURLForResource(R.drawable.no_image);;
                    Log.d("Dennis","Kasa kay mandali?");
                }
                else
                {
                    try {
                        thumbnail = article.getString("urlToImage");
                    } catch (JSONException e){

                    }
                }


//                String thumbnail= article.getString("urlToImage")!=null? article.getString("urlToImage"):String.valueOf(R.drawable.no_image);
//                if(thumbnail == null)
//                {
//                    Log.d("Dennis","I am entering news ");
//                    thumbnail = String.valueOf(R.drawable.no_image);
//                }

//                if(article.getString("urlToImage") != null)
//                {
//                    thumbnail = article.getString("urlToImage");
//
//                }
//                else
//                {
//
//                    thumbnail =  String.valueOf(R.drawable.no_image);
//                }
//                try {
//                    thumbnail = article.getString("urlToImage");
//                } catch (Exception e){
//
//                    if(article.getString("urlToImage").equals(null))
//                    {
//
//                    }
//
//                }
                ZonedDateTime publicationDate = ZonedDateTime.parse(
                        article.getString("publishedAt"))
                        .withZoneSameLocal(ZoneId.of(Constants.ZONEID_GMT));
                String webUrl = article.getString("url");
                articles.add(new NewsArticle(title,section, thumbnail,
                        publicationDate, webUrl));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        adapter = new NewsArticlesAdapter(articles, this);
        rv.setAdapter(adapter);
        adapter.setClickListener(this);
    }


    private void getPerStockPriceDetails()  {
        GridView gridView = (GridView)findViewById(R.id.simpleGridView);

        stockPrices = new ArrayList<>();


//        String stockprice[] = new String[7];
        try{
//            BigDecimal bd = new BigDecimal(latestPriceResults.getString("last")).setScale(2, RoundingMode.HALF_UP);
//            double curr = bd.doubleValue();
            String current_Price=latestPriceResults.getString("last").equals("null")?"Current Price:0.0":"Current Price: "+"\n" +Math.round(Double.parseDouble(latestPriceResults.getString("last"))*100.0)/100.0;
            String low=latestPriceResults.getString("low").equals("null")?"Low: 0.0":"Low: "+Math.round(Double.parseDouble(latestPriceResults.getString("low"))*100.0)/100.0;
            String bid_price=latestPriceResults.getString("bidPrice").equals("null")?"Bid Price: 0.0":"Bid Price: "+Math.round(Double.parseDouble(latestPriceResults.getString("bidPrice"))*100.0)/100.0;
            String Open = latestPriceResults.getString("open").equals("null")?"OpenPrice: 0.0":"OpenPrice:"+ Math.round(Double.parseDouble(latestPriceResults.getString("open"))*100.0)/100.0;
            String mid = latestPriceResults.getString("mid").equals("null") ? "Mid: 0.0":"Mid:"+Math.round(Double.parseDouble(latestPriceResults.getString("mid"))*100.0)/100.0;
            String high = latestPriceResults.getString("high").equals("null")? "High: 0.0":"High: "+Math.round(Double.parseDouble(latestPriceResults.getString("high"))*100.0)/100.0;
            String volume = latestPriceResults.getString("volume").equals("null") ? "Volume: 0.0":"Volume: "+Math.round(Double.parseDouble(latestPriceResults.getString("volume"))*100.0)/100.0;

//            stockprice[0] = latestPriceResults.getString("last").equals("null")?"Current Price:0.0":"Current Price:"+latestPriceResults.getString("last");
//            stockprice[1] = latestPriceResults.getString("low").equals("null")?"Low : 0.0":"Low:"+latestPriceResults.getString("low");
//            stockprice[2] = latestPriceResults.getString("bidPrice").equals("null")?"Bid Price: 0.0":"Bid Price:"+latestPriceResults.getString("bidPrice");
//            stockprice[3] = latestPriceResults.getString("open").equals("null")?"OpenPrice: 0.0":"OpenPrice:"+ latestPriceResults.getString("open");
//            stockprice[4] = latestPriceResults.getString("mid").equals("null") ? "Mid: 0.0":"Mid:"+latestPriceResults.getString("mid");
//            stockprice[5] = latestPriceResults.getString("high").equals("null")? "High: 0.0":"High:"+latestPriceResults.getString("high");
//            stockprice[6] = latestPriceResults.getString("volume").equals("null") ? "Volume: 0.0":"Volume:"+latestPriceResults.getString("volume");
            stockPrices.add(new StockPrice(current_Price,low,bid_price,Open,mid,high,volume));
        }

        catch(JSONException e)
        {
            e.printStackTrace();
        }
//        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
//                android.R.layout.simple_list_item_1, stockprice);
        StockPriceAdapter stockpriceAdapter = new StockPriceAdapter(stockPrices,this);
        gridView.setAdapter(stockpriceAdapter);
    }


    private String getDifference(final ZonedDateTime articleTimestamp) {
        ZoneId zoneId = ZoneId.of("GMT");
        ZonedDateTime articleTimeZoneAtPT = articleTimestamp.withZoneSameLocal(zoneId);
        ZonedDateTime now = ZonedDateTime.now((zoneId));
        long timeDifference = zonedDateTimeDifference(articleTimeZoneAtPT, now, ChronoUnit.SECONDS);
        if(timeDifference / Constants.TO_DAYS > 0) {
            return timeDifference/ Constants.TO_DAYS +" days ago";
        } else if(timeDifference / Constants.TO_HOURS > 0){
            return timeDifference / Constants.TO_HOURS +" hours ago";
        } else if(timeDifference / Constants.TO_MINUTES > 0){
            return timeDifference / Constants.TO_MINUTES +" minutes ago";
        } else {
            return timeDifference+" seconds ago";
        }
    }

    private static long zonedDateTimeDifference(final ZonedDateTime d1,
                                                final ZonedDateTime d2,
                                                final ChronoUnit unit) {
        return unit.between(d1, d2);
    }







    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        SharedPreferences pref = this.getSharedPreferences(Constants.FAVOURITES, 0);
        getMenuInflater().inflate(R.menu.menu_favourite, menu);
        menu.findItem(R.id.favourite_titlebar).setIcon(pref.getString(ticker, null) == null?
                R.drawable.ic_baseline_star_border_24:R.drawable.ic_baseline_star_24);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
//                homePageAdapter.notifyDataSetChanged();
                break;
            case R.id.favourite_titlebar:
                SharedPreferences pref = this.getSharedPreferences(Constants.FAVOURITES, 0);
                SharedPreferences.Editor addToFavourites = pref.edit();
                SharedPreferences port = this.getSharedPreferences(Constants.PORTFOLIO,0);
                SharedPreferences fav_order = this.getSharedPreferences(Constants.FORDER,0);
                SharedPreferences.Editor fedit = fav_order.edit();
                try{
                    current_stock = StockDetailForLocal.toStock(port.getString(ticker," "));
                }
                catch(JSONException e) {e.printStackTrace();}
                ZonedDateTime now = ZonedDateTime.now(zoneId);

                StockDetailForLocal sdfl = current_stock==null?new StockDetailForLocal(ticker,latest_price,changeValue,company_name,quantity,String.valueOf(now)): new StockDetailForLocal(ticker,latest_price,changeValue,company_name,current_stock.quantity,String.valueOf(now));
                if(pref.getString(ticker,null)==null){
                    item.setIcon(R.drawable.ic_baseline_star_24);
                    addToFavourites.putString(ticker,sdfl.toMyString());
                    if(fav_order.getString("forder",null)==null)
                    {
                        fedit.putString("forder",ticker);
                    }
                    else
                    {
                        String getticker = fav_order.getString("forder","");
                        if(!getticker.contains(ticker))
                        {
                            StringBuilder sb = new StringBuilder();
                            if(getticker.length()==0)
                            {
                                sb.append(ticker);
                            }
                            else
                            {
                                sb.append(getticker).append(",").append(ticker);
                            }

//

                            fedit.putString("forder",sb.toString());
                        }

                    }
                    fedit.apply();
                    Toast.makeText(this,
                            "\""+ticker+"\"" +" was added to favourites", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    item.setIcon(R.drawable.ic_baseline_star_border_24);
                    addToFavourites.remove(ticker);

                    String getticker = fav_order.getString("forder","");

                    if(getticker.startsWith(ticker))
                    {
                        StringBuilder sb = new StringBuilder();
                        sb.append(getticker);
                        sb.delete(0,ticker.length()+1);
                        fedit.putString("forder",sb.toString());
                    }
                    else
                    {
                        StringBuilder sb = new StringBuilder();
                        sb.append(getticker);
                        int first = sb.indexOf(ticker);
                        sb.delete(first-1,first+ticker.length());
                        fedit.putString("forder",sb.toString());
                    }
                    fedit.apply();
                    Toast.makeText(this,
                            "\""+ticker+"\""+" was removed from favourites", Toast.LENGTH_SHORT).show();
                }
                addToFavourites.apply();
//                homePageAdapter.notifyDataSetChanged();
                break;

        }
        return super.onOptionsItemSelected(item);
    }







    public static void makeTextViewResizable(final TextView tv,
                                             final int maxLine, final String expandText, final boolean viewMore) {

        if (tv.getTag() == null) {
            tv.setTag(tv.getText());
        }
        ViewTreeObserver vto = tv.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {

            @SuppressWarnings("deprecation")
            @Override
            public void onGlobalLayout() {

                ViewTreeObserver obs = tv.getViewTreeObserver();
                obs.removeGlobalOnLayoutListener(this);
                if (maxLine == 0) {
                    int lineEndIndex = tv.getLayout().getLineEnd(0);
                    String text = tv.getText().subSequence(0,
                            lineEndIndex)
                            + " " + "\n" + "\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\tt\t\t\t\t\t\t\t\t\t\t" + expandText;
                    tv.setText(text);
                    tv.setMovementMethod(LinkMovementMethod.getInstance());
                    tv.setText(
                            addClickablePartTextViewResizable(tv.getText()
                                            .toString(), tv, maxLine, expandText,
                                    viewMore), TextView.BufferType.SPANNABLE);
                } else if (maxLine > 0 && tv.getLineCount() >= maxLine) {
                    int lineEndIndex = tv.getLayout().getLineEnd(maxLine-1);
                    String text = tv.getText().subSequence(0,
                            lineEndIndex)
                            + "... " + "\n" + "\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t" + expandText;
                    tv.setText(text);
                    tv.setMovementMethod(LinkMovementMethod.getInstance());
                    tv.setText(
                            addClickablePartTextViewResizable(tv.getText()
                                            .toString(), tv, maxLine, expandText,
                                    viewMore), TextView.BufferType.SPANNABLE);
                } else {
                    int lineEndIndex = tv.getLayout().getLineEnd(
                            tv.getLayout().getLineCount() - 1);
                    String text = tv.getText().subSequence(0, lineEndIndex)
                            + " " + "\n" + "\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t" + expandText;
                    tv.setText(text);
                    tv.setMovementMethod(LinkMovementMethod.getInstance());
                    tv.setText(
                            addClickablePartTextViewResizable(tv.getText()
                                            .toString(), tv, lineEndIndex, expandText,
                                    viewMore), TextView.BufferType.SPANNABLE);
                }
            }
        });

    }

    private static SpannableStringBuilder addClickablePartTextViewResizable(
            final String strSpanned, final TextView tv, final int maxLine,
            final String spannableText, final boolean viewMore) {
        SpannableStringBuilder ssb = new SpannableStringBuilder(strSpanned);

        if (strSpanned.contains(spannableText)) {
            ssb.setSpan(
                    new MySpannable(false) {

                        @Override
                        public void onClick(View widget) {

                            if (viewMore) {
                                tv.setLayoutParams(tv.getLayoutParams());
                                tv.setText(tv.getTag().toString(),
                                        TextView.BufferType.SPANNABLE);
                                tv.invalidate();
                                makeTextViewResizable(tv, -1, "Show Less",
                                        false);
                                tv.setTextColor(Color.BLACK);
                            } else {
                                tv.setLayoutParams(tv.getLayoutParams());
                                tv.setText(tv.getTag().toString(),
                                        TextView.BufferType.SPANNABLE);
                                tv.invalidate();
                                makeTextViewResizable(tv, 2, "Show More...",
                                        true);
                                tv.setTextColor(Color.BLACK);
                            }

                        }
                    }, strSpanned.indexOf(spannableText),
                    strSpanned.indexOf(spannableText) + spannableText.length(), 0);

        }
        return ssb;

    }

    @Override
    public void onClick(View view, int position) {
        Uri uri = Uri.parse(articles.get(position).webUrl); // missing 'http://' will cause crashed
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        startActivity(intent);
    }

    @Override
    public void onLongClick(View view, int position) {

        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog);
        ImageView gcIcon = dialog.findViewById(R.id.dialog_google_chrome_icon);
        gcIcon.setImageResource(R.drawable.ic_chrome);
        ImageView twitter = dialog.findViewById(R.id.twitter);
        twitter.setImageResource(R.drawable.ic_twitter_logo_dialog);
        ImageView img = dialog.findViewById(R.id.long_press_image);
        TextView dialogText = dialog.findViewById(R.id.dialog_text);
        dialogText.setText(articles.get(position).title);
        twitter.setOnClickListener(v -> {
            String shareText = Constants.SHARE_TWITTER_TEXT
                    .replace("__link__", articles.get(position).webUrl);
            Intent twitterIntent = new Intent("android.intent.action.VIEW",
                    Uri.parse(Constants.TWITTER_INTENT.replace("__shareText__", shareText)));
            startActivity(twitterIntent);
        });
        gcIcon.setOnClickListener(v-> {
            Uri uri = Uri.parse(articles.get(position).webUrl); // missing 'http://' will cause crashed
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            startActivity(intent);
        });
        Picasso.with(this).load(articles.get(position).thumbnail).into(img);
//        Picasso.with(this).load(R.drawable.ic_twitter_logo).into(twitter);
        dialog.show();

    }

    public static boolean isNumeric(String str) {
        return str.matches("-?\\d+(\\.\\d+)?");  //match a number with optional '-' and decimal.
    }

    public String getURLForResource (int resourceId) {
        //use BuildConfig.APPLICATION_ID instead of R.class.getPackage().getName() if both are not same
        return Uri.parse("android.resource://"+R.class.getPackage().getName()+"/" +resourceId).toString();
    }


}