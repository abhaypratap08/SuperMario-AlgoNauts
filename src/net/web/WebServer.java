package net.web;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.Marshaller;
import jakarta.xml.bind.Unmarshaller;
import org.apache.commons.lang3.StringUtils;
import utils.Logger;

import java.io.ByteArrayOutputStream;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Scanner;

/**
 * The class responsible for communicating
 * with the web server, keeping track and
 * updating the scores' leaderboard.
 *
 * @version 1.1.1
 */
public class WebServer {
    private final URL serverUrl;

    public WebServer(URL serverUrl) {
        this.serverUrl = serverUrl;
    }

    /**
     * Sends the final score to the web server
     * for insertion to the public leaderboard.
     *
     * @param score The score object to be sent.
     */
    public void publishScore(Score score) {
        try {
            // Set up the connection with the web server
            HttpURLConnection connection = (HttpURLConnection) serverUrl.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/xml");
            connection.setDoOutput(true);

            // Create the marshaller for the Score class
            JAXBContext jaxbContext = JAXBContext.newInstance(score.getClass());
            Marshaller marshaller = jaxbContext.createMarshaller();

            // Marshal the object to a string
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            marshaller.marshal(score, outputStream);

            // Send the string to the web server
            byte[] postData = outputStream.toByteArray();
            connection.getOutputStream().write(postData);

            // Wait for the server's response
            Scanner scanner = new Scanner(connection.getInputStream());
            String response = scanner.nextLine();
            scanner.close();

            if (Objects.equals(response, "Error"))
                Logger.log(this, "The server returned an error while publishing the score");

            connection.disconnect();
        } catch (Exception ignored) {
            Logger.log(this, "An error occurred while publishing the score");
            Logger.log(this, "Is the server connected?");
        }
    }

    /**
     * Retrieves the scores from the leaderboard
     * to show them on the leaderboard screen.
     */
    public ArrayList<String> getScores() {
        try {
            // Set up the connection with the web server
            HttpURLConnection connection = (HttpURLConnection) serverUrl.openConnection();
            connection.setRequestMethod("GET");
            connection.connect();

            // Read the response from the web server
            Scanner scanner = new Scanner(connection.getInputStream());
            String response = scanner.nextLine();

            scanner.close();
            connection.disconnect();

            // Create the marshaller for the Leaderboard class
            Leaderboard leaderboard = new Leaderboard();

            JAXBContext jaxbContext = JAXBContext.newInstance(leaderboard.getClass());
            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();

            // Unmarshal the string to an object
            leaderboard = (Leaderboard) unmarshaller.unmarshal(new StringReader(response));

            // Format the returned ArrayList of scores
            ArrayList<String> scores = new ArrayList<>();
            for (int i = 0; i < leaderboard.getScores().size(); i++) {
                String username = leaderboard.getScores().get(i).username;
                int points = leaderboard.getScores().get(i).points;

                String index = String.valueOf(i + 1);
                String score = StringUtils.leftPad(index, 2, "0") + ". "
                        + StringUtils.rightPad(username, 16) + " "
                        + StringUtils.leftPad(String.valueOf(points), 5);
                scores.add(score);
            }
            // Or show that there are no scores to display
            if (leaderboard.getScores().isEmpty()) scores.add("   No scores to display   ");

            return scores;
        } catch (Exception ignored) {}

        Logger.log(this, "An error occurred while fetching the scores");
        Logger.log(this, "Is the server connected?");

        ArrayList<String> errorMessage = new ArrayList<>();
        errorMessage.add("Error fetching the scores!");
        return errorMessage;
    }
}
