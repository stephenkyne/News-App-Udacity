package stephenkyne.example.org.newsapp;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;


public class ArticleAdapter extends ArrayAdapter<Article> {

    public ArticleAdapter(Context context, ArrayList<Article> articles) {
        super(context, 0, articles);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(
                    R.layout.article_list_item, parent, false);
        }
        // Get the {@link Article} object located at this position in the list
        Article currentArticle = getItem(position);

        /*
           // Title of the article area.
         */
        // Title / Name of the Article is stored here
        TextView titleTextView = convertView.findViewById(R.id.title_text_view);

        // Get the current Title / Name of the Article
        String articleTitle = currentArticle.getTitle();

        // Checks to see if articleTitle is Null or empty
        if (articleTitle == null || articleTitle.equals("")) {
            //is null or empty, articleTitle is overwritten with "Title Missing".
            articleTitle = getContext().getString(R.string.unknown_title);
        }

        // the set the current Title / Name of the Article to the text view.
        titleTextView.setText(articleTitle);

        /*
           // Section area.
         */
        // Find the text view that the section name will be stored.
        TextView sectionTextView = convertView.findViewById(R.id.section_text_view);

        // Get the current Section of the Article
        String sectionName = currentArticle.getSection();

        // Calls a method
        // Gets the right color for the section
        // Even if sectionName is Null or Empty a color is selected.
        String sectionColor = getSectionColor(sectionName);

        // Check that sectionName is not null or empty
        if (sectionName == null || sectionName.equals("")) {
            // if it is, "Section Unknown" is displayed
            sectionName = getContext().getString(R.string.unknown_Section);
        }

        // the set the Section of the Article to the text view.
        sectionTextView.setText(sectionName);

        // Finds the layout where the section name TextView is located.
        View sectionLayout = convertView.findViewById(R.id.layout_section_date_author);

        //Sets the correct or default section color to the section layout
        sectionLayout.setBackgroundColor(Color.parseColor(sectionColor));

        /*
           // Date and time the article was published on the web.
         */
        // Find the text view that the date and time will be stored.
        TextView dateTextView = convertView.findViewById(R.id.date_text_view);

        // Gets the web published date and time of the article.
        // Stores in @param rawDate
        String rawDate = currentArticle.getWebDate();
        // @param formattedDateTime is defined.
        //It will be displayed to the user.
        String formattedDateTime;

        // Check that rawDate is not null or empty
        if (rawDate == null || rawDate.equals("")) {
            // It is null or empty, "Date Missing" is stored in @param formattedDateTime
            formattedDateTime = getContext().getString(R.string.unknown_date);
        } else {
            // Is Not Null or empty
            // Sends rawData to the formatted and returned.
            formattedDateTime = formatDate(rawDate);
        }

        // Formatted date and time displayed.
        dateTextView.setText(formattedDateTime);

        /*
           // Author name the article.
         */
        // Find the text view that the date and time will be stored.
        TextView authorTextView = convertView.findViewById(R.id.author_text_view);

        // Gets the Authors name
        String theAuthor = currentArticle.getAuthor();

        // Check to make sure its not null, if null or empty
        if (theAuthor == null || theAuthor.equals("")) {
            // Is null or empty, "Unknown Author" is assigned to theAuthor
            theAuthor = getContext().getString(R.string.unknown_author);
        }

        // Author name Or "Unknown author" is displayed
        authorTextView.setText(theAuthor);

        /*
           // Summary / trailText of the article.
         */
        // Find the text view that the summary stored.
        TextView summaryTextView = convertView.findViewById(R.id.summary_text_vew);

        // Get the Summary / trailText
        // Stores this text in summaryHasTags As is may have HTML tags in the text
        String summaryHasTags = currentArticle.getSummary();

        // @param summaryNoTags will be displayed, but checks and formatting need to take place.
        String summaryNoTags;

        // Check to make sure its not null, if null or empty
        if (summaryHasTags == null || summaryHasTags.equals("")) {
            // Is null or empty, "Missing Summary" is assigned to summaryNoTags
            summaryNoTags = getContext().getString(R.string.unknown_summary);
        } else {
            // Not null or empty
            // Called the removeHtmlTags method to read and remove html tags inside "< >"
            summaryNoTags = removeHtmlTags(summaryHasTags);
        }

        // Article Summary is displays summaryNoTags
        summaryTextView.setText(summaryNoTags);


