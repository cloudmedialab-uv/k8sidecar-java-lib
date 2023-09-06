package sidecar.java.lib;

import io.cloudevents.CloudEvent;
import sidecar.java.lib.logging.Logback;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

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
    FilterChain chain = new FilterChain(req, res);
    quaF.apply(req, res, cloudEvent, chain);
  }

  /**
   * Handles a regular incoming HTTP request.
   *
   * @param req the incoming request
   * @param res the outgoing response
   */
  public void handleRequest(HttpServletRequest req, HttpServletResponse res) {
    FilterChain chain = new FilterChain(req, res);
    triF.apply(req, res, chain);
  }
}
