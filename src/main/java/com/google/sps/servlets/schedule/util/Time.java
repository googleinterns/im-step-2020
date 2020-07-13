package com.google.sps.servlets;

import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.util.GregorianCalendar;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.joda.time.DateTimeZone;
import org.joda.time.DateTime;

/** Should be able to READ string. */
public class Time {
    // For recurrences!
    public final String SUNDAY = "SU";
    public final String MONDAY = "MO";
    public final String TUESDAY = "TU";
    public final String WEDNESDAY = "WE";
    public final String THURSDAY = "TH";
    public final String FRIDAY = "FR";
    public final String SATURDAY = "SA";

    public final DateTime NEXT_SUNDAY = nextDayOfWeek(1);
    public final DateTime NEXT_MONDAY = nextDayOfWeek(2);
    public final DateTime NEXT_TUESDAY = nextDayOfWeek(3);
    public final DateTime NEXT_WEDNESDAY = nextDayOfWeek(4);
    public final DateTime NEXT_THURSDAY = nextDayOfWeek(5);
    public final DateTime NEXT_FRIDAY = nextDayOfWeek(6);
    public final DateTime NEXT_SATURDAY = nextDayOfWeek(7);

    public static ZoneId timeZoneId = null;
    public static String timezone = "";
    

    private DateTimeFormatter formatter = DateTimeFormatter
            .ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX")
            .withZone(timeZoneId);

    public void setTimeZoneId(String timeZone) {
        this.timezone = timeZone;
        this.timeZoneId = ZoneId.of( timeZone ) ; 
    }

    // Set time x weeks ahead.
    public String addWeeks(Date date, int weeks) {
        // Convert Time To Calendar to add the amount of weeks.
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        c.add(Calendar.WEEK_OF_YEAR, weeks);

        // Convert calendar back to Date
        Date newDate = c.getTime();
        return formatter.ISO_OFFSET_DATE_TIME.withZone(timeZoneId).format(newDate.toInstant());
    }

    // TODO(paytondennis@): Problem: Java is PUSHING your date back based on time zones. For instance, if you set Central time (-05:00). It will
    // push it 5 hours back. This makes our data start days and end days, 5 hours forward and 5 hours back. Need to fix. So we see the day shifted etc for central: 7pm-6:59pm

    // Create your own method of THIS!
    // Set time to given values // NOTE this is in UTC: Hence 9am (CT - central time) is 4am in UTC
    public String setTime(int weeks, int days, int hour, int min) {
        DateTime date = new DateTime()
            .withZone(DateTimeZone.forID(timezone))
            .plusWeeks(weeks)
            .plusDays(days)
            .withHourOfDay(hour)
            .withMinuteOfHour(min)
            .withSecondOfMinute(0)
            .withMillisOfSecond(0);
        return formatter.ISO_OFFSET_DATE_TIME.withZone(timeZoneId).format(date.toDate().toInstant());
    }


    // --------------- Recurrences -------------------- //
    // Use to create recurrences: https://icalendar.org/rrule-tool.html

    // Abstraction to make entering days, a tad easier
    // List<String> recurrence
    public String onDays(Integer day) {
        String dOw = "";
        switch(day) {
            case 1:
              dOw = MONDAY;
              break;
            case 2:
              dOw = TUESDAY;
              break;
            case 3:
              dOw = WEDNESDAY;
              break;
            case 4:
              dOw = THURSDAY;
              break;
            case 5:
              dOw = FRIDAY;
              break;
            case 6:
              dOw = SATURDAY;
              break;
            default:
              dOw = SUNDAY;
            }

        return dOw;
        // StringBuilder str = new StringBuilder(); 
        // for (String day : recurrence) {
        //     str.append(day).append(",");
        // }

        // // delete the last comma
        // String s = str.toString();
        // s = s.substring(0, s.length() - 1);
    }

    // Note COUNT is how many times this 1 event is repeated. So to schedule for a month, count must be 12 (3 events/week * 4 weeks/month)
    // Also note the name... Week not Weekly because we might want to change the interval in the future
    public String createWeekRecurrence(String days, String endDate) {
        endDate = endDate.replace("-", "");
        endDate = endDate.replace(":", "");
        endDate = endDate.replace(".", "");
        endDate = endDate.substring(0, 8);
    
        return new StringBuilder("RRULE:FREQ=WEEKLY;INTERVAL=1;BYDAY=" + days + ";UNTIL=" + endDate + "T000000Z;").toString();
    }

    // --------------- ----------- -------------------- //

     // SUN - 1 // MON - 2 // TUE - 3 // WED - 4 // THU - 5 // FRI - 6 // SAT - 7 // 
    public DateTime nextDayOfWeek(int day) {

        Calendar c = Calendar.getInstance();
        int diff = day - c.get(Calendar.DAY_OF_WEEK);
        if (diff <= 0) {
            diff += 7;
        }
        c.add(Calendar.DAY_OF_MONTH, diff);
        return new DateTime(c.getTime());

        
    }
}


 // Convert string to DateTime

    // String timeStr = date.toString();
    // DateTime dateTime = DateTime.parseRfc3339(timeStr);
    // long millis = dateTime.getValue();

    // String result = formatter.format(new Date(millis).toInstant());
    // System.out.println(result);