package com.google.sps.servlets;



// See for more details: https://developers.google.com/calendar/v3/reference/events/insert

/** EVENT: Specify the Start time. */
public class Start {

    private String dateTime = ""; // Formatted according to RFC3339
    private String timeZone = ""; // Formatted as an IANA Time Zone Database name, e.g. "Europe/Zurich"

    public Start(String dateTime, String timeZone) {
        this.dateTime = dateTime;
        this.timeZone = timeZone;
    }
}