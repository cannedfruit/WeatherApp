package com.cannedfruit.weatherapp;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.cannedfruit.weatherapp.CurrentWeatherXMLParser.Current;
import com.cannedfruit.weatherapp.XMLParser.Day;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

/**
 * Created by Sarah on 03/10/2015.
 */


public class MainActivity extends AppCompatActivity {

    private static final String PREFS = "prefs";
    private static final String PREF_CITY = "city";
    SharedPreferences mSharedPreferences;

    private static final String QUERY_URL = "http://api.openweathermap.org/data/2.5/weather?q=";
    private static final String FORECAST_URL = "http://api.openweathermap.org/data/2.5/forecast/daily?q=";
    private static final String DEBUG_TAG = "OPEN WEATHER MAP";
    private static final String NOT_FOUND = "City not found";

    TextView textCity;
    TextView textTemperature;
    TextView textCondition;
    TextView textAirPressure;
    TextView textHumidity;
    EditText editCity;
    Button searchButton;
    ImageView imageDay1;
    ImageView imageDay2;
    ImageView imageDay3;
    ImageView imageDay4;
    TextView textDay1;
    TextView textDay2;
    TextView textDay3;
    TextView textDay4;
    TextView titleDay1;
    TextView titleDay2;
    TextView titleDay3;
    TextView titleDay4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_main);

        textCity = (TextView) findViewById(R.id.textCity);
        editCity = (EditText) findViewById(R.id.editCity);

        //clear previous text when editText is focused on
        editCity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editCity.setText("");
            }
        });
        //allow a user to enter data through the keyboard
        editCity.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(final TextView v, final int actionId, final KeyEvent event) {
                boolean handled = true;

                // Some phones disregard the IME setting option in the xml, instead
                // they send IME_ACTION_UNSPECIFIED so we need to catch that
                if (EditorInfo.IME_ACTION_DONE == actionId || EditorInfo.IME_ACTION_UNSPECIFIED == actionId) {
                    // Grab the EditText's input
                    String inputCity = editCity.getText().toString();

                    // Put it into memory (don't forget to commit!)
                    SharedPreferences.Editor e = mSharedPreferences.edit();
                    e.putString(PREF_CITY, inputCity);
                    e.apply();

                    // query the city which returns info from openweathermap and parses the xml
                    try {
                        queryCity(inputCity);
                    } catch (IOException e1) {
                        e1.printStackTrace();
                        textCity.setText(NOT_FOUND);
                    }

                    handled = false;
                }

                return handled;
            }

        });

        //or a user can search a city using the button
        searchButton = (Button) findViewById(R.id.searchButton);
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Grab the EditText's input
                String inputCity = editCity.getText().toString();

                // Put it into memory (don't forget to commit!)
                SharedPreferences.Editor e = mSharedPreferences.edit();
                e.putString(PREF_CITY, inputCity);
                e.apply();

                // query the city which returns info from openweathermap and parses the xml
                try {
                    queryCity(inputCity);
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        });

        //if a user has entered a city previously, it will save this data
        displayCity();
        textCondition = (TextView) findViewById(R.id.textCondition);
        textTemperature = (TextView) findViewById(R.id.textTemperature);
        textAirPressure = (TextView)findViewById(R.id.textAirPressure);
        textHumidity = (TextView)findViewById(R.id.textHumidity);

        //load 4 day forecast
        titleDay1 = (TextView) findViewById(R.id.titleDay1);
        titleDay2 = (TextView) findViewById(R.id.titleDay2);
        titleDay3 = (TextView) findViewById(R.id.titleDay3);
        titleDay4 = (TextView) findViewById(R.id.titleDay4);
        textDay1 = (TextView) findViewById(R.id.textDay1);
        textDay2 = (TextView) findViewById(R.id.textDay2);
        textDay3 = (TextView) findViewById(R.id.textDay3);
        textDay4 = (TextView) findViewById(R.id.textDay4);
        imageDay1 = (ImageView) findViewById(R.id.imageDay1);
        imageDay2 = (ImageView) findViewById(R.id.imageDay2);
        imageDay3 = (ImageView) findViewById(R.id.imageDay3);
        imageDay4 = (ImageView) findViewById(R.id.imageDay4);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void displayCity() {

        // Access the device's key-value storage
        mSharedPreferences = getSharedPreferences(PREFS, MODE_PRIVATE);

        // Read the stored city,
        // or an empty string if nothing found
        final String name = mSharedPreferences.getString(PREF_CITY, "");

        if (name.length() > 0) {

            textCity.setText(name);
            try {
                queryCity(name);
            } catch (IOException e) {
                e.printStackTrace();
            }

        } else {

            // otherwise, show a dialog to ask for their name
            AlertDialog.Builder alert = new AlertDialog.Builder(this);
            alert.setTitle("Hello!");
            alert.setMessage("What city do you want to search?");

            // Create EditText for entry
            final EditText input = new EditText(this);
            alert.setView(input);

            // Make an "OK" button to save the name
            alert.setPositiveButton("search", new DialogInterface.OnClickListener() {

                public void onClick(DialogInterface dialog, int whichButton) {

                    // Grab the EditText's input
                    String inputName = input.getText().toString();

                    // Put it into memory (don't forget to commit!)
                    SharedPreferences.Editor e = mSharedPreferences.edit();
                    e.putString(PREF_CITY, inputName);
                    e.apply();

                    // call queryCity to call API
                    textCity.setText(inputName);
                    try {
                        queryCity(inputName);
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                }
            });

            // Make a "Cancel" button
            // that simply dismisses the alert
            alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {

                public void onClick(DialogInterface dialog, int whichButton) {}
            });

            alert.show();
        }
    }

    private void queryCity(String searchString) throws IOException{

        // Before attempting to fetch the URL, makes sure that there is a network connection.
        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {

            // Prepare your search string to be put in a URL
            // It might have reserved characters or something
            String urlString = "";
            try {
                urlString = URLEncoder.encode(searchString, "UTF-8");
            } catch (UnsupportedEncodingException e) {

                // if this fails for some reason, let the user know why
                e.printStackTrace();
                Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }


            new DownloadXmlTask().execute(QUERY_URL + urlString + getString(R.string.QueryAPI));
            new DownloadForecastXML().execute(FORECAST_URL + urlString + getString(R.string.forecastAPI));
        } else {
            textCity.setText(R.string.NoNetwork);
        }
    }

    // Uses AsyncTask to create a task away from the main UI thread. This task takes a
    // URL string and uses it to create an HttpUrlConnection. Once the connection
    // has been established, the AsyncTask downloads the contents of the webpage as
    // an InputStream. Finally, the InputStream is converted into a string, which is
    // displayed in the UI by the AsyncTask's onPostExecute method.
    private class DownloadXmlTask extends AsyncTask<String, Void, Current> {
        @Override
        protected Current doInBackground(String... urls) {
            // params comes from the execute() call: params[0] is the url.
            try {
                return downloadUrl(urls[0]);
            } catch (XmlPullParserException | IOException e) {
                e.printStackTrace();
                return null;
            }
        }
        @Override
        protected void onPostExecute(Current result) {
            if (result != null) {
                String location = result.city + ", " + result.country;
                textCity.setText(location);
                textTemperature.setText(result.temperature);
                textCondition.setText(result.weather);
                textHumidity.setText(result.humidity);
                humidityChange(result.highHum, result.medHum);
                textAirPressure.setText(result.pressure);
                apChange(result.highAP);
                backgroundChange(result.icon);
            } else {
                textCity.setText(NOT_FOUND);
            }
        }
    }

    //depending on relative humidity information returned from openweathermap,
    //icon representing humidity changes depending on low (<50), med(50-70), and high (>70)
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    private void humidityChange(boolean high, boolean med){
        if (high){
            TextView highHum = (TextView)findViewById(R.id.textHumidity);
            highHum.setCompoundDrawablesRelativeWithIntrinsicBounds(R.mipmap.high_hum, 0, 0, 0);
        } else if (med){
            TextView medHum = (TextView)findViewById(R.id.textHumidity);
            medHum.setCompoundDrawablesRelativeWithIntrinsicBounds(R.mipmap.med_hum, 0, 0, 0);
        } else {
            TextView lowHum = (TextView)findViewById(R.id.textHumidity);
            lowHum.setCompoundDrawablesRelativeWithIntrinsicBounds(R.mipmap.low_hum, 0, 0, 0);
        }
    }

    //depending on air pressure information returned from openweathermap,
    //icon representing air pressure changes depending on low (<1013.25hpa) or high (>1013.25hpa)
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    private void apChange(boolean high){
        if (high){
            TextView highAP = (TextView)findViewById(R.id.textAirPressure);
            highAP.setCompoundDrawablesRelativeWithIntrinsicBounds(R.mipmap.high_ap, 0, 0, 0);
        } else{

            TextView highHum = (TextView)findViewById(R.id.textAirPressure);
            highHum.setCompoundDrawablesRelativeWithIntrinsicBounds(R.mipmap.low_ap, 0, 0, 0);
        }
    }
    //depending on the icon determined to represent weather changes by openweathermap.org,
    //the background is changed to reflect the weather conditions
    private void backgroundChange(String icon) {

        LinearLayout layout = (LinearLayout) findViewById(R.id.weatherLayout);
        switch (icon) {
            case "01d":
            case "01n":
                layout.setBackgroundResource(R.drawable.sun_new);
                break;
            case "02d":
            case "02n":
                layout.setBackgroundResource(R.drawable.few_clouds_new);
                break;
            case "03d":
            case "03n":
                layout.setBackgroundResource(R.drawable.clouds_new);
                break;
            case "04d":
            case "04n":
                layout.setBackgroundResource(R.drawable.broken_clouds_new);
                break;
            case "09d":
            case "09n":
                layout.setBackgroundResource(R.drawable.show_rain_new);
                break;
            case "10d":
            case "10n":
                layout.setBackgroundResource(R.drawable.rain_new);
                break;
            case "11d":
            case "11n":
                layout.setBackgroundResource(R.drawable.thunder_new);
                break;
            case "13d":
            case "13n":
                layout.setBackgroundResource(R.drawable.snow_new);
                break;
            default:
                layout.setBackgroundResource(R.drawable.mist_new);
                break;
        }
    }

    // Given a URL, establishes an HttpUrlConnection and retrieves
    // the web page content as a InputStream, which it returns as
    // a string.
    private Current downloadUrl(String myurl) throws IOException, XmlPullParserException {
        InputStream is;
        CurrentWeatherXMLParser weatherParse = new CurrentWeatherXMLParser();

        URL url = new URL(myurl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setReadTimeout(10000 /* milliseconds */);
        conn.setConnectTimeout(15000 /* milliseconds */);
        conn.setRequestMethod("GET");
        conn.setDoInput(true);
        // Starts the query
        conn.connect();
        int response = conn.getResponseCode();
        Log.d(DEBUG_TAG, "The response is: " + response);
        is = conn.getInputStream();

        //starts the parse
        return weatherParse.parse(is);

    }


    // Uses AsyncTask to create a task away from the main UI thread. This task takes a
    // URL string and uses it to create an HttpUrlConnection. Once the connection
    // has been established, the AsyncTask downloads the contents of the webpage as
    // an InputStream. Finally, the InputStream is converted into a string, which is
    // displayed in the UI by the AsyncTask's onPostExecute method.
    private class DownloadForecastXML extends AsyncTask<String, Void, Day> {
        @Override
        protected Day doInBackground(String... urls) {
            // params comes from the execute() call: params[0] is the url.
            try {
                return downloadForecastUrl(urls[0]);
            } catch (XmlPullParserException | IOException e) {
                e.printStackTrace();
                return null;
            }
        }
        @Override
        protected void onPostExecute(XMLParser.Day result) {
            if (result != null) {
                titleDay1.setText(result.day1.date);
                titleDay2.setText(result.day2.date);
                titleDay3.setText(result.day3.date);
                titleDay4.setText(result.day4.date);

                String day1 = result.day1.temperature + "\n" + result.day1.humidity + "\n" + result.day1.pressure;
                String day2 = result.day2.temperature + "\n" + result.day2.humidity + "\n" + result.day2.pressure;
                String day3 = result.day3.temperature + "\n" + result.day3.humidity + "\n" + result.day3.pressure;
                String day4 = result.day4.temperature + "\n" + result.day4.humidity + "\n" + result.day4.pressure;

                textDay1.setText(day1);
                textDay2.setText(day2);
                textDay3.setText(day3);
                textDay4.setText(day4);

                int icon1 = getResources().getIdentifier(iconChange(result.day1.icon), "mipmap", getPackageName());
                imageDay1.setImageResource(icon1);
                int icon2 = getResources().getIdentifier(iconChange(result.day2.icon), "mipmap", getPackageName());
                imageDay2.setImageResource(icon2);
                int icon3 = getResources().getIdentifier(iconChange(result.day3.icon), "mipmap", getPackageName());
                imageDay3.setImageResource(icon3);
                int icon4 = getResources().getIdentifier(iconChange(result.day4.icon), "mipmap", getPackageName());
                imageDay4.setImageResource(icon4);
            } else {
                textDay1.setText(NOT_FOUND);
            }
        }
    }
    //depending on the icon determined to represent weather changes by openweathermap.org,
    //the background is changed to reflect the weather conditions
    private String iconChange(String icon) {

        switch (icon) {
            case "01d":
            case "01n":
                return "sun_icon";
            case "02d":
            case "02n":
                return "few_clouds_icon";
            case "03d":
            case "03n":
                return "clouds_icon";
            case "04d":
            case "04n":
                return "broken_clouds_icon";
            case "09d":
            case "09n":
                return "show_rain_icon";
            case "10d":
            case "10n":
                return "rain_icon";
            case "11d":
            case "11n":
                return "thunder_icon";
            case "13d":
            case "13n":
                return "snow_icon";
            default:
                return "mist_icon";
        }
    }

    // Given a URL, establishes an HttpUrlConnection and retrieves
    // the web page content as a InputStream, which it returns as
    // a string.
    private Day downloadForecastUrl(String myurl) throws IOException, XmlPullParserException {
        InputStream is;
        XMLParser weatherParse = new XMLParser();

        URL url = new URL(myurl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setReadTimeout(10000 /* milliseconds */);
        conn.setConnectTimeout(15000 /* milliseconds */);
        conn.setRequestMethod("GET");
        conn.setDoInput(true);
        // Starts the query
        conn.connect();
        int response = conn.getResponseCode();
        Log.d(DEBUG_TAG, "The response is: " + response);
        is = conn.getInputStream();

        //starts the parse
        return weatherParse.parse(is);

    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            View v = getCurrentFocus();
            if ( v instanceof EditText) {
                Rect outRect = new Rect();
                v.getGlobalVisibleRect(outRect);
                if (!outRect.contains((int)event.getRawX(), (int)event.getRawY())) {
                    v.clearFocus();
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                }
            }
        }
        return super.dispatchTouchEvent( event );
    }
}
