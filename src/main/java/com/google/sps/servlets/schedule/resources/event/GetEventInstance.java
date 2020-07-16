package com.google.sps.servlets;


// See for more details: https://developers.google.com/calendar/v3/reference/events/instances

/** Returns instances of the specified recurring event */
public class GetEventInstance {
    
    public String createGetEventInstanceURL(String calendar_id, String eventId, String accessToken) {
        return new StringBuilder("https://www.googleapis.com/calendar/v3/calendars/" + calendar_id + "/events/"
        + eventId + "/instances?access_token=").append(accessToken).toString();
    }
}
