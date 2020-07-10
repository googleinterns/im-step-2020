package com.google.sps.servlets;


/** Read a setting from the user's calendar */
public class GetSetting {

    public String createGetSettingURL(String setting, String accessToken) {
        return new StringBuilder("https://www.googleapis.com/calendar/v3/users/me/settings/" + setting + "?access_token=").append(accessToken).toString();
    }
}