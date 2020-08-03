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

import com.google.gson.Gson;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
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
@WebServlet("/get-calendar-settings")
public final class GetCalendarSettingsServlet extends HttpServlet {

  @Override
  /** This can be extended to send other settings. */
  public void doGet(HttpServletRequest request, HttpServletResponse response) 
    throws IOException {
    // Grab access token and make sure it's valid!
    String accessToken = (String) request.getSession(false).getAttribute("access_token");
    if (!new HTTP().isAccessTokenValid(accessToken)) {
      response.sendRedirect("/request-permission");
      return;
    }

    // Grab session
    String searchKeyword = (String) request.getParameter("searchKeyword");
    request.getSession().setAttribute("searchKeyword", searchKeyword);

    // Get primary ID
    GetCalendar getCalendar = new GetCalendar();
    String json = new HTTP().get(getCalendar.createGetCalendarURL("primary", accessToken));
    JSONObject jsonObject = new HTTP().parseJSON(json);
    String primary_id = (String) jsonObject.get("id");

    // Get event recurrence
    String eventRecurrenceLengthJSON = new Datastore().getUserSetting(primary_id, "eventRecurrenceLength");
    Integer eventRecurrenceLength = new Gson().fromJson(eventRecurrenceLengthJSON, Integer.class);

    // Get study intensity
    String userEventsChoiceJSON = new Datastore().getUserSetting(primary_id, "userEventsChoice");
    Integer userEventsChoice = new Gson().fromJson(userEventsChoiceJSON, Integer.class);

    // Perform simple calculation of videos to query for
    int videos = userEventsChoice * eventRecurrenceLength;
    if (videos > 50) videos = 45; // enforce video limit


    JSONObject sendJSON = new JSONObject();
    sendJSON.put("numberOfVideos", videos);
    

    response.setContentType("application/json");
    response.getWriter().print(sendJSON);
  }
}
