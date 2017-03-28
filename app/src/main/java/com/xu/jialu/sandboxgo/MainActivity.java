package com.xu.jialu.sandboxgo;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    public static final String HTTP_SANDBOX_IMAGE = "http://cis.bentley.edu/sandbox/wp-content/uploads/TutorImage/";

    private ProgressDialog pDialog;
    private ListView lv;
    private String TAG = MainActivity.class.getSimpleName();
    private static final String BASICURL = "https://www.googleapis.com/calendar/v3/calendars/bentleycis@gmail.com/events?&singleEvents=true&orderBy=startTime&timeMin=";
    private static final String APIKEY = "&key=AIzaSyBXwv2VXYi1Xd6w04suJlAc2bhSO57xr-Y";
    private static final String IMAGESUFFIX = "-150x150.jpg";

    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
    String formattedCurrentTime = sdf.format(new Date()).substring(0, 19) + "-04:00"; // UTC - 4 hours
    String formattedTimeMax = sdf.format(new Date()).substring(0, 11) + "23:59:59-04:00"; // End of day (UTC - 4 hours)
    Date currentTime;
    Date startTime;
    Date endTime;

    ArrayList<HashMap<String, String>> tutorList;
    ImageLoader imageLoader = ImageLoader.getInstance(); // Get singleton instance

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tutorList = new ArrayList<>();

        lv = (ListView) findViewById(R.id.list);

        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(this).build();
        ImageLoader.getInstance().init(config);



        new GetWorkingNowTutors().execute();

    }

    private class GetWorkingNowTutors extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // Showing progress dialog
            pDialog = new ProgressDialog(MainActivity.this);
            pDialog.setMessage("Please wait...");
            pDialog.setCancelable(false);
            pDialog.show();

        }

        @Override
        protected Void doInBackground(Void... arg0) {
            HttpHandler sh = new HttpHandler();

            // Making an HTTP request to Sandbox's Public Google Calendar API and getting response
            String jsonStr = sh.makeServiceCall(BASICURL + formattedCurrentTime + "&timeMax=" + formattedTimeMax + APIKEY);

            Log.e(TAG, "Response from url: " + jsonStr);
            Log.e(TAG, formattedTimeMax);

            if (jsonStr != null) {
                try {
                    JSONObject jsonObj = new JSONObject(jsonStr);

                    // Getting JSON Array node
                    JSONArray events = jsonObj.getJSONArray("items");

                    // looping through All Contacts
                    for (int i = 0; i < events.length(); i++) {
                        JSONObject eventItem = events.getJSONObject(i);

                        String name = eventItem.getString("summary");
                        String tutorStartTime = eventItem.getJSONObject("start").getString("dateTime");
                        String tutorEndTime = eventItem.getJSONObject("end").getString("dateTime");

                        //convert time String to Date
                        try {
                            currentTime = sdf.parse(formattedCurrentTime);
                            startTime = sdf.parse(tutorStartTime);
                            endTime = sdf.parse(tutorEndTime);
                        } catch (java.text.ParseException e) {
                            e.printStackTrace();
                        }
                        Log.e(TAG, tutorStartTime);
                        Log.e(TAG, tutorEndTime);
                        Log.e(TAG, String.valueOf(currentTime.compareTo(startTime)));

                        // fixing naming consistence problem
                        if (name.equals("Emily Z.")){
                            name = "EmilyZ";
                        }
                        String imageSrc = HTTP_SANDBOX_IMAGE + name + IMAGESUFFIX;

                          // tmp hash map for working-now tutors
                        HashMap<String, String> currentWorkingTutorMap = new HashMap<>();

                        // adding each child node to HashMap key => value
                        // if the current time has past the start time of the tutoring and has not yet past the end time
                        // of the tutoring, then add
                        if (currentTime.compareTo(startTime) >= 0 && currentTime.compareTo(endTime) == -1) {
                            currentWorkingTutorMap.put("name", name);
                            currentWorkingTutorMap.put("imagesource", imageSrc);
                        }


                        // adding contact to contact list
                        tutorList.add(currentWorkingTutorMap);
                    }
                } catch (final JSONException e) {
                    Log.e(TAG, "Json parsing error: " + e.getMessage());
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(),
                                    "Json parsing error: " + e.getMessage(),
                                    Toast.LENGTH_LONG)
                                    .show();
                        }
                    });

                }
            } else {
                Log.e(TAG, "Couldn't get json from server.");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(),
                                "Couldn't get json from server. Check LogCat for possible errors!",
                                Toast.LENGTH_LONG)
                                .show();
                    }
                });

            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            // Dismiss the progress dialog
            if (pDialog.isShowing())
                pDialog.dismiss();
            /**
             * Updating parsed JSON data into ListView
             * */

            CustomAdapter adapter = new CustomAdapter(
                    MainActivity.this, tutorList,
                    R.layout.list_item, new String[]{"name","imagesource"}, new int[]{R.id.name, R.id.tutorImage});

            lv.setAdapter(adapter);

        }



    }


}


