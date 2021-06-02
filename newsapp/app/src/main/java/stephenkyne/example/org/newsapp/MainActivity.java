package stephenkyne.example.org.newsapp;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Context;
import android.content.Loader;
import android.app.LoaderManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.net.NetworkInfo;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<List<Article>> {

    /**
     * Constant value for the article loader ID. We can choose any integer.
     * This really only comes into play if you're using multiple loaders.
     */
    private static final int ARTICLE_LOADER_ID = 1;
    private TextView mEmptyStateTextView;
    /**
     * Adapter for the list of articles
     */
    private ArticleAdapter articleAdapter;

    // URL base used for news from the Guardian data api
    public static final String ARTICLE_JSON_BASE_URL = "https://content.guardianapis.com/";

    // API Key, this is mine. otherwise use "test"
    public static final String API_KEY = "test";

    // Query Parameters
    public static final String PARAM_Q_QUERY = "q";
    public static final String PARAM_ORDER_BY = "order-by";
    public static final String PARAM_PAGE_SIZE = "page-size";
    public static final String PARAM_SHOW_FIELDS = "show-fields";
    public static final String PARAM_FORMAT = "format";
    public static final String PARAM_SHOW_TAGS = "show-tags";
    public static final String PARAM_API_KEY = "api-key";

    //The show tags we want our API to return
    // Used to get the author details
    public static final String SHOW_TAGS_CONTRIBUTOR = "contributor";

    // The show fields we want our API to return
    // Gets trailText, Summary of the article
    public static final String SHOW_FIELDS_TRAILTEXT = "trailText";

    // The format we want our API to return
    public static final String REQUEST_FORMAT = "json";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Find the {@link ListView} object in the view hierarchy of the {@link Activity}.
        // There should be a {@link ListView} with the view ID called list, which is declared in the
        // word_list.xml layout file.
        ListView articleListView = findViewById(R.id.list);

        // Finds textview to use for empty state
        mEmptyStateTextView = findViewById(R.id.empty_view);

        // Tells art article List View to use mEmptyStateTextView on Empty state
        articleListView.setEmptyView(mEmptyStateTextView);
        //  ArrayList<Article> articles = QueryUtils.extractArticles();
        // Create an {@link ArticleAdapter}, whose data source is a list of {@link Article}s. The
        // adapter knows how to create list items for each item in the list.

        // Create a new adapter that takes an empty list of articles as input
        articleAdapter = new ArticleAdapter(this, new ArrayList<Article>());

        // Make the {@link articleListView} use the {@link articleAdapter} we created above, so that the
        // {@link articleListView} will display list items for each {@link Article} in the list.
        articleListView.setAdapter(articleAdapter);

        // Set a click listener to open a web browser to go to the article
        articleListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {

                // Get the {@link Article} object at the given position the user clicked on
                Article article = articleAdapter.getItem(position);
                // gets website address / location of the news article
                String websiteUrl = article.getWebUrl();

                // Calls an intent to open a webBrowser and go to article website address.
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(websiteUrl));
                startActivity(intent);
            }
        });

        // Get a reference to the ConnectivityManager to check state of network connectivity
        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);

        // Get details on the currently active default data network
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

        // If there is a network connection, fetch data
        if (networkInfo != null && networkInfo.isConnected()) {
            // Get a reference to the LoaderManager, in order to interact with loaders.
            LoaderManager loaderManager = getLoaderManager();

            // Initialize the loader. Pass in the int ID constant defined above and pass in null for
            // the bundle. Pass in this activity for the LoaderCallbacks parameter (which is valid
            // because this activity implements the LoaderCallbacks interface).
            loaderManager.initLoader(ARTICLE_LOADER_ID, null, this);
        } else {
            // Otherwise, display error
            // First, hide loading indicator so error message will be visible
            View loadingIndicator = findViewById(R.id.loading_indicator);
            loadingIndicator.setVisibility(View.GONE);

            // Update empty state with no connection error message
            mEmptyStateTextView.setText(R.string.no_internet_connection);
        }
    }

    // Creates the query to access and call the select information from the news site.
    @Override
    public Loader<List<Article>> onCreateLoader(int i, Bundle bundle) {

        // Creates sharedPrefs to access the keys and values for the query
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);

        //Gets the section / news feed to be displayed
        String newsSectionSelected = sharedPrefs.getString(
                getString(R.string.settings_select_feed_key),
                getString(R.string.settings_select_feed_default));

        // Gets the number of Articles to be displayed value
        String numberOfArticles = sharedPrefs.getString(
                getString(R.string.settings_number_articles_key),
                getString(R.string.settings_number_articles_default));

        // Gets the order by settings
        String orderBy = sharedPrefs.getString(
                getString(R.string.settings_order_by_key),
                getString(R.string.settings_order_by_default));
        // Base web address/
        Uri baseUri = Uri.parse(ARTICLE_JSON_BASE_URL);
        Uri.Builder uriBuilder = baseUri.buildUpon();
        // the Section (like "sports", "world news" or "search"
        uriBuilder.appendEncodedPath(newsSectionSelected);
        uriBuilder.appendQueryParameter(PARAM_Q_QUERY, "");
        uriBuilder.appendQueryParameter(PARAM_FORMAT, REQUEST_FORMAT);
        uriBuilder.appendQueryParameter(PARAM_PAGE_SIZE, numberOfArticles);
        // Show Tags, gets the author, can also get a thumbnail (not used)
        uriBuilder.appendQueryParameter(PARAM_SHOW_TAGS, SHOW_TAGS_CONTRIBUTOR);
        // Gets the trailText / the summary of the article
        uriBuilder.appendQueryParameter(PARAM_SHOW_FIELDS, SHOW_FIELDS_TRAILTEXT);
        // order by, "newest" or "oldest"
        uriBuilder.appendQueryParameter(PARAM_ORDER_BY, orderBy);
        // API key
        uriBuilder.appendQueryParameter(PARAM_API_KEY, API_KEY);
        // returns the build URI / URL
        return new ArticleLoader(this, uriBuilder.toString());
    }

    @Override
    public void onLoadFinished(Loader<List<Article>> loader, List<Article> articles) {
        // Hide loading indicator because the data has been loaded
        View loadingIndicator = findViewById(R.id.loading_indicator);
        loadingIndicator.setVisibility(View.GONE);

        // Set empty state text to display "Sorry, No articles found."
        mEmptyStateTextView.setText(R.string.no_articles);

        // Clear the adapter of previous article data
        articleAdapter.clear();

        // If there is a valid list of {@link Articles}s, then add them to the adapter's
        // data set. This will trigger the ListView to update.
        if (articles != null && !articles.isEmpty()) {
            articleAdapter.addAll(articles);
        }
    }

    @Override
    public void onLoaderReset(Loader<List<Article>> loader) {
        // articleAdapter is cleared and all data removed
        articleAdapter.clear();
    }

    // Menu, used to control some query settings.
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            Intent settingsIntent = new Intent(this, SettingsActivity.class);
            startActivity(settingsIntent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}