        // Return the whole list item layout (containing 5 TextViews) so that it can be shown in
        // the convertView.
        return convertView;
    }

    /**
     * Return the formatted date time string
     * Nulls and empty Strings are checked before calling the method.
     * No Nulls or empty strings should get to this method.
     *
     * @param dateString date format is "yyyy-MM-dd'T'kk:mm:ss'Z'" in UTC
     *                   returns in "yyyy-MM-dd HH:mm:ss"
     */
    private String formatDate(String dateString) {

        // Parse the dateString into a Date object
        // default date time format used is given
        SimpleDateFormat simpleDateFormat =
                new SimpleDateFormat("yyyy-MM-dd'T'kk:mm:ss'Z'");
        Date dateObject = null;
        try {
            dateObject = simpleDateFormat.parse(dateString);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        // A new date time format is given.
        SimpleDateFormat articleDateFormat = new SimpleDateFormat("MMM d, yyyy  h:mm a", Locale.ENGLISH);
        String formattedDateUTC = articleDateFormat.format(dateObject);
        // Changes to UTC format into users Local time
        articleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        Date date = null;
        try {
            date = articleDateFormat.parse(formattedDateUTC);
            articleDateFormat.setTimeZone(TimeZone.getDefault());
        } catch (ParseException e) {
            e.printStackTrace();
        }
        //Formatted date and time is returned as one String
        return articleDateFormat.format(date);
    }

    /*
     *@param summaryText, Summary Text of the article. It may contain HTML Tags (e.g. <STRONG>)
     * Nulls and empty Strings are checked before calling the method.
     * No Nulls or empty strings should get to this method.
     * This method reads and removes all the HTML Tags
     */
    private String removeHtmlTags(String summaryText) {
        String text = summaryText;
        text = text.replaceAll("<.*?>", "");
        return text;
    }

    /*
     *@Param section, the section the article is in.
     * This methods get uses the "section" name to find the correct color for that section.
     * Nulls and empty Strings are checked before calling the method.
     * No Nulls or empty strings should get to this method.
     * color matches the guardian website for the section
     * return is used once at the end as due to changing to a string.
     */
    private String getSectionColor(String section) {
        //value of the color in its int value stored in the color.xml file
        int backgroundColor;

        if (section.equals(getContext().getString(R.string.section_world_news)) ||
                section.equals(getContext().getString(R.string.section_us_news)) ||
                section.equals(getContext().getString(R.string.section_australia_news))) {
            backgroundColor = R.color.world_color;
        } else if (section.equals(getContext().getString(R.string.section_science)) ||
                section.equals(getContext().getString(R.string.section_technology_news))) {
            backgroundColor = R.color.science_color;
        } else if (section.equals(getContext().getString(R.string.section_sport_news)) ||
                section.equals(getContext().getString(R.string.section_rugby_union)) ||
                section.equals(getContext().getString(R.string.section_cricket)) ||
                section.equals(getContext().getString(R.string.section_golf)) ||
                section.equals(getContext().getString(R.string.section_f1)) ||
                section.equals(getContext().getString(R.string.section_boxing)) ||
                section.equals(getContext().getString(R.string.section_football))) {
            backgroundColor = R.color.sport_color;
        } else if (section.equals(getContext().getString(R.string.section_environment))) {
            backgroundColor = R.color.environment_color;
        } else if (section.equals(getContext().getString(R.string.section_society))) {
            backgroundColor = R.color.society_color;
        } else if (section.equals(getContext().getString(R.string.section_fashion))) {
            backgroundColor = R.color.fashion_color;
        } else if (section.equals(getContext().getString(R.string.section_business))) {
            backgroundColor = R.color.business_color;
        } else if (section.equals(getContext().getString(R.string.section_culture)) ||
                section.equals(getContext().getString(R.string.section_film)) ||
                section.equals(getContext().getString(R.string.section_stage)) ||
                section.equals(getContext().getString(R.string.section_life_and_style)) ||
                section.equals(getContext().getString(R.string.section_books)) ||
                section.equals(getContext().getString(R.string.section_music)) ||
                section.equals(getContext().getString(R.string.section_art_design)) ||
                section.equals(getContext().getString(R.string.section_media)) ||
                section.equals(getContext().getString(R.string.section_games)) ||
                section.equals(getContext().getString(R.string.section_television_radio))) {
            backgroundColor = R.color.culture_color;
        } else {
            backgroundColor = R.color.default_color;
        }

        // String the color and is returned
        String colorInText;

        // the string value of backgroundColor is turned into a string.
        // sending the int back did not result in the right color.
        // the string value works
        colorInText = getContext().getString(backgroundColor);
        return colorInText;
    }
}
