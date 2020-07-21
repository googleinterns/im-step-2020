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
@WebServlet("/display-calendar-settings")
public final class DisplayCalendarSettingsServlet extends HttpServlet {
    private HTTP http = new HTTP();


  @Override
  /** This can be extended to send other settings. */
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


    // Get timezone of calendar
    GetSetting getSetting = new GetSetting();
    String json = "";
    try {
      json = http.get(getSetting.createGetSettingURL("timezone", accessToken));
      JSONObject jsonObject = http.parseJSON(json);
      String timezone = (String) jsonObject.get("value");
      
      // Get primary ID
      GetCalendar getCalendar = new GetCalendar();
      json = http.get(getCalendar.createGetCalendarURL("primary", accessToken));
      jsonObject = http.parseJSON(json);
      String main_id = (String) jsonObject.get("id");

      // Get study schedule ID
      // TODO(paytondennis@): In the future we will store created schedules in Datastore. 
      // Static variables w/ function, Loop through calendar id's for our study schedule 
      // (we will eventually)
      // NOTE: Our purpose is 
      String study_id = (String) request.getSession(false).getAttribute("study-schedule-id");
      if (study_id == null) study_id = "";

      JSONObject sendJSON = new JSONObject();
      sendJSON.put("main", main_id);
      sendJSON.put("study", study_id);
      sendJSON.put("timezone", timezone);

      response.setContentType("application/json");
      response.getWriter().print(sendJSON);
      return;
    } catch (Exception e) {
      JSONObject sendJSON = new JSONObject();
      sendJSON.put("error", "There was an error getting Display calendar information.");
      System.out.println(e);
    }
  }
}
