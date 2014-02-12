package com.applico.sportsreader;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Filterable;
import android.widget.Spinner;
import android.widget.Toast;


public class TeamSelectActivity extends Activity{

	SharedPreferences preferences;

    private ProgressDialog pDialog;
    private final static String URL_header = "http://api.espn.com/v1/sports/";
    // TODO Limit baked in here, need to extract
    private final static String URL_key = "/teams/?limit=500&apikey=7xvfvmwkutcq2q7xkr2pgtqk";
    
    private ArrayList<Team> allTeams = new ArrayList<Team>();
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
		
        super.onCreate(savedInstanceState);

		preferences = PreferenceManager
				.getDefaultSharedPreferences(getApplicationContext());

		setContentView(R.layout.activity_teamselect);
        
        Spinner spinner_sport = (Spinner) findViewById(R.id.spinner_sport_name);
		// Create an ArrayAdapter using the string array and a default spinner layout
		ArrayAdapter<CharSequence> adapter_sport = ArrayAdapter.createFromResource(this,
		         R.array.sports_array, android.R.layout.simple_spinner_item);
	    // Specify the layout to use when the list of choices appears
		adapter_sport.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		// Apply the adapter to the spinner
		spinner_sport.setAdapter(adapter_sport);
        spinner_sport.setOnItemSelectedListener(new sportOnItemSelectedListener());

        Spinner spinner_sport_league = (Spinner) findViewById(R.id.spinner_sport_league);
        spinner_sport_league.setVisibility(View.INVISIBLE);
        
        Spinner spinner_sport_team = (Spinner) findViewById(R.id.spinner_sport_team);
        spinner_sport_team.setVisibility(View.INVISIBLE);

        Button button = (Button) findViewById(R.id.team_URL);
        button.setVisibility(View.INVISIBLE);

        // Restore preferences
        final String sportSelected = preferences.getString(getString(R.string.preferences_sport), "Baseball");
        if(sportSelected != null){
        	int spinner_sport_position = adapter_sport.getPosition(sportSelected);
        	spinner_sport.setSelection(spinner_sport_position);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (null != pDialog && pDialog.isShowing())
            pDialog.dismiss();
        
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(getString(R.string.preferences_sport), ((Spinner)findViewById(R.id.spinner_sport_name)).getSelectedItem().toString());
        if(null != findViewById(R.id.spinner_sport_league) && null != ((Spinner)findViewById(R.id.spinner_sport_league)).getSelectedItem()){
        	editor.putString(getString(R.string.preferences_league), ((Spinner)findViewById(R.id.spinner_sport_league)).getSelectedItem().toString());
        }
        if(null != findViewById(R.id.spinner_sport_team) && null != ((Spinner)findViewById(R.id.spinner_sport_team)).getSelectedItem()){
        	editor.putString(getString(R.string.preferences_team), ((Spinner)findViewById(R.id.spinner_sport_team)).getSelectedItem().toString());
        }
        //editor.putString(getString(R.string.preferences_teamID), ((Spinner)findViewById(R.id.spinner_sport_name)).getSelectedItem().toString());
        editor.commit();

    }

    @Override
    protected void onResume() {
        super.onResume();

        Spinner spinner_sport = (Spinner) findViewById(R.id.spinner_sport_name);
		// Create an ArrayAdapter using the string array and a default spinner layout
		ArrayAdapter<CharSequence> adapter_sport = ArrayAdapter.createFromResource(this,
		         R.array.sports_array, android.R.layout.simple_spinner_item);
		
        // Restore preferences
    	if(preferences == null){
    		preferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
    	}
        final String sportSelected = preferences.getString(getString(R.string.preferences_sport), "baseball");
        if(sportSelected != null){
        	int spinner_sport_position = adapter_sport.getPosition(sportSelected);
        	spinner_sport.setSelection(spinner_sport_position);
        }
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        
        // TODO Need to cleanup resources here

    }
	
    public class TeamComparator implements Comparator<Team> {

		@Override
		public int compare(Team lhs, Team rhs) {
            return lhs.name.compareTo(rhs.name);
		}
    }
    
    public static class Team {
    	public String name;
    	public String teamId;
    	public String URL;
    	
    	public Team(String inName, String inTeamId, String inURL){
    		name = inName;
    		teamId = inTeamId;
    		URL = inURL;
    	}
    	public String toString(){
    		return name;
    	}
    	public boolean equals(Team t){
    		return name.equals(t.name) && teamId.equals(t.teamId);
    	}
    }

