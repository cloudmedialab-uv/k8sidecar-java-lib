package sidecar.example;

import java.util.Enumeration;
import javax.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sidecar.java.lib.SidecarFilter;

// Middleware function
public class Function {

  private static final Logger logger = LoggerFactory.getLogger(Function.class);
  private static String loggingLevel = null;

  public static void main(String[] args) {
    // Retrieves the value of the specified environment variable.
    loggingLevel = System.getenv("LOGGING_LEVEL");

    //if LOGGING_LEVEL is no set default value is DEBUG
    if (loggingLevel == null) {
      loggingLevel = "DEBUG";
    }

    //Setting logging level
    if ("DEBUG".equals(loggingLevel)) {
      // Set logging level to DEBUG
    } else if ("INFO".equals(loggingLevel)) {
      // Set logging level to INFO
    } else if ("PROD".equals(loggingLevel)) {
      // Set logging level to PROD
    }

    // Setting up SidecarFilter
    SidecarFilter server = new SidecarFilter((req, res, chain) -> {
      try {
        // Call logging function
        logRequest(req, loggingLevel);

        //Next
        chain.next();
      } catch (Exception e) {
        logger.error("Error processing request", e);
      }
      return null;
    });

    // Server start
    server.listen();
  }

  /**
   * Logs detailed information about the incoming HTTP request based on the logging level.
   *
   * @param req   The incoming HttpServletRequest to be logged.
   * @param level The logging level (DEBUG, INFO, PROD).
   */
  private static void logRequest(HttpServletRequest req, String level) {
    switch (level) {
      case "DEBUG":
        // In DEBUG level, provide detailed logging of the request.

        logger.debug("------ DEBUG Logging Level ------");

        // Log the HTTP request method (e.g., GET, POST, PUT).
        logger.debug("Request Method: {}", req.getMethod());

        // Log the requested URI.
        logger.debug("Request URI: {}", req.getRequestURI());

        // Log the protocol used (e.g., HTTP/1.1).
        logger.debug("Request Protocol: {}", req.getProtocol());

        // Log the remote IP address of the requester.
        logger.debug("Remote Address: {}", req.getRemoteAddr());

        // Log all headers associated with this request.
        Enumeration<String> headerNames = req.getHeaderNames();
        while (headerNames.hasMoreElements()) {
          String headerName = headerNames.nextElement();
          logger.debug(
            "Header [{}]: {}",
            headerName,
            req.getHeader(headerName)
          );
        }

        // Log all query parameters passed with this request.
        Enumeration<String> paramNames = req.getParameterNames();
        while (paramNames.hasMoreElements()) {
          String paramName = paramNames.nextElement();
          logger.debug(
            "Query Parameter [{}]: {}",
            paramName,
            req.getParameter(paramName)
          );
        }

        break;
      case "INFO":
        // In INFO level, provide informational logging with essential request details.

        logger.info("------ INFO Logging Level ------");

        // Log the HTTP request method.
        logger.info("Request Method: {}", req.getMethod());

        // Log the requested URI.
        logger.info("Request URI: {}", req.getRequestURI());

        // Log the remote IP address of the requester.
        logger.info("Remote Address: {}", req.getRemoteAddr());

        break;
      case "PROD":
        // In PROD level, provide minimal logging with only high-level request information.

        logger.info("------ PROD Logging Level ------");

        // Log the remote IP address of the requester.
        logger.info("Request from IP: {}", req.getRemoteAddr());

        // Log the requested URI.
        logger.info("Request URI: {}", req.getRequestURI());

        break;
      default:
        // Handle cases where an unknown or unspecified logging level is provided.

        logger.warn("Unknown logging level specified: {}", level);
        break;
    }
  }
}
