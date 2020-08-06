package com.google.sps.servlets;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;

import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.ResourceId;
import com.google.api.services.youtube.model.SearchListResponse;
import com.google.api.services.youtube.model.SearchResultSnippet;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.SortDirection;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.InputStream;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.lang.*;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Collections;
import java.util.stream.Collectors;
import com.google.gson.*;

@WebServlet("/videoQuery")
public class YouTubeServlet extends HttpServlet{
    // Need Dev-Key to make an authorized search.
    // For example: ... DEVELOPER_KEY = "YOUR ACTUAL KEY";
    private static boolean needNextPageToken = false;

    private static String previousPageToken = "";
    private static String previousSearchTerm = "";
    private static String currentSearchTerm = "";
    private static String nextPageToken = "";

    private static int numVideos = 0;

    
    private static String DEVELOPER_KEY = "";

    private ArrayList<String> links = new ArrayList<String>();

    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static final String APPLICATION_NAME = "First Time Coders";
    private static final String EMBED_TEMP_LINK = "https://www.youtube.com/embed/";
    private static final String DIRECT_TEMP_LINK = "https://www.youtube.com/watch?v=";

    

    public void init() {
      try {
        InputStream in = YouTubeServlet.class.getResourceAsStream("/yt-key.txt");
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        while (reader.ready()) {
          DEVELOPER_KEY = reader.readLine();
        }
        reader.close();
        numVideos = 1;
        System.out.println("YouTube Servlet Init completed");
      } catch (Exception e) {
        System.out.println("Error: " + e);
      }
    }
    /**
     * Build and return an authorized API client service.
     *
     * @return an authorized API client service
     * @throws GeneralSecurityException, IOException
     */
    public static YouTube getService() throws GeneralSecurityException, IOException {
        final NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        return new YouTube.Builder(httpTransport, JSON_FACTORY, null)
            .setApplicationName(APPLICATION_NAME)
            .build();
    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
      System.out.println("GET Request: YouTubeServlet");     
      try {
        /* ----------- VARIABLES ----------- */
        long results = 0;
        String embedUrl = "";
        String directUrl = "";
        String videoTitle = "";
        currentSearchTerm = (String) request.getSession(false).getAttribute("searchKeyword");
        // Use for gathering video metadata
        YouTube youtubeService = getService();
        ResourceId videoId = new ResourceId();
        SearchResultSnippet videoInfoSnippet = new SearchResultSnippet();
        
        
        // Safety Net: If we receive zero for number of videos
        // from POST request, default to 1.
        if (numVideos == 0) {
          results = 1;
        } else {
          results = numVideos;
        }

        // Check if the search term has programming in it (to filter out unrelated things)
        System.out.println(currentSearchTerm);
        currentSearchTerm = currentSearchTerm.toLowerCase();
        boolean termFound = false;
        String [] terms = currentSearchTerm.split(" ");
        for (String term : terms) {
          if (term.equals("programming")) { 
            termFound = true;
          }
        }
        if (!termFound) {
          currentSearchTerm = currentSearchTerm + " programming";
        }

        /* CHECKPOINT */
        System.out.println("Previous Page Token: " + previousPageToken);
        System.out.println("Current Page Token: " + nextPageToken);
        System.out.println("Previous Search: " + previousSearchTerm);
        System.out.println("Current Search: " + currentSearchTerm);
<<<<<<< HEAD
        // Start Datastore Service
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        // Define and execute the API request
        YouTube.Search.List api_request = youtubeService.search().list("snippet");
    
=======
        System.out.println("Number of Videos: " + results);

>>>>>>> 23c8982f281dbb8c76ebef90a87103b0ad7252a5
        // Check if we need to requery the same term
        /* Reasons to requery:
            - Term changed => currentPageToken is set to default
            - Additional content wanted: needNextPageToken is true
        */
        if (currentSearchTerm.equals(previousSearchTerm) && !needNextPageToken) {
          System.out.println("Do not query again: YouTube Servlet");
        }
        else {
          System.out.println("Query again: YouTube Servlet");
          links.clear();
          if (!needNextPageToken) { // If nextPageToken not needed, reset both the previous and next
            System.out.println("Reset tokens");
            previousPageToken = "";
            nextPageToken = "";
          }
        }

        // Define and execute the API request
        YouTube.Search.List api_request = youtubeService.search().list("snippet");
        // Check if the next page token is needed
        if (needNextPageToken) {
          // Run an empty query: no videos that soul purpose is to get the next page token
          SearchListResponse empty_query = api_request.setKey(DEVELOPER_KEY)
            .setQ(currentSearchTerm) // Q term (Search Term)
            .setOrder("relevance") // Relevant to Q term
            .setMaxResults(results) // Number of Videos
            .setType("video") // Specify we want videos, fixes issue of grabbing playlists
            .setVideoDuration("medium") // Specify videos between 4 min and 20 mins
            .setPageToken(previousPageToken)
            .execute();

          nextPageToken = empty_query.getNextPageToken();
        }
        System.out.println("Next Page Token: " + nextPageToken);


        if (links.size() == 0) { // Basically, will prevent duplicating the video links
          SearchListResponse api_response = api_request.setKey(DEVELOPER_KEY)
            .setQ(currentSearchTerm) // Q term (Search Term)
            .setOrder("relevance") // Relevant to Q term
            .setMaxResults(results) // Number of Videos
            .setType("video") // Specify we want videos, fixes issue of grabbing playlists
            .setVideoDuration("medium") // Specify videos between 4 min and 20 mins
            .setPageToken(nextPageToken)
            .execute();
          for (int i=0; i < (int) results; i++) {
            // Retrieve the video's id
            videoId = (ResourceId)  api_response.getItems().get(i).get("id");
            // Create direct URL to video by appending the templated link and videoID
            embedUrl = EMBED_TEMP_LINK + videoId.getVideoId();
            directUrl = DIRECT_TEMP_LINK + videoId.getVideoId();
            // Gather the video's Title
            videoInfoSnippet = (SearchResultSnippet) api_response.getItems().get(i).get("snippet");       
            videoTitle = videoInfoSnippet.getTitle();

            links.add(embedUrl);
            links.add(directUrl);
          }
        }
      }
      catch (GeneralSecurityException e) {
        response.setContentType("text/html;");
        response.getWriter().println("Task Failed: Error Occurred");
        response.getWriter().println();
        response.getWriter().println("Location: YouTubeServlet doGet()");
        response.getWriter().println();
        response.getWriter().println(e);
      }
      // Convert to JSON
      String json = convertToJson(links);
      
      // Write JSON to page
      response.setContentType("text/html");
      response.getWriter().println(json);
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
      /* TODO: Create a POST request to accept Payton's Servlet */
      System.out.println("POST Request: YouTubeServlet");
      // What are we accepting from Payton's Servlet
      // - Number of videos
      String info = "";
      try {
        info = request.getReader().lines().collect(Collectors.joining(System.lineSeparator()));
        info = info.substring(1,info.length()-1);
        System.out.println(info);
        String [] arrayInfo = info.split(",");
        previousSearchTerm = currentSearchTerm;
        numVideos = Integer.parseInt(arrayInfo[0]);
        needNextPageToken = Boolean.parseBoolean(arrayInfo[1]);
        response.setContentType("text/html");
        response.getWriter().println(true);
      } catch (Exception e) {
        response.getWriter().println(false);
        response.setContentType("text/html;");
        response.getWriter().println("Task Failed: Error Occurred");
        response.getWriter().println();
        response.getWriter().println("Location: YouTubeServlet doPost()");
        response.getWriter().println();
        response.getWriter().println(e);
      }
    }

    private String convertToJson(ArrayList<String> array) {
      Gson gson = new Gson();
      String json = gson.toJson(array);
      return json;
    }
}