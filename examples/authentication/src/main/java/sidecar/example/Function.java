package sidecar.example;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import k8sidecar.java.lib.FilterChain;
import k8sidecar.java.lib.SidecarFilter;
import k8sidecar.java.lib.TriFunction;

// Middleware function
public class Function {

  /**
   * The main method sets up the server using the BasicAuthMiddleware for authentication.
   * It initiates the server to listen for incoming requests.
   */
  public static void main(String[] args) {
    // Create an instance of the authentication middleware function.
    TriFunction<HttpServletRequest, HttpServletResponse, FilterChain> userFunction =
      BasicAuthMiddleware::authenticate;
    // Instantiate the server with the authentication function.
    SidecarFilter server = new SidecarFilter(userFunction);
    // Start the server to listen for requests.
    server.listen();
  }
}
