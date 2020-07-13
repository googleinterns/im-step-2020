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
  private Time TIME = new Time();
  private UserPreferences USER = new UserPreferences();

  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response) 
    throws ServletException, IOException {
      
    String accessToken = (String) request.getSession(false).getAttribute("access_token");


    /////////////////////////// 1) Create Calendar

    DefaultHttpClient httpClient = new DefaultHttpClient();

    JSONObject calendar = createNewCalendar(httpClient, accessToken, "Study Schedule");
    String id = (String) calendar.get("id");
    request.getSession().setAttribute("study-schedule-id", id);

    ////////////////////////// 2) Create Events on Calendar

    List<String> resources = (List<String>) request.getSession(false).getAttribute("resources"); // TODO: get from data store
    System.out.println(resources);

    // Grab timezone to be used with creation of event.
    JSONObject timeZoneSetting = getSetting(accessToken, "timezone");
    String timezone = (String) timeZoneSetting.get("value");
    TIME.setTimeZoneId(timezone);

    // Let's try to create events! We depend on RECURRING EVENTS. So, upto the next week, we try to create 
    Integer successfulEventsCreated = 0;

    // Set user calendars to get FreeBusy info here
    List<String> ids = getAllCalendarIds(httpClient, accessToken);

    Integer stepValue = USER.EVENT_LOOK_SPAN / 2;
    Integer day = 0;
    List<Integer> eventAlreadyScheduled = new ArrayList<Integer>();

    for (int i = 0; i < USER.EVENT_LOOK_SPAN; i++) {
      // If we created max number of events
      if (successfulEventsCreated == UserPreferences.USER_EVENTS_CHOICE) break;

      // If day is greater than or equal to span
      if (day >= USER.EVENT_LOOK_SPAN) {
        day = 0;
        // Move day to next day that was not already scheduled
        for (Integer event : eventAlreadyScheduled) {
          if (event == day) day++;
          else break;
        }
        --stepValue;
      }


      String res = "";
      try {
        res = resources.get(i);
      } catch(Exception e) {
        res = USER.DESCRIPTION;
      }

      List<DateTime> times = getStartInformationForPossibleEvent(httpClient, accessToken, day , timezone, ids);
      if (!times.isEmpty()) {

        JSONObject event = createNewEvent(httpClient, accessToken, timezone, id, times.get(0), times.get(1), "Study Session", res, USER.EVENT_RECURRENCE_LENGTH);

        ++successfulEventsCreated;
        // Add day
        eventAlreadyScheduled.add(day);
      } 

      // Increase step value
      day += stepValue;
    }

    //////////////////////// 4) Call Fixer | ALTER BY USER SETTING: just delete event / find next available time / force move to next day /
    // the fixer should be given a list of days and then perform user action

		httpClient.getConnectionManager().shutdown();
   
    response.sendRedirect("/"); 
  }

  // ---------------- Functions to create new calendar ----------------- // 

  JSONObject createNewCalendar(DefaultHttpClient httpClient, String accessToken, String summary) {
    NewCalendar calendar = new NewCalendar(summary);
    HttpPost postRequest = new HttpPost(calendar.createNewCalendarURL(accessToken));
    
    Gson gson = new Gson();
    String json = gson.toJson(calendar);

    return http.postWithData(httpClient, postRequest, json);
  } 

  JSONObject createNewEvent(DefaultHttpClient httpClient, String accessToken, String timeZone, String id, DateTime startTime, DateTime endTime, 
  String summary, String description, int recurrenceLengthInWeeks) {
    
    // 1) Set start date and end date for recurrence
    int dayOfWeek = startTime.getDayOfWeek();

    // 2) Add event fields: Summary, Description, StartTime, EndTime, Recurrence
    NewEvent event = new NewEvent(summary, description);

    // Get zone ID since Java must be told the time zone.
    ZoneId z = ZoneId.of( timeZone ) ; 

    DateTimeFormatter fmt = DateTimeFormatter
            .ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX")
            .withZone(z);

    // Set event START TIME and END TIME
    event.setStart(fmt.format(startTime.toDate().toInstant()), timeZone);
    event.setEnd(fmt.format(endTime.toDate().toInstant()), timeZone);

    // Add a recurrence to this event that: repeats on THE DAYS GIVEN until X week(s) from now
    event.addRecurrence(TIME.createWeekRecurrence(TIME.onDays(dayOfWeek), TIME.addWeeks(startTime.toDate(), recurrenceLengthInWeeks)));

    // 3) Make request
    Gson gson = new Gson();
    String json = gson.toJson(event);

    HttpPost postRequest = new HttpPost(event.createNewEventURL(id, accessToken));
    return http.postWithData(httpClient, postRequest, json);
  }

  JSONObject getSetting(String accessToken, String setting) {
    GetSetting getSetting = new GetSetting();
    String json = "";
    try {
      json = http.get(getSetting.createGetSettingURL(setting, accessToken));
    } catch (Exception e) {
      System.out.println("There was an error getting the setting: " + e);
    }
    return http.parseJSON(json);
  }

  JSONObject getFreeBusy(DefaultHttpClient httpClient, String accessToken, String timeMin, String timeMax, String timeZone, List<String> ids) {
    GetFreeBusy busy =  new GetFreeBusy(timeMin, timeMax, timeZone);

    for (String id : ids) {
      busy.addId(id);
    }
    busy.updateCalendarExpansionTo(ids.size());

    HttpPost postRequest = new HttpPost(busy.createGetFreeBusyURL(accessToken));

    Gson gson = new Gson();
    String json = gson.toJson(busy);

    return http.postWithData(httpClient, postRequest, json);
  }

  JSONObject getCalendarsList(DefaultHttpClient httpClient, String accessToken) {
    ListCalendars list =  new ListCalendars();
    String json = "";
    try {
      json = http.get(list.createListCalendarsURL(accessToken));
    } catch (Exception e) {
      System.out.println("There was an error getting the list of calendars." + e);
    }
    return http.parseJSON(json);
  }


  // ----------------- // Utility Event Functions // --------------------- //
  // NOTE: timeMin and timeMax will be used in freeBusy span time. startDate is used for the date to try. These HAVE TO MATCH UP!
  // This function will be used to get possible start times, for an event
  public List<DateTime> getStartInformationForPossibleEvent(DefaultHttpClient httpClient, String accessToken, Integer currentDate, String timeZone, List<String> ids ) {
    // Get length of current date
    String dayStart = TIME.setTime(0 + USER.START_WEEK, currentDate + USER.START_DAY, 0, 0);
    String dayEnd = TIME.setTime(0 + USER.START_WEEK, currentDate + USER.START_DAY, 23, 59);
    
    JSONObject jsonObject = getFreeBusy(httpClient, accessToken, dayStart, dayEnd, timeZone, ids);

    // Set default preferences
    USER.applyStudySessionStartTimes();
    USER.applyDurationStartTimes();

    // Go through nested response
    JSONObject calendar = (JSONObject) jsonObject.get("calendars");

    JSONObject obj = (JSONObject) calendar.get(ids.get(0));
    JSONArray array = (JSONArray) obj.get("busy"); // array cannot be null so we must initialize it with primary id
    for (String id : ids) {
      if (id == ids.get(0)) continue; // So we don't add our primary ID twice!
      System.out.println("Adding: " + id + " to our array");
      try {
        JSONObject cal = (JSONObject) calendar.get(id);
        JSONArray busyTime = (JSONArray) cal.get("busy");
        array.addAll(busyTime);
      } catch (Exception e) {
        System.out.println("An exception occurred!" + e);
        continue;
      }
    }

    // What are we trying to do?
    // We trying to determine if a time period is valid. So we loop through each time period and it's duration and check it against each busy period.
    // After it's been checked against every busy period and we haven't continued, then it's valid!
    // NOTE: We add 
    DateTime timeToTry = new DateTime().withZone(DateTimeZone.forID(TIME.timezone)).plusWeeks(USER.START_WEEK).plusDays(currentDate + USER.START_DAY);
    DateTime timeToTryEnd = null;

    List<DateTime> listOfValidTimes = new ArrayList<DateTime>();

      // Loop through specific study session start times and see if one works.
      for (Map.Entry<Integer,Integer> time : USER.STUDY_SESSION_START_TIME.entrySet()) {
        timeToTry = timeToTry.withZone(DateTimeZone.forID(TIME.timezone)).withHourOfDay(time.getKey()).withMinuteOfHour(time.getValue()).withSecondOfMinute(0);
        System.out.println(timeToTry);

        // Check that we have not already gone past the time we are trying to schedule!
        if (new DateTime().isAfter(timeToTry)) continue;

        // Start with the max duration
        for (Map.Entry<Integer,Integer> entry : USER.STUDY_SESSION_LENGTH.entrySet())  {
          Boolean foundOverlap = false;
          // Get duration of starttime
          timeToTryEnd = timeToTry.plusHours(entry.getKey()).plusMinutes(entry.getValue());
          Interval studySession = new Interval(timeToTry, timeToTryEnd);

          Iterator i = array.iterator();

          // Loop through busy periods and check if our duration could fit at any point.
          while (i.hasNext()) {
            // Grab each start and end time.
            JSONObject busyTimePeriod = (JSONObject) i.next();
            String busyStartTime = (String) busyTimePeriod.get("start");
            String busyEndTime = (String) busyTimePeriod.get("end");

            DateTime startBusy = DateTime.parse(busyStartTime).withZone(DateTimeZone.forID(TIME.timezone));
            DateTime endBusy = DateTime.parse(busyEndTime).withZone(DateTimeZone.forID(TIME.timezone));

            // Create an interval
            Interval busyInterval = new Interval(startBusy, endBusy);

            // If study session ever overlaps with any of the busy overlaps WITH ANY busy interval we can no longer set that as an event!
            if (busyInterval.overlaps(studySession)) {
              //System.out.println("OVERLAPS: Busy Interval:  " + busyInterval + " ==================  Study Session: " + studySession);
              foundOverlap = true;
              break;
            } else {
              //System.out.println("NONE: Busy Interval:  " + busyInterval + " ==================  Study Session: " + studySession);
            }
          }

          // Study session with duraion FOUND. FUTURE: We could just add to the list here
          if (!foundOverlap) {
            // TODO(paytondennis@) Check ratio here coming soon; SOON
            // Check ratio of next 4 weeks. Need to call freebusy for that time
            listOfValidTimes.add(timeToTry);
            listOfValidTimes.add(timeToTryEnd);
            return listOfValidTimes;
          }
      }
    }
    
    return listOfValidTimes;
  }

  /* Walkthrough:
  // this function will: move to date. init use freebusy to try and find the 
  // we are looking for an 80% ratio of the user being free. If 4 events, at least 3 should not be busy
  // we are using freebusy because it automatically tells us, you are busy HERE




  We're creating a LIGHT schedule so the max events we will set is 3
  Variables: MAX_EVENTS_LIGHT = 3 weekly events for light | RATIO_FOR_FREE_BUSY = 80%. We can set that recurring event at that time if the ratio is 80% or above

  // Read from UserPreferences.java to set variables
  // 1) Just within week try to find the best time for a recurring event, (We should stop at MAX_EVENTS_LIGHT if successfully created OR if we check every day).

    // Go 1 by 1 each day in the next day, call FreeBusy on the day we are currently looking at. --> it gives us back times when the user is busy during that day
    // we receive those times and then try to see if there's enough time for preset DURATION_MAX all the way to DURATION_MIN at SPECIFIC POINTS (eg. 9am/9:30am). 
    
    // 2) IF we have found a possible POINT and DURATION that works for the week. We will check the following weeks if this possible time beats the ratio, if it does
      // We give back the user a List<Integer> (for now) START HOUR, START MIN,LENGTH HOURS, LENGTH IN MINUTES (specific points) and (DURATION_MIN) 


                                                        
      |--------|         |------------|        |----|


  */

  // TODO(paytondennis): Change this function to UserPreference list. Just read from the list and return that.
  // As soon as we get permission, we can invoke default preferences that the user can change.

  // This function will be used to get all the calendar ID's
  public List<String> getAllCalendarIds(DefaultHttpClient httpClient, String accessToken) {
    List<String> ids =  new ArrayList<String>();
    JSONObject userCalendars = getCalendarsList(httpClient, accessToken);
    JSONArray items = (JSONArray) userCalendars.get("items");
    Iterator iter = items.iterator();
    while (iter.hasNext()) {
      JSONObject calendarResource = (JSONObject) iter.next();
      String calender_id = (String) calendarResource.get("id");
      if (calender_id.contains("#")) continue;
      ids.add(calender_id);
      System.out.println(calender_id);
    }
    return ids;
  }

  // ALTER BY USER SETTING: just delete event / find next available time / force move to next day / leave_as_is
  // the fixer should be given a list of days and then perform user action specified for events.
  public void SchedulerFixer() {
      
  }
}
