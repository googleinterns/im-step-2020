// Copyright 2019 Google LLC
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
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.List;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.File;  // Import the File class
import java.io.FileNotFoundException;  // Import this class to handle errors
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.stream.Collectors;
import java.util.List;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Calendar;
import java.util.TimeZone;
import java.text.SimpleDateFormat;

import org.joda.time.DateTimeZone;
import org.joda.time.DateTime;
import java.time.temporal.TemporalAdjusters;

import org.json.simple.JSONObject;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.google.gson.*;
import java.lang.reflect.Type;
import com.google.common.reflect.TypeToken;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


/** Scheduler Handler which handles the User's Settings or generates the schedule
* This decision is determined by a hidden input when the user submits the SETTINGS form. If we do not get
* that input, then we know we want to Generate the Schedule.
* 
* Also, handles retrieving resources for the schedule by doing a POST request.
*
 */
@WebServlet("/schedule-handler")
public final class ScheduleHandlerServlet extends HttpServlet {

  /** Checks if the user wants to set settings or generate schedule
   * This is done by a hidden input in the settings servlet
   * 
   * @param request 
   * @return none we simply set the resources in our Servlet Session
  */

  private Datastore DB = new Datastore();
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) 
    throws IOException {

    // Grab access token and make sure it's valid! REQUIRED!
    String accessToken = (String) request.getSession(false).getAttribute("access_token");

    Boolean isValid = new HTTP().isAccessTokenValid(accessToken);
    if (!isValid) {
      response.sendRedirect("/request-permission");
      return;
    }

    try {
      boolean generateSchedule = Boolean.parseBoolean(request.getParameter("generate"));
      if (generateSchedule) {
        response.sendRedirect("/schedule-generator");
        return;
      }
    } catch (Exception e) {
      System.out.println("There was an error trying to determine if we want to generate the schedule: " + e);
    }

    getInputAndSaveToDB(request, response);
    response.sendRedirect("/home.html");

    
  }

  /** COMMS: We set the resources in our session.
   * 
   * @param request a JSON body of an array of resource links 
   * @return none we simply set the resources in our Servlet Session
  */
  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) {

    response.setContentType("application/json");
    String links = "";
    
    // Try to get resources
    try {
      links = request.getReader().lines().collect(Collectors.joining(System.lineSeparator()));
      links = links.replaceAll("[\\]\\[ \\s]",""); // regex expression simply ignore brackets and spaces
      List<String> resources = new ArrayList<String>(Arrays.asList(links.split(",")));
      
      // Get access token
      String accessToken = (String) request.getSession(false).getAttribute("access_token");
      // Get primary ID
      GetCalendar getCalendar = new GetCalendar();
      String json = new HTTP().get(getCalendar.createGetCalendarURL("primary", accessToken));
      JSONObject jsonObject = new HTTP().parseJSON(json);
      String primary_id = (String) jsonObject.get("id");

      if (DB.getUser(primary_id) == null) {
        response.sendRedirect("/request-permission");
        return;
      }

      Gson g = new Gson();
      json = g.toJson(resources);
      DB.updateTextProperty(primary_id, "resources", json);
      

      response.getWriter().print(true);

    } catch (Exception e) {
      System.out.println("There was an error trying to get the resources! " + e);
      try {
        response.sendRedirect("/home.html");
        return;
      } catch (Exception err) {
        System.out.println(err);
      }
    }
  }

  /** getInputAndSaveToDB: Saves input to DB. 
   * 
   * @param request 
   * @param response go back to home.html
   * @return none simply fill to database
  */
  public void getInputAndSaveToDB(HttpServletRequest request, HttpServletResponse response) {
    try {
      // Get access token
      String accessToken = (String) request.getSession(false).getAttribute("access_token");

      // Get primary ID
      GetCalendar getCalendar = new GetCalendar();
      String json = new HTTP().get(getCalendar.createGetCalendarURL("primary", accessToken));
      JSONObject jsonObject = new HTTP().parseJSON(json);
      String primary_id = (String) jsonObject.get("id");

      
      // UPDATE Start Day
      int startDay = Integer.parseInt(request.getParameter("startDay"));
      Gson g = new Gson();
      json = g.toJson(startDay);
      DB.updateTextProperty(primary_id, "startDay", json);


      // UPDATE Start Week
      int startWeek = Integer.parseInt(request.getParameter("startWeek"));
      json = g.toJson(startWeek);
      DB.updateTextProperty(primary_id, "startWeek", json);

      // UPDATE Difficulty of schedule
      int intensity = Integer.parseInt(request.getParameter("intensity"));
      json = g.toJson(intensity);
      DB.updateTextProperty(primary_id, "userEventsChoice", json);

      // UPDATE Description
      String user_description = request.getParameter("description");
      json = g.toJson(user_description);
      DB.updateTextProperty(primary_id, "description", user_description);

      // UPDATE possible study session days
      int studySessionDays = Integer.parseInt(request.getParameter("days"));
      List<Integer> days = new ArrayList<Integer>();
      for (int i = 1; i <= 7; i++)
        days.add(i);
      if (studySessionDays == 1) {
        days.clear();
        for (int i = 1; i <= 5; i++)
          days.add(i);
      } else if (studySessionDays == 2) {
        days.clear();
        days.add(6); 
        days.add(7);
      } else if (studySessionDays == 3) {
        days.clear();
        String[] x = request.getParameterValues("cd"); 
        for (String s: x) {           
          if (s.equals("sunday")) {
            days.add(7);
          } else if (s.equals("monday")) {
            days.add(1);
          } else if (s.equals("tuesday")) {
            days.add(2);
          } else if (s.equals("wednesday")) {
            days.add(3);
          } else if (s.equals("thursday")) {
            days.add(4);
          } else if (s.equals("friday")) {
            days.add(5);
          } else {
            days.add(6);
          }
        }
      }
      json = g.toJson(days);
      DB.updateTextProperty(primary_id, "onDays", json);

      // UPDATE possible start times for events
      String startTimes = request.getParameter("times");
      List<List<Integer>> startTime = processStartTimes(startTimes);
      json = g.toJson(startTime);
      DB.updateTextProperty(primary_id, "start", json);

      // UPDATE DURATION of events
      String duration = request.getParameter("durations");
      List<List<Integer>> study_session_length = processDuration(duration);
      json = g.toJson(study_session_length);
      DB.updateTextProperty(primary_id, "length", json);

      // UPDATE Event Look Span
      int span = Integer.parseInt(request.getParameter("span"));
      json = g.toJson(span);
      DB.updateTextProperty(primary_id, "eventLookSpan", json);
      

      // UPDATE Start Recurrence Length
      int recurLength = Integer.parseInt(request.getParameter("recurrenceLength"));
      json = g.toJson(recurLength);
      DB.updateTextProperty(primary_id, "eventRecurrenceLength", json);

      // UPDATE Overlapping Events
      Boolean overlappingEvents = Boolean.valueOf(request.getParameter("overlapping"));
      json = g.toJson(overlappingEvents);
      DB.updateTextProperty(primary_id, "deleteOverlappingEvents", json);
      

      response.sendRedirect("/home.html");
    } catch (Exception e) {
      System.out.println("Failed to update settings because of: " + e);
    }
  }

  /** UTILITY: processes user input on settings page to set in UserPreferences.
   * The user must enter these times in a specific format. e.g. 3:00pm
   * Because we parse the input expecting this format.
   * 
   * @param startTimes a string of comma separated start times written by the user
   * @return a list of start times and end times that the user gave us.
  */
  public List<List<Integer>> processStartTimes(String startTimes) {
    List<List<Integer>> possibleTimes = new ArrayList<List<Integer>>();

    List<String> times = Arrays.asList(startTimes.split(","));

    try {
      for (String time : times) {
        List<String> hourAndMinute = Arrays.asList(time.split(":"));
        String hour = hourAndMinute.get(0);
        hour = hour.replaceAll("\\s+","");
        int numHour = Integer.parseInt(hour);

        String minute = hourAndMinute.get(1);
        if (minute.contains("p") || minute.contains("P")) {
          if (numHour != 12) numHour += 12;
        }
        minute = minute.replaceAll("\\s+","").replaceAll("A", "")
                        .replaceAll("a", "")
                        .replaceAll("M", "").replaceAll("P", "")
                        .replaceAll("p", "").replaceAll("m", "");
        
        int numMin = Integer.parseInt(minute);

        possibleTimes.add(new ArrayList<Integer>(Arrays.asList(numHour, numMin)));
      }
    } catch (Exception e) {
      return possibleTimes;
    }

    return possibleTimes;
  }

  /** UTILITY: processes user input on settings page to set in UserPreferences.
   * The user must enter these durations in a specific format. e.g. 1 hour 0min
   * Because we parse the input expecting this format. NOTE: 0 must be included
   * 
   * @param durations a string of comma separated start times written by the user
   * @return a list of durations that the user gave us.
  */
  public List<List<Integer>> processDuration(String startTimes) {
    List<List<Integer>> possibleDuration = new ArrayList<List<Integer>>();
  
    List<String> durations = Arrays.asList(startTimes.split(","));
    Pattern p = Pattern.compile("[^' \" \\s, \\[A-z\\]]*\\d"); // regex expression. simply grabs each number and ignores all chars within brackets
    int index = 0;
    for (String time : durations) {
      Matcher n = p.matcher(time);

      int hour = 0;
      int min = 0;
      int count = 0;
      Boolean minSet = false;
      Boolean hourSet = false;
      while (n.find()) {
        if (count == 2) break;
        try {
          int timeValue = Integer.parseInt(n.group().trim());
          if (count == 0) {
            hour = timeValue;
            hourSet = true;
          }
          if (count == 1) {
            min = timeValue;
            minSet = true;
          }
          
        } catch (Exception e) {
          System.out.println("There was an error parsing the durations from the calendar settings because of " + e);
          break;
        }
        count++;
      }
      if (hour < 0 || hour > 23) {
        hour = 1;
      } 
      if (min < 0 || min > 1440) {
        min = 30;
      }

      possibleDuration.add(new ArrayList<Integer>(Arrays.asList(hour, min)));
      index++;
    }

    return possibleDuration;
  }
}
