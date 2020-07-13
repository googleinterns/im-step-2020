package com.google.sps.servlets;

<<<<<<< HEAD
=======

>>>>>>> da21d7ca49358b710d53dda3300bb8ae1482eb51
/** Get calendar from ID. */
public class GetCalendar {

    public String createGetCalendarURL(String calendar_id, String accessToken) {
        return new StringBuilder("https://www.googleapis.com/calendar/v3/calendars/" + calendar_id + "?access_token=").append(accessToken).toString();
    }
}
