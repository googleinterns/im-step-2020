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


/** Servlet that sends Calendar settings. */
/** DisplayCalendarSettingsServlet sends 3 pieces of information to display the calendar settings
 * To display calendar settings we need AT LEAST 2 pieces of information 
 * 1) a calendar ID 2) timezone. This will be displayed using an iFrame.
 * 
 * As of now, we display the user's primary calendar and the study schedule ID.
 * 
 * We check if we have a calendar already on saved on the Servlet session, then we just send that one
 * Else we check if the user has a Study Schedule on their list of calendars. Then we send that one
 * Else we return an error that there is no calendar to display.
 */
@WebServlet("/display-calendar-settings")
public final class DisplayCalendarSettingsServlet extends HttpServlet {
    private HTTP http = new HTTP();


  @Override
  /** doGet
   * 
   * The fields are as follows: timezone, primary id and study schedule id
   * 
   * @param request none
   * @return response JSON object consisting of fields required to display the object
   */
  public void doGet(HttpServletRequest request, HttpServletResponse response) 
    throws IOException {

    String accessToken = (String) request.getSession(false).getAttribute("access_token");

    Boolean isValid = http.isAccessTokenValid(accessToken);
    if (!isValid) {
      JSONObject sendJSON = new JSONObject();
      sendJSON.put("error", "The user did not give us permission.");
      response.setContentType("application/json");
      response.getWriter().print(sendJSON);
      return;
    }

    // Get primary ID
    GetCalendar getCalendar = new GetCalendar();
    String json = http.get(getCalendar.createGetCalendarURL("primary", accessToken));
    JSONObject jsonObject = http.parseJSON(json);
    String main_id = (String) jsonObject.get("id");

    // CREATE NEW USER TO DATABASE
    // Note this does not duplicate entries
    Datastore d = new Datastore();
    d.manageUserSettings(main_id);

    // Get timezone of calendar
    try {
      GetSetting getSetting = new GetSetting();
      String timezone = "";
      if (Time.timezone == null) {
        json = http.get(getSetting.createGetSettingURL("timezone", accessToken));
        jsonObject = http.parseJSON(json);
        timezone = (String) jsonObject.get("value");
      } else {
        timezone = Time.timezone;
      }

      // Get study schedule ID
      String study_id = (String) request.getSession(false).getAttribute("study-schedule-id");

      JSONObject sendJSON = new JSONObject();
      response.setContentType("application/json");

      if (!currentCalendarIsValid(accessToken, study_id)) {
        
        // Check to see we have a study schedule on the user's calendar. NOTE: returns the latest
        String latestStudyScheduleID = checkStudyScheduleOnCalendarList(accessToken);
        if (!latestStudyScheduleID.equals("")) {
          sendJSON.put("main", main_id);
          sendJSON.put("study", latestStudyScheduleID);
          sendJSON.put("timezone", timezone);
          response.getWriter().print(sendJSON);
          return;
        }
        sendJSON.put("errorDeletedSchedule", "Most recent study calendar has been deleted.");
        return;
      }

      // Else our current study calendar is valid on our session is valid, return that

      sendJSON.put("main", main_id);
      sendJSON.put("study", study_id);
      sendJSON.put("timezone", timezone);

      response.getWriter().print(sendJSON);
      return;
    } catch (Exception e) {
      JSONObject sendJSON = new JSONObject();
      sendJSON.put("error", "There was an error getting display calendar information.");
      System.out.println(e);
    }
  }

  // Makes sure the current calendar is valid.
  public Boolean currentCalendarIsValid(String accessToken, String id) {
    ListCalendars list =  new ListCalendars();
    String json = "";
    try {
      json = http.get(list.createListCalendarsURL(accessToken));
    } catch (Exception e) {
      System.out.println("There was an error getting the list of calendars." + e);
      return false;
    }
    JSONObject userCalendars = http.parseJSON(json);
    JSONArray items = (JSONArray) userCalendars.get("items");
    Iterator iter = items.iterator();

    List<String> listAttr =  new ArrayList<String>();

    // Check our last saved ID.
    while (iter.hasNext()) {
      JSONObject calendarResource = (JSONObject) iter.next();
      String calenderAttr = (String) calendarResource.get("id");
      listAttr.add(calenderAttr);
    }

    for (String calendar_id : listAttr) {
      if (calendar_id.equals(id)) {
        return true;
      }
    }
    return false;
  }

  // If the study calendar ID is not in our session, look a Study Calendar on the user's Calendars
  public String checkStudyScheduleOnCalendarList(String accessToken) {
    ListCalendars list =  new ListCalendars();
    String json = "";
    try {
      json = http.get(list.createListCalendarsURL(accessToken));
    } catch (Exception e) {
      System.out.println("There was an error getting the list of calendars." + e);
      return "";
    }
    JSONObject userCalendars = http.parseJSON(json);
    JSONArray items = (JSONArray) userCalendars.get("items");
    Iterator iter = items.iterator();

    // Check to see if any calendar has the title Study Schedule
    while (iter.hasNext()) {
      JSONObject calendarResource = (JSONObject) iter.next();
      String id = (String) calendarResource.get("id");
      String summary = (String) calendarResource.get("summary");
      if (summary.equals("Study Schedule")) {
        return id;
      }
    }

    return "";
  }
}