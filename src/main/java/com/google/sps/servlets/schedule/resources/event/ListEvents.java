package com.google.sps.servlets;

import java.util.ArrayList;
import java.util.List;



// See for more details: https://developers.google.com/calendar/v3/reference/events/list

/** Get a list of user events */
public class ListEvents {
    
    // We should be getting the DATE 
    public String createListEventsURL(String calendar_id, String accessToken) {
        return new StringBuilder("https://www.googleapis.com/calendar/v3/calendars/" + calendar_id + 
        "/events?access_token=").append(accessToken).toString();
    }
}