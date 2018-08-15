package com.example.csaba.newsappswipe1;

public class Event {

    public final String title;
    public final String webUrl;
    public final String section;
    public final String thumbnail;
    public final String bodyText;
    public final String webPublicationDate;



    public Event(String eventTitle, String eventUrl, String eventSection, String eventThumbnail, String eventBodyText, String eventDate) {
        title = eventTitle;
        webUrl = eventUrl;
        section = eventSection;
        thumbnail = eventThumbnail;
        bodyText = eventBodyText;
        webPublicationDate = eventDate;
    }


}
