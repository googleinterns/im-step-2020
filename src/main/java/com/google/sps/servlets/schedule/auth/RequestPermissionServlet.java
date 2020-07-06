package com.google.sps.servlets;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.File;  // Import the File class
import java.io.FileNotFoundException;  // Import this class to handle errors
import java.util.Scanner; // Import the Scanner class to read text files


@WebServlet("/request-permission")
public class RequestPermissionServlet extends HttpServlet {

    // For RedirectURL, change the 3rd line in secret.txt 
    // Use if running in production:
        // http://im-step.appspot.com/authorize

    // Use if running locally on port 8080:
        // https://8080-4271c707-a3a3-471c-bf8f-7ccb224d6188.us-central1.cloudshell.dev/authorize

    private static String GOOGLE_CLIENT_ID = "";
    private static String GOOGLE_REDIRECT_URL = "";

    // Get client ID
    public void init() {
        ArrayList<String> secret = new ArrayList<String>();

        try {
            InputStream in = RequestPermissionServlet.class.getResourceAsStream("/secret.txt");
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
      
            while(reader.ready()) {
                secret.add(reader.readLine());
            }
      
            GOOGLE_CLIENT_ID = secret.get(0);
            GOOGLE_REDIRECT_URL = secret.get(2);
            
          } catch (Exception e) {
            System.out.println("The secret file could not be found on your system! Please check path!");
            e.printStackTrace();
          }
    }


  /**
   * Handles the HTTP GET method.
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
  protected void processRequest(HttpServletRequest req, HttpServletResponse resp)
    throws ServletException, IOException {
        String scopes = "https://www.googleapis.com/auth/calendar";

        StringBuilder oauthUrl = new StringBuilder();
        oauthUrl.append("https://accounts.google.com/o/oauth2/v2/auth")
            .append("?scope=").append(scopes)  // Scope is the api permissions we are requesting
            .append("&access_type=offline")  // We can have access to the user's permission offline
            .append("&include_granted_scopes=true")
            .append("&response_type=code")  
            .append("&state=state_parameter_passthrough_value")
            .append("&redirect_uri=").append(GOOGLE_REDIRECT_URL) // This is the servlet that google redirects to after successfully authorization
            .append("&client_id=").append(GOOGLE_CLIENT_ID) // This is the client id from the api console registration
            .append("&approval_prompt=force"); // This requires them to verify which account to use, if they are already signed in
        
        try { 
            resp.sendRedirect(oauthUrl.toString());
        } catch (Exception e) {
            System.out.println("Failed to ask the user access to their permissions because of: " + e);
        }

    }


}