package com.google.sps.servlets;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


// See more information at: https://developers.google.com/calendar/v3/reference/freebusy/query

/** Get busy data from calendar */
public class GetFreeBusy {

    private String timeMin = ""; // Minimum amount of time
    private String timeMax = ""; // Maximum amount of time 
    private String timeZone = ""; // Time Zone
    private Integer calendarExpansionMax = 1; // Maximum number of calendars for which FreeBusy information is to be provided.
    private List<Id> items = new ArrayList<>(); // List of calender id's

    public GetFreeBusy(String timeMin, String timeMax, String timeZone) {
        this.timeMin = timeMin;
        this.timeMax = timeMax;
        this.timeZone = timeZone;
    }

    // Add an calender id.
    public void addId(String calender_id) {
        Id id = new Id(calender_id);
        items.add(id);
    }

    // Update calendar amount
    public void updateCalendarExpansionTo(Integer amount) {
        if (amount > 50) return; // Error check, MAX is 50
        calendarExpansionMax = amount;
    }


    public String createGetFreeBusyURL(String accessToken) {
        return new StringBuilder("https://www.googleapis.com/calendar/v3/freeBusy?access_token=").append(accessToken).toString();
    }
}