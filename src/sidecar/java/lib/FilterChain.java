package sidecar.java.lib;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Enumeration;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents a chain that can forward the request to a subsequent step.
 */
public class FilterChain {

  private HttpServletRequest req;
  private HttpServletResponse res;

  private static Logger log = LoggerFactory.getLogger(FilterChain.class);

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
    HttpURLConnection conn = null;

    try {
      int port = Integer.parseInt(System.getenv("PPORT")) + 1;

      URL url = new URL("http", "127.0.0.1", port, req.getRequestURI());
      log.info(
        "Making request to URL: {} with method: {}",
        url,
        req.getMethod()
      );

      conn = (HttpURLConnection) url.openConnection();
      conn.setRequestMethod(req.getMethod());
      conn.setConnectTimeout(5000);
      conn.setReadTimeout(5000);

      Enumeration<String> headerNames = req.getHeaderNames();
      while (headerNames.hasMoreElements()) {
        String headerName = headerNames.nextElement();
        conn.setRequestProperty(headerName, req.getHeader(headerName));
      }

      if ("POST".equals(req.getMethod()) || "PUT".equals(req.getMethod())) {
        StringBuilder postData = new StringBuilder();
        Enumeration<String> paramNames = req.getParameterNames();
        while (paramNames.hasMoreElements()) {
          if (postData.length() > 0) postData.append('&');
          String param = paramNames.nextElement();
          postData.append(URLEncoder.encode(param, "UTF-8"));
          postData.append('=');
          postData.append(URLEncoder.encode(req.getParameter(param), "UTF-8"));
        }
        byte[] postDataBytes = postData.toString().getBytes("UTF-8");

        conn.setDoOutput(true);
        try (OutputStream os = conn.getOutputStream()) {
          os.write(postDataBytes);
        }
      }

      int responseCode = conn.getResponseCode();
      if (responseCode >= 200 && responseCode < 300) {
        try (InputStream is = conn.getInputStream()) {
          byte[] buffer = new byte[1024];
          int bytesRead;
          StringBuilder responseBody = new StringBuilder();
          while ((bytesRead = is.read(buffer)) != -1) {
            res.getOutputStream().write(buffer, 0, bytesRead);
            responseBody.append(new String(buffer, 0, bytesRead));
          }
          log.info(
            "Response Code: {}. Response Body: {}",
            responseCode,
            responseBody.toString().trim()
          );
        }
      } else {
        try (InputStream es = conn.getErrorStream()) {
          byte[] buffer = new byte[1024];
          int bytesRead;
          StringBuilder errorBody = new StringBuilder();
          while ((bytesRead = es.read(buffer)) != -1) {
            errorBody.append(new String(buffer, 0, bytesRead));
          }
          log.error(
            "Failed to redirect. Response Code: {}. Error Message: {}",
            responseCode,
            errorBody.toString().trim()
          );
        }
      }
    } catch (Exception e) {
      log.error("Exception while processing the request", e);
    } finally {
      if (conn != null) {
        conn.disconnect();
      }
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
