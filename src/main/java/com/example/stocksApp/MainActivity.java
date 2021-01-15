package com.example.stocksApp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.stocksApp.adapter.HomePageAdapter;
import com.example.stocksApp.adapter.ItemMoveCallback;
import com.example.stocksApp.adapter.SwipeToDeleteCallback;
import com.example.stocksApp.interfaces.ClickListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.TreeMap;
import java.util.stream.Collectors;

import static android.view.View.GONE;


public class MainActivity extends AppCompatActivity implements ClickListener {
    private JSONArray suggestionsData,latest_test_portfolio,latest_test_favourites;

    private JSONObject latestPriceResults;
    public static final String TAG = "MainActivity",BOOKMARK = "bookmarks", KEYWORD = "query";
    boolean dataReceived = false,favouriteData = false,isVisible = false,toggle = false,spinner_flag=false;
    private HomePageAdapter adapter,portfolio_adapter;

    private List<StockDetailForLocal> favourites;
    private List<StockDetailForLocal> portfolio;
    String[] suggested = new String[5];
    List<String> PortfoliotickersinLocal = new ArrayList<>();
    List<String> FavouritetickersinLocal = new ArrayList<>();
    ProgressBar spinner;
    TextView fetching_text;
    int count =0;
    Handler handler = new Handler();
    Handler handlers = new Handler();
    Runnable runnable,r_spinner;
    double net_worth=0.0;
    int delay = 15*1000;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        count =0 ;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar myToolbar = findViewById(R.id.toolbar_search);
        setSupportActionBar(myToolbar);
//        ProgressBar spinner;
        spinner =  findViewById(R.id.progressBar1);
        spinner.setVisibility(View.VISIBLE);
        fetching_text = findViewById(R.id.fetching_text);
        fetching_text.setVisibility(View.VISIBLE);
        TextView current_date = findViewById(R.id.current_date);
        SimpleDateFormat dateTimeInPST = new SimpleDateFormat("MMMM dd, yyyy");
        dateTimeInPST.setTimeZone(TimeZone.getTimeZone("America/Los_Angeles"));
        String str = String.valueOf(dateTimeInPST.format(new Date()));


        dataReceived = false;
        favouriteData = false;
        spinner_flag = true;
        SharedPreferences total = this.getSharedPreferences(Constants.TOTAL_BALANCE, 0);
        SharedPreferences.Editor editor = total.edit();
        if(total.getString("total_balance",null)==null)
        {
            editor.putString("total_balance","20000");
        }
        editor.apply();
        Log.d("TOTAL BALANCE", total.getString("total_balance",""));


        favourites = new ArrayList<>();
        SharedPreferences pref = this.getSharedPreferences(Constants.FAVOURITES, 0);

        SharedPreferences forder = this.getSharedPreferences(Constants.FORDER, 0);
        SharedPreferences.Editor fedit = forder.edit();
        String s = forder.getString("forder","");
        String[] arr_fav = s.split(",");
        Map<String,?> keys = pref.getAll();
        for(String a: arr_fav)
        {
            try {
                if(keys.get(a)!=null)
                {
                    StockDetailForLocal stock = StockDetailForLocal.toStock(keys.get(a).toString());
                    favourites.add(stock);
                    FavouritetickersinLocal.add(a);
                }

            }
            catch(JSONException e) {e.printStackTrace();}

        }

//        for(Map.Entry<String,?> entry : keys.entrySet()) {
//            try {
//
//
//                FavouritetickersinLocal.add(entry.getKey());
//                favourites.add(StockDetailForLocal.toStock(entry.getValue().toString()));
//            } catch (JSONException e) {
//                e.printStackTrace();
//            }
//        }

//        if (favourites.size() == 0) {
//            this.findViewById(R.id.no_favourites).setVisibility(View.VISIBLE);
//        }

