package com.google.sps.servlets;



// See more information here: https://developers.google.com/calendar/v3/reference/calendars/get

/** Get calendar from ID. */
public class GetCalendar {

    public String createGetCalendarURL(String calendar_id, String accessToken) {
        return new StringBuilder("https://www.googleapis.com/calendar/v3/calendars/" + calendar_id + "?access_token=").append(accessToken).toString();
    }
}