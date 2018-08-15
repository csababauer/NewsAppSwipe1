package com.example.csaba.newsappswipe1;

import android.os.AsyncTask;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Random;

import com.squareup.picasso.Picasso;



public class MainActivity extends AppCompatActivity  {

    private static final String requestUrl =

            /** api builder link: https://open-platform.theguardian.com/explore/  */
            /** swipe app : https://antonioleiva.com/swiperefreshlayout/*/

            //   "https://content.guardianapis.com/search?q=technology/bitcoin&from-date=2014-01-01&api-key=1da394a8-c369-4807-bc2a-a49b797f8e62";
            //   "https://content.guardianapis.com/search?q=news&format=json&from-date=2010-01-01&show-tags=contributor&order-by=newest&api-key=1da394a8-c369-4807-bc2a-a49b797f8e62";
            //"https://content.guardianapis.com/search?page-size=1&order-by=newest&use-date=published&show-tags=contributor&show-fields=lastModified,headline,thumbnail,bodyText,webPublicationDate&show-elements=image&order-by=newest&api-key=1da394a8-c369-4807-bc2a-a49b797f8e62";
            "https://content.guardianapis.com/search?page-size=1&order-by=newest&use-date=published&show-tags=contributor&show-fields=lastModified,headline,thumbnail,bodyText&show-elements=image&order-by=newest&api-key=1da394a8-c369-4807-bc2a-a49b797f8e62";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        final SwipeRefreshLayout swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_container);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swipeRefreshLayout.setRefreshing(true);
                (new Handler()).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        swipeRefreshLayout.setRefreshing(false);


                        // Kick off an {@link AsyncTask} to perform the network request
                        GuardianAsyncTask task = new GuardianAsyncTask();
                        task.execute();


                    }
                },1500);
            }
        });


        // Kick off an {@link AsyncTask} to perform the network request
        GuardianAsyncTask task = new GuardianAsyncTask();
        task.execute();
    }

//    @Override
//    public void onRefresh() {
//        ImageView image = findViewById(R.id.thumbnail);
//        image.setVisibility(View.GONE);
//    }


    /**
     * {@link AsyncTask} to perform the network request on a background thread, and then
     * update the UI with the first earthquake in the response.
     */
    private class GuardianAsyncTask extends AsyncTask<URL, Void, Event> {

        @Override
        protected Event doInBackground(URL... urls) {
            // Create URL object
            URL url = createUrl(requestUrl);

            // Perform HTTP request to the URL and receive a JSON response back
            String jsonResponse = "";
            try {
                jsonResponse = makeHttpRequest(url);
            } catch (IOException e) {
            }

            // Extract relevant fields from the JSON response and create an {@link Event} object
            Event news = extractFeatureFromJson(jsonResponse);

            // Return the {@link Event} object as the result fo the {@link TsunamiAsyncTask}
            return news;
        }

        @Override
        protected void onPostExecute(Event news) {
            if (news == null) {
                return;
            }

            updateUi(news);
        }

        /**
         * create URL
         */
        private URL createUrl(String stringUrl) {
            URL url = null;
            try {
                url = new URL(stringUrl);
            } catch (MalformedURLException exception) {
                return null;
            }
            return url;
        }

        /**
         * make Http request
         */
        private String makeHttpRequest(URL url) throws IOException {
            String jsonResponse = "";
            // If the URL is null, then return early.
            if (url == null) {
                return jsonResponse;
            }
            HttpURLConnection urlConnection = null;
            InputStream inputStream = null;
            try {
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.setReadTimeout(10000 /* milliseconds */);
                urlConnection.setConnectTimeout(15000 /* milliseconds */);
                urlConnection.connect();

                // If the request was successful (response code 200),
                // then read the input stream and parse the response.
                if (urlConnection.getResponseCode() == 200) {
                    inputStream = urlConnection.getInputStream();
                    jsonResponse = readFromStream(inputStream);
                } else {
                }
            } catch (IOException e) {
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (inputStream != null) {
                    // function must handle java.io.IOException here
                    inputStream.close();
                }
            }
            return jsonResponse;
        }

        /**
         * read from stream
         */
        private String readFromStream(InputStream inputStream) throws IOException {
            StringBuilder output = new StringBuilder();
            if (inputStream != null) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream, Charset.forName("UTF-8"));
                BufferedReader reader = new BufferedReader(inputStreamReader);
                String line = reader.readLine();
                while (line != null) {
                    output.append(line);
                    line = reader.readLine();
                }
            }
            return output.toString();
        }

        /**
         * extract from JSON
         */
        private Event extractFeatureFromJson(String newsJSON) {
            try {
                // Create a JSONObject from the JSON response string
                JSONObject baseJsonResponse = new JSONObject(newsJSON);

                JSONObject responseJSONObject = baseJsonResponse.getJSONObject("response");

                // Extract the JSONArray associated with the key called "results"
                JSONArray resultsArray = responseJSONObject.getJSONArray("results");

                // If there are results in the features array
                if (resultsArray.length() > 0) {

                    // Get a single article at position i within the list of articles
                    JSONObject currentArticle = resultsArray.getJSONObject(0);

                    // Extract out the title, time, and tsunami values
                    String webTitle = currentArticle.getString("webTitle");
                    String webUrl = currentArticle.getString("webUrl");
                    String sectionName = currentArticle.getString("sectionName");
                    String webPublicationDate = currentArticle.getString("webPublicationDate");


                    String thumbnailUrl = null;
                    String bodyText = null;


                    /**thumbnail*/
                    if (currentArticle.has("fields")) {
                        // Extract the JSONObject associated with the key called "fields"
                        JSONObject fieldsObject = currentArticle.getJSONObject("fields");

                        //if (fieldsObject.has("trailText")) {

                            // Extract the value for the key called "thumbnail"
                            thumbnailUrl = fieldsObject.getString("thumbnail");
                            Log.i("link here:", thumbnailUrl );

                            //fields - bodyText
                            bodyText = fieldsObject.getString("bodyText");


                    }

                    // Create a new {@link Event} object
                    return new Event(webTitle, webUrl, sectionName, thumbnailUrl, bodyText, webPublicationDate);
                }
            } catch (JSONException e) {
            }
            return null;
        }

        /**
         * update UI
         */
        private void updateUi(Event news) {
            TextView titleTextView =  findViewById(R.id.title);
            titleTextView.setText(news.title);

            // Display article
            TextView dateTextView =  findViewById(R.id.bodyText);
            dateTextView.setText(news.bodyText);

            // Display date
            TextView sectionTextView = (TextView) findViewById(R.id.section);
            sectionTextView.setText(news.webPublicationDate);

            ImageView picture = findViewById(R.id.thumbnail);

//            Picasso.get().load(news.thumbnail)
//                    .placeholder(R.drawable.swipe)
//                    .resize(200, 200)
//                    .centerCrop().into(picture);

//            Picasso.get()
//                    .load(news.thumbnail)
//                    .centerCrop()
//                    .fit()
//                    .into(picture);


            //.resize(150, 100)

            Picasso.get()
                    .load(news.thumbnail)
                    .resize(400, 300)
                    .onlyScaleDown()
                    .into(picture);

        }
    }
}