        portfolio = new ArrayList<>();
        SharedPreferences port = this.getSharedPreferences(Constants.PORTFOLIO, 0);
        Map<String,?> portFolioMap = port.getAll();
        Log.d("TAG"+"PORTFOLIO MAP",String.valueOf(portFolioMap.size()));
//        for(Map.Entry<String,?> entry : portFolioMap.entrySet()) {
//            try {
//                keytoCallAPI = entry.getKey();
////                updatedData.getString("last");
//                Log.d(TAG+"Checking API data",entry.getValue().toString());
//
//                StockDetailForLocal stock = StockDetailForLocal.toStock(entry.getValue().toString());
//                order.put(entry.getKey(), ZonedDateTime.parse(stock.date));
//                net_worth += Double.parseDouble(stock.getLatestPrice())*Double.parseDouble(stock.getQuantity());
//
//                portfolio.add(StockDetailForLocal.toStock(entry.getValue().toString()));
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//
//        }
        SharedPreferences order = this.getSharedPreferences(Constants.ORDER, 0);
        SharedPreferences.Editor edit = order.edit();
        String s1 = order.getString("order","");
        String[] arr = s1.split(",");
        for(String a: arr)
        {
            try {
                if(portFolioMap.get(a)!=null)
                {
                    StockDetailForLocal stock = StockDetailForLocal.toStock(portFolioMap.get(a).toString());
                    net_worth = net_worth + ((Double.parseDouble(stock.quantity) * Double.parseDouble(stock.latestPrice)));
                    portfolio.add(stock);
                    PortfoliotickersinLocal.add(a);
                }

            }
            catch(JSONException e) {e.printStackTrace();}

        }



        TextView net = (TextView)findViewById(R.id.net_worth_val);
        net_worth+=Double.parseDouble(total.getString("total_balance","0"));
        DecimalFormat df = new DecimalFormat("###.##");
//        net.setText("Net_worth"+"\n" +String.valueOf(df.format(net_worth)));

        Log.d(TAG+"All the tickers:",PortfoliotickersinLocal.toString());
        if (portfolio.size() == 0) {


//            TextView net_worth = findViewById(R.id.net_worth);
//            net_worth.setText("Net Worth:" +"\n" +20000);
//            "\n \n \n \t\t\t\t\t\t\t\t\t\t\t\t" +"No stocks in portfolio section");
        }
//        Log.d("TAG"+"ORDERRRR",String.valueOf(order.size()));
//        for(Map.Entry<String,ZonedDateTime> entry: order.entrySet())
//        {
//
//            Log.d("Order:"+entry.getKey(),String.valueOf(entry.getValue()));
//
////            Log.d("Date:"+"dateeeee",entry.getValue()+"");
//        }
//        final Handler handler = new Handler();
//        Runnable proceedsRunnable = new Runnable() {
//            @Override
//            public void run() {
//
//                if(dataReceived)
//                {
//                    spinner.setVisibility(GONE);
//                    txt.setVisibility(GONE);
////                    for(Map.Entry<String,?> entry : portFolioMap.entrySet()) {
////                        try {
////                            String key = entry.getKey();
////                            JSONObject updatedData = getLatestPriceForStocks(key);
////                            Log.d(TAG+"Check"+updatedData.getString("ticker"), updatedData.getString("last"));
////                        } catch (JSONException e) {
////                            e.printStackTrace();
////                        }
////                    }
//                    getLatestPriceForStocks(tickersinLocal);
//
//                }
//
////                    findViewById(R.id.scroll_view).setVisibility(View.VISIBLE);
//                    //Basically here the code will come to fetch the data for portfolio and favourite sections.
//                else
//                {
//                    handler.postDelayed(this, 15000);
//                }
//
//
//
//            }
//        };
//        handler.post(proceedsRunnable);





//        SharedPreferences pref1 = this.getSharedPreferences(Constants.PORTFOLIO,0);
//        Log.d(TAG+"Main Portfolio",pref1.getString("AAPL",""));
//        getLatestPriceForStocks(FavouritetickersinLocal,PortfoliotickersinLocal);

