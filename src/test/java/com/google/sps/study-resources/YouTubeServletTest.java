package com.google.sps.servlets;

import com.google.gson.Gson;
import java.nio.charset.StandardCharsets;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

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


public class YouTubeServletTest extends Mockito {

  private final LocalServiceTestHelper helper = new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig(),
            new LocalTaskQueueTestConfig(),
            new LocalBlobstoreServiceTestConfig(),
            new LocalUserServiceTestConfig());

  @Before
  public void setUp() {
    helper.setUp();
  }

  @After
  public void tearDown() {
    helper.tearDown();
  }

  @Test
  public void testdoPost() throws Exception {
    /*
      This is a test to see we successfully take a string and parse an Integer out of it.
      Our test input is a string containing the number 5.
      Testing: YouTubeServlet.doPost()
    */

    HttpServletRequest request = mock(HttpServletRequest.class);
    HttpServletResponse response = mock(HttpServletResponse.class);
    HttpSession session = mock(HttpSession.class);

    String json = "5";
    // Set up rules for going through the servlet
    when(request.getSession(false)).thenReturn(session);
    when(request.getReader()).thenReturn(new BufferedReader(new StringReader(json)));

    StringWriter stringWriter = new StringWriter();
    PrintWriter writer = new PrintWriter(stringWriter);
    when(response.getWriter()).thenReturn(writer);
    // Test doPost
    new YouTubeServlet().doPost(request,response);

    writer.flush();

    // We expect doPost to write back true if we did not encounter an error
    assertEquals(true, stringWriter.toString().contains("true"));
  }

  @Test
  public void testdoGet() throws Exception {
    /*
      
      Testing: YouTubeServlet.doGet()
    */

    HttpServletRequest request = mock(HttpServletRequest.class);
    HttpServletResponse response = mock(HttpServletResponse.class);
    HttpSession session = mock(HttpSession.class);
    YouTubeServlet youtube = new YouTubeServlet();
    // Establish rule
    when(request.getSession(false)).thenReturn(session);
    when((String) request.getSession(false).getAttribute("searchKeyword")).thenReturn("Python Programming");

    StringWriter stringWriter = new StringWriter();
    PrintWriter writer = new PrintWriter(stringWriter);
    when(response.getWriter()).thenReturn(writer);
    
    // First, call doPost, which is required to pass a number through
    youtube.init();
    youtube.doGet(request, response);
    
    ArrayList<String> expected = new ArrayList<String>();
    expected.add("\"https://www.youtube.com/embed/kLZuut1fYzQ\"");
    expected.add("\"https://www.youtube.com/watch?v\\u003dkLZuut1fYzQ\"");
    
    System.out.println("Expected: " + expected.toString().replaceAll(" ", ""));
    System.out.println("Fetched: " + stringWriter.toString().replaceAll(" ", ""));
    assertEquals(true, stringWriter.toString().replaceAll(" ", "").contains(expected.toString().replaceAll(" ", "")));
  }

}