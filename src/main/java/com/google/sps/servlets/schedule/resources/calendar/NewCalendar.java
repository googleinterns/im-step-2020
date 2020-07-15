package com.google.sps.servlets;

// See more information here: https://developers.google.com/calendar/v3/reference/calendars/insert

/** Add a new calendar to the user's list of calendars */
public class NewCalendar {
    private String summary = null;

    public NewCalendar(String summary) {
        this.summary = summary;
    }

    public String createNewCalendarURL(String accessToken) {
        return new StringBuilder("https://www.googleapis.com/calendar/v3/calendars?access_token=").append(accessToken).toString();
    }
}