package com.google.sps.servlets;

import java.util.ArrayList;
import java.util.List;



// See for more details: https://developers.google.com/calendar/v3/reference/events/insert

/** Add a new event to the user's list of events */
public class NewEvent {
    private String summary = ""; // Title of event
    private String description = ""; // Description of event 
    private Start start = null; // Start time of event -- REQUIRED
    private End end = null; // End time of event -- REQUIRED
    private List<Attachment> attachments = new ArrayList<>(); // List of attachments
    private List<String> recurrence = new ArrayList<>(); // List of recurrences

    public NewEvent(String summary, String description) {
        this.summary = summary;
        this.description = description;
    }

    // Add an attachment.
    public void addAttachment(String title, String fileUrl, String iconLink) {
        Attachment attachment = new Attachment(title, fileUrl, iconLink); 
        attachments.add(attachment);
    }

    // Convert start to string using JSON.
    public void setStart(String dateTime, String timeZone) {
        this.start = new Start(dateTime, timeZone);
    }

    // Convert end to string using JSON
    public void setEnd(String dateTime, String timeZone) {
        this.end = new End(dateTime, timeZone);
    }

    public void addRecurrence(String rec) {
        recurrence.add(rec);
    }

    public String createNewEventURL(String calendar_id, String accessToken) {
        return new StringBuilder("https://www.googleapis.com/calendar/v3/calendars/" + calendar_id + "/events?access_token=").append(accessToken).toString();
    }
}