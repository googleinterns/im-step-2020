package com.google.sps.servlets;



// See for more details: https://developers.google.com/calendar/v3/reference/events/insert

/** EVENT: Specify the End time! */
public class End {

    private String dateTime = ""; // Formatted according to RFC3339
    private String timeZone = ""; // Formatted as an IANA Time Zone Database name, e.g. "Europe/Zurich"

    public End(String dateTime, String timeZone) {
        this.dateTime = dateTime;
        this.timeZone = timeZone;
    }
}