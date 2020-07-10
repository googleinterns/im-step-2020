package com.google.sps.servlets;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.entity.AbstractHttpEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import org.joda.time.format.DateTimeFormat;
import org.json.simple.JSONObject;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

// Gson
import com.google.gson.*;

// Date and Time
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.time.ZoneId;

import org.joda.time.Interval;
import org.joda.time.DateTimeZone;
import org.joda.time.DateTime;

/**
 * FUTURE: 1) STORE CREATED SCHEDULES IN DATASTORE
 */

@WebServlet("/schedule-generator")
public class ScheduleGenerationServlet extends HttpServlet {

  private static HTTP http = new HTTP();

  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response) 
    throws ServletException, IOException {
      
    String accessToken = (String) request.getSession(false).getAttribute("access_token");

    List<String> resources = (List<String>) request.getSession(false).getAttribute("resources");

    /////////////////////////// 1) Create Calendar

    DefaultHttpClient httpClient = new DefaultHttpClient();

    JSONObject calendar = createNewCalendar(httpClient, accessToken, "Study Schedule");
    String id = (String) calendar.get("id");
    request.getSession().setAttribute("study-schedule-id", id);

    //////////////////////// 4) Call Fixer | ALTER BY USER SETTING: just delete event / find next available time / force move to next day /
    // the fixer should be given a list of days and then perform user action

		httpClient.getConnectionManager().shutdown();
   
    response.sendRedirect("/"); 
  }

  // ---------------- Functions to upload to user's calendar ----------------- // 

  JSONObject createNewCalendar(DefaultHttpClient httpClient, String accessToken, String summary) {
    NewCalendar calendar = new NewCalendar(summary);
    HttpPost postRequest = new HttpPost(calendar.createNewCalendarURL(accessToken));
    
    Gson gson = new Gson();
    String json = gson.toJson(calendar);

    return http.postWithData(httpClient, postRequest, json);
  } 

}
