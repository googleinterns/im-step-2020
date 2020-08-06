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

import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
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
import org.mockito.*;
import java.io.*;
import javax.servlet.http.*;
import org.junit.Test;
import org.springframework.mock.web.DelegatingServletInputStream;

/** Check the logic when we have that parameter and when we don't. */
/** ScheduleHandlerTest
 * Check doGet --> checks if we want to generate the schedule or not
 * Check doPost --> check that the given resources have been set
 * Check processStartTimes --> check that the given format translates correctly
 * Check processDurations --> check that given format translates correctly
 *  
 */
public class ScheduleHandlerServletTest extends Mockito {

  @Test
  public void testdoGetAccessTokenError() throws Exception {
    
    // Mocks request and response
    HttpServletRequest request = mock(HttpServletRequest.class);       
    HttpServletResponse response = mock(HttpServletResponse.class);    
    HttpSession session = mock(HttpSession.class);

    when (request.getSession(false)).thenReturn(session);
    when (session.getAttribute("access_token")).thenReturn("invalidAccessToken");

    // Creates a new ScheduleHandler
    new ScheduleHandlerServlet().doGet(request, response);

    // Verify that we go back to request permission
    verify(response, atLeast(1)).sendRedirect("/request-permission");

    // Verify that we do not try to generate schedule.
    verify(request, atLeast(0)).getParameter("generate");
  }

  @Test
  public void testdoPostSetsResources() throws Exception {
    
    // Mocks request and response
    HttpServletRequest request = mock(HttpServletRequest.class);       
    HttpServletResponse response = mock(HttpServletResponse.class); 
    HttpSession session = mock(HttpSession.class);

    // Mock helper
    LocalServiceTestHelper helper =
      new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig());
    helper.setUp();

    List<String> expectedResources = new ArrayList<String>(Arrays.asList("Link1", "Link2", "Link3"));

    // Mock request body in JSON format.
    Gson gson = new Gson();
    String json = gson.toJson(expectedResources);

    when(request.getInputStream()).thenReturn(new DelegatingServletInputStream(
            new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8))));
    when(request.getReader()).thenReturn( new BufferedReader(new StringReader(json)));
    when(request.getContentType()).thenReturn("application/json");
    when(request.getCharacterEncoding()).thenReturn("UTF-8");
    when(request.getSession(false)).thenReturn(session);

    when(session.getAttribute("resources")).thenReturn(expectedResources);
    when(session.getAttribute("access_token")).thenReturn("invalidToken");

    // Create a new ScheduleHandler and do a POST method
    new ScheduleHandlerServlet().doPost(request, response);


    verify(response, atLeast(1)).sendRedirect("/home.html");

    // // Create a writer that sets the response to something we can check
    // StringWriter stringWriter = new StringWriter();
    // PrintWriter writer = new PrintWriter(stringWriter);
    // when(response.getWriter()).thenReturn(writer);

    // // Make sure we flush the servlet response
    // writer.flush();
    // // Verify that we receive the resources and have sent that message back to the client.
    // assertEquals(true, stringWriter.toString().contains("true"));

    // // Verify that the session contains resources.
    // List<String> resources = (List<String>) session.getAttribute("resources");
    // assertEquals(expectedResources, resources);

    helper.tearDown();
 
  }

  @Test
  public void testprocessStartTimes() throws Exception {
    // If proper format process times.
    String startTimes = "9:00Am, 12:30Pm, 4:21pm, 8:39PM";
    List<List<Integer>> actualStartTimes = new ScheduleHandlerServlet().processStartTimes(startTimes);

    ArrayList<ArrayList<Integer>> expected = new ArrayList<ArrayList<Integer>>() {{
      add(new ArrayList<Integer>() {{
        add(9);
        add(0);
      }});
      add(new ArrayList<Integer>() {{
        add(12);
        add(30);
      }});
      add(new ArrayList<Integer>() {{
        add(16);
        add(21);
      }});
      add(new ArrayList<Integer>() {{
        add(20);
        add(39);
      }});
    }};

    assertEquals(expected, actualStartTimes);

    // If incorrect format return nothing
    startTimes = "asd10:00am, 12:30pm, 14:30pm, a18:30PM";
    actualStartTimes = new ScheduleHandlerServlet().processStartTimes(startTimes);
    expected.clear();
    assertEquals(expected, actualStartTimes);

    // If incorrect halfway return correct times already processed.
    startTimes = "10:00am, 12m:30pm";
    actualStartTimes = new ScheduleHandlerServlet().processStartTimes(startTimes);
    expected.clear();
    expected.add(new ArrayList<Integer>() {{ add(10); add(0); }});
    assertEquals(expected, actualStartTimes);

  } 

  @Test
  public void testprocessDurations() throws Exception {
    // If proper format process times.
    String durations = "1 hour 0 minutes, 4hrs 32min, 0h 30m, 1hr 15min";
    List<List<Integer>> actualDurations = new ScheduleHandlerServlet().processDuration(durations);

    ArrayList<ArrayList<Integer>> expected = new ArrayList<ArrayList<Integer>>() {{
      add(new ArrayList<Integer>() {{
        add(1);
        add(0);
      }});
      add(new ArrayList<Integer>() {{
        add(4);
        add(32);
      }});
      add(new ArrayList<Integer>() {{
        add(0);
        add(30);
      }});
      add(new ArrayList<Integer>() {{
        add(1);
        add(15);
      }});
    }};

    assertEquals(expected, actualDurations);

    // As of now, the user has to ENTER 0 for each unit HOUR and MINUTES. hr min can be in different formats.
    // WHAT CANNOT CHANGE IS having a value there EVEN if 0 for each unit.
    durations = "1hour 30min, 0hr 15min, 1hr 30min, 1HR 100MIN";
    actualDurations = new ScheduleHandlerServlet().processDuration(durations);

    expected = new ArrayList<ArrayList<Integer>>() {{
      add(new ArrayList<Integer>() {{
        add(1);
        add(30);
      }});
      add(new ArrayList<Integer>() {{
        add(0);
        add(15);
      }});
      add(new ArrayList<Integer>() {{
        add(1);
        add(30);
      }});
      add(new ArrayList<Integer>() {{
        add(1);
        add(100);
      }});
    }};
    assertEquals(expected, actualDurations);
  }
}