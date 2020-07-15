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

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.InputStream;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Objects;
import com.google.gson.Gson;

@WebServlet("/videoResults")
public class YouTubeServlet extends HttpServlet{
    // Need Dev-Key to make an authorized search.
    // For example: ... DEVELOPER_KEY = "YOUR ACTUAL KEY";

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
      try {
        YouTube youtubeService = getService();
        long results = 5;
        // Use for creating links
        String embedTemplateLink = "https://www.youtube.com/embed/";
        String directTemplateLink = "https://www.youtube.com/watch?v=";
        String embedUrl = "";
        String directUrl = "";
        ResourceId videoId = new ResourceId();
        /* IF NEEDED: Use for video title
        SearchResultSnippet videoInfoSnippet = new SearchResultSnippet();
        String videoTitle = "";*/
            
        // Define and execute the API request
        YouTube.Search.List api_request = youtubeService.search().list("snippet");
        SearchListResponse api_response = api_request.setKey(DEVELOPER_KEY)
          .setQ("python programming beginner") // Q term (Search Term)
          .setOrder("relevance") // Relevant to Q term
          .setMaxResults(results) // Number of Videos
          .setType("video") // Specify we want videos, fixes issue of grabbing playlists
          .execute();
        
        if (links.size() == 0) { // Basically, will prevent duplicating the video links
          for (int i=0; i < (int) results; i++) {
            // Retrieve the video's id
            videoId = (ResourceId)  api_response.getItems().get(i).get("id");
            // Create direct URL to video by appending the templated link and videoID
            embedUrl = embedTemplateLink + videoId.getVideoId();
            directUrl = directTemplateLink + videoId.getVideoId();
            /* IF NEEDED: Pull the Video Title
            videoInfoSnippet = (SearchResultSnippet) api_response.getItems().get(i).get("snippet");       
            videoTitle = videoInfoSnippet.getTitle();*/
            links.add(embedUrl);
            links.add(directUrl);
          }
        }
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

    private String convertToJson(ArrayList<String> array) {
      Gson gson = new Gson();
      String json = gson.toJson(array);
      return json;
    }
}