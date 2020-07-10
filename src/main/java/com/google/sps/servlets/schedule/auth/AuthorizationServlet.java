package com.google.sps.servlets;

import java.io.IOException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.File;  // Import the File class
import java.io.FileNotFoundException;  // Import this class to handle errors
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.InputStream;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.simple.JSONObject;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;


@WebServlet("/authorize")
public class AuthorizationServlet extends HttpServlet {

  // For RedirectURL, change the 3rd line in secret.txt
  // Use if running in production:
        // http://im-step.appspot.com/authorize

  // Use if running locally on port 8080:
        // https://8080-4271c707-a3a3-471c-bf8f-7ccb224d6188.us-central1.cloudshell.dev/authorize


  private String GOOGLE_CLIENT_ID = "";
  private String GOOGLE_CLIENT_SECRET = "";
  private String GOOGLE_REDIRECT_URL = "";
  private HTTP http = new HTTP();

  public void init() {
    ArrayList<String> secret = new ArrayList<String>();

    try {
      InputStream in = AuthorizationServlet.class.getResourceAsStream("/secret.txt");
      BufferedReader reader = new BufferedReader(new InputStreamReader(in));

      while(reader.ready()) secret.add(reader.readLine());

      GOOGLE_CLIENT_ID = secret.get(0);
      GOOGLE_CLIENT_SECRET = secret.get(1);
      GOOGLE_REDIRECT_URL = secret.get(2);
      
    } catch (Exception e) {
      System.out.println("The secret file could not be found on your system! Please check path!");
      e.printStackTrace();
    }
  }

  /**
   * Handles the HTTP <code>GET</code> method.
   *
   * @param request servlet request
   * @param response servlet response
   * @throws ServletException if a servlet-specific error occurs
   * @throws IOException if an I/O error occurs
   */
  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
    processRequest(request, response);
  }


  /**
   * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
   *
   * @param request servlet request
   * @param response servlet response
   * @throws ServletException if a servlet-specific error occurs
   * @throws IOException if an I/O error occurs
   */
  protected void processRequest(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

    // If the user denied access, we get back an error, such as...
    // error=access_denied&state=session%3Dpotatoes

    if (req.getParameter("error") != null) {
      System.out.println("We have received the error!");
      resp.setContentType("text/html");
      resp.getWriter().println("Something went wrong when we tried to get permission, please retry!");
      return;
    } else {

      // Google returns a code that can be exchanged for an access token
      String code = req.getParameter("code");

      if (code == null) {
        resp.setContentType("text/html");
        resp.getWriter().println("<h1>The app must get permission from the '/request-permission' Servlet before making this request!</h1>");
        return;
      }

      HashMap<String, String> params = new HashMap<String, String>();

      params.put("code", code);
      params.put("client_id", GOOGLE_CLIENT_ID);
      params.put("client_secret", GOOGLE_CLIENT_SECRET);
      params.put("redirect_uri", GOOGLE_REDIRECT_URL);
      params.put("grant_type", "authorization_code");

      // Grab the access token by post to Google
      String body = http.post("https://accounts.google.com/o/oauth2/token", params);

   // ex. returns
   //   {
   //       "access_token": "ya29.AHES6ZQS-BsKiPxdU_iKChTsaGCYZGcuqhm_A5bef8ksNoU",
   //       "token_type": "Bearer",
   //       "expires_in": 3600,
   //       "id_token": "eyJhbGciOiJSUzI1NiIsImtpZCI6IjA5ZmE5NmFjZWNkOGQyZWRjZmFiMjk0NDRhOTgyN2UwZmFiODlhYTYifQ.eyJpc3MiOiJhY2NvdW50cy5nb29nbGUuY29tIiwiZW1haWxfdmVyaWZpZWQiOiJ0cnVlIiwiZW1haWwiOiJhbmRyZXcucmFwcEBnbWFpbC5jb20iLCJhdWQiOiI1MDgxNzA4MjE1MDIuYXBwcy5nb29nbGV1c2VyY29udGVudC5jb20iLCJhdF9oYXNoIjoieUpVTFp3UjVDX2ZmWmozWkNublJvZyIsInN1YiI6IjExODM4NTYyMDEzNDczMjQzMTYzOSIsImF6cCI6IjUwODE3MDgyMTUwMi5hcHBzLmdvb2dsZXVzZXJjb250ZW50LmNvbSIsImlhdCI6MTM4Mjc0MjAzNSwiZXhwIjoxMzgyNzQ1OTM1fQ.Va3kePMh1FlhT1QBdLGgjuaiI3pM9xv9zWGMA9cbbzdr6Tkdy9E-8kHqrFg7cRiQkKt4OKp3M9H60Acw_H15sV6MiOah4vhJcxt0l4-08-A84inI4rsnFn5hp8b-dJKVyxw1Dj1tocgwnYI03czUV3cVqt9wptG34vTEcV3dsU8",
   //       "refresh_token": "1/Hc1oTSLuw7NMc3qSQMTNqN6MlmgVafc78IZaGhwYS-o"
   //   }

      // Get access token from JSON and request info from Google
      JSONObject jsonObject = http.parseJSON(body);

      // Google tokens expire after an hour, but since we requested offline access we can get a new token without user involvement via the refresh token
      String accessToken = (String) jsonObject.get("access_token");

      // Store the access token in session. Now we have this access token across our servlets for about 30 minutes!
      req.getSession().setAttribute("access_token", accessToken);

      resp.sendRedirect("/schedule-handler"); 
    }
  }
}
