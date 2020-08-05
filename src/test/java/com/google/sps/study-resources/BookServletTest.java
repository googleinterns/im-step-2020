package com.google.sps.servlets;

import com.google.gson.Gson;
import java.nio.charset.StandardCharsets;

import org.json.simple.JSONObject;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletOutputStream;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.After;
import org.mockito.*;
import java.io.*;
import javax.servlet.http.*;
import org.junit.Test;
import org.springframework.mock.web.DelegatingServletInputStream;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalTaskQueueTestConfig;
import com.google.appengine.tools.development.testing.LocalBlobstoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalUserServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;

public class BookServletTest extends Mockito {
  
  @Test
  public void testMakeURL() throws Exception {
    /*
      Test that given the id and title, we should make a full URL (replacing an spaces in the title with underscores)
    */
    BookServlet books = new BookServlet();
    String id = "aJQILlLxRmAC";
    String title = "Python Programming";
    String expected = "https://www.google.com/books/edition/Python_Programming/aJQILlLxRmAC";

    System.out.println("Expected: " + expected);
    System.out.println("Fetched: " + books.makeURL(title, id));
    assertEquals(true, expected.equals(books.makeURL(title, id)));
  }

  @Test
  public void testMakeFullTitle_1() throws Exception {
    /*
      Test that given a title and subtitle, we should receive an appended string of the two
    */
    BookServlet books = new BookServlet();
    String title = "Python Programming";
    String subtitle = "An Introduction to Computer Science";
    String expected = "Python Programming An Introduction to Computer Science";

    System.out.println("Expected: " + expected);
    System.out.println("Fetched: " + books.makeFullTitle(title, subtitle));
    assertEquals(true, expected.equals(books.makeFullTitle(title, subtitle)));
  }

  @Test
  public void testMakeFullTitle_2() throws Exception {
    /*
      Test that if the subtitle is null, we should receive only the title back
    */
    BookServlet books = new BookServlet();
    String title = "Python Programming";
    String subtitle = null;
    String expected = "Python Programming";

    System.out.println("Expected: " + expected);
    System.out.println("Fetched: " + books.makeFullTitle(title, subtitle));
    assertEquals(true, expected.equals(books.makeFullTitle(title, subtitle)));
  }
}