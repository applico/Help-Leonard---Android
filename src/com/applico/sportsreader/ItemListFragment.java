package com.applico.sportsreader;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

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
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

/**
 * A list fragment representing a list of Items. This fragment
 * also supports tablet devices by allowing list items to be given an
 * 'activated' state upon selection. This helps indicate which item is
 * currently being viewed in a {@link ItemDetailFragment}.
 * <p>
 * Activities containing this fragment MUST implement the {@link Callbacks}
 * interface.
 */
public class ItemListFragment extends ListFragment {

    /**
     * The serialization (saved instance state) Bundle key representing the
     * activated item position. Only used on tablets.
     */
    private static final String STATE_ACTIVATED_POSITION = "activated_position";

    /**
     * The fragment's current callback object, which is notified of list item
     * clicks.
     */
    private Callbacks mCallbacks = sDummyCallbacks;

    /**
     * The current activated item position. Only used on tablets.
     */
    private int mActivatedPosition = ListView.INVALID_POSITION;

    private ProgressDialog pDialog;
    private static final int limit = 50;
    private static final String URL_header = "http://api.espn.com/v1/sports/";
    private static final String URL_key = "&limit=" + limit + "&_accept=json%2Fxml&apikey=7xvfvmwkutcq2q7xkr2pgtqk";
    
    /**
     * A callback interface that all activities containing this fragment must
     * implement. This mechanism allows activities to be notified of item
     * selections.
     */
    public interface Callbacks {
        /**
         * Callback for when an item has been selected.
         */
        public void onItemSelected(String id);
    }

    /**
     * A dummy implementation of the {@link Callbacks} interface that does
     * nothing. Used only when this fragment is not attached to an activity.
     */
    private static Callbacks sDummyCallbacks = new Callbacks() {
        @Override
        public void onItemSelected(String id) {
        }
    };

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ItemListFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        new HeadlineLoadTask().execute();
        
        setListAdapter(new ArrayAdapter<HeadlineContent.HeadlineItem>(
                getActivity(),
                android.R.layout.simple_list_item_activated_1,
                android.R.id.text1,
                HeadlineContent.ITEMS));
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Restore the previously serialized activated item position.
        if (savedInstanceState != null
                && savedInstanceState.containsKey(STATE_ACTIVATED_POSITION)) {
            setActivatedPosition(savedInstanceState.getInt(STATE_ACTIVATED_POSITION));
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // Activities containing this fragment must implement its callbacks.
        if (!(activity instanceof Callbacks)) {
            throw new IllegalStateException("Activity must implement fragment's callbacks.");
        }

        mCallbacks = (Callbacks) activity;
    }

    @Override
    public void onDetach() {
        super.onDetach();

        // Reset the active callbacks interface to the dummy implementation.
        mCallbacks = sDummyCallbacks;
    }

    @Override
    public void onListItemClick(ListView listView, View view, int position, long id) {
        super.onListItemClick(listView, view, position, id);

        // Notify the active callbacks interface (the activity, if the
        // fragment is attached to one) that an item has been selected.
        mCallbacks.onItemSelected(HeadlineContent.ITEMS.get(position).id);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mActivatedPosition != ListView.INVALID_POSITION) {
            // Serialize and persist the activated item position.
            outState.putInt(STATE_ACTIVATED_POSITION, mActivatedPosition);
        }
    }

    /**
     * Turns on activate-on-click mode. When this mode is on, list items will be
     * given the 'activated' state when touched.
     */
    public void setActivateOnItemClick(boolean activateOnItemClick) {
        // When setting CHOICE_MODE_SINGLE, ListView will automatically
        // give items the 'activated' state when touched.
        getListView().setChoiceMode(activateOnItemClick
                ? ListView.CHOICE_MODE_SINGLE
                : ListView.CHOICE_MODE_NONE);
    }

    private void setActivatedPosition(int position) {
        if (position == ListView.INVALID_POSITION) {
            getListView().setItemChecked(mActivatedPosition, false);
        } else {
            getListView().setItemChecked(position, true);
        }

        mActivatedPosition = position;
    }


	class HeadlineLoadTask extends AsyncTask<String, String, String> {
	
	    @Override
	    protected void onPreExecute() {
	    	// TODO This should be allowed to persist if more results are shown as you scroll
	    	// down the page instead of clearing out the list
	    	HeadlineContent.ITEMS.clear();
	        pDialog = ProgressDialog.show(getActivity(), "",
	                "Loading. Please wait...", true);
	    }
	
	    @Override
	    protected String doInBackground(String... params) {
	        try {
	        	String URL = "";
	        	SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
		        final String sportSelected = preferences.getString(getString(R.string.preferences_sport), "");
		        final String leagueSelected = preferences.getString(getString(R.string.preferences_league), "");
		        final String teamIDSelected = preferences.getString(getString(R.string.preferences_teamID), "");
		        // If any of the preferences are blank
		        if("".equals(sportSelected) || "".equals(leagueSelected) || "".equals(teamIDSelected)){
		        	URL = URL_header + "/news/?" + URL_key;
		        }
		        else{
		        	URL = URL_header + sportSelected.toLowerCase() + "/" + leagueSelected.toLowerCase() + "/news/?teams=" + teamIDSelected + URL_key;
		        }
	        	
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
	            	// Successfully made the call, now parse the results
	            	JSONArray allHeadlines = jo.getJSONArray("headlines");
	            	int i=0, length = allHeadlines.length();
	            	for(; i < length; ++i){
	                    JSONObject headline = allHeadlines.getJSONObject(i);
	                    JSONObject links = headline.getJSONObject("links");
	                    JSONObject mobile = links.getJSONObject("mobile");
	                    
	                    HeadlineContent.addItem(new HeadlineContent.HeadlineItem(String.valueOf(i), 
	                    		              headline.getString("headline"), 
	                    		              headline.getString("description"),
	                    		              mobile.getString("href") ));
	            	}
	            }
	            else{
            		Toast.makeText(getActivity(), "Error: Not supported: " + URL,
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
	            Log.e("ERROR SOMEWHERE!!!! ", e.toString());
	        }
	        return null;
	
	    }
	
	    @Override
	    protected void onPostExecute(String file_url) {
	    	// TODO This is going to cause a crash if the screen orientation changes. This needs to be handled,
	    	// possibly with preserving the Fragment's state
	        if (pDialog.isShowing())
	            pDialog.dismiss();
	        setListAdapter(new ArrayAdapter<HeadlineContent.HeadlineItem>(
	                getActivity(),
	                android.R.layout.simple_list_item_activated_1,
	                android.R.id.text1,
	                HeadlineContent.ITEMS));
	    }
	}
	
}
