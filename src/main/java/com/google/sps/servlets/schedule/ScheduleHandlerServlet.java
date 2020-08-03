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
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) 
    throws IOException {

    try {
      int invalidCall = Integer.parseInt(request.getParameter("saveSettings"));
    } catch (Exception e) {
      if (UserPreferences.STUDY_SESSION_LENGTH.isEmpty()) UserPreferences.applyDefaultDuration();
      if (UserPreferences.STUDY_SESSION_START_TIME.isEmpty()) UserPreferences.applyDefaultStartTime();
      response.sendRedirect("/schedule-generator");
      return;
    }

    
    try {
      // Start Day
      int startDay = Integer.parseInt(request.getParameter("startDay"));
      UserPreferences.START_DAY = startDay;

      // Start Week
      int startWeek = Integer.parseInt(request.getParameter("startWeek"));
      UserPreferences.START_WEEK = startWeek;

      // Difficulty of schedule
      int intensity = Integer.parseInt(request.getParameter("intensity"));
      UserPreferences.USER_EVENTS_CHOICE = intensity;

      // Description
      String user_description = request.getParameter("description");
      UserPreferences.DESCRIPTION = user_description;

      // Start Day
      int studySessionDays = Integer.parseInt(request.getParameter("days"));
      if (studySessionDays == 0) {
        UserPreferences.STUDY_SESSION_DAYS_CHOICE = UserPreferences.STUDY_SESSION_DAYS.WEEKDAY;
      } else if (studySessionDays == 1) {
        UserPreferences.STUDY_SESSION_DAYS_CHOICE = UserPreferences.STUDY_SESSION_DAYS.WEEKEND;
      }

      // Get possible start times and end times for events
      String startTimes = request.getParameter("times");
      UserPreferences.STUDY_SESSION_START_TIME = processStartTimes(startTimes);

      String duration = request.getParameter("durations");
      UserPreferences.STUDY_SESSION_LENGTH = processDuration(duration);

      // Event Look Span
      int span = Integer.parseInt(request.getParameter("span"));
      UserPreferences.EVENT_LOOK_SPAN = span;

      // Start Recurrence Length
      int recurLength = Integer.parseInt(request.getParameter("recurrenceLength"));
      UserPreferences.EVENT_RECURRENCE_LENGTH = recurLength;

      response.sendRedirect("/home.html");
    } catch (Exception e) {
      System.out.println(e);
    }
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
      
      UserPreferences.resources = resources;

      response.getWriter().print(true);

    } catch (Exception e) {
      System.out.println("There was an error trying to get the resources!" + e);
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
        //minute = minute.replace("\\/[a-zA-Z]+/g", "");

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
          int timeValue = Integer.parseInt(n.group());
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
