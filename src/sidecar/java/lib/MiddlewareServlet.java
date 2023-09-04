package sidecar.java.lib;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/*")
public class MiddlewareServlet extends HttpServlet {

  SidecarFilter middleware;

  public MiddlewareServlet(SidecarFilter middleware) {
    this.middleware = middleware;
  }

  @Override
  protected void service(HttpServletRequest req, HttpServletResponse res)
    throws ServletException, IOException {
    middleware.handleRequest(req, res);
  }
}
