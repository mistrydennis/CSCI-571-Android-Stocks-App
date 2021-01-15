package com.example.stocksApp;

import org.json.JSONException;
import org.json.JSONObject;

import java.time.ZonedDateTime;

public class NewsArticle {
    public String title;
    public String section;
    public String thumbnail;
    public ZonedDateTime publicationDate;
    public String webUrl;

    public NewsArticle(String title, String section, String thumbnail, ZonedDateTime publicationDate, String webUrl){
        this.title = title;
        this.section = section;
        this.thumbnail = thumbnail;
        this.publicationDate = publicationDate;
        this.webUrl = webUrl;
    }

    public String toMyString() {
        return "{ \"title\": \"" + title + "\", \"section\": \"" + section + "\", \"thumbnail\": \""
                + thumbnail + "\", \"publicationDate\": \""
                + publicationDate + "\", \"webUrl\": \""+webUrl+"\" }";
    }

    public static com.example.stocksApp.NewsArticle toNewsArticle(String article) throws JSONException {
        JSONObject artcle = new JSONObject(article);
        String title = artcle.getString("title");
        String section = artcle.getJSONObject("source").getString("name");
        String thumbnail = artcle.getString("urlToImage");
        ZonedDateTime publicationDate = ZonedDateTime
                .parse(artcle.getString("publishedAt"));
        String webUrl = artcle.getString("url");
        return new com.example.stocksApp.NewsArticle(title, section, thumbnail, publicationDate, webUrl);
    }
}
