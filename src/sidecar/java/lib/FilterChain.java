package sidecar.java.lib;

import java.io.InputStream;
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
    HttpURLConnection conn = null;

    try {
      // Obtener el puerto de la variable de entorno y sumar 1
      int port = Integer.parseInt(System.getenv("PPORT")) + 1;

      URL url = new URL("http", "127.0.0.1", port, req.getRequestURI());
      conn = (HttpURLConnection) url.openConnection();
      conn.setRequestMethod(req.getMethod());
      conn.setConnectTimeout(5000); // 5 segundos de timeout para la conexión
      conn.setReadTimeout(5000); // 5 segundos de timeout para lectura

      // Copiar encabezados de la solicitud original
      Enumeration<String> headerNames = req.getHeaderNames();
      while (headerNames.hasMoreElements()) {
        String headerName = headerNames.nextElement();
        conn.setRequestProperty(headerName, req.getHeader(headerName));
      }

      if ("POST".equals(req.getMethod()) || "PUT".equals(req.getMethod())) {
        // Copiar parámetros de la solicitud y codificarlos
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

        // Enviar datos de la solicitud
        conn.setDoOutput(true);
        try (OutputStream os = conn.getOutputStream()) {
          os.write(postDataBytes);
        }
      }

      int responseCode = conn.getResponseCode();
      if (responseCode == HttpURLConnection.HTTP_OK) {
        try (InputStream is = conn.getInputStream()) {
          byte[] buffer = new byte[1024];
          int bytesRead;
          while ((bytesRead = is.read(buffer)) != -1) {
            res.getOutputStream().write(buffer, 0, bytesRead);
          }
        }
      } else {
        // Manejar respuesta de error aquí, si es necesario
        System.err.println("Error al redirigir: " + responseCode);
      }
    } catch (Exception e) {
      e.printStackTrace();
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
