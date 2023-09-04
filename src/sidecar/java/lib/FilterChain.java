package sidecar.java.lib;

import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Enumeration;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class FilterChain {

  HttpServletRequest req;
  HttpServletResponse res;

  public FilterChain(HttpServletRequest req, HttpServletResponse res) {
    this.req = req;
    this.res = res;
  }

  public void next() {
    try {
      int port = Integer.parseInt(System.getenv("PPORT")) + 1;
      URL url = new URL("http", req.getServerName(), port, req.getRequestURI());
      HttpURLConnection conn = (HttpURLConnection) url.openConnection();
      conn.setRequestMethod(req.getMethod());

      // Copia headers
      Enumeration<String> headerNames = req.getHeaderNames();
      while (headerNames.hasMoreElements()) {
        String headerName = headerNames.nextElement();
        conn.setRequestProperty(headerName, req.getHeader(headerName));
      }

      // Copia parámetros
      StringBuilder postData = new StringBuilder();
      for (
        Enumeration<String> e = req.getParameterNames();
        e.hasMoreElements();
      ) {
        if (postData.length() != 0) postData.append('&');
        String param = e.nextElement();
        postData.append(URLEncoder.encode(param, "UTF-8"));
        postData.append('=');
        postData.append(URLEncoder.encode(req.getParameter(param), "UTF-8"));
      }
      byte[] postDataBytes = postData.toString().getBytes("UTF-8");

      conn.setDoOutput(true);
      conn.getOutputStream().write(postDataBytes);

      conn.connect();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
