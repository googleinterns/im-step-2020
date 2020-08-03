package com.google.sps.servlets;

import java.util.ArrayList;
import java.util.List;



// See for more details: https://developers.google.com/calendar/v3/reference/events/delete

/** Delete a specific event */
public class DeleteEvent {
    
    // We should be getting the DATE 
    public String createDeleteEventURL(String calendar_id, String eventId, String accessToken) {
        return new StringBuilder("https://www.googleapis.com/calendar/v3/calendars/" + calendar_id + 
        "/events/" + eventId + "?access_token=").append(accessToken).toString();
    }
}