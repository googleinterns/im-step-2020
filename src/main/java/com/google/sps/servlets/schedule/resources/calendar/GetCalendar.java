package com.google.sps.servlets;

<<<<<<< HEAD
=======

<<<<<<< HEAD
>>>>>>> da21d7ca49358b710d53dda3300bb8ae1482eb51
=======

// See more information here: https://developers.google.com/calendar/v3/reference/calendars/get

>>>>>>> e71770c65d8b306e1ea98e386c0c27325cde1a8f
/** Get calendar from ID. */
public class GetCalendar {

    public String createGetCalendarURL(String calendar_id, String accessToken) {
        return new StringBuilder("https://www.googleapis.com/calendar/v3/calendars/" + calendar_id + "?access_token=").append(accessToken).toString();
    }
}
