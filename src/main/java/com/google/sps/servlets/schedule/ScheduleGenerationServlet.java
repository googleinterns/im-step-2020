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
import org.apache.http.client.methods.HttpPatch;
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
    
    // Grab access token! REQUIRED!
    String accessToken = (String) request.getSession(false).getAttribute("access_token");

    DefaultHttpClient httpClient = new DefaultHttpClient();

    String id = "";
    // Check calendar not already made
    if (studyScheduleNotMade(httpClient, accessToken, "summary")) {
      JSONObject calendar = createNewCalendar(httpClient, accessToken, "Study Schedule");
      id = (String) calendar.get("id");
      request.getSession().setAttribute("study-schedule-id", id);
    }
    id = (String) request.getSession(false).getAttribute("study-schedule-id");

    // Grab resources
    List<String> resources = (List<String>) request.getSession(false).getAttribute("resources"); // TODO: get from data store

    // Grab timezone to be used with creation of event.
    JSONObject timeZoneSetting = getSetting(accessToken, "timezone");
    String timezone = (String) timeZoneSetting.get("value");
    TIME.setTimeZoneId(timezone);

    // Let's try to create events! We depend on RECURRING EVENTS. So, upto the next week, we try to create 
    int successfulEventsCreated = 0;

    // Calendars we take into account for FreeBusy information here
    List<String> allCalendarIds = getAllCalendarsAttribute(httpClient, accessToken, "id");

    List<String> ids = new ArrayList<String>();

    // Remove bad calendar ids. The calendars that contain '#', are not actual calendars that have events.
    for (String calendar_id : allCalendarIds) {
      if (!calendar_id.contains("#")) {
        ids.add(calendar_id);
      }
    }

    // To evenly space events within range.
    int stepValue = USER.EVENT_LOOK_SPAN / 2;
    int day = 0;
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

      String res = USER.DESCRIPTION;

      List<DateTime> times = getStartInformationForPossibleEvent(httpClient, accessToken, day , timezone, ids);
      if (!times.isEmpty()) {

        JSONObject event = createNewEvent(httpClient, accessToken, id, times.get(0), times.get(1), "Study Session", res, USER.EVENT_RECURRENCE_LENGTH);

        ++successfulEventsCreated;
        // Add day
        eventAlreadyScheduled.add(day);
      } 

      // Increase step value
      day += stepValue;
    }

    //////////////////////// 4) Call Fixer | ALTER BY USER SETTING: just delete event / find next available time / force move to next day /
    // the fixer should be given a list of days and then perform user action

    putResourcesInEvents(httpClient, accessToken, id, resources);

		httpClient.getConnectionManager().shutdown();
   
    response.sendRedirect("/"); 
  }

  // ---------------- Functions For Google Calendar API Access ----------------- // 

  // Creates a new calendar for the user
  JSONObject createNewCalendar(DefaultHttpClient httpClient, String accessToken, String summary) {
    NewCalendar calendar = new NewCalendar(summary);
    HttpPost postRequest = new HttpPost(calendar.createNewCalendarURL(accessToken));
    
    Gson gson = new Gson();
    String json = gson.toJson(calendar);

    return http.postWithData(httpClient, postRequest, json);
  } 

  // Creates a new calendar event on the calendar_id specified.
  JSONObject createNewEvent(DefaultHttpClient httpClient, String accessToken, String id, DateTime startTime, DateTime endTime, 
  String summary, String description, int recurrenceLengthInWeeks) {
    
    // 1) Set start date and end date for recurrence
    int dayOfWeek = startTime.getDayOfWeek();

    // 2) Add event fields: Summary, Description, StartTime, EndTime, Recurrence
    NewEvent event = new NewEvent(summary, description);

    // Get zone ID since Java must be told the time zone.
    ZoneId z = ZoneId.of( TIME.timezone ) ; 

    DateTimeFormatter fmt = DateTimeFormatter
            .ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX")
            .withZone(z);

    // Set event START TIME and END TIME
    event.setStart(fmt.format(startTime.toDate().toInstant()), TIME.timezone);
    event.setEnd(fmt.format(endTime.toDate().toInstant()), TIME.timezone);

    // Add a recurrence to this event that: repeats on THE DAYS GIVEN until X week(s) from now
    event.addRecurrence(TIME.createWeekRecurrence(TIME.onDays(dayOfWeek), TIME.addWeeks(startTime.toDate(), recurrenceLengthInWeeks)));

    // 3) Make request
    Gson gson = new Gson();
    String json = gson.toJson(event);

    HttpPost postRequest = new HttpPost(event.createNewEventURL(id, accessToken));
    return http.postWithData(httpClient, postRequest, json);
  }

  // Updates calendar id event
  JSONObject updateEvent(DefaultHttpClient httpClient, String accessToken, String calendar_id, String id, String description) {
    PatchEvent calendar = new PatchEvent(description);
    HttpPatch patchRequest = new HttpPatch(calendar.createPatchEventURL(calendar_id, id, accessToken));
    
    Gson gson = new Gson();
    String json = gson.toJson(calendar);

    return http.patchWithData(httpClient, patchRequest, json);
  }

  // Returns a setting on the user's Google Calendar
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

  // Returns the freeBusy information for a specific timeMin and timeMax
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

  // Returns a list of calendars that the user has on their calendar
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

  // Returns a list of events on a user's calendar
  JSONObject getListEvents(DefaultHttpClient httpClient, String accessToken, String calendar_id) {
    ListEvents list =  new ListEvents();
    String json = "";
    try {
      json = http.get(list.createListEventsURL(calendar_id, accessToken));
    } catch (Exception e) {
      System.out.println("There was an error getting the list of events for a user's calendar." + e);
    }
    return http.parseJSON(json);
  }

  // Returns a list of instances for a recurring event
  JSONObject getEventRecurrenceInstances(DefaultHttpClient httpClient, String accessToken, String calendar_id, String recurring_event_id) {
    GetEventInstance instances =  new GetEventInstance();
    String json = "";
    try {
      json = http.get(instances.createGetEventInstanceURL(calendar_id, recurring_event_id, accessToken));
    } catch (Exception e) {
      System.out.println("There was an error getting the list of instances for a recurring event." + e);
    }
    return http.parseJSON(json);
  }

  // This function will be used to get all the calendar ID's
  public List<String> getAllCalendarsAttribute(DefaultHttpClient httpClient, String accessToken, String attr) {

    JSONObject userCalendars = getCalendarsList(httpClient, accessToken);
    JSONArray items = (JSONArray) userCalendars.get("items");
    Iterator iter = items.iterator();

    List<String> listAttr =  new ArrayList<String>();
    while (iter.hasNext()) {
      JSONObject calendarResource = (JSONObject) iter.next();
      String calenderAttr = (String) calendarResource.get(attr);
      listAttr.add(calenderAttr);
    }
    return listAttr;
  }


  // ----------------- // Utility Functions // --------------------- //

  // This function gives us the Start Time and End Time which we directly pass to createNewEvent! So let's say we're given a day. We get the freeBusy information for user on that day. 
  // Then for each study session duration simply loop through each study session start time and see if range is valid.
  public List<DateTime> getStartInformationForPossibleEvent(DefaultHttpClient httpClient, String accessToken, Integer currentDate, String timeZone, List<String> ids ) {
    // Get length of current date
    String dayStart = TIME.setTime(0 + USER.START_WEEK, currentDate + USER.START_DAY, 0, 0);
    String dayEnd = TIME.setTime(0 + USER.START_WEEK, currentDate + USER.START_DAY, 23, 59);
    
    JSONObject jsonObject = getFreeBusy(httpClient, accessToken, dayStart, dayEnd, timeZone, ids);

    // Go through nested response
    JSONObject calendar = (JSONObject) jsonObject.get("calendars");

    JSONObject obj = (JSONObject) calendar.get(ids.get(0));
    JSONArray array = (JSONArray) obj.get("busy"); // array cannot be null so we must initialize it with primary id
    for (String id : ids) {
      if (id == ids.get(0)) continue; // So we don't add our primary ID twice!
      try {
        JSONObject cal = (JSONObject) calendar.get(id);
        JSONArray busyTime = (JSONArray) cal.get("busy");
        array.addAll(busyTime);
      } catch (Exception e) {
        System.out.println("An exception occurred when trying to get busytime!" + e);
        continue;
      }
    }

    // We simply loop through each duration and then loop through each starting time and try to schedule that event on that day!
    DateTime timeToTry = new DateTime().withZone(DateTimeZone.forID(TIME.timezone)).plusWeeks(USER.START_WEEK).plusDays(currentDate + USER.START_DAY);
    DateTime timeToTryEnd = null;

    List<DateTime> listOfValidTimes = new ArrayList<DateTime>();

    // Start with the max duration
    for (Map.Entry<Integer,Integer> entry : USER.STUDY_SESSION_LENGTH.entrySet())  {

      // Loop through specific study session start times and see if one works.
      for (Map.Entry<Integer,Integer> time : USER.STUDY_SESSION_START_TIME.entrySet()) {
        timeToTry = timeToTry.withZone(DateTimeZone.forID(TIME.timezone)).withHourOfDay(time.getKey()).withMinuteOfHour(time.getValue()).withSecondOfMinute(0);
        //System.out.println(timeToTry);

        // Check that we have not already gone past the time we are trying to schedule!
        if (new DateTime().isAfter(timeToTry)) continue;

        // Start with the max duration
        Boolean foundOverlap = false;
        // Get duration of starttime ; NOTE: we subtract 1 because overlap works by: If BUSY: 5:00pm-6:00pm ; We would not schedule: 4:00PM-5:00pm but we should.
        timeToTryEnd = timeToTry.plusHours(entry.getKey()).plusMinutes(entry.getValue() - 1);
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
            foundOverlap = true;
            break;
          }
        }

        // Study session with duraion FOUND. FUTURE: We could just add to the list here
        if (!foundOverlap) {
          // TODO(paytondennis@) Check ratio here coming soon; SOON
          // Check ratio of next 4 weeks. Need to call freebusy for that time
          listOfValidTimes.add(timeToTry);
          listOfValidTimes.add(timeToTryEnd.plusMinutes(1)); // add 1 minute back, because look at above comment!
          return listOfValidTimes;
        }
      }
    }
    
    return listOfValidTimes;
  }

  // Put resources in events!
  // *) To get event instances, you need an event ID. SO first we must get all recurring events and their ID in that calendar
  // 1) Get event by getting event instances: https://developers.google.com/calendar/v3/reference/events/instances
  // 2) Use id attribute
  // 3) Make patch request using event INSTANCE id not recurring id and calendar id: https://developers.google.com/calendar/v3/reference/events/patch
  
  // Puts resources in each recurring event instance
  public void putResourcesInEvents(DefaultHttpClient httpClient, String accessToken, String calendar_id, List<String> resources) {
    // Get all recurring event IDS. (TODO: We want to track if an event already has a resource!)
    JSONObject obj = getListEvents(httpClient, accessToken, calendar_id);
    JSONArray items = (JSONArray) obj.get("items");
    Iterator i = items.iterator();

    int resourceCount = 0;
    while (i.hasNext()) {
      JSONObject event = (JSONObject) i.next();
      String recurring_eid = (String) event.get("id");

      // For this id, loop through each instance
      JSONObject instances = getEventRecurrenceInstances(httpClient, accessToken, calendar_id, recurring_eid);
      JSONArray recurringEvents = (JSONArray) instances.get("items");
      Iterator j = recurringEvents.iterator();
      
      while (j.hasNext()) {
        JSONObject specificInstance = (JSONObject) j.next();
        String specific_recurring_eid = (String) specificInstance.get("id");


        String res = "";
        try {
          res = resources.get(resourceCount) + " \n---\n" + USER.DESCRIPTION;
        } catch (Exception e) {
          break;
        }
        // Now make updates to this event as necessary
        updateEvent(httpClient, accessToken, calendar_id, specific_recurring_eid, res);
        
        ++resourceCount;
      }
    }
  }

  // Check that we have not already made study schedule
  public Boolean studyScheduleNotMade(DefaultHttpClient httpClient, String accessToken, String attr) {
    List<String> allCalendarTitles = getAllCalendarsAttribute(httpClient, accessToken, "summary");
    for (String title : allCalendarTitles) {
      if (title.equals("Study Schedule")) {
        return false;
      }
    }
    return true;
  }


  // If have time...
  // ALTER BY USER SETTING: just delete event / find next available time / force move to next day / leave_as_is
  // the fixer should be given a list of days and then perform user action specified for events.
  public void SchedulerFixer() {
      
  }
}
