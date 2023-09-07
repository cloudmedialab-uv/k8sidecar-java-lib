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
      // Parse the port number from environment variables and increment by one
      int port = Integer.parseInt(System.getenv("PPORT")) + 1;

      // Create a new URL targeting the localhost and the calculated port
      URL url = new URL("http", "127.0.0.1", port, req.getRequestURI());
      log.info(
        "Making request to URL: {} with method: {}",
        url,
        req.getMethod()
      );

      // Open a connection to the created URL
      conn = (HttpURLConnection) url.openConnection();
      conn.setRequestMethod(req.getMethod());
      conn.setConnectTimeout(5000);
      conn.setReadTimeout(5000);

      // Copy all headers from the original request to the new connection
      Enumeration<String> headerNames = req.getHeaderNames();
      while (headerNames.hasMoreElements()) {
        String headerName = headerNames.nextElement();
        conn.setRequestProperty(headerName, req.getHeader(headerName));
      }

      // If the request method is POST or PUT, copy the request body
      if ("POST".equals(req.getMethod()) || "PUT".equals(req.getMethod())) {
        conn.setDoOutput(true);

        // Directly copy the body, regardless of content type
        try (
          InputStream originalInput = req.getInputStream();
          OutputStream newOutput = conn.getOutputStream()
        ) {
          byte[] buffer = new byte[1024];
          int bytesRead;

          while ((bytesRead = originalInput.read(buffer)) != -1) {
            newOutput.write(buffer, 0, bytesRead);
          }
        } catch (Exception ex) {
          log.error(
            "Failed to copy the request body. Error Message: {}",
            ex.toString().trim()
          );
        }
      }

      // Retrieve and handle the response from the connection
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
        // Handle any errors in the response
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
      // Ensure connection is closed
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
