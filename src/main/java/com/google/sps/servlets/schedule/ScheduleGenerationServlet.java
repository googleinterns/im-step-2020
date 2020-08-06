package com.google.sps.servlets;

import com.google.gson.reflect.TypeToken;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Random;
import java.util.TimeZone;
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

import org.joda.time.LocalDate;
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

@WebServlet("/schedule-generator")
public class ScheduleGenerationServlet extends HttpServlet {

  private static HTTP http = new HTTP();
  private Time TIME = new Time();

  private Datastore DB = new Datastore();
  private Gson g = new Gson();

  private int successfulRecurringEventsCreated = 0;

  // Primary ID used to identify user on databsse
  private String primary_id = "";

  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response) 
    throws ServletException, IOException {
    // reset successfulEventsCreated
    successfulRecurringEventsCreated = 0;
    
    // Grab access token and make sure it's valid! REQUIRED!
    String accessToken = (String) request.getSession(false).getAttribute("access_token");

    if (!http.isAccessTokenValid(accessToken)) {
      response.sendRedirect("/request-permission");
      return;
    }

    // Check to make sure the user has a settings page ready to go to read from in the database!
    GetCalendar getCalendar = new GetCalendar();
    String json = new HTTP().get(getCalendar.createGetCalendarURL("primary", accessToken));
    JSONObject jsonObject = new HTTP().parseJSON(json);
    this.primary_id = (String) jsonObject.get("id");
    if (DB.getUser(primary_id) == null) {
      response.sendRedirect("/request-permission");
      return;
    }

    String userEventsJSON = DB.getUserSetting(this.primary_id, "userEventsChoice");
    int userEventsChoice = g.fromJson(userEventsJSON, Integer.class);

    GenerateSchedule(request);

    if (this.successfulRecurringEventsCreated < userEventsChoice) {
      GenerateSchedule(request);
    }

    if (this.successfulRecurringEventsCreated < userEventsChoice) {
      response.setContentType("application/json");
      JSONObject sendJSON = new JSONObject();
      sendJSON.put("error", "The Schedule Generator was not able to schedule maximum events." + 
      "1) Add more times and durations 2) Add more days or a bigger span to attempt to add recurring events! 3) Or free up your calendar busy bee! ;)");
      response.getWriter().print(sendJSON);
      return;
    }
   
    response.sendRedirect("/home.html"); 
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

  // Get an Event Resources
  JSONObject getEvent(DefaultHttpClient httpClient, String accessToken, String calendar_id, String eventId) {
    GetEvent eventObj =  new GetEvent();
    String json = "";
    try {
      json = http.get(eventObj.createGetEventURL(calendar_id, eventId, accessToken, Time.timezone));
    } catch (Exception e) {
      System.out.println("There was an error getting the event resource." + e);
    }
    return http.parseJSON(json);
  }

  // Delete event
  void deleteEvent(String accessToken, String calendar_id, String eventId) {
    DeleteEvent eventObj =  new DeleteEvent();
    String json = "";
    try {
      json = http.delete(eventObj.createDeleteEventURL(calendar_id, eventId, accessToken));
    } catch (Exception e) {
      System.out.println("There was an error deleting the event. " + e);
    }
  }

  // Creates a new calendar event on the calendar_id specified.
  void createNewEvent(DefaultHttpClient httpClient, String accessToken, String id, DateTime startTime, DateTime endTime, 
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
    http.postWithData(httpClient, postRequest, json);
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
      System.out.println("There was an error getting the list of instances for a recurring event. " + e);
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

/** This is a pure function meaning that it will generate the same schedule if the same settings are present.
 * 
 * In a nutshell, we simply try to evenly place recurring events within our SPAN. 
 * For example our SPAN == 7 and the today is Monday assuming Default Settings and FreeTime every day.
 *   X     X     X   
 *   M  T  W  T  F  S  S
 * 
 * stepValue: Is the number of days we skip. Currently, within the span we skip span/3 days to start.
 * Example: Our span is 7. 7/3 = 2. We skip every other day.
 * 
 * 
 * The interesting case is when we land on a INVALID day. 
 * When this happens, we simply move our countofDaysFromNow + 1 + stepValue.
 * All this means, is we go to 1 week from tomorrow and try that day. Increasing our stepValue by 1.
 * 
 * @param request To get access to resources, access token, and more.
 * @return none we simply generate the schedule.
*/
  private void GenerateSchedule(HttpServletRequest request) {
    String accessToken = (String) request.getSession(false).getAttribute("access_token");
    String searchKeyword = (String) request.getSession(false).getAttribute("searchKeyword");

    DefaultHttpClient httpClient = new DefaultHttpClient();

    String id = "";
    // Check calendar not already made
    if (studyScheduleNotMade(httpClient, accessToken, "summary")) {
      JSONObject calendar = createNewCalendar(httpClient, accessToken, "Study Schedule");
      id = (String) calendar.get("id");
      request.getSession().setAttribute("study-schedule-id", id);
    }
    id = (String) request.getSession(false).getAttribute("study-schedule-id");
    
    // Grab timezone to be used with creation of event.
    JSONObject timeZoneSetting = getSetting(accessToken, "timezone");
    String timezone = (String) timeZoneSetting.get("value");
    TIME.setTimeZoneId(timezone);

    // Calendars we take into account for FreeBusy information here
    List<String> allCalendarIds = getAllCalendarsAttribute(httpClient, accessToken, "id");
    List<String> ids = new ArrayList<String>();

    // Remove bad calendar ids. The calendars that contain '#', are not actual calendars that have events.
    for (String calendar_id : allCalendarIds) {
      if (!calendar_id.contains("#")) {
        ids.add(calendar_id);
      }
    }

    // Let's try to create events! We depend on RECURRING EVENTS. So, upto the next week, we try to create 
   
    // To evenly space events within range.
    String spanGSON = DB.getUserSetting(this.primary_id, "eventLookSpan");
    int span = g.fromJson(spanGSON, Integer.class);

    int stepValue = span / 3;
    int countOfDaysFromNow = 0;
    List<Integer> eventAlreadyScheduled = new ArrayList<Integer>();

    // Get study schedule intensity from database
    String userEventsJSON = DB.getUserSetting(this.primary_id, "userEventsChoice");
    int userEventsChoice = g.fromJson(userEventsJSON, Integer.class);

    // Get event look span from database
    String eventLookJSON = DB.getUserSetting(this.primary_id, "eventLookSpan");
    int eventLookSpan = g.fromJson(eventLookJSON, Integer.class);

    // Get days event could be on
    String onDaysJSON = DB.getUserSetting(this.primary_id, "onDays");
    List<Integer> possibleDays = g.fromJson(onDaysJSON, new TypeToken<List<Integer>>(){}.getType());

    // Get event recurrence length
    String eventRecurrenceLengthJSON = DB.getUserSetting(this.primary_id, "eventRecurrenceLength");
    int eventRecurrenceLength = g.fromJson(eventRecurrenceLengthJSON, Integer.class);
    for (int i = 0; i < span; i++) {
      // If we created max number of events
      if (this.successfulRecurringEventsCreated == userEventsChoice) break;

      if (new DateTime().plusDays(countOfDaysFromNow).isAfter(new DateTime().plusWeeks(eventLookSpan))) break;

      Boolean isvalid = checkDateIsValid(possibleDays, getCurrentDay(countOfDaysFromNow));

      // Check that day is valid
      if (!isvalid) {
        span += 7; // Increase our possible span. 
        //countOfDaysFromNow += stepValue; // To try the next day, 1 week in advance.
        countOfDaysFromNow++;
        stepValue++; // Increase step value.
        continue;
      }

      // If day is greater than or equal to span
      if (countOfDaysFromNow >= span) {
        countOfDaysFromNow = 0;
        // Move day to next day that was not already scheduled
        for (Integer event : eventAlreadyScheduled) {
          if (event == countOfDaysFromNow) countOfDaysFromNow++;
          else break;
        }
        // Decrease step value because we stepValue exceeded our span.
        --stepValue;
      }

      List<DateTime> times = getStartInformationForPossibleEvent(httpClient, accessToken, countOfDaysFromNow , timezone, ids);

      // If we have times to schedule, schedule those times
      if (!times.isEmpty() && isvalid) {

        try {
          createNewEvent(httpClient, accessToken, id, times.get(0), times.get(1), "Study Session - " + searchKeyword, "", eventRecurrenceLength);
          ++this.successfulRecurringEventsCreated;
        } catch (Exception e) {
          System.out.println("There was an error trying to create a new event" + e);
        }
        // Add day
        eventAlreadyScheduled.add(countOfDaysFromNow);
      } 

      // Move to next day
      countOfDaysFromNow += stepValue;
    }
    String resourcesGSON = DB.getUserSetting(this.primary_id, "resources");
    List<String> resources = g.fromJson(resourcesGSON, List.class);
    

    editEventInstance(httpClient, accessToken, id, resources, ids);
  }

  // Current day of the week
  public int getCurrentDay(final int day) {
    // Get start week
    String startWeekJSON = DB.getUserSetting(this.primary_id, "startWeek");
    int startWeek = g.fromJson(startWeekJSON, Integer.class);

    // Get start Day
    String startDayJSON = DB.getUserSetting(this.primary_id, "startDay");
    int startDay = g.fromJson(startDayJSON, Integer.class);

    DateTime currentDateTime = new DateTime().plusWeeks(startWeek).plusDays(startDay + day);
    return currentDateTime.getDayOfWeek();
  }

  // Check that a given day is a valid day to set an event based on user settings
  public Boolean checkDateIsValid(List<Integer> validDays, int currentDay) {
    if (validDays.isEmpty()) return true;

    for (Integer possibleDay : validDays) {
      if (possibleDay == currentDay) return true;
    }
    return false;
  }

  // This function gives us the Start Time and End Time which we directly pass to createNewEvent! So let's say we're given a day. We get the freeBusy information for user on that day. 
  // Then for each study session duration simply loop through each study session start time and see if range is valid.
  public List<DateTime> getStartInformationForPossibleEvent(DefaultHttpClient httpClient, String accessToken, Integer currentDay, String timeZone, List<String> ids ) {
    // Get start week
    String startWeekJSON = DB.getUserSetting(this.primary_id, "startWeek");
    int startWeek = g.fromJson(startWeekJSON, Integer.class);

    // Get start Day
    String startDayJSON = DB.getUserSetting(this.primary_id, "startDay");
    int startDay = g.fromJson(startDayJSON, Integer.class);

    
    // Get length of current date
    String dayStart = TIME.setTime(0 + startWeek, currentDay + startDay, 0, 0);
    String dayEnd = TIME.setTime(0 + startWeek, currentDay + startDay, 23, 59);
    
    JSONObject jsonObject = getFreeBusy(httpClient, accessToken, dayStart, dayEnd, timeZone, ids);

<<<<<<< HEAD
    // Set default preferences
    //USER.applyStudySessionStartTimes();
    //USER.applyDurationStartTimes();

=======
>>>>>>> 23c8982f281dbb8c76ebef90a87103b0ad7252a5
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
    DateTime timeToTry = new DateTime().withZone(DateTimeZone.forID(Time.timezone)).plusWeeks(startWeek).plusDays(currentDay + startDay);
    DateTime timeToTryEnd = null;

    List<DateTime> listOfValidTimes = new ArrayList<DateTime>();

<<<<<<< HEAD
    // Start with the max duration
    
    for (List<Integer> entry : USER.STUDY_SESSION_LENGTH)  {
=======
    // Get start times
    String startTimesJSON = DB.getUserSetting(this.primary_id, "start");
    List<List<Integer>> starttimes = g.fromJson(startTimesJSON, new TypeToken<List<List<Integer>>>(){}.getType());
    
    // Get max duration
    String durationsJSON = DB.getUserSetting(this.primary_id, "length");
    List<List<Integer>> durations = g.fromJson(durationsJSON, new TypeToken<List<List<Integer>>>(){}.getType());
>>>>>>> 23c8982f281dbb8c76ebef90a87103b0ad7252a5

    // Start with the max duration
    for (List<Integer> duration : durations)  {
      // Loop through specific study session start times and see if one works.
<<<<<<< HEAD
      for (List<Integer> time : USER.STUDY_SESSION_START_TIME) {
        timeToTry = timeToTry.withZone(DateTimeZone.forID(TIME.timezone)).withHourOfDay(time.get(0)).withMinuteOfHour(time.get(1)).withSecondOfMinute(0);
        System.out.println(timeToTry);
=======
      for (List<Integer> time : starttimes) {
        int rnd = new Random().nextInt(starttimes.size());
        List<Integer> possible = starttimes.get(rnd);
        timeToTry = timeToTry.withZone(DateTimeZone.forID(Time.timezone)).withHourOfDay(possible.get(0)).withMinuteOfHour(possible.get(1)).withSecondOfMinute(0);
    
>>>>>>> 23c8982f281dbb8c76ebef90a87103b0ad7252a5

        // Check that we have not already gone past the time we are trying to schedule!
        if (new DateTime().isAfter(timeToTry)) continue;


        // Start with the max duration
<<<<<<< HEAD
          Boolean foundOverlap = false;
          // Get duration of starttime
          timeToTryEnd = timeToTry.plusHours(entry.get(0)).plusMinutes(entry.get(1));
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
=======
        Boolean foundOverlap = false;
        // Get duration of starttime ; NOTE: we subtract 1 because overlap works by: If BUSY: 5:00pm-6:00pm ; We would not schedule: 4:00PM-5:00pm but we should.
        timeToTryEnd = timeToTry.plusHours(duration.get(0)).plusMinutes(duration.get(1) - 1);

        Interval studySession = new Interval(timeToTry, timeToTryEnd);

        Iterator i = array.iterator();

        // Loop through busy periods and check if our duration could fit at any point.
        while (i.hasNext()) {
          // Grab each start and end time.
          JSONObject busyTimePeriod = (JSONObject) i.next();
          String busyStartTime = (String) busyTimePeriod.get("start");
          String busyEndTime = (String) busyTimePeriod.get("end");
>>>>>>> 23c8982f281dbb8c76ebef90a87103b0ad7252a5


          DateTime startBusy = DateTime.parse(busyStartTime).withZone(DateTimeZone.forID(Time.timezone));
          DateTime endBusy = DateTime.parse(busyEndTime).withZone(DateTimeZone.forID(Time.timezone));

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
  
  // Puts resources in each recurring event instance and tries to fix each event.
  public void editEventInstance(DefaultHttpClient httpClient, String accessToken, String calendar_id, List<String> resources, List<String> ids) {
    ids.remove(calendar_id);
    // Get all recurring event IDS.
    JSONObject obj = getListEvents(httpClient, accessToken, calendar_id);
    JSONArray items = (JSONArray) obj.get("items");
    Iterator i = items.iterator();

    // Calculate event recurrence
    String eventRecurrenceLengthJSON = DB.getUserSetting(this.primary_id, "eventRecurrenceLength");
    Integer eventRecurrenceLength = g.fromJson(eventRecurrenceLengthJSON, Integer.class);

    // Get study intensity
    String userEventsChoiceJSON = DB.getUserSetting(this.primary_id, "userEventsChoice");
    Integer userEventsChoice = g.fromJson(userEventsChoiceJSON, Integer.class);

    int resourceCount = 0;
    while (i.hasNext()) {
      if (resourceCount == eventRecurrenceLength * userEventsChoice) break;
      // Create current IDS in this recurrence
      List<String> currentIdsInRecurrence = new ArrayList<String>();
      JSONObject event = (JSONObject) i.next();
      String recurring_eid = (String) event.get("id");

      // For this id, loop through each instance
      JSONObject instances = getEventRecurrenceInstances(httpClient, accessToken, calendar_id, recurring_eid);
      JSONArray recurringEvents = (JSONArray) instances.get("items");
      Iterator j = recurringEvents.iterator();
      
      int counter = 0;
      JSONObject beginningEvent = null;
      JSONObject endEvent = null;
      while (j.hasNext()) {
        ++resourceCount;
        JSONObject specificInstance = (JSONObject) j.next();
        String specific_recurring_eid = (String) specificInstance.get("id");
        currentIdsInRecurrence.add(specific_recurring_eid);

        if (counter == 0) beginningEvent = specificInstance;
        endEvent = specificInstance; 

        putResourcesInEvents(resources, resourceCount, httpClient, accessToken, calendar_id, specific_recurring_eid);

        ++counter;
        if (counter == eventRecurrenceLength) break;
      }
      fixOverlappingEvents(httpClient, accessToken, calendar_id, beginningEvent, endEvent, ids, currentIdsInRecurrence);
    }
  }

  public void putResourcesInEvents(List<String> resources, int resourceCount, DefaultHttpClient httpClient, String accessToken, String calendar_id, String specific_recurring_eid) {
    String res = "";
    String description = DB.getUserSetting(this.primary_id, "description");
    try {
      res = "Resource: " + resources.get(resourceCount) + " \n----\n" + description;
    } catch (Exception e) {
      res = description;
    }
    // Now make updates to this event as necessary
    try {
      updateEvent(httpClient, accessToken, calendar_id, specific_recurring_eid, res);
    } catch (Exception e) {
      System.out.println(e);
    }
  }

  public void fixOverlappingEvents(DefaultHttpClient httpClient, String accessToken, String calendar_id, JSONObject beginningEvent, JSONObject endEvent,
  List<String> ids, List<String> currentIdsInRecurrence) {
    // Get Free Busy information for span of recurring event

    // Get range for FreeBusy information
    JSONObject eventResourceStart = null;
    JSONObject eventResourceEnd = null;

    if (beginningEvent == null || endEvent == null) return;
    String freeBusyStartID = (String) beginningEvent.get("id");
    String freeBusyEndID = (String) endEvent.get("id");
    try {
      eventResourceStart = getEvent(httpClient, accessToken, calendar_id, freeBusyStartID);
      eventResourceEnd = getEvent(httpClient, accessToken, calendar_id, freeBusyEndID);
    } catch (Exception e) {
      System.out.println(e);
      return;
    }
    JSONObject startOBJ = (JSONObject) eventResourceStart.get("start");
    JSONObject endOBJ = (JSONObject) eventResourceEnd.get("end");

    String startTime = (String) startOBJ.get("dateTime");
    String endTime = (String) endOBJ.get("dateTime");

    DateTime startEventFreeBusyTime = DateTime.parse(startTime);
    DateTime endEventFreeBusyTime = DateTime.parse(endTime);

    // Get free busy for entire span of time.
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX")
                                                   .withZone(Time.timeZoneId);
                                                  
    String s = formatter.ISO_OFFSET_DATE_TIME.withZone(Time.timeZoneId).format(startEventFreeBusyTime.toDate().toInstant());
    String e = formatter.ISO_OFFSET_DATE_TIME.withZone(Time.timeZoneId).format(endEventFreeBusyTime.toDate().toInstant());

    JSONObject freeBusy = null;
    try {
      freeBusy = getFreeBusy(httpClient, accessToken, s, e, Time.timezone, ids);
    } catch (Exception err) {
      System.out.println(err);
      return;
    }

    String deleteOverlappingEventsJSON = DB.getUserSetting(this.primary_id, "deleteOverlappingEvents");
    Boolean deleteOverlappingEvents = g.fromJson(deleteOverlappingEventsJSON, Boolean.class);

    // Loop back through events
    for (String id : currentIdsInRecurrence) {
      // Perform action if overlap!
      JSONObject instanceObject = getEvent(httpClient, accessToken, calendar_id, id);
  
      JSONObject sObject = (JSONObject) instanceObject.get("start");
      JSONObject eObject = (JSONObject) instanceObject.get("end");

      String sTime = (String) sObject.get("dateTime");
      String eTime = (String) eObject.get("dateTime");

      DateTime sEventTime = DateTime.parse(sTime);
      DateTime eEventTime = DateTime.parse(eTime);

      if (foundOverlap(sEventTime, eEventTime, freeBusy, ids)) {
        if (deleteOverlappingEvents)
          deleteEvent(accessToken, calendar_id, id);
        
      }

    // Get event endtime and stop until busyfreetime is after endtime
    }
  }

  // UTILITY function that returns true if found a overlap!
  Boolean foundOverlap(DateTime eventStart, DateTime eventEnd, JSONObject jsonObject, List<String> ids) {
    // Go through nested response
   JSONObject calendar = (JSONObject) jsonObject.get("calendars");

   JSONObject obj = (JSONObject) calendar.get(ids.get(0));
   JSONArray array = (JSONArray) obj.get("busy"); // array cannot be null so we must initialize it with primary id
   for (String id : ids) {
     if (id == ids.get(0)) continue; 
     try {
       JSONObject cal = (JSONObject) calendar.get(id);
       JSONArray busyTime = (JSONArray) cal.get("busy");
       array.addAll(busyTime);
     } catch (Exception e) {
       System.out.println("An exception occurred when trying to get busytime!" + e);
       continue;
     }
   }

   Interval studySession = new Interval(eventStart, eventEnd.plusMinutes(-1));

   Iterator i = array.iterator();

   // Loop through busy periods and check if our duration could fit at any point.
   while (i.hasNext()) {
     // Grab each start and end time.
     JSONObject busyTimePeriod = (JSONObject) i.next();
     String busyStartTime = (String) busyTimePeriod.get("start");
     String busyEndTime = (String) busyTimePeriod.get("end");


     DateTime startBusy = DateTime.parse(busyStartTime).withZone(DateTimeZone.forID(Time.timezone));
     DateTime endBusy = DateTime.parse(busyEndTime).withZone(DateTimeZone.forID(Time.timezone));

     // Create an interval
     Interval busyInterval = new Interval(startBusy, endBusy);

     // If study session ever overlaps with any of the busy overlaps WITH ANY busy interval we can no longer set that as an event!
     if (busyInterval.overlaps(studySession) || studySession.overlaps(busyInterval)) return true;

     if (startBusy.isAfter(eventEnd)) break;
     
   }
   return false;
 }

  // Check that we have not already made study schedule
  public Boolean studyScheduleNotMade(DefaultHttpClient httpClient, String accessToken, String attr) {
    List<String> allCalendarTitles = getAllCalendarsAttribute(httpClient, accessToken, attr);
    for (String title : allCalendarTitles) {
      if (title.equals("Study Schedule")) {
        return false;
      }
    }
    return true;
  }

}
