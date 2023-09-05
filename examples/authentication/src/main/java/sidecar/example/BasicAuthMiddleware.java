package sidecar.example;

import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sidecar.java.lib.FilterChain;

public class BasicAuthMiddleware {

  // Logger instance for recording relevant events or issues.
  private static Logger logger = LoggerFactory.getLogger(
    BasicAuthMiddleware.class
  );

  /**
   * This function acts as a middleware for basic authentication.
   * It checks the request header for the specified token name and validates its value.
   * If valid, the request continues. If not, it responds with an unauthorized error.
   *
   * @param req - Incoming request object.
   * @param res - Response object.
   * @param chain - Chain to proceed with the request if validation passes.
   * @return Void
   */
  public static Void authenticate(
    HttpServletRequest req,
    HttpServletResponse res,
    FilterChain chain
  ) {
    try {
      // Retrieve authentication-related environment variables.
      String tokenName = System.getenv("AUTH_TOKEN_NAME");
      String tokenKey = System.getenv("AUTH_TOKEN_KEY");

      // If the environment variables aren't properly set, return an error.
      if (tokenName == null || tokenKey == null) {
        logger.error("Authentication environment variables are not set.");
        res.sendError(
          HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
          "Internal Configuration Error"
        );
        return null;
      }

      // Fetch the client's token from the request header.
      String clientToken = req.getHeader(tokenName);

      // Validate the client's token against the expected value.
      if (tokenKey.equals(clientToken)) {
        // If tokens match, proceed with the request.
        chain.next();
      } else {
        // If tokens don't match, respond with an unauthorized error.
        res.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
      }
    } catch (Exception e) {
      logger.error("Error encountered during authentication: ", e);
      try {
        res.sendError(
          HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
          "Authentication Error"
        );
      } catch (IOException ioException) {
        logger.error("Error sending the error response: ", ioException);
      }
    }

    return null;
  }
}
