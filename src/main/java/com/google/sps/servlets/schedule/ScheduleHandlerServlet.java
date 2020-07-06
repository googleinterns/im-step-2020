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
import java.util.List;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.List;


/** Servlet that handles the schedule. */
@WebServlet("/schedule-handler")
public final class ScheduleHandlerServlet extends HttpServlet {

  /** We want to GRAB all calendar ID's. */
  @Override
  public void init() {
  }

  /** Read through all filters */
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

    System.out.println("You're on the ScheduleHandler Servlet!");
    response.setContentType("text/html");
    response.getWriter().println("<h2>You have successfully authorized the app! And we can now make calls to the Google Calendar API!</h2>");

    //response.sendRedirect("/schedule-generator"); 
  }


}
