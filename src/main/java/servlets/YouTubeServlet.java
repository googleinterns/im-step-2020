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

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.Objects;
import com.google.gson.Gson;

@WebServlet("/videoResults")
public class YouTubeServlet extends HttpServlet{
    // Need Dev-Key to make an authorized search.
    // For example: ... DEVELOPER_KEY = "YOUR ACTUAL KEY";
    private static final String DEVELOPER_KEY = "AIzaSyD938GXrKFmO-z9VH49UJ7o04Z82iHJ8Rw";

    private static final String APPLICATION_NAME = "YouTube Data Retrieval";
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();

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
        long num = 1;
        String templateLink = "www.youtube.com/watch?v=";
        String directUrl = "";
            
        // Define and execute the API request
        YouTube.Search.List api_request = youtubeService.search().list("id");
        SearchListResponse api_response = api_request.setKey(DEVELOPER_KEY)
          .setOrder("relevance")
          .setMaxResults(num)
          .setQ("python programming")
          .execute();
        
        ResourceId attempt = new ResourceId();

        // Retrieve the video's id
        attempt = (ResourceId)  api_response.getItems().get(0).get("id");
        // Create direct URL to video by appending the templated link and videoID
        directUrl = templateLink + attempt.getVideoId();
        response.setContentType("text/html;");
        response.getWriter().println(directUrl);
      }
      catch (GeneralSecurityException e) {

      }

    }

    /*
    public static void main(String[] args)
        throws GeneralSecurityException, IOException, GoogleJsonResponseException {
        YouTube youtubeService = getService();
        long num = 1;
        
        // Define and execute the API request
        YouTube.Search.List request = youtubeService.search().list("id");
        SearchListResponse response = request.setKey(DEVELOPER_KEY)
            .setMaxResults(num)
            .setOrder("rating")
            .setQ("python programming")
            .execute();

        ResourceId attempt = new ResourceId();

        System.out.println(response.getItems().getClass());
        System.out.println();
        System.out.println(response.getItems().get(0).getClass());
        System.out.println(response.getItems().get(0).get("id"));
        System.out.println();
        System.out.println(response.getItems().get(0).get("id").getClass());
        attempt = (ResourceId)  response.getItems().get(0).get("id");
        System.out.println();
        System.out.println(attempt.getVideoId());
        // response.getItems().get(0).get("snippet");
    }
    */
}