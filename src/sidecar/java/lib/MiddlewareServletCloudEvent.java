package sidecar.java.lib;

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

@WebServlet("/*")
public class MiddlewareServletCloudEvent extends HttpServlet {

  SidecarFilter middleware;

  public MiddlewareServletCloudEvent(SidecarFilter middleware) {
    this.middleware = middleware;
  }

  @Override
  protected void service(HttpServletRequest req, HttpServletResponse res)
    throws ServletException, IOException {
    // Extraer los encabezados de la solicitud HTTP en un Map
    Map<String, String> headers = new HashMap<>();
    Enumeration<String> headerNames = req.getHeaderNames();
    while (headerNames.hasMoreElements()) {
      String headerName = headerNames.nextElement();
      headers.put(headerName, req.getHeader(headerName));
    }

    // Extraer el cuerpo de la solicitud HTTP en un array de bytes
    byte[] body = req.getInputStream().readAllBytes();

    MessageReader messageReader = HttpMessageFactory.createReader(
      headers,
      body
    );

    CloudEvent cloudEvent = messageReader.toEvent();

    middleware.handleRequest(req, res, cloudEvent);
  }
}
