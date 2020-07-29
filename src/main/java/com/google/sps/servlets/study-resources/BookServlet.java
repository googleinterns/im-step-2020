import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.api.services.books.Books;
import com.google.api.services.books.BooksRequestInitializer;
import com.google.api.services.books.Books.Volumes.List;
import com.google.api.services.books.model.Volume;
import com.google.api.services.books.model.Volumes;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.InputStream;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.Objects;
import com.google.gson.Gson;

import java.io.IOException;
import java.security.GeneralSecurityException;

@WebServlet("/bookResults")
public class BookServlet extends HttpServlet{

  private static String DEVELOPER_KEY = "";
  private static final String APPLICATION_NAME = "First Time Coders";
  private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
  private ArrayList<String> links = new ArrayList<String>();

  public void init() {
    try {
        InputStream in = YouTubeServlet.class.getResourceAsStream("/books-key.txt");
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        while (reader.ready()) {
          DEVELOPER_KEY = reader.readLine();
        }
        reader.close();
      System.out.println("Book Servlet Init completed");
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
  public static Books getService() throws GeneralSecurityException, IOException {
        final NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        return new Books.Builder(httpTransport, JSON_FACTORY, null)
            .setApplicationName(APPLICATION_NAME)
            .build();
  }

  public static String makeURL(String title, String id) {
    // https://www.google.com/books/edition/[TITLE]/[ID]
    return "https://www.google.com/books/edition/" + title + "/" + id;
    
  }

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    try {

      Books bookService = getService();
      long results = 25;
      String title = "";
      String id = "";
      String url = "";
      
      // Build query from Books API using volumes.list()
      List api_request = bookService.volumes().list("items")
      .setKey(DEVELOPER_KEY)
      .setQ("Python Programming")
      .setOrderBy("relevance")
      .setPrintType("BOOKS")
      .setMaxResults(results);
      // Execute query
      Volumes api_response = api_request.execute();

      // Retrieve title,id and build URL
      if (links.size() == 0) {
        for (int i = 0; i < (int) results; i++) {
          title = api_response.getItems().get(i).getVolumeInfo().getTitle();
          id = api_response.getItems().get(i).getId();
          url = makeURL(title, id);
          links.add(url);
        }
      }
      String json = convertToJson(links);

      response.setContentType("text/html;");
      response.getWriter().println(json);
    }
    catch (GeneralSecurityException e) {
      response.setContentType("text/html;");
      response.getWriter().println("Task Failed: Error Occurred");
      response.setContentType("text/html;");
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