package com.google.sps.servlets;

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

import org.json.simple.JSONObject;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

@WebServlet("/bookQuery")
public class BookServlet extends HttpServlet{

  private static String DEVELOPER_KEY = "";
  private static final String APPLICATION_NAME = "First Time Coders";
  private static String previousSearchTerm = "";
  private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
  private JSONArray links = new JSONArray();

  public void init() {
    try {
        InputStream in = BookServlet.class.getResourceAsStream("/books-key.txt");
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

  public static String checkForBackSlash(String title) {
    int index = title.indexOf("\\");
    if (index != -1) {
      String before = title.substring(0, index);
      String after = title.substring(index+1);
      return before + after;
    }
    else {
      return title;
    }
  }

  public static String makeURL(String title, String id) {
    // https://www.google.com/books/edition/[TITLE]/[ID]
    String bookTitle = title.replaceAll(" ", "_");
    if (bookTitle.contains("/")) {
      bookTitle = bookTitle.replaceAll("/","_");
    }
    return "https://www.google.com/books/edition/" + bookTitle + "/" + id;
  }

  public static String makeFullTitle(String title, String subtitle) {
    if (subtitle != null && !subtitle.contains("\\")) {
      return title + " " + subtitle;
    }
    else {
      return title;
    }
  }

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, GoogleJsonResponseException {
    try {
      Books bookService = getService();
      long results = 25;
      String currentSearchTerm = (String) request.getSession(false).getAttribute("searchKeyword");
      currentSearchTerm = currentSearchTerm.toLowerCase();
      String title = "";
      String subtitle = "";
      String fullTitle = "";
      String id = "";
      String url = "";
      
      // Build query from Books API using volumes.list()
      if (currentSearchTerm.equals(previousSearchTerm)) {
        System.out.println("Do not query again: Books Servlet");
      } else {
        System.out.println("Query again: Books Servlet");
        links = new JSONArray();
      }
  
      // Retrieve title,id and build URL
      if (links.size() == 0) {
        Volumes api_response = bookService.volumes().list("items")
        .setKey(DEVELOPER_KEY)
        .setQ(currentSearchTerm)
        .setOrderBy("relevance")
        .setPrintType("BOOKS")
        .setMaxResults(results)
        .set("country", "US")
        .execute();
        // Execute query
        for (int i = 0; i < (int) results; i++) {
          title = api_response.getItems().get(i).getVolumeInfo().getTitle();
          subtitle = api_response.getItems().get(i).getVolumeInfo().getSubtitle();
          title = checkForBackSlash(title);
          fullTitle = makeFullTitle(title, subtitle);
          id = api_response.getItems().get(i).getId();
          url = makeURL(title, id);
          JSONObject json = new JSONObject();
          json.put("Title", fullTitle);
          json.put("URL", url);
          links.add(json);
        }
        previousSearchTerm = currentSearchTerm;
      }
     

      response.setContentType("text/html;");
      response.getWriter().println(links);
    }
    catch (GeneralSecurityException e) {
      response.setContentType("text/html;");
      response.getWriter().println("Task Failed: Error Occurred");
      response.setContentType("text/html;");
      response.getWriter().println();
      response.getWriter().println(e);
    }
  }
}