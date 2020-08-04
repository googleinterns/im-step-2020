/// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.sps.servlets;

import com.google.appengine.api.datastore.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.servlet.RequestDispatcher;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.List;

import org.json.simple.JSONObject;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;


/** Servlet that gets Calendar settings. */
/** ReadUserSettingsFromDBServlet simply reads from 
 * To display calendar settings we need AT LEAST 2 pieces of information 
 * 1) a calendar ID 2) timezone. This will be displayed using an iFrame.
 * 
 * As of now, we display the user's primary_idcalendar and the study schedule ID.
 * 
 * We check if we have a calendar already on saved on the Servlet session, then we just send that one
 * Else we check if the user has a Study Schedule on their list of calendars. Then we send that one
 * Else we return an error that there is no calendar to display.
 */
@WebServlet("/getSettingsState")
public class ReadUserSettingsFromDBServlet extends HttpServlet {
  private Datastore DB = new Datastore();
  private List<String> userSettings = new ArrayList<String>() {{
    add("description");
    add("eventLookSpan");
    add("eventRecurrenceLength");
    add("length");
    add("notes");
    add("onDays");
    add("start");
    add("startDay");
    add("startWeek");
    add("userEventsChoice");
    add("deleteOverlappingEvents");
  }};

  @Override
  /** doGet
   * 
   * The fields are as follows: 
   * 
   * @param request 
   * @return response JSON object consisting of fields required to display the object
   */
  public void doGet(HttpServletRequest request, HttpServletResponse response) 
    throws IOException {

    String accessToken = (String) request.getSession(false).getAttribute("access_token");

    Boolean isValid = new HTTP().isAccessTokenValid(accessToken);
    if (!isValid) {
      response.sendRedirect("/home.html");
      return;
    }

    // Check to make sure the user has a settings page ready to go to read from in the database!
    GetCalendar getCalendar = new GetCalendar();
    String json = new HTTP().get(getCalendar.createGetCalendarURL("primary", accessToken));
    JSONObject jsonObject = new HTTP().parseJSON(json);
    String primary_id = (String) jsonObject.get("id");
    if (DB.getUser(primary_id) == null) {
      response.sendRedirect("/home.html");
      return;
    }

    try {
      String defaults = request.getParameter("defaults");
      Boolean getDefaults = Boolean.valueOf(defaults);
      if (getDefaults) DB.applyDefaults(primary_id);
    } catch (Exception e) {
      System.out.println(e);
    }

    getSettingsFromDB(primary_id, response);
  }

  public void getSettingsFromDB(String primary_id, HttpServletResponse response) {
    JSONObject sendJSON = new JSONObject();
    try {
      response.setContentType("application/json");

      List<String> settings = new ArrayList<String>();

      for (String setting : userSettings) {
        settings.add(DB.getUserSetting(primary_id, setting));
      }
      

      sendJSON.put("settings", settings);
      response.getWriter().print(sendJSON);
    } catch (Exception e) {
      System.out.println("Failed to get settings from DB: " + e);
      sendJSON.put("error", "Failed to get settings.");
      try {
        response.getWriter().print(sendJSON);
      } catch (Exception err) {
        System.out.println(err);
      }
    }
  }
}

