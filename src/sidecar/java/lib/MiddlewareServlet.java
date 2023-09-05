package sidecar.java.lib;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet middleware to process incoming HTTP requests that don't require CloudEvent processing.
 */
@WebServlet("/*")
public class MiddlewareServlet extends HttpServlet {

  private SidecarFilter middleware;

  /**
   * Constructs a new MiddlewareServlet instance.
   *
   * @param middleware the associated sidecar filter
   */
  public MiddlewareServlet(SidecarFilter middleware) {
    this.middleware = middleware;
  }

  @Override
  protected void service(HttpServletRequest req, HttpServletResponse res)
    throws ServletException, IOException {
    middleware.handleRequest(req, res);
  }
}
