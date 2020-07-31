package com.google.sps.servlets;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import java.io.IOException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.apache.http.entity.StringEntity;

import org.json.simple.JSONObject;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class HTTP {
  private static Boolean accessTokenReceived = false;

  // makes a GET request to url and returns body as a string
  public String get(String url) throws ClientProtocolException, IOException {
    return execute(new HttpGet(url));
  }

  // makes a DELETE request to url and returns body as a string
  public String delete(String url) throws ClientProtocolException, IOException {
    //return execute(new HttpDelete(url));
    HttpClient httpClient = new DefaultHttpClient();
    HttpDelete delete = new HttpDelete(url);
    HttpResponse response = httpClient.execute(delete);

    if (response.getStatusLine().getStatusCode() != 200 && response.getStatusLine().getStatusCode() != 204) {
      throw new RuntimeException("Failed to perform delete request with error code: " + response.getStatusLine().getStatusCode());
    }

    return "";
    
  }

  // makes a POST request to url with form parameters and returns body as a string
  public String post(String url, Map<String, String> formParameters) throws ClientProtocolException, IOException {
    HttpPost request = new HttpPost(url);

    List<NameValuePair> nvps = new ArrayList<NameValuePair>();

    for (String key : formParameters.keySet()) {
      nvps.add(new BasicNameValuePair(key, formParameters.get(key)));
    }

    request.setEntity(new UrlEncodedFormEntity(nvps));

    return execute(request);
  }

  public JSONObject postWithData(DefaultHttpClient httpClient, HttpPost postRequest, String json) {
    HttpResponse response = null;
    try {
      StringEntity input = new StringEntity(json);
      input.setContentType("application/json");
      postRequest.setEntity(input);

      response = httpClient.execute(postRequest);

      if (response.getStatusLine().getStatusCode() != 200) {
        HttpEntity entity = response.getEntity();
        String body = EntityUtils.toString(entity);

        throw new RuntimeException("Expected 200 but got " + response.getStatusLine().getStatusCode() + ", with body " + body);
      }
    } catch (Exception e) {
      System.out.println("Something went wrong!" + e);
    }

    return responseToJSON(response);
  }

  public JSONObject patchWithData(DefaultHttpClient httpClient, HttpPatch patchRequest, String json) {
    HttpResponse response = null;
    try {
      StringEntity input = new StringEntity(json);
      input.setContentType("application/json");
      patchRequest.setEntity(input);

      response = httpClient.execute(patchRequest);

      if (response.getStatusLine().getStatusCode() != 200) {
        HttpEntity entity = response.getEntity();
        String body = EntityUtils.toString(entity);

        throw new RuntimeException("Expected 200 but got " + response.getStatusLine().getStatusCode() + ", with body " + body);
      }
    } catch (Exception e) {
      System.out.println("Something went wrong!" + e);
    }

    return responseToJSON(response);
  }

  // makes request and checks response code for 200
  public String execute(HttpRequestBase request) throws ClientProtocolException, IOException {
    HttpClient httpClient = new DefaultHttpClient();
    HttpResponse response = httpClient.execute(request);

    HttpEntity entity = response.getEntity();
    String body = EntityUtils.toString(entity);

    if (response.getStatusLine().getStatusCode() != 200) {
      throw new RuntimeException("Expected 200 but got " + response.getStatusLine().getStatusCode() + ", with body " + body);
    }

    // Quick check to see if we get the code back. After our first request we want to be able to SEND JSON request!
    JSONObject jsonObject = parseJSON(body);
    String accessToken = (String) jsonObject.get("access_token");
    if (accessToken != null) accessTokenReceived = true;
    

    return body;
  }


  // ======================== // Utility functions // =============================== //

  // Turns string into a JSON Object that we can interact with
  public JSONObject parseJSON(String json) {
    JSONObject jsonObject = null;

    try {
      jsonObject = (JSONObject) new JSONParser().parse(json);
    } catch (ParseException e) {
      throw new RuntimeException("Unable to parse json because of: " + e);
    }

    return jsonObject;

  }

  public Boolean isAccessTokenValid(String accessToken) {
    try {
      String json = get("https://www.googleapis.com/oauth2/v1/tokeninfo?access_token=" + accessToken);
      JSONObject jsonObject = parseJSON(json);
      if (jsonObject.get("error") == "invalid_token") return false;
      return true;
    } catch (Exception e) {
      System.out.println("There was an error checking the access token.");
      return false;
    }
  }



  // TODO: You could just add this to postWithData, see if you need this with anything else
  // Turn response that we get from server into JSON. So that we can get data back.
  public JSONObject responseToJSON(HttpResponse response) {
    HttpEntity entity = response.getEntity();
    String body = "";
    try {
      body = EntityUtils.toString(entity);
    } catch (Exception e) {
      System.out.println("There was an error with trying to turn the response back to JSON! " + e);
    }

    return parseJSON(body);
  }
}