    public class sportOnItemSelectedListener implements OnItemSelectedListener {
        public void onItemSelected(AdapterView<?> parent,
            View view, int pos, long id) {
            
        	SharedPreferences.Editor editor = preferences.edit();
            
        	if(!"None Selected".equals(parent.getSelectedItem().toString())){
	            final String choice = parent.getSelectedItem().toString();
	            editor.putString(getString(R.string.preferences_sport), choice);
	            
	            ArrayList<CharSequence> leagues = new ArrayList<CharSequence>();
	            // Determine which leagues to show
	            if("Basketball".equals(choice)){
	            	leagues.add("nba");
	            	leagues.add("wnba");
	            	leagues.add("mens-college-basketball");
	            	leagues.add("womens-college-basketball");
	            }
	            else if("Football".equals(choice)){
	            	leagues.add("nfl");
	            	leagues.add("college-football");
	            }
	            else if("Hockey".equals(choice)){
	            	leagues.add("nhl");
	            	/* These aren't supported yet
	            	leagues.add("mens-college-hockey");
	            	leagues.add("womens-college-hockey");
	            	*/
	            }
	            else{
	            	// Default to Baseball
	            	leagues.add("mlb");
	            }
	            
	            Spinner spinner_sport_league = (Spinner) findViewById(R.id.spinner_sport_league);
	            spinner_sport_league.setVisibility(View.VISIBLE);
	            
				ArrayAdapter<CharSequence> adapter_sport_league = new ArrayAdapter<CharSequence>(TeamSelectActivity.this, android.R.layout.simple_spinner_item, leagues);
			    // Specify the layout to use when the list of choices appears
				adapter_sport_league.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
				// Apply the adapter to the spinner
				spinner_sport_league.setAdapter(adapter_sport_league);
		        spinner_sport_league.setOnItemSelectedListener(new sportLeagueOnItemSelectedListener());
	
		        // Restore preferences, if exist
		        final String leagueSelected = preferences.getString(getString(R.string.preferences_league), "mlb");
		        if(leagueSelected != null){
		        	int spinner_league_position = adapter_sport_league.getPosition(leagueSelected);
		        	spinner_sport_league.setSelection(spinner_league_position);
		        }
        	}
        	else{
        		// Clear out the selected team
	            editor.putString(getString(R.string.preferences_sport), null);
	            editor.putString(getString(R.string.preferences_league), null);
	            editor.putString(getString(R.string.preferences_team), null);
	            editor.putString(getString(R.string.preferences_teamID), null);

	            Spinner spinner_sport_league = (Spinner) findViewById(R.id.spinner_sport_league);
	            spinner_sport_league.setVisibility(View.INVISIBLE);
	            
	            Spinner spinner_sport_team = (Spinner) findViewById(R.id.spinner_sport_team);
	            spinner_sport_team.setVisibility(View.INVISIBLE);

	            Button button = (Button) findViewById(R.id.team_URL);
	            button.setVisibility(View.INVISIBLE);
        	}
            editor.commit();
        }

        public void onNothingSelected(AdapterView parent) {
          // Do nothing.
        }
    }

    public class sportLeagueOnItemSelectedListener implements OnItemSelectedListener {
        public void onItemSelected(AdapterView<?> parent,
            View view, int pos, long id) {
            SharedPreferences.Editor editor = preferences.edit();
            final String choice = parent.getSelectedItem().toString();
            editor.putString(getString(R.string.preferences_league), choice);
            editor.commit();

	        allTeams.clear();

            new TeamLoadTask().execute(choice);
        }

        public void onNothingSelected(AdapterView parent) {
          // Do nothing.
        }
    }
    
    public class sportTeamOnItemSelectedListener implements OnItemSelectedListener {
        public void onItemSelected(AdapterView<?> parent,
            View view, int pos, long id) {
        	final Team mSelected = (Team) parent.getItemAtPosition(pos);
        	
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString(getString(R.string.preferences_teamID), mSelected.teamId);
            editor.putString(getString(R.string.preferences_team), mSelected.name);
            editor.commit();
            
            // Update the button's onclick when this changes:

	        Button button = (Button) findViewById(R.id.team_URL);
	        button.setOnClickListener(new Button.OnClickListener() {
	            public void onClick(View v) {
	                try{
		            	// This makes the assumption that the URL passed in is valid
		            	Intent myIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(mSelected.URL));
		            	startActivity(myIntent);
	                }
	                catch(Exception e){
	            		Toast.makeText(TeamSelectActivity.this, "Error viewing the URL: " + mSelected.URL,
	                            Toast.LENGTH_SHORT).show();
	                }
	            }
	        });
	        button.setVisibility(View.VISIBLE);
            
        }

