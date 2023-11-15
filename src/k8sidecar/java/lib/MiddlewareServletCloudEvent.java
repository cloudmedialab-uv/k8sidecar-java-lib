package k8sidecar.java.lib;

import io.cloudevents.CloudEvent;
import io.cloudevents.core.message.MessageReader;
import io.cloudevents.http.HttpMessageFactory;
import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * MiddlewareServletCloudEvent processes incoming HTTP requests containing
 * CloudEvent information and forwards them to the appropriate user-provided function.
 */
@WebServlet("/*")
public class MiddlewareServletCloudEvent extends HttpServlet {

  private SidecarFilter middleware;

  /**
   * Constructs a new MiddlewareServletCloudEvent instance.
   *
   * @param middleware the associated sidecar filter
   */
  public MiddlewareServletCloudEvent(SidecarFilter middleware) {
    this.middleware = middleware;
  }

  @Override
  protected void service(HttpServletRequest req, HttpServletResponse res)
    throws ServletException, IOException {
    CloudEvent cloudEvent = getCloutEvent(req, res);

    // Pass the request, response, and CloudEvent to the middleware for processing
    middleware.handleRequest(req, res, cloudEvent);
  }

  private CloudEvent getCloutEvent(
    HttpServletRequest req,
    HttpServletResponse res
  ) throws IOException {
    // Extract HTTP request headers into a Map
    Map<String, String> headers = new HashMap<>();
    Enumeration<String> headerNames = req.getHeaderNames();
    while (headerNames.hasMoreElements()) {
      String headerName = headerNames.nextElement();
      headers.put(headerName, req.getHeader(headerName));
    }

    // Extract HTTP request body into a byte array
    byte[] body = req.getInputStream().readAllBytes();

    // Convert the headers and body into a CloudEvent object
    MessageReader messageReader = HttpMessageFactory.createReader(
      headers,
      body
    );
    return messageReader.toEvent();
  }
}
