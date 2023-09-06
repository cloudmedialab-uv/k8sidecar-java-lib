package sidecar.java.lib;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Enumeration;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Represents a chain that can forward the request to a subsequent step.
 */
public class FilterChain {

  private HttpServletRequest req;
  private HttpServletResponse res;

  /**
   * Constructs a new FilterChain instance.
   *
   * @param req the incoming request
   * @param res the outgoing response
   */
  public FilterChain(HttpServletRequest req, HttpServletResponse res) {
    this.req = req;
    this.res = res;
  }

  /**
   * Forwards the request to the next service in the chain.
   */
  public void next() {
    try {
      // Get the port from environment variable and increment by 1
      int port = Integer.parseInt(System.getenv("PPORT")) + 1;

      System.out.println(req.getServerName() + " port: " + port + " uri: " + req.getRequestURI());

      URL url = new URL("http", "127.0.0.1", port, req.getRequestURI());
      HttpURLConnection conn = (HttpURLConnection) url.openConnection();
      conn.setRequestMethod(req.getMethod());

      // Copy headers from the original request
      Enumeration<String> headerNames = req.getHeaderNames();
      while (headerNames.hasMoreElements()) {
        String headerName = headerNames.nextElement();
        conn.setRequestProperty(headerName, req.getHeader(headerName));
      }

      // Copy request parameters and encode them
      StringBuilder postData = new StringBuilder();
      for (
        Enumeration<String> e = req.getParameterNames();
        e.hasMoreElements();
      ) {
        if (postData.length() != 0) postData.append('&');
        String param = e.nextElement();
        postData.append(URLEncoder.encode(param, "UTF-8"));
        postData.append('=');
        postData.append(URLEncoder.encode(req.getParameter(param), "UTF-8"));
      }
      byte[] postDataBytes = postData.toString().getBytes("UTF-8");

      // Send the request data
      conn.setDoOutput(true);
      try (OutputStream os = conn.getOutputStream()) {
        os.write(postDataBytes);
      }

      conn.connect();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  // Getters and Setters

  public HttpServletRequest getReq() {
    return req;
  }

  public void setReq(HttpServletRequest req) {
    this.req = req;
  }

  public HttpServletResponse getRes() {
    return res;
  }

  public void setRes(HttpServletResponse res) {
    this.res = res;
  }
}
