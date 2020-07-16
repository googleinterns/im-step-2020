package com.google.sps.servlets;

import java.util.ArrayList;
import java.util.List;



// See for more details: https://developers.google.com/calendar/v3/reference/events/list

/** Get a list of user events */
public class PatchEvent {
    private String description = "";

    PatchEvent(String description) {
        this.description = description;
    }
    
    // We should be getting the DATE 
    public String createPatchEventURL(String calendar_id, String recurring_eid, String accessToken) {
        return new StringBuilder("https://www.googleapis.com/calendar/v3/calendars/" + calendar_id + 
        "/events/" + recurring_eid + "?access_token=").append(accessToken).toString();
    }
}
