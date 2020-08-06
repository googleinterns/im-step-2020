/// Copyright 2019 Google LLC
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

import org.json.simple.JSONObject;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;


/** Servlet that sends learning level */
@WebServlet("/updateLearningLevel")
public final class HomeLearningLevelServlet extends HttpServlet {
    private Datastore DB = new Datastore();

  @Override
  /** This can be extended to send other settings. */
  public void doGet(HttpServletRequest request, HttpServletResponse response) 
    throws IOException {
        String accessToken = (String) request.getSession(false).getAttribute("access_token");

        Boolean isValid = new HTTP().isAccessTokenValid(accessToken);
        if (!isValid) {
            response.sendRedirect("/home.html");
            return;
        }

        String setAttr = (String) request.getParameter("set");
        Boolean setLearningLevel = Boolean.parseBoolean(setAttr);

        String setNotesAttr = (String) request.getParameter("setNotes");
        Boolean setNotes = Boolean.parseBoolean(setNotesAttr);

        GetCalendar getCalendar = new GetCalendar();
        String json = new HTTP().get(getCalendar.createGetCalendarURL("primary", accessToken));
        JSONObject jsonObject = new HTTP().parseJSON(json);
        String primary_id = (String) jsonObject.get("id");

        if (DB.getUser(primary_id) == null) {
            response.sendRedirect("/home.html");
            return;
        }

        String level = "";
        String notes = "";
        JSONObject sendJSON = new JSONObject();
        response.setContentType("application/json");

        if (setLearningLevel) {
            level = (String) request.getParameter("level");
            DB.updateTextProperty(primary_id, "userEventsChoice", level);
            response.getWriter().print(true);
        } else if (setNotes) {
            notes = (String) request.getParameter("notes");
            DB.updateTextProperty(primary_id, "notes", notes);
            response.getWriter().print(true);
        }
        else {
            level = DB.getUserSetting(primary_id, "userEventsChoice");
            notes = DB.getUserSetting(primary_id, "notes");
            sendJSON.put("level", level);
            sendJSON.put("notes", notes);
            response.getWriter().print(sendJSON);
        }
    }
}
