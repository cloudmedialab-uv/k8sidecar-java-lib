package sidecar.java.lib.logging;

import ch.qos.logback.classic.Level;

public class Logback {
   public static void setLevel(String level) {
      ch.qos.logback.classic.Logger root = (ch.qos.logback.classic.Logger) org.slf4j.LoggerFactory
            .getLogger(ch.qos.logback.classic.Logger.ROOT_LOGGER_NAME);

      switch (level) {
         case "ERROR":
            root.setLevel(Level.ERROR);
            break;
         case "WARN":
            root.setLevel(Level.WARN);
            break;
         case "INFO":
            root.setLevel(Level.INFO);
            break;
         case "DEBUG":
            root.setLevel(Level.DEBUG);
            break;
         case "TRACE":
            root.setLevel(Level.TRACE);
            break;
         case "ALL":
            root.setLevel(Level.ALL);
            break;
         default:
            root.setLevel(Level.INFO);
            break;
      }
      System.out.println("Logging level " + level + " mapped to " + root.getLevel().toString());

   }

   public static void checkLoggingLevel() {
      ch.qos.logback.classic.Logger root = (ch.qos.logback.classic.Logger) org.slf4j.LoggerFactory
            .getLogger(ch.qos.logback.classic.Logger.ROOT_LOGGER_NAME);
      root.error("Check logging - level error");
      root.warn("Check logging - level warn");
      root.info("Check logging - level info");
      root.debug("Check logging - level debug");
      root.trace("Check logging - level trace");
   }
}

