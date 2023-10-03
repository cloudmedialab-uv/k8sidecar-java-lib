package sidecar.java.lib;

import io.cloudevents.CloudEvent;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.io.IOUtils;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import sidecar.java.lib.logging.Logback;

/**
 * SidecarFilter serves as a middleware, intercepting incoming HTTP requests
 * and processing them before forwarding. Depending on the user-provided function,
 * the filter can process requests with or without CloudEvent information.
 */
public class SidecarFilter {

  // Represents a user-provided function that processes HTTP requests with CloudEvent information
  private QuaFunction<HttpServletRequest, HttpServletResponse, CloudEvent, FilterChain, Void> quaF;

  // Represents a user-provided function that processes regular HTTP requests
  private TriFunction<HttpServletRequest, HttpServletResponse, FilterChain, Void> triF;

  /**
   * Constructor for SidecarFilter when a CloudEvent function is provided.
   *
   * @param userFunction the user-provided function for CloudEvent processing
   */
  public SidecarFilter(
    QuaFunction<HttpServletRequest, HttpServletResponse, CloudEvent, FilterChain, Void> userFunction
  ) {
    this.quaF = userFunction;
    this.triF = null;
  }

  /**
   * Constructor for SidecarFilter when a regular HTTP function is provided.
   *
   * @param userFunction the user-provided function for regular HTTP processing
   */
  public SidecarFilter(
    TriFunction<HttpServletRequest, HttpServletResponse, FilterChain, Void> userFunction
  ) {
    this.triF = userFunction;
    this.quaF = null;
  }

  /**
   * Starts a Jetty server to listen for incoming requests on a specific port.
   */
  public void listen() {
    int port = Integer.parseInt(System.getenv("PPORT"));
    Server server = new Server(port);
    ServletContextHandler context = new ServletContextHandler();
    if (quaF == null) {
      context.addServlet(new ServletHolder(new MiddlewareServlet(this)), "/*");
    } else {
      context.addServlet(
        new ServletHolder(new MiddlewareServletCloudEvent(this)),
        "/*"
        );
        
    }    
      
    server.setHandler(context);
    Logback.setLevel("INFO");

    try {
      server.start();
      server.join();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * Handles an incoming HTTP request with CloudEvent information.
   *
   * @param req the incoming request
   * @param res the outgoing response
   * @param cloudEvent the associated CloudEvent
   */
  public void handleRequest(
    HttpServletRequest req,
    HttpServletResponse res,
    CloudEvent cloudEvent
  ) {
    try {
      Pair<HttpServletRequestWrapper, HttpServletRequestWrapper> wrappers = getRequestWrappers(
        req
      );
      // Initialize the filter chain with the copied request and the original response
      FilterChain chain = new FilterChain(wrappers.getRight(), res);
      quaF.apply(wrappers.getLeft(), res, cloudEvent, chain);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * Handles a regular incoming HTTP request.
   *
   * @param req the incoming request
   * @param res the outgoing response
   */
  public void handleRequest(HttpServletRequest req, HttpServletResponse res) {
    try {
      Pair<HttpServletRequestWrapper, HttpServletRequestWrapper> wrappers = getRequestWrappers(
        req
      );
      // Initialize the filter chain with the copied request and the original response
      FilterChain chain = new FilterChain(wrappers.getRight(), res);
      triF.apply(wrappers.getLeft(), res, chain);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * Extracts the body from the given request and creates original and copied wrappers.
   *
   * @param req the incoming request
   * @return a pair of HttpServletRequestWrappers: the first is the original, the second is the copied version
   */
  private Pair<HttpServletRequestWrapper, HttpServletRequestWrapper> getRequestWrappers(
    HttpServletRequest req
  ) throws IOException {
    // Read the body of the original request
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    IOUtils.copy(req.getInputStream(), baos);
    byte[] bodyBytes = baos.toByteArray();

    // Replace the original request's body with a version that can be read again
    HttpServletRequestWrapper originalRequestWrapper = new HttpServletRequestWrapper(
      req
    ) {
      @Override
      public ServletInputStream getInputStream() {
        return new ServletInputStreamImpl(new ByteArrayInputStream(bodyBytes));
      }

      @Override
      public BufferedReader getReader() {
        return new BufferedReader(new InputStreamReader(getInputStream()));
      }
    };

    // Create a copy of the request and replace its body with a readable version
    HttpServletRequestWrapper copiedRequestWrapper = new HttpServletRequestWrapper(
      originalRequestWrapper
    ) {
      @Override
      public ServletInputStream getInputStream() {
        return new ServletInputStreamImpl(
          new ByteArrayInputStream(bodyBytes.clone())
        );
      }

      @Override
      public BufferedReader getReader() {
        return new BufferedReader(new InputStreamReader(getInputStream()));
      }
    };

    return new Pair<>(originalRequestWrapper, copiedRequestWrapper);
  }
}