        public void onNothingSelected(AdapterView parent) {
          // Do nothing.
        }
    }
    
	class TeamLoadTask extends AsyncTask<String, String, String> {
		
	    @Override
	    protected void onPreExecute() {
	        pDialog = ProgressDialog.show(TeamSelectActivity.this, "",
	                "Loading. Please wait...", true);
	    }
	
	    @Override
	    protected String doInBackground(String... params) {
	    	if(preferences == null){
	    		preferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
	    	}
	    	String sportSelected = preferences.getString(getString(R.string.preferences_sport), "baseball");
	    	String leagueSelected = preferences.getString(getString(R.string.preferences_league), "mlb");
	    	if(sportSelected != null){
	    		sportSelected = sportSelected.toLowerCase();
	    	}
	    	if(leagueSelected != null){
	    		leagueSelected = leagueSelected.toLowerCase();
	    	}
	    	String URL = URL_header + sportSelected + "/" + leagueSelected + URL_key;
	    	
	        try {
	            DefaultHttpClient httpClient = new DefaultHttpClient();
	            HttpPost httpPost = new HttpPost(URL);
	            HttpResponse httpResponse = httpClient.execute(httpPost);
	            HttpEntity httpEntity = httpResponse.getEntity();
	
	            // if this is null the web service returned an empty page
	            if (httpEntity == null) // response is empty so exit out
	                return null;
	
	            String jsonString = EntityUtils.toString(httpEntity);
	            
	        	JSONObject jo = new JSONObject(jsonString);
	            if(null != jo && "success".equals(jo.getString("status"))){
	            	allTeams.clear();
	            	// Successfully made the call, now parse the results
	            	JSONArray all = jo.getJSONArray("sports");
	            	int i=0, length = all.length();
	            	for(; i < length; ++i){
	                    JSONObject sports = all.getJSONObject(i);
	                    JSONArray leagues = sports.getJSONArray("leagues");
		            	for(int j=0; j < leagues.length(); ++j){
		                    JSONObject league = leagues.getJSONObject(j);
		                    JSONArray teams = league.getJSONArray("teams");
			            	for(int k=0; k < teams.length(); ++k){
			                    JSONObject team = teams.getJSONObject(k);
			                    JSONObject links = team.getJSONObject("links");
			                    JSONObject mobile = links.getJSONObject("mobile");
			                    JSONObject mobileTeams = mobile.getJSONObject("teams");
			                    
			            		allTeams.add(new Team(team.getString("location") + " " + team.getString("name"), team.getString("id"), mobileTeams.getString("href")));
			            	}
		            	}
	            	}
	            }
	            else{
            		Toast.makeText(TeamSelectActivity.this, "Error: Not supported: " + URL,
                            Toast.LENGTH_SHORT).show();
		            Log. e("SportsReader", "Success was not returned, likely not supported");
	            }
	        } catch (UnsupportedEncodingException e) {
	            e.printStackTrace();
	        } catch (ClientProtocolException e) {
	            e.printStackTrace();
	        } catch (IOException e) {
	            e.printStackTrace();
	        } catch (JSONException e) {
	            e.printStackTrace();
	        } catch (Exception e) {
	            Log.e("SportsReader", e.toString());
	        }
	        // Sort the list
	        Collections.sort(allTeams, new TeamComparator());
	        return null;
	
	    }
	
	    @Override
	    protected void onPostExecute(String file_url) {
	    	
	    	// TODO Bug where this code gets executed twice on a config change (screen orientation change)
	    	// This needs to be called only once
	        if (null != pDialog && pDialog.isShowing())
	            pDialog.dismiss();

	        Spinner spinner_sport_team = (Spinner) findViewById(R.id.spinner_sport_team);
            spinner_sport_team.setVisibility(View.VISIBLE);
			ArrayAdapter<Team> adapter_sport_team = new ArrayAdapter<Team>(TeamSelectActivity.this, android.R.layout.simple_spinner_item, allTeams);
		    // Specify the layout to use when the list of choices appears
			adapter_sport_team.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			// Apply the adapter to the spinner
			spinner_sport_team.setAdapter(adapter_sport_team);
			spinner_sport_team.setOnItemSelectedListener(new sportTeamOnItemSelectedListener());

	        // Restore preferences, if exist
			// I need to loop through and find the real object in the ArrayList, since it uses a custom class Team
			// It would be nice to be able to store the 'Team' object directly in a preference to avoid looping here
	        final String teamSelected = preferences.getString(getString(R.string.preferences_team), "");
	        final String teamIDSelected = preferences.getString(getString(R.string.preferences_teamID), "");
	        if(teamSelected != null && teamIDSelected != null){
	        	Object[] tmp = allTeams.toArray();
	        	Team selectedTeam = new Team(teamSelected, teamIDSelected, "");
	        	for(Object t : tmp){
	        		if(selectedTeam.equals((Team)t)){
	        			selectedTeam = (Team) t;
	        			break;
	        		}
	        	}
	        	int spinner_team_position = adapter_sport_team.getPosition(selectedTeam);
	        	spinner_sport_team.setSelection(spinner_team_position);
	        }
	    }
	}
    
}