package com.google.sps.servlets;

import java.util.ArrayList;
import java.util.List;



// See for more details: https://www.googleapis.com/calendar/v3/calendars/calendarId/events/eventId

/** Delete a specific event */
public class GetEvent {
    
    // We should be getting the DATE 
    public String createGetEventURL(String calendar_id, String eventId, String accessToken, String timeZone) {
        return new StringBuilder("https://www.googleapis.com/calendar/v3/calendars/" + calendar_id + 
        "/events/" + eventId + "?timeZone=" + timeZone + "&access_token=").append(accessToken).toString();
    }
}