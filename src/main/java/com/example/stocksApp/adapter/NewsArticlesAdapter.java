package com.example.stocksApp.adapter;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.stocksApp.Constants;
import com.example.stocksApp.NewsArticle;
import com.example.stocksApp.R;
import com.example.stocksApp.interfaces.ClickListener;
import com.squareup.picasso.Picasso;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import jp.wasabeef.picasso.transformations.RoundedCornersTransformation;

public class NewsArticlesAdapter extends RecyclerView.Adapter<NewsArticlesAdapter.ArticleViewHolder> {

    private static final String TAG = "NewsArticlesAdapter";
    private List<NewsArticle> articles;
    private Context context;
    private ClickListener clickListener;

    public NewsArticlesAdapter(List<NewsArticle> articles, Context context) {
        this.articles = articles;
        this.context = context;
    }

    @NonNull
    @Override
    public ArticleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.newscard_recycler_view, parent, false);
        final ArticleViewHolder articleViewHolder = new ArticleViewHolder(v);
        return articleViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull NewsArticlesAdapter.ArticleViewHolder articleViewHolder, int position) {
        NewsArticle currentArticle = articles.get(position);
        articleViewHolder.title.setText(currentArticle.title);
        articleViewHolder.description.setText(currentArticle.section);
        articleViewHolder.timeDifference.setText(getDifference(currentArticle.publicationDate));


        Picasso.with(context).load(currentArticle.thumbnail).transform(new RoundedCornersTransformation(10, 0)).resize(90, 90)
                .onlyScaleDown().into(articleViewHolder.newsCardImage);


    }

    @Override
    public int getItemCount() {
        return articles.size();
    }

    public void setClickListener(ClickListener clickListener) {
        this.clickListener = clickListener;
    }

    public ClickListener getClickListener()
    {
        return this.clickListener;
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
            return timeDifference+"seconds ago";
        }
    }

    private static long zonedDateTimeDifference(final ZonedDateTime d1,
                                                final ZonedDateTime d2,
                                                final ChronoUnit unit) {
        return unit.between(d1, d2);
    }

    public class ArticleViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener, View.OnLongClickListener {
        CardView cv;
        TextView title;
        TextView description, timeDifference;
        ImageView newsCardImage;
        ImageView bookmarkIcon;
        String articleId;

        ArticleViewHolder(View itemView) {
            super(itemView);
//            SharedPreferences pref = context.getSharedPreferences(Constants.BOOKMARK, 0);
//            SharedPreferences.Editor addToBookmark = pref.edit();
            cv = itemView.findViewById(R.id.recycler_card);
            title = itemView.findViewById(R.id.title);
            description = itemView.findViewById(R.id.description);
            newsCardImage = itemView.findViewById(R.id.newscardImage);
            timeDifference = itemView.findViewById(R.id.time_difference);
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
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
        }

        @Override
        public void onClick(View v) {
            clickListener.onClick(v, getAdapterPosition());
        }

        @Override
        public boolean onLongClick(View v) {
            clickListener.onLongClick(v, getAdapterPosition());
            return true;
        }
    }
}