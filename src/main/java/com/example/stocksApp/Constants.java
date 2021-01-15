package com.example.stocksApp;

public class Constants {

    public static final String AUTO_COMPLETE_API_ENDPOINT = "http://androidnode-env.eba-4mpfywnf.us-east-1.elasticbeanstalk.com/autoComplete/";

    public static final String STOCK_DETAILS_ENDPOINT = "http://androidnode-env.eba-4mpfywnf.us-east-1.elasticbeanstalk.com/company/";

//    public static final String LATEST_PRICE_ENDPOINT= "http://angularnodeapp-env.eba-m8pfb7sw.us-east-1.elasticbeanstalk.com/company_latest_price/";
    public static final String LATEST_PRICE_ENDPOINT= "http://androidnode-env.eba-4mpfywnf.us-east-1.elasticbeanstalk.com/company_latest_price/";

    public static final String HISTORICAL_CHART_DATA_ENDPOINT ="http://192.168.1.5:8080/company_historical/";

    public static final String NEWS_API_ENDPOINT="http://androidnode-env.eba-4mpfywnf.us-east-1.elasticbeanstalk.com/news_data/";

    public static final String FAVOURITES = "favourites";

    public static final String PORTFOLIO = "portfolio";

    public static final String KEYWORD = "query";

    public static final String ORDER = "order";

    public static final String FORDER = "forder";

    public static final String TOTAL_BALANCE = "total_balance";

    public static final int TO_DAYS = 86400, TO_HOURS = 3600, TO_MINUTES = 60;

    public static final String ZONEID_LA = "America/Los_Angeles", ZONEID_GMT = "GMT";



    public static final String TWITTER_INTENT =
            "https://twitter.com/intent/tweet?text=__shareText__&hashtags=CSCI571StockApp";

    public static final String SHARE_TWITTER_TEXT = "Check out this Link : __link__";

    public static final String DEFAULT_IMAGE = "https://drive.google.com/file/d/1Pd2H208xuAD2Ye0EZrSEGTKjRJd8qOjn/view?usp=sharing";

}
