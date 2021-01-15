package com.example.stocksApp;

import org.json.JSONException;
import org.json.JSONObject;

import java.time.ZonedDateTime;

public class StockDetailForLocal {
    public String ticker;
    public String latestPrice;
    public String change;
    public String ticker_company_name;
    public String quantity;
    public String date;

    public StockDetailForLocal(String ticker,String latestPrice,String change,String ticker_company_name,String quantity,String date)
    {
        this.ticker = ticker;
        this.latestPrice = latestPrice;
        this.change = change;
        this.ticker_company_name = ticker_company_name;
        this.quantity = quantity;
        this.date = date;
    }

    public String getQuantity()
    {
        return this.quantity;
    }
    public String getLatestPrice()
    {
        return this.latestPrice;
    }
    public String getDate() { return this.date; }

    public String toMyString() {
        return "{ \"ticker\": \"" + ticker + "\", \"latestPrice\": \"" + latestPrice + "\", \"change\": \""
                + change + "\", \"ticker_company_name\": \""+ticker_company_name+"\", \"quantity\": \""+quantity+"\",\"current\":\""+date+"\" }";
    }

    public static StockDetailForLocal toStock(String article) throws JSONException {
        JSONObject artcle = new JSONObject(article);
        String ticker = artcle.getString("ticker");
        String latestPrice = artcle.getString("latestPrice");
        String change = artcle.getString("change");
        String ticker_company_name = artcle.getString("ticker_company_name");
        String quantity = artcle.getString("quantity");
        String date = artcle.getString("current");
        return new StockDetailForLocal(ticker, latestPrice, change, ticker_company_name, quantity,date);
    }
}
