package com.applico.sportsreader;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Helper class for providing sample content for user interfaces created by
 * Android template wizards.
 * <p>
 * TODO: Replace all uses of this class before publishing your app.
 */
public class HeadlineContent {

    /**
     * An array of sample (dummy) items.
     */
    public static List<HeadlineItem> ITEMS = new ArrayList<HeadlineItem>();

    /**
     * A map of sample (dummy) items, by ID.
     */
    public static Map<String, HeadlineItem> ITEM_MAP = new HashMap<String, HeadlineItem>();

    /*
    static {
    	
        StrictMode.ThreadPolicy policy = new StrictMode.
        ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy); 
        
        
        String readESPNFeed = readESPNFeed(50);
        try {
        	JSONObject jo = new JSONObject(readESPNFeed);
            //Log.i(DummyContent.class.getName(),
            //        "JSON: " + jo.toString());
            if("success".equals(jo.getString("status"))){
            	// Successfully made the call, now parse the results
            	JSONArray allHeadlines = jo.getJSONArray("headlines");
            	int i=0, length = allHeadlines.length();
            	for(; i < length; ++i){
                    JSONObject headline = allHeadlines.getJSONObject(i);
                    JSONObject links = headline.getJSONObject("links");
                    JSONObject mobile = links.getJSONObject("mobile");
                    
                    addItem(new DummyItem(String.valueOf(i), 
                    		              headline.getString("headline"), 
                    		              headline.getString("description"),
                    		              mobile.getString("href") ));
            	}
            }
        } catch (Exception e) {
          e.printStackTrace();
        }
    	
    }
    */
/*
    public static String readESPNFeed(int resultsLimit) {
        StringBuilder builder = new StringBuilder();
        HttpClient client = new DefaultHttpClient();
        // Just to be safe, address later if performance is hit
        if(resultsLimit < 20){
        	resultsLimit = 20;
        }
        else if(resultsLimit > 100){
        	resultsLimit = 100;
        }
        //HttpGet httpGet = new HttpGet("http://ip.jsontest.com/");
        HttpGet httpGet = new HttpGet("http://api.espn.com/v1/sports/news/?_accept=json%2Fxml&limit=" + resultsLimit + "&apikey=7xvfvmwkutcq2q7xkr2pgtqk");
        try {
          HttpResponse response = client.execute(httpGet);
          StatusLine statusLine = response.getStatusLine();
          int statusCode = statusLine.getStatusCode();
          if (statusCode == 200) {
            HttpEntity entity = response.getEntity();
            InputStream content = entity.getContent();
            BufferedReader reader = new BufferedReader(new InputStreamReader(content));
            String line;
            while ((line = reader.readLine()) != null) {
              builder.append(line);
            }
          } else {
            Log.e(DummyContent.class.toString(), "Failed to download file");
          }
        } catch (ClientProtocolException e) {
          e.printStackTrace();
        } catch (IOException e) {
          e.printStackTrace();
        }
        return builder.toString();
      }
*/
    
    public static void addItem(HeadlineItem item) {
        ITEMS.add(item);
        ITEM_MAP.put(item.id, item);
    }

    /**
     * A dummy item representing a piece of content.
     */
    public static class HeadlineItem {
        public String id;
        public String headline;
        public String desc;
        public String url;

        public HeadlineItem(String id, String headline, String desc, String url) {
            this.id = id;
            this.headline = headline;
            this.desc = desc;
            this.url = url;
        }

        @Override
        public String toString() {
            return headline;
        }
    }
}
