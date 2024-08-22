package k8sidecar.java.lib;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents a chain that can forward the request to a subsequent step.
 */
public class FilterChain {

    private HttpServletRequest req;
    private HttpServletResponse res;

    private static Logger log = LoggerFactory.getLogger(FilterChain.class);

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
            // Parse the port number from environment variables and increment by one
            int port = Integer.parseInt(System.getenv("PPORT")) + 1;

            // Create a new URL targeting the localhost and the calculated port
            URL url = new URL("http", "0.0.0.0", port, req.getRequestURI());
            log.info(
                "Making request to URL: {} with method: {}",
                url,
                req.getMethod());

            // Open a connection to the created URL
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod(req.getMethod());
            conn.setConnectTimeout(0);
            conn.setReadTimeout(0);

            // Copy all headers from the original request to the new connection
            Enumeration<String> headerNames = req.getHeaderNames();
            while (headerNames.hasMoreElements()) {
                String headerName = headerNames.nextElement();
                conn.setRequestProperty(headerName, req.getHeader(headerName));
            }

            // If the request method is POST, PUT, OR DELETE, copy the request body
            if ("POST".equals(req.getMethod()) || "PUT".equals(req.getMethod()) || "DELETE".equals(req.getMethod()) ) {
                conn.setDoOutput(true);

                conn.connect();

                // Directly copy the body, regardless of content type
                try (
                  InputStream originalInput = req.getInputStream();
                  OutputStream newOutput = conn.getOutputStream()) {

                    byte[] buffer = new byte[1024];
                    int bytesRead;
                    while ((bytesRead = originalInput.read(buffer)) != -1) {
                        newOutput.write(buffer, 0, bytesRead);
                    }
                    newOutput.flush();
                } catch (Exception ex) {
                    log.error(
                        "Failed to copy the request body. Error Message: {}",
                        ex.toString().trim());
                }
            }else{
                conn.connect();
            }

            // Retrieve and handle the response from the connection
            int responseCode = conn.getResponseCode();

            InputStream is;

            if ( responseCode >= 400) {
               is = conn.getErrorStream();
            }
            else {
               is = conn.getInputStream();
            }

            try{
                res.setStatus(responseCode);
                log.debug("Send to previous element in chain status: {}", responseCode);
                for (Map.Entry<String, List<String>> entries : conn.getHeaderFields().entrySet()) {
                    if (entries.getKey() != null &&
                        !entries.getKey().contains("Transfer-Encoding")) {
                        String values = "";
                        for (String value : entries.getValue()) {
                            values += value + ",";
                        }
                        values = values.substring(0, values.length() - 1);
                        res.setHeader(entries.getKey(), values);
                        log.info("Send to previous element in chain header: {} with value {}", entries.getKey(), values);
                    }
                }

                OutputStream outResponse = res.getOutputStream();
                // TODO is.transferTo()
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = is.read(buffer)) != -1) {
                    outResponse.write(buffer, 0, bytesRead);
                }
                outResponse.flush();
                is.close();
            }catch(IOException ex){
               log.error("Exception while sending the response", ex);
            }

        } catch (Exception e) {
            log.error("Exception while processing the request", e);
        } finally {
            // Ensure connection is closed
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
