/**
 * Sample Java code for youtube.search.list
 * See instructions for running these code samples locally:
 * https://developers.google.com/explorer-help/guides/code_samples#java
 */

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
import java.lang.Integer;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Collections;
import java.util.stream.Collectors;
import com.google.gson.*;

@WebServlet("/videoResults")
public class YouTubeServlet extends HttpServlet{
    // Need Dev-Key to make an authorized search.
    // For example: ... DEVELOPER_KEY = "YOUR ACTUAL KEY";
    private static int NUM_VIDEOS = 0;
    private static String DEVELOPER_KEY = "";
    private static final String APPLICATION_NAME = "First Time Coders";
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private ArrayList<String> links = new ArrayList<String>();

    public void init() {
      try {
        InputStream in = YouTubeServlet.class.getResourceAsStream("/yt-key.txt");
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        while (reader.ready()) {
          DEVELOPER_KEY = reader.readLine();
        }
        reader.close();
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
        YouTube youtubeService = getService();
        long results = NUM_VIDEOS;
        // Use for creating links
        String embedTemplateLink = "https://www.youtube.com/embed/";
        String directTemplateLink = "https://www.youtube.com/watch?v=";
        String embedUrl = "";
        String directUrl = "";
        String previousSearchTerm = "";
        String currentSearchTerm = (String) request.getSession(false).getAttribute("searchKeyword");
        ResourceId videoId = new ResourceId();
        SearchResultSnippet videoInfoSnippet = new SearchResultSnippet();
        String videoTitle = "";

        // Check if the search term has programming in it (to filter out unrelated things)
        currentSearchTerm = currentSearchTerm.toLowerCase();
        int indexOf = -1;
        String [] terms = currentSearchTerm.split(" ");
        for (String term : terms) {
          if (term.equals("programming")) { 
            indexOf = 1;
          }
        }
        if (indexOf == -1) {
          currentSearchTerm = currentSearchTerm + " programming";
        }
        System.out.println(currentSearchTerm);
        // Start Datastore Service
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        // Define and execute the API request
        YouTube.Search.List api_request = youtubeService.search().list("snippet");
        
        if (!previousSearchTerm.equals(currentSearchTerm)) {
          System.out.println("Do not need to re-query.");
          links.clear();
        }
        if (links.size() == 0) { // Basically, will prevent duplicating the video links
          System.out.println("Quering videos");
          SearchListResponse api_response = api_request.setKey(DEVELOPER_KEY)
            .setQ(currentSearchTerm) // Q term (Search Term)
            .setOrder("relevance") // Relevant to Q term
            .setMaxResults(results) // Number of Videos
            .setType("video") // Specify we want videos, fixes issue of grabbing playlists
            .setVideoDuration("medium") // Specify videos between 4 min and 20 mins
            .execute();
          for (int i=0; i < (int) results; i++) {
            // Retrieve the video's id
            videoId = (ResourceId)  api_response.getItems().get(i).get("id");
            // Create direct URL to video by appending the templated link and videoID
            embedUrl = embedTemplateLink + videoId.getVideoId();
            directUrl = directTemplateLink + videoId.getVideoId();
            // Gather the video's Title
            videoInfoSnippet = (SearchResultSnippet) api_response.getItems().get(i).get("snippet");       
            videoTitle = videoInfoSnippet.getTitle();

            links.add(embedUrl);
            links.add(directUrl);

            /* ---------- DATASTORE STORAGE ------------ */

            // Prepare a new Entity
            Entity video = new Entity("Video");
            boolean inDatastore = false;
            // Set title and url to properties
            video.setProperty("Title", videoTitle);
            video.setProperty("URL", directUrl);
            video.setProperty("Search Term", currentSearchTerm);
            // Put the entity into datastore
            Query query = new Query("Video");
            PreparedQuery queryResults = datastore.prepare(query);
            if (queryResults.asIterable().iterator().hasNext()) {
              for (Entity entity : queryResults.asIterable()) {
                if ((String) entity.getProperty("URL") == directUrl) {
                  inDatastore = true;
                }
              }
            }
            if (!inDatastore) {
              datastore.put(video);
            }
          }
        }
        // Keep track of if the search term changes
        previousSearchTerm = currentSearchTerm;
      }
      catch (GeneralSecurityException e) {
        response.setContentType("text/html;");
        response.getWriter().println("Task Failed: Error Occurred");
        response.setContentType("text/html;");
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
    public void doPost(HttpServletRequest request, HttpServletResponse response) {
      /* TODO: Create a POST request to accept Payton's Servlet */
      System.out.println("POST Request: YouTubeServlet");
      // What are we accepting from Payton's Servlet
      // - Number of videos
      String info = "";
      try {
        info = request.getReader().lines().collect(Collectors.joining(System.lineSeparator()));
        NUM_VIDEOS = Integer.parseInt(info);

        response.getWriter().println(true);
      } catch (Exception e) {
        System.out.println("Issue Found: " + e);
      }
    }

    private String convertToJson(ArrayList<String> array) {
      Gson gson = new Gson();
      String json = gson.toJson(array);
      return json;
    }
}