package com.applico.sportsreader;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;


public class TitleActivity extends Activity{

	public static final String URL_Twitter = "https://mobile.twitter.com";
	public static final String URL_PurchaseTickets = "https://m.stubhub.com/sports-tickets/";
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
		
        super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_title);

        final Button button_headlines = (Button) findViewById(R.id.title_button_headlines);
        final Button button_teamselect = (Button) findViewById(R.id.title_button_teamselect);
        final Button button_twitter = (Button) findViewById(R.id.title_button_twitter);
        final Button button_purchasetickets = (Button) findViewById(R.id.title_button_purchasetickets);

        button_headlines.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Passing '0' to the activity for now, may use later
                startActivityForResult(new Intent(TitleActivity.this, ItemListActivity.class), 0);
            }
        });
        button_teamselect.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Passing '0' to the activity for now, may use later
                startActivityForResult(new Intent(TitleActivity.this, TeamSelectActivity.class), 0);
            }
        });
        /*
         * This should use the Twitter API to make a call
         */
        button_twitter.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                try{
	            	// This makes the assumption that the URL passed in is valid
	            	Intent myIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(URL_Twitter));
	            	startActivity(myIntent);
                }
                catch(Exception e){
            		Toast.makeText(TitleActivity.this, "Error viewing the URL: " + URL_Twitter,
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
        /*
         * This should use the StubHub API to make a call
         */
        button_purchasetickets.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                try{
	            	// This makes the assumption that the URL passed in is valid
	            	Intent myIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(URL_PurchaseTickets));
	            	startActivity(myIntent);
                }
                catch(Exception e){
            		Toast.makeText(TitleActivity.this, "Error viewing the URL: " + URL_PurchaseTickets,
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
	
}