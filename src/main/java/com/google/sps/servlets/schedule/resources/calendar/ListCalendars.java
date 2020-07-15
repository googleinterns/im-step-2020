package com.google.sps.servlets;

// Find more information at: https://developers.google.com/calendar/v3/reference/calendarList/list

/** Get user's list of calendars */
public class ListCalendars {

    public String createListCalendarsURL(String accessToken) {
        return new StringBuilder("https://www.googleapis.com/calendar/v3/users/me/calendarList?access_token=").append(accessToken).toString();
    }
}