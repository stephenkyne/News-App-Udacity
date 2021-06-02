package stephenkyne.example.org.newsapp;

import android.text.TextUtils;
import android.util.Log;
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
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public final class QueryUtils {
    // LOG TAG
    private static final String LOG_TAG = "QueryUtils Error: ";

    //Read timeout for setting up the HTTP request, in milliseconds
    static final int TIMEOUT_READ = 10000;

    // Connect timeout for setting up the HTTP request, in milliseconds
    static final int TIMEOUT_CONNECT = 15000;

    // HTTP response code when the request is successful
    static final int SUCCESS_RESPONSE_CODE = 200;

    //Request method type "GET" for reading information from the server
    static final String REQUEST_METHOD_GET = "GET";

    //JSON Keys used
    static final String KEY_RESPONSE = "response";
    static final String KEY_RESULTS = "results";
    // "webTitle" is used twice
    // The title of the article.
    // The name of the article's author
    static final String KEY_WEB_TITLE = "webTitle";
    static final String KEY_SECTION_NAME = "sectionName";
    static final String KEY_WEB_PUB_DATE = "webPublicationDate";
    static final String KEY_WEB_URL = "webUrl";
    static final String KEY_TAGS = "tags";
    static final String KEY_FIELDS = "fields";
    static final String KEY_TRAIL_TEXT = "trailText";

    // Sends off the URL to be checked.
    public static List<Article> fetchArticleData(String requestUrl) {
        URL url = createUrl(requestUrl);
        String jsonResponse = null;
        // sends url to make a HTTP Request
        try {
            jsonResponse = makeHttpRequest(url);
        } catch (IOException e) {
            Log.e(LOG_TAG, "makeHttpRequest problem. ", e);
        }

        // Calls the jsonResponse and stores the return value in articles.
        // One article at a time  with requested data that is there.
        List<Article> articles = extractFeatureFromJson(jsonResponse);

        // articles, an array of articles is returned
        return articles;
    }

    // URL is checked
    private static URL createUrl(String stringUrl) {
        URL url = null;
        try {
            url = new URL(stringUrl);
        } catch (MalformedURLException e) {
            Log.e(LOG_TAG, "Problem building the URL: ", e);
        }
        return url;
    }

    // called from the fetchArticleData
    // Try's to make a connection to the URL sent.
    private static String makeHttpRequest(URL url) throws IOException {
        String jsonResponse = "";

        // Checks for null URL, if null returns and doest not try to make connection
        if (url == null) {
            return jsonResponse;
        }

        // URL not null, try's to make a connection
        HttpURLConnection urlConnection = null;
        InputStream inputStream = null;
        try {
            urlConnection = (HttpURLConnection) url.openConnection();
            // Set a timeout to read of 10000 ms = 1 second
            urlConnection.setReadTimeout(TIMEOUT_READ);
            // Set a timeout to read of 15000 ms = 1.5 second
            urlConnection.setConnectTimeout(TIMEOUT_CONNECT);
            // "get" string is used to get data
            urlConnection.setRequestMethod(REQUEST_METHOD_GET);
            urlConnection.connect();

            // Checks to see if the connection response code is correct, "200"
            // If the response code is NOT 200 the error is logged.
            if (urlConnection.getResponseCode() == SUCCESS_RESPONSE_CODE) {
                // Response code correct, gets InputStream and reads from it
                inputStream = urlConnection.getInputStream();
                jsonResponse = readFromStream(inputStream);
            } else {
                Log.e(LOG_TAG, "makeHttpRequest response code: " + urlConnection.getResponseCode());
            }
            // Errors are caught and printed in the log.
        } catch (IOException e) {
            Log.e(LOG_TAG, "makeHttpRequest Problem: ", e);
        } finally {
            if (urlConnection != null) {
                // urlConnection is disconnected.
                urlConnection.disconnect();
            }
            if (inputStream != null) {
                // inputStream is closed.
                inputStream.close();
            }
        }
        // The jsonResponse is returned to fetchArticleData
        return jsonResponse;
    }

    // called by makeHttpRequest
    // Reads the downloaded data from and changes it into a String.
    private static String readFromStream(InputStream inputStream) throws IOException {
        StringBuilder output = new StringBuilder();
        if (inputStream != null) {
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream,
                    StandardCharsets.UTF_8);
            BufferedReader reader = new BufferedReader(inputStreamReader);
            String line = reader.readLine();
            while (line != null) {
                output.append(line);
                line = reader.readLine();
            }
        }
        // Out put is returned to makeHttpRequest
        return output.toString();
    }

    /**
     * Return a list of {@link Article} objects that has been built up from
     * parsing a JSON response.
     */
    private static List<Article> extractFeatureFromJson(String articleJSON) {
        // Check if empty or null.
        if (TextUtils.isEmpty(articleJSON)) {
            return null;
        }
        // List of of Articles is created, data is stored inside here.
        List<Article> articles = new ArrayList<>();

        //Trying to read the JSON
        try {
            // JSONObject with the data and arrays is stored so it can be searched.
            JSONObject baseJsonResponse = new JSONObject(articleJSON);

            // Finds the object "response", this contains the array where the articles data is stored.
            JSONObject responseJsonObject = baseJsonResponse.getJSONObject(KEY_RESPONSE);

            // Finds and gets the Array of "results", these are the array of articles
            JSONArray resultsArray = responseJsonObject.getJSONArray(KEY_RESULTS);

            // Search's the Array and take out the wanted values stored inside.
            for (int i = 0; i < resultsArray.length(); i++) {
                // gets the values in the array at the current value of i
                JSONObject currentArticle = resultsArray.getJSONObject(i);
                // Stores the value of "webTitle" into articleTitle
                // This is the Title of the article
                String articleTitle = currentArticle.getString(KEY_WEB_TITLE);
                // Stores the value of "sectionName" into section
                // returned are, World news, US news, Sports ect...
                String section = currentArticle.getString(KEY_SECTION_NAME);
                // Stores the value of "webPublicationDate" into webPubDate
                String webPubDate = currentArticle.getString(KEY_WEB_PUB_DATE);
                // For a given article, extract the value for the key called "webUrl"
                // This is the website address of the article
                String articleUrl = currentArticle.getString(KEY_WEB_URL);

                // checks for nulls
                String author = null;
                if (currentArticle.has(KEY_TAGS)) {
                    // Finds the key with "tags"
                    JSONArray tagsArray = currentArticle.getJSONArray(KEY_TAGS);
                    if (tagsArray.length() != 0) {
                        // Extract the first JSONObject in the tagsArray
                        // Array, starts at zero
                        JSONObject firstTagsItem = tagsArray.getJSONObject
                                (0);
                        // String author is assigned the value of "webTitle"
                        author = firstTagsItem.getString(KEY_WEB_TITLE);
                    }
                }

                // For a given news, if it contains the key called "fields", extract JSONObject
                // associated with the key "fields"
                String trailText = null;
                if (currentArticle.has(KEY_FIELDS)) {
                    // Extract the JSONObject associated with the key called "fields"
                    JSONObject fieldsObject = currentArticle.getJSONObject(KEY_FIELDS);
                    // Looks for the  "trailText" key, if it is there they get the value for trailText
                    if (fieldsObject.has(KEY_TRAIL_TEXT)) {
                        trailText = fieldsObject.getString(KEY_TRAIL_TEXT);
                    }
                }

                // Section article is in, Date and time Published, Article title, Article URL,
                // author Name, Summary of article
                Article article = new Article(section, webPubDate, articleTitle, articleUrl,
                        author, trailText);
                articles.add(article);
            }
        } catch (JSONException e) {
            // Error is caught
            // Error message is created and posted
            Log.e(LOG_TAG, "extractFeatureFromJson problem: ", e);
        }
        // Return the list of articles
        return articles;
    }
}