        handlers.postDelayed(r_spinner = new Runnable() {
            @Override
            public void run() {
                count++;
                if(count==10)
                {
                    spinner.setVisibility(GONE);
                    fetching_text.setVisibility(GONE);
                    findViewById(R.id.scroll_view).setVisibility(View.VISIBLE);
                    current_date.setText(String.valueOf(str));
                    setUpRecyclerView();
                    net.setText(String.valueOf(df.format(net_worth)));
                    TextView footer_text= (TextView) findViewById(R.id.footer_text); //txt is object of TextView
                    footer_text.setMovementMethod(LinkMovementMethod.getInstance());
                    footer_text.setOnClickListener(new View.OnClickListener() {
                        public void onClick(View v) {
                            Intent browserIntent = new Intent(Intent.ACTION_VIEW);
                            browserIntent.setData(Uri.parse("https://www.tiingo.com/"));
                            startActivity(browserIntent);
                        }
                    });
                }
                else
                    handlers.postDelayed(r_spinner,100);


            }
        },100);
    }



    private void setUpRecyclerView() {
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.sectioned_recycler_view);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        DividerItemDecoration divide1 = new DividerItemDecoration(this, linearLayoutManager.getOrientation());
        recyclerView.addItemDecoration(divide1);
        recyclerView.setLayoutManager(linearLayoutManager);

        RecyclerView recyclerView2 = (RecyclerView) findViewById((R.id.sectioned_recycler_view2));
        LinearLayoutManager linearLayoutManager1 = new LinearLayoutManager(this);
        DividerItemDecoration divide = new DividerItemDecoration(this, linearLayoutManager1.getOrientation());
        recyclerView2.addItemDecoration(divide);
        recyclerView2.setLayoutManager(linearLayoutManager1);

        // Fetch the data from local Storage and then populate in the corresponding recycler view.

        portfolio_adapter = new HomePageAdapter(portfolio,this,true);
        ItemTouchHelper.Callback callback =
                new ItemMoveCallback(portfolio_adapter);
        ItemTouchHelper touchHelper = new ItemTouchHelper(callback);
        touchHelper.attachToRecyclerView(recyclerView);
        recyclerView.setAdapter(portfolio_adapter);
        portfolio_adapter.notifyDataSetChanged();
        portfolio_adapter.setClickListener(this);

        adapter = new HomePageAdapter(favourites,this,false);
        ItemTouchHelper.Callback callbackForFav =
                new ItemMoveCallback(adapter);
        ItemTouchHelper touchHelperforFav = new ItemTouchHelper(callbackForFav);
        touchHelperforFav.attachToRecyclerView(recyclerView2);
        recyclerView2.setAdapter(adapter);
        adapter.notifyDataSetChanged();
        adapter.setClickListener(this);



        enableSwipeToDeleteAndUndo(recyclerView2);
    }


    @SuppressLint("RestrictedApi")
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.search_menu, menu);
        SearchManager searchManager = (SearchManager)
                getSystemService(Context.SEARCH_SERVICE);
        MenuItem searchMenuItem = menu.findItem(R.id.search);
        SearchView searchView = (SearchView) searchMenuItem.getActionView();

        final SearchView.SearchAutoComplete suggestions =
                searchView.findViewById(R.id.search_src_text);
        suggestions.setThreshold(1);
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        suggestions.setOnItemClickListener((parent, view, position, id) -> {
            String queryString = (String)parent.getItemAtPosition(position);
            searchView.setQuery(queryString, false);
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            @Override
            public boolean onQueryTextSubmit(String query) {
                Intent stockIntent = new Intent(searchView.getContext(), StockDetails.class);
                if(query.length()>0)
                {
                    int querytill = query.indexOf(" ");
                    if(querytill!=-1)
                    {
                        query = query.substring(0,querytill);
                        stockIntent.putExtra("query", query);
                        startActivity(stockIntent);
                    }

                }

                return true;
            }
            @Override
            public boolean onQueryTextChange(String newText) {
                if(newText.length() < 3)
                    return false;
                // TODO - test & add during final demo
                getSuggestionList(newText, suggestions);
                return true;
            }
        });
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                finish();
                break;
        }
        return true;
    }

    private void getSuggestionList(String query, SearchView.SearchAutoComplete suggestions) {
        String url = Constants.AUTO_COMPLETE_API_ENDPOINT + query;
        RequestQueue que = Volley.newRequestQueue(getBaseContext());

        JsonObjectRequest jsonRequest = new JsonObjectRequest(Request.Method.GET, url,
                null, response -> {
            try {
//                Log.d(TAG,response+"");
//                String[] suggested = new String[5];
                suggestionsData = response.getJSONArray("data");
                List<String> suggest = new ArrayList<>();
//                Log.d("val",suggestionsData+"");
                for(int i=0; i < Math.min(suggestionsData.length(),5);i++){
                    try {
                        if(suggestionsData.getJSONObject(i)!=null)
                        {
                            suggest.add(suggestionsData.getJSONObject(i)
                                    .getString("ticker") + " - " +suggestionsData.getJSONObject(i).getString("name"));
//                            suggested[i] = suggestionsData.getJSONObject(i)
//                                    .getString("ticker") + " - " +suggestionsData.getJSONObject(i).getString("name");
                        }

//                        Log.d("This is the response",suggested[i]);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                if(suggest.size()>0)
                {
                    ArrayAdapter<String> stockAdapter = new ArrayAdapter<>(MainActivity.this,
                            android.R.layout.simple_dropdown_item_1line, suggest);
                    suggestions.setAdapter(stockAdapter);
//                    suggestions.setHeight(100);
                    suggestions.showDropDown();
                }




            } catch (JSONException e) {
                e.printStackTrace();
            }
        }, error -> {
            Log.e(TAG, error.toString());
        } );
        que.add(jsonRequest);
    }

    private void getLatestPriceForStocks(List<String> fqueries,List<String> pqueries) {







        Log.d(TAG+"Net worth",String.valueOf(net_worth));
//        if(dataReceived || favouriteData)
//        {
//            Log.d(TAG+"CHeck","AM i?");
//            isVisible = true;
//            Log.d(TAG+"Favourite size",favourites.size()+"");
//
//            spinner.setVisibility(GONE);
//            fetching_text.setVisibility(GONE);
//            findViewById(R.id.scroll_view).setVisibility(View.VISIBLE);
//
//        }


    }

    private void getLatestPriceForAllStocks(List<String>fqueries,List<String> pqueries)
    {
        StringBuilder sb = new StringBuilder();
        for(String query:pqueries)
        {
            sb.append(query);
            sb.append(",");
        }
        if(sb.length()>0)
        {
            sb.deleteCharAt(sb.length()-1);

            String url = Constants.LATEST_PRICE_ENDPOINT + sb.toString();
            RequestQueue que = Volley.newRequestQueue(getBaseContext());

            JsonObjectRequest jsonRequest = new JsonObjectRequest(Request.Method.GET, url,
                    null, response -> {
                try {
                    latest_test_portfolio=response.getJSONArray("data");
                    Log.d(TAG+"Calling all data together",latest_test_portfolio.toString());
                    dataReceived = true;
                }
                catch(Exception e){e.printStackTrace();}



            }, error -> Log.e(TAG, error.toString()));
            que.add(jsonRequest);

        }

        sb = new StringBuilder();
        for(String query:fqueries)
        {
            sb.append(query);
            sb.append(",");
        }

        if(sb.length()>0)
        {
            sb.deleteCharAt(sb.length()-1);
            String url = Constants.LATEST_PRICE_ENDPOINT + sb.toString();
            RequestQueue que = Volley.newRequestQueue(getBaseContext());

            JsonObjectRequest jsonRequest = new JsonObjectRequest(Request.Method.GET, url,
                    null, response -> {
                try {
                    latest_test_favourites=response.getJSONArray("data");
                    Log.d(TAG+"Calling all data together",latest_test_favourites.toString());
                    favouriteData = true;
                }
                catch(Exception e){e.printStackTrace();}



            }, error -> Log.e(TAG, error.toString()));
            que.add(jsonRequest);
        }


    }

    @Override
    protected void onStart() {
//        handler.removeCallbacks(r_spinner);
        super.onStart();
    }


    private void displayData(List<String> fqueries,List<String> pqueries)
    {
        favourites = new ArrayList<>();
        portfolio = new ArrayList<>();
        SharedPreferences total = getSharedPreferences(Constants.TOTAL_BALANCE,0);

        SharedPreferences order = getSharedPreferences(Constants.ORDER,0);


        String s = order.getString("order","");
        String[] arr = s.split(",");
        SharedPreferences forder = getSharedPreferences(Constants.FORDER,0);
        String s1 = forder.getString("forder","");
        String[] arr_fav = s1.split(",");

        getLatestPriceForAllStocks(fqueries,pqueries);

        if(dataReceived)
        {
            if(latest_test_portfolio.length()>0)
            {
                Map<String,JSONObject> jsonValues = new HashMap<>();
                for(int i=0;i<latest_test_portfolio.length();i++)
                {
                    try {
                        JSONObject jobj = (JSONObject) latest_test_portfolio.get(i);
                        jsonValues.put(jobj.getString("ticker"),jobj);
                        //Get the values corresponding to that particular object.(comparing order and ticker name).

                    }
                    catch(JSONException e)
                    {e.printStackTrace();}

                }
                Log.d("Yayyy"+TAG+arr.length,latest_test_portfolio+"");
                for(int i=0;i<arr.length;i++)
                {



                    try {

                        latestPriceResults = (JSONObject) jsonValues.get(arr[i]);
                        if(latestPriceResults!=null) {

                            String tres = latestPriceResults.getString("ticker");
                            SharedPreferences pref = getSharedPreferences(Constants.PORTFOLIO, 0);
                            SharedPreferences.Editor addToPortfolio = pref.edit();

                            StockDetailForLocal currstock = StockDetailForLocal.toStock(pref.getString(arr[i], ""));

                            currstock.latestPrice = latestPriceResults.getString("last");
                            DecimalFormat df = new DecimalFormat("###.##");
                            currstock.change = String.valueOf(df.format(Double.parseDouble(latestPriceResults.getString("last")) -
                                    Double.parseDouble(latestPriceResults.getString("prevClose"))));
                            Log.d(TAG, "Got the latest data available");
                            portfolio.add(currstock);
                            addToPortfolio.putString(arr[i], currstock.toMyString());
                            addToPortfolio.apply();

                            portfolio_adapter.setList(portfolio);
                            portfolio_adapter.notifyDataSetChanged();


                            Log.d(TAG + "Quanitty getting added to net_worth", String.valueOf(Double.parseDouble(currstock.quantity) * Double.parseDouble(currstock.latestPrice)));
                            net_worth = net_worth + (Double.parseDouble(currstock.quantity) * Double.parseDouble(currstock.latestPrice));
                        }


                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }
                net_worth = net_worth + Double.parseDouble(total.getString("total_balance","0"));
            }
            TextView net = findViewById(R.id.net_worth_val);
            DecimalFormat df = new DecimalFormat("###.##");
            net.setText(String.valueOf(df.format(net_worth)));
        }

        if(favouriteData)
        {
            if(latest_test_favourites.length()>0)
            {

                Map<String,JSONObject> jsonValues = new HashMap<>();
                for(int i=0;i<latest_test_favourites.length();i++)
                {
                    try {
                        JSONObject jobj = (JSONObject) latest_test_favourites.get(i);
                        jsonValues.put(jobj.getString("ticker"),jobj);
                        //Get the values corresponding to that particular object.(comparing order and ticker name).

                    }
                    catch(JSONException e)
                    {e.printStackTrace();}

                }
                for(int i=0;i<arr_fav.length;i++)
                {
                    try {
                        latestPriceResults = (JSONObject) jsonValues.get(arr_fav[i]);
                        SharedPreferences preffav = getSharedPreferences(Constants.FAVOURITES, 0);
                        SharedPreferences.Editor addToFav = preffav.edit();
                        StockDetailForLocal currstockinFav = StockDetailForLocal.toStock(preffav.getString(arr_fav[i],""));
                        if(latestPriceResults!=null)
                        {
                            currstockinFav.latestPrice = latestPriceResults.getString("last");
                            DecimalFormat df = new DecimalFormat("###.##");
                            currstockinFav.change = String.valueOf(df.format(Double.parseDouble(latestPriceResults.getString("last"))-
                                    Double.parseDouble(latestPriceResults.getString("prevClose"))));


                            favourites.add(currstockinFav);
                            addToFav.putString(arr_fav[i],currstockinFav.toMyString());
                            addToFav.apply();
                            adapter.setList(favourites);
                            adapter.notifyDataSetChanged();
                        }



                    }
                    catch(JSONException e)
                    {
                        e.printStackTrace();
                    }
                }
            }




        }

    }
    @Override
    public void onResume() {

        findViewById(R.id.scroll_view).setVisibility(GONE);
        if(!spinner_flag) {
            new CountDownTimer(3000, 500) {

                public void onTick(long millisUntilFinished) {
                    findViewById(R.id.scroll_view).setVisibility(GONE);
                    spinner.setVisibility(View.VISIBLE);
                    fetching_text.setVisibility(View.VISIBLE);
                }

                public void onFinish() {
                    isVisible = true;
                    findViewById(R.id.scroll_view).setVisibility(View.VISIBLE);
                    spinner.setVisibility(GONE);
                    fetching_text.setVisibility(GONE);


                }
            }.start();
        }

        spinner_flag = false;



        favourites = new ArrayList<>();
        FavouritetickersinLocal.clear();

        SharedPreferences pref = this.getSharedPreferences(Constants.FAVOURITES, 0);
        Map<String,?> favList = pref.getAll();

        SharedPreferences forder = this.getSharedPreferences(Constants.FORDER, 0);
        SharedPreferences.Editor fedit = forder.edit();
        String s = forder.getString("forder","");
        String[] arr_fav = s.split(",");


        for(String a: arr_fav)
        {
            try {
                if(favList.get(a)!=null) {
                    StockDetailForLocal stock = StockDetailForLocal.toStock(favList.get(a).toString());
                    favourites.add(stock);
                    FavouritetickersinLocal.add(a);
                }
            }
            catch(Exception e) {e.printStackTrace();}

        }
        Log.d("Inside the favourites on resume method",favourites.size()+"");

        if (adapter != null) {
            adapter.setList(favourites);
            adapter.notifyDataSetChanged();
        }



        portfolio = new ArrayList<>();
        PortfoliotickersinLocal.clear();
        SharedPreferences prefport = this.getSharedPreferences(Constants.PORTFOLIO, 0);
        Map<String,?> portFolioMap = prefport.getAll();

        SharedPreferences order = this.getSharedPreferences(Constants.ORDER, 0);
        SharedPreferences.Editor edit = order.edit();
        String s1 = order.getString("order","");
        String[] arr = s1.split(",");


        for(String a: arr)
        {
            try {
                if(portFolioMap.get(a)!=null) {
                    StockDetailForLocal stock = StockDetailForLocal.toStock(portFolioMap.get(a).toString());
                    portfolio.add(stock);
                    PortfoliotickersinLocal.add(a);
                }
            }
            catch(Exception e) {e.printStackTrace();}

        }

        Log.d(TAG,"Port:"+portfolio.size()+"");
        if (portfolio_adapter != null) {
            portfolio_adapter.setList(portfolio);
            portfolio_adapter.notifyDataSetChanged();
        }










        getLatestPriceForAllStocks(FavouritetickersinLocal,PortfoliotickersinLocal);


        handler.postDelayed(runnable = new Runnable() {
            @Override
            public void run() {
                net_worth = 0.0;


                    displayData(FavouritetickersinLocal,PortfoliotickersinLocal);


//                getLatestPriceForStocks(FavouritetickersinLocal,PortfoliotickersinLocal);
                handler.postDelayed(runnable,delay);
            }
        },delay);
        super.onResume();




    }

    @Override
    protected void onPause() {
        handler.removeCallbacks(runnable);
        count = 0;
        handlers.removeCallbacks(r_spinner);

        super.onPause();
    }

    @Override
    public void onClick(View view, int position) {

//        Button b = findViewById(R.id.onclickDetail);
//        b.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent stockIntent = new Intent(MainActivity.this,StockDetails.class);
////                Intent stockIntent = new Intent(v.getContext(), StockDetails.class);
//
////                stockIntent.putExtra(Constants.KEYWORD,portfolio.get(position).ticker);
////                startActivity(stockIntent);
//                stockIntent.putExtra(Constants.KEYWORD,favourites.get(position).ticker);
//                startActivity(stockIntent);
//            }
//        });
//        Intent stockIntent = new Intent(MainActivity.this,StockDetails.class);
////                Intent stockIntent = new Intent(v.getContext(), StockDetails.class);
//
//        stockIntent.putExtra(Constants.KEYWORD,portfolio.get(position).ticker);
//        startActivity(stockIntent);
        
    }

    @Override
    public void onLongClick(View view, int position) {

    }

    private void enableSwipeToDeleteAndUndo(RecyclerView recyclerView) {
        SwipeToDeleteCallback swipeToDeleteCallback = new SwipeToDeleteCallback(this) {
            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int i) {


                final int position = viewHolder.getAdapterPosition();
                final StockDetailForLocal item = adapter.getData().get(position);

                adapter.removeItem(position);
                SharedPreferences pref = getSharedPreferences(Constants.FAVOURITES,0);
                SharedPreferences.Editor removeFromFavourites = pref.edit();
                SharedPreferences fav_order = getSharedPreferences(Constants.FORDER,0);
                SharedPreferences.Editor fedit = fav_order.edit();
                if(pref.getString(item.ticker,null)!=null)
                {
                    removeFromFavourites.remove(item.ticker);
                    removeFromFavourites.apply();
                    String getticker = fav_order.getString("forder","");

                    if(getticker.startsWith(item.ticker))
                    {
                        StringBuilder sb = new StringBuilder();
                        sb.append(getticker);
                        sb.delete(0,item.ticker.length()+1);
                        fedit.putString("forder",sb.toString());
                    }
                    else
                    {
                        StringBuilder sb = new StringBuilder();
                        sb.append(getticker);
                        int first = sb.indexOf(item.ticker);
                        sb.delete(first-1,first+item.ticker.length());
                        fedit.putString("forder",sb.toString());
                    }
                    fedit.apply();
                }




            }
        };

        ItemTouchHelper itemTouchhelper = new ItemTouchHelper(swipeToDeleteCallback);
        itemTouchhelper.attachToRecyclerView(recyclerView);
    }


}
