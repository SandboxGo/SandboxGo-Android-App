package com.xu.jialu.sandboxgo;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
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
    Date newDateAndTime;
    Date startTime;
    Date endTime;
    static String selectedDate = "";
    static String selectedTime = "";
    HttpHandler sh = new HttpHandler();

    private static TextView dateView;
    private static TextView timeView;

    ArrayList<HashMap<String, String>> tutorList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tutorList = new ArrayList<>();

        lv = (ListView) findViewById(R.id.list);


        Button datepicker;
        Button timepicker;
        Button nowpicker;
        datepicker = (Button) findViewById(R.id.datePicker);
        timepicker = (Button) findViewById(R.id.timePicker);
        nowpicker = (Button) findViewById(R.id.now);

        dateView = (TextView) findViewById(R.id.dateText);
        timeView = (TextView) findViewById(R.id.timeText);

        // get current time
        try {
            currentTime = sdf.parse(formattedCurrentTime);
        } catch (ParseException e) {
            e.printStackTrace();
        }


        datepicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog(v);
            }
        });

        timepicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTimePickerDialog(v);
            }
        });

        nowpicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tutorList.clear();
                new GetWorkingNowTutors().execute();
            }
        });

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
                            startTime = sdf.parse(tutorStartTime);
                            endTime = sdf.parse(tutorEndTime);
                        } catch (java.text.ParseException e) {
                            e.printStackTrace();
                        }

                        // fix naming consistence problem
                        if (name.equals("Emily Z")){
                            name = "EmilyZ";
                        }
                        if (name.equals("Emily K.")){
                            name = "EmilyK";
                        }
                        if (name.equals("Se Jin")){
                            name = "SeJin";
                        }
                        String imageSrc = HTTP_SANDBOX_IMAGE + name + IMAGESUFFIX;
                        String course = getTutorCourse(name);

                        // tmp hash map for working-now tutors
                        HashMap<String, String> currentWorkingTutorMap = new HashMap<>();

                        // adding each child node to HashMap key => value
                        // if the current time has past the start time of the tutoring and has not yet past the end time
                        // of the tutoring, then add
                        if (currentTime.compareTo(startTime) >= 0 && currentTime.compareTo(endTime) == -1) {
                            currentWorkingTutorMap.put("name", name);
                            currentWorkingTutorMap.put("imagesource", imageSrc);
                            currentWorkingTutorMap.put("course", course);
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
                    R.layout.list_item, new String[]{"name","imagesource","course"}, new int[]{R.id.name, R.id.tutorImage, R.id.course});

            lv.setAdapter(adapter);

        }

    }

    // date picker method
    public void showDatePickerDialog(View v) {
        DialogFragment newFragment = new DatePickerFragment();
        FragmentManager fm = MainActivity.this.getFragmentManager();
        newFragment.show(fm, "datePicker");
    }

    // time picker method
    public void showTimePickerDialog(View v) {
        DialogFragment newFragment = new TimePickerFragment();
        FragmentManager fm2 = MainActivity.this.getFragmentManager();
        newFragment.show(fm2, "timePicker");
    }


    // Time Picker Static Class
    public static class TimePickerFragment extends DialogFragment
            implements TimePickerDialog.OnTimeSetListener {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current time as the default values for the picker
            final Calendar c = Calendar.getInstance();
            int hour = c.get(Calendar.HOUR_OF_DAY);
            int minute = c.get(Calendar.MINUTE);

            // Create a new instance of TimePickerDialog and return it
            return new TimePickerDialog(getActivity(), this, hour, minute,
                    android.text.format.DateFormat.is24HourFormat(getActivity()));
        }

        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            // Do something with the time chosen by the user
            selectedTime = String.format("%02d:%02d:00", hourOfDay, minute);
            timeView.setText(selectedTime);
            ((MainActivity) getActivity()).applyTime();
        }
    }

    // Date Picker Static Class
    public static class DatePickerFragment extends DialogFragment
            implements DatePickerDialog.OnDateSetListener {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current date as the default date in the picker
            final Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);

            // Create a new instance of DatePickerDialog and return it
            return new DatePickerDialog(getActivity(), this, year, month, day);
        }

        public void onDateSet(DatePicker view, int year, int month, int day) {
            // Do something with the date chosen by the user
            selectedDate = String.format("%04d-%02d-%02d", year, month + 1, day);
            dateView.setText(selectedDate);
            ((MainActivity) getActivity()).applyTime();
        }
    }

        // apply selected date and time
    public void applyTime() {
        new GetWorkingTutors().execute();
    }


    // Get selected-time working tutors via an AsyncTask
    private class GetWorkingTutors extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // Showing progress dialog
            pDialog = new ProgressDialog(MainActivity.this);
            pDialog.setMessage("Please wait...");
            pDialog.setCancelable(false);
            pDialog.show();
            // clear previous tutorList
            tutorList.clear();

        }

        @Override
        protected Void doInBackground(Void... arg0) {

            String newDateStr;
            if (!selectedTime.equals("")) {
                newDateStr = selectedDate + "T" + selectedTime + "-04:00";
            } else {
                newDateStr = selectedDate + "T00:00:00-04:00";
            }
            String newTimeMax = selectedDate + "T23:59:59-04:00";

            // get selected time
            try {
                newDateAndTime = sdf.parse(newDateStr);
            } catch (ParseException e) {
                e.printStackTrace();
            }

            Log.e(TAG, selectedTime);
            Log.e(TAG, newDateStr);
            Log.e(TAG, newTimeMax);
            Log.e(TAG, BASICURL + newDateStr + "&timeMax=" + newTimeMax + APIKEY);

            // Making an HTTP request to Sandbox's Public Google Calendar API and getting response
            String newJSONStr = sh.makeServiceCall(BASICURL + newDateStr + "&timeMax=" + newTimeMax + APIKEY);

            Log.e(TAG, "Response from url: " + newJSONStr);

            if (newJSONStr != null) {
                try {
                    JSONObject jsonObj = new JSONObject(newJSONStr);

                    // Getting JSON Array node
                    JSONArray events = jsonObj.getJSONArray("items");

                    // looping through All Contacts
                    for (int i = 0; i < events.length(); i++) {
                        JSONObject eventItem = events.getJSONObject(i);

                        String name = eventItem.getString("summary");
                        String tutorStartTime = eventItem.getJSONObject("start").getString("dateTime");
                        String tutorEndTime = eventItem.getJSONObject("end").getString("dateTime");
                        Log.e(TAG, tutorStartTime);
                        Log.e(TAG, tutorEndTime);
                        //convert time String to Date
                        try {
                            startTime = sdf.parse(tutorStartTime);
                            endTime = sdf.parse(tutorEndTime);
                        } catch (java.text.ParseException e) {
                            e.printStackTrace();
                        }

                        // fix naming consistence problem
                        if (name.equals("Emily Z") || name.equals("Emily Z.")){
                            name = "EmilyZ";
                        }
                        if (name.equals("Emily K.")){
                            name = "EmilyK";
                        }
                        if (name.equals("Se Jin")){
                            name = "SeJin";
                        }
                        String imageSrc = HTTP_SANDBOX_IMAGE + name + IMAGESUFFIX;
                        String course = getTutorCourse(name);

                        // tmp hash map for working-now tutors
                        HashMap<String, String> currentWorkingTutorMap = new HashMap<>();

                        // adding each child node to HashMap key => value
                        // if the current time has past the start time of the tutoring and has not yet past the end time
                        // of the tutoring, then add; OR if the user only chooses date, show ALL tutors working on that day
                        if (newDateAndTime.compareTo(startTime) >= 0 && newDateAndTime.compareTo(endTime) == -1 || newDateStr.substring(10).equals("T00:00:00-04:00")) {
                            currentWorkingTutorMap.put("name", name);
                            currentWorkingTutorMap.put("imagesource", imageSrc);
                            currentWorkingTutorMap.put("course", course);
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
                    R.layout.list_item, new String[]{"name","imagesource","course"}, new int[]{R.id.name, R.id.tutorImage, R.id.course});

            lv.setAdapter(adapter);

        }

    }


    // Get specific courses for a specific tutor
    private String getTutorCourse(String name) {
        switch (name.toLowerCase()) {
            case "max":
                return "IT101, CS603, CS605, CS607";
            case "mark":
                return "IT101, CS603, CS605, CS607";
            case "emilyk":
                return "IT101, CS603, CS605, CS607";
            case "jake":
                return "IT101, CS603, CS605, CS607";
            case "tim":
                return "IT101, CS603, CS605, CS607";
            case "karan":
                return "IT101, CS603, CS605, CS607";
            case "nicollette":
                return "IT101, CS603, CS605, CS607";
            case "emilyz":
                return "IT101, CS603, CS605, CS607";
            case "brandon":
                return "IT101, CS603, CS605, CS607";
            case "taylor":
                return "IT101, CS603, CS605, CS607";
            case "yang":
                return "IT101, CS603, CS605, CS607";
            case "jacob":
                return "IT101, CS603, CS605, CS607";
            case "jonathan":
                return "IT101, CS603, CS605, CS607";
            case "saalik":
                return "IT101, CS603, CS605, CS607";
            case "yue":
                return "IT101, CS603, CS605, CS607";
            case "mae":
                return "IT101, CS603, CS605, CS607";
            case "rob":
                return "IT101, CS603, CS605, CS607";
            case "angela":
                return "IT101, CS603, CS605, CS607";
            case "kaitlyn":
                return "IT101, CS603, CS605, CS607";
            case "michael":
                return "IT101, CS603, CS605, CS607";
            case "anoushka":
                return "IT101, CS603, CS605, CS607";
            case "sejin":
                return "IT101, CS603, CS605, CS607";
            case "sebastian":
                return "IT101, CS603, CS605, CS607";
            case "becky":
                return "IT101, CS603, CS605, CS607";
            case "sumant":
                return "IT101, CS603, CS605, CS607";
            case "pranita":
                return "IT101, CS603, CS605, CS607";
            case "andrew":
                return "IT101, CS603, CS605, CS607";
        }
        return "Sorry, no course information for this tutor";
    }

}


