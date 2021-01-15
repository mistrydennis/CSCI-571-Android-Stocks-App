//package com.example.stocksApp;
//
//import android.content.Intent;
//import android.net.Uri;
//import android.text.method.LinkMovementMethod;
//import android.view.View;
//import android.widget.TextView;
//
//import java.text.DecimalFormat;
//import java.text.SimpleDateFormat;
//import java.util.Date;
//import java.util.TimeZone;
//
//import static android.view.View.GONE;
//
//public class Dump {
//
//    //        TextView plus = findViewById(R.id.expandable_icon_more);
//    //        TextView minus = findViewById(R.id.expandable_icon_less);
//    //
//    //
//    //        plus.setOnClickListener(new View.OnClickListener() {
//    //
//    //            @Override
//    //            public void onClick(View v) {
//    //
//    //                plus.setVisibility(View.GONE);
//    //                minus.setVisibility(View.VISIBLE);
//    //
//    //
//    //            }
//    //        });
//    //
//    //        minus.setOnClickListener(new View.OnClickListener() {
//    //
//    //            @Override
//    //            public void onClick(View v) {
//    //
//    //                minus.setVisibility(View.GONE);
//    //                plus.setVisibility(View.VISIBLE);
//    //                tv.setMaxLines(2);
//    //
//    //            }
//    //        });
//
////        if (tv.getLineCount() >= 2) {
////            int lineEndIndex = tv.getLayout().getLineEnd(0);
////            String text = tv.getText().subSequence(0, lineEndIndex - 3) + "\u2026";
////            tv.setText(text);
////        }
//
//    TextView current_date = findViewById(R.id.current_date);
//    TextView net = findViewById(R.id.net_worth_val);
//    SimpleDateFormat dateTimeInPST = new SimpleDateFormat("MMMM dd, yyyy");
//        dateTimeInPST.setTimeZone(TimeZone.getTimeZone("America/Los_Angeles"));
//    String str = String.valueOf(dateTimeInPST.format(new Date()));
//    DecimalFormat df = new DecimalFormat("###.##");
//        handlers.postDelayed(r_spinner = new Runnable() {
//        @Override
//        public void run() {
//            count++;
//            if(count==10)
//            {
//                spinner.setVisibility(GONE);
//                fetching_text.setVisibility(GONE);
//                findViewById(R.id.scroll_view).setVisibility(View.VISIBLE);
//                current_date.setText(String.valueOf(str));
//                setUpRecyclerView();
//                net.setText(String.valueOf(df.format(net_worth)));
//                TextView footer_text= (TextView) findViewById(R.id.footer_text); //txt is object of TextView
//                footer_text.setMovementMethod(LinkMovementMethod.getInstance());
//                footer_text.setOnClickListener(new View.OnClickListener() {
//                    public void onClick(View v) {
//                        Intent browserIntent = new Intent(Intent.ACTION_VIEW);
//                        browserIntent.setData(Uri.parse("https://www.tiingo.com/"));
//                        startActivity(browserIntent);
//                    }
//                });
//            }
//            else
//                handlers.postDelayed(r_spinner,100);
//
//
//        }
//    },100);
//}
//
