package stephenkyne.example.org.newsapp;

public class Article {

    private String articleSection;

    private String articleWebDate;

    private String articleTitle;

    private String articleWebUrl;

    private String articleAuthor;

    private String articleSummary;

    // From JSON
    // sectionName, webPublicationDate, webTitle ( Article title), webUrl, webTitle (author Name),
    // trailText (Summary of article).
    public Article (String section, String webDate, String title, String webUrl, String author,
                    String summary){
        articleSection = section;
        articleWebDate = webDate;
        articleTitle = title;
        articleWebUrl = webUrl;
        articleAuthor = author;
        articleSummary = summary;

    }

    // Gets the section the article is in, example, "Sport", "World news"
    public String getSection(){
        return articleSection;
    }

    // Gets the date the article was published in UTC time.
    public String getWebDate(){
        return articleWebDate;
    }

    // Gets the Title of article
    public String getTitle(){
        return articleTitle;
    }

    // Gets the website address of the article
    public String getWebUrl(){
        return articleWebUrl;
    }

    // Gets the name of the Author who wrote article
    public String getAuthor(){
        return articleAuthor;
    }

    // Gets gets the trailText, Summary text of the article
    public String getSummary(){
        return articleSummary;
    }
}
