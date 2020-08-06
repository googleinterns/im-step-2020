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

import com.google.gson.Gson;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import javax.servlet.ServletConfig;
import javax.servlet.ServletOutputStream;
import org.json.simple.JSONObject;
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
public class DisplayCalendarSettingsTest extends Mockito {

  private HTTP HTTP =  new HTTP();

  @Test
  public void testcurrentCalendarInvalid() throws Exception {

    assertFalse(new DisplayCalendarSettingsServlet().currentCalendarIsValid("invalidAccessToken", "calendar_id"));
    assertEquals("", new DisplayCalendarSettingsServlet().checkStudyScheduleOnCalendarList("invalidAccessToken"));
    
    // Mocks request and response
    // HttpServletRequest request = mock(HttpServletRequest.class);       
    // HttpServletResponse response = mock(HttpServletResponse.class);
    // HttpSession session = mock(HttpSession.class);    

    // when(request.getSession(false)).thenReturn(session);
    // when(session.getAttribute("access_token")).thenReturn("accessToken");

    // StringWriter stringWriter = new StringWriter();
    // PrintWriter writer = new PrintWriter(stringWriter);
    // when(response.getWriter()).thenReturn(writer);

    // // Creates a new ScheduleHandler
    // new DisplayCalendarSettingsServlet().doGet(request, response);
    
    // JSONObject expectedResponse = new JSONObject();
    // expectedResponse.put("error", "The user did not give us permission.");

    // assertEquals(expectedResponse.toString(), stringWriter.toString());

  }

  // @Test
  // public void testdoPostSetsResources() throws Exception {
    
  //   // Mocks request and response
  //   HttpServletRequest request = mock(HttpServletRequest.class);       
  //   HttpServletResponse response = mock(HttpServletResponse.class); 
  //   HttpSession session = mock(HttpSession.class);
    
  //   List<String> expectedResources = new ArrayList<String>(Arrays.asList("Link1", "Link2", "Link3"));

  //   // Mock request body in JSON format.
  //   Gson gson = new Gson();
  //   String json = gson.toJson(expectedResources);

  //   when(request.getInputStream()).thenReturn(new DelegatingServletInputStream(
  //           new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8))));
  //   when(request.getReader()).thenReturn( new BufferedReader(new StringReader(json)));
  //   when(request.getContentType()).thenReturn("application/json");
  //   when(request.getCharacterEncoding()).thenReturn("UTF-8");
  //   when(request.getSession()).thenReturn(session);

  //   when(session.getAttribute("resources")).thenReturn(expectedResources);

  //   // Create a writer that sets the response to something we can check
  //   StringWriter stringWriter = new StringWriter();
  //   PrintWriter writer = new PrintWriter(stringWriter);
  //   when(response.getWriter()).thenReturn(writer);

  //   // Create a new ScheduleHandler and do a POST method
  //   new ScheduleHandlerServlet().doPost(request, response);

  //   // Make sure we flush the servlet response
  //   writer.flush();
  //   // Verify that we receive the resources and have sent that message back to the client.
  //   assertEquals(true, stringWriter.toString().contains("true"));

  //   // Verify that the session contains resources.
  //   List<String> resources = (List<String>) session.getAttribute("resources");
  //   assertEquals(expectedResources, resources);
 
  // }

}