package com.google.sps.servlets;



// See for more details: https://developers.google.com/calendar/v3/reference/events/insert

/** Add a new event to the user's list of events */
public class Attachment {

    public String title =  "";
    public String fileUrl = "";
    public String iconLink = "";

    public Attachment(String title, String fileUrl, String iconLink) {
        this.title = title;
        this.fileUrl = fileUrl;
        this.iconLink = iconLink;
    }
}