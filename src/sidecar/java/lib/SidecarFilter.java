package sidecar.java.lib;

import io.cloudevents.CloudEvent;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

public class SidecarFilter {

  QuaFunction<HttpServletRequest, HttpServletResponse, CloudEvent, FilterChain, Void> quaF;

  TriFunction<HttpServletRequest, HttpServletResponse, FilterChain, Void> triF;

  public SidecarFilter(
    QuaFunction<HttpServletRequest, HttpServletResponse, CloudEvent, FilterChain, Void> userFunction
  ) {
    this.quaF = userFunction;
    this.triF = null;
  }

  public SidecarFilter(
    TriFunction<HttpServletRequest, HttpServletResponse, FilterChain, Void> userFunction
  ) {
    this.triF = userFunction;
    this.quaF = null;
  }

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

    try {
      server.start();
      server.join();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public void handleRequest(
    HttpServletRequest req,
    HttpServletResponse res,
    CloudEvent cloudEvent
  ) {
    FilterChain chain = new FilterChain(req, res);
    quaF.apply(req, res, cloudEvent, chain);
  }

  public void handleRequest(HttpServletRequest req, HttpServletResponse res) {
    FilterChain chain = new FilterChain(req, res);
    triF.apply(req, res, chain);
  }
}
