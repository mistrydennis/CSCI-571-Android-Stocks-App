package com.example.stocksApp;

import java.time.ZonedDateTime;

public class Stock {
    public String title;
    public String description;
    public String thumbnail;
    public String articleId;
    public ZonedDateTime publicationDate;
    public String webUrl;

    public Stock(String title,String description, String thumbnail)
    {
        this.title = title;
        this.description = description;
        this.thumbnail = thumbnail;
    }
}
