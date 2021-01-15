package com.example.stocksApp;

public class StockPrice {

    public String company_latest_price;
    public String low;
    public String bid_Price;
    public String open_Price;
    public String mid;
    public String high;
    public String Volume;

    public StockPrice(String company_latest_price,String low,String bid_Price,String open_Price,String mid,String high,String volume)
    {
        this.company_latest_price = company_latest_price;
        this.low = low;
        this.bid_Price = bid_Price;
        this.open_Price = open_Price;
        this.mid = mid;
        this.high = high;
        this.Volume = volume;
    }

}
