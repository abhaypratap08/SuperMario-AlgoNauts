package utils;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Util class responsible for logging
 * some important errors to the console.
 *
 * @version 1.0.0
 */
public class Logger {
    /**
     * Creates a pretty-formatted
     * string of the current time.
     *
     * @return The current time string.
     */
    private static String currentTime() {
        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss");
        Date date = new Date();

        return formatter.format(date);
    }

    /**
     * Logs a message to the console with
     * some details about the time and object.
     *
     * @param object The object that requested the log.
     * @param message The message to be logged.
     */
    public static void log(Object object, String message) {
        String className = object.getClass().getSimpleName();
        String prefix = "[" + currentTime() + " @ " + className + "] ";

        System.out.println(prefix + message);
    }

    /**
     * Logs a message to the console with some
     * details about the time and the class' name.
     *
     * @param className The name of the class that requested the log.
     * @param message The message to be logged.
     */
    public static void log(String className, String message) {
        String prefix = "[" + currentTime() + " @ " + className + "] ";

        System.out.println(prefix + message);
    }
}
