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


/** Servlet that handles the schedule. */
@WebServlet("/schedule-handler")
public final class ScheduleHandlerServlet extends HttpServlet {

  @Override
  public void init() {
  }

  /** Read through all filters and SET THEM in User Preferences.java */


  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) 
    throws IOException {
    System.out.println("You're on the ScheduleHandler Servlet!");

    //response.sendRedirect("/");

    response.sendRedirect("/schedule-generator"); 
  }

  /** We set the resources, we simply use this to build the schedule! */
  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) {
    System.out.println("You did a post method!");

    response.setContentType("application/json");
    String links = "";
    
    // Try to get resources!
    try {
      links = request.getReader().lines().collect(Collectors.joining(System.lineSeparator()));

      Gson g = new Gson(); 
      Type collectionType = new TypeToken<List<String>>(){}.getType();

      List<String> resources =  new ArrayList<String>();
      resources = (List<String>) new Gson().fromJson(links , collectionType);
      request.getSession().setAttribute("resources", resources);

      response.getWriter().print(true);

    } catch (Exception e) {
      System.out.println("There was an error trying to get the resources!" + e);
    }
  }
}
