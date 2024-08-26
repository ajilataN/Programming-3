package Utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Iterator;
import java.util.Map;

/**
 * Utility class for parsing JSON messages received from the server.
 *
 * The expected format of the message:
 * {"music":"{\"reviewerID\": \"A3V5XBBT7OZG5G\", \"asin\": \"0001393774\", \"reviewerName\": \"gflady\", \"verified\": true, \"reviewText\": \"...\", \"overall\": 5.0, \"reviewTime\": \"02 23, 2016\", \"summary\": \"...\", \"unixReviewTime\": 1456185600}"}
 *
 * Main methods:
 * - extractReviewText: Extracts the content of the "reviewText" field from the given JSON string.
 * - extractTopics: Splits a string of topics into an array of individual topics based on commas and whitespace, used for subscription.
 */
public class MessageParser {
    private static final ObjectMapper objectMapper = new ObjectMapper();
    /**
     * Extracts the "reviewText" field from a JSON message.
     *
     * @param reviewJson The JSON string containing the review data.
     * @return The content of the "reviewText" field, or null if not found or invalid input.
     */
    public static String extractReviewText(String reviewJson) {
        if (reviewJson == null || reviewJson.trim().isEmpty()) {
            return null;
        }
        try {
            // Check if the input is a valid JSON object or array
            if (!isValidJson(reviewJson)) {
                //System.err.println("Input is not valid JSON.");
                return null;
            }
            // Create a jsonNode and read the tree
            JsonNode jsonNode = objectMapper.readTree(reviewJson);

            // Iterate over the fields in the JSON object
            Iterator<Map.Entry<String, JsonNode>> fields = jsonNode.fields();
            if (fields.hasNext()) {
                // Get the first field
                Map.Entry<String, JsonNode> firstEntry = fields.next();
                JsonNode internalNode = firstEntry.getValue();

                // Check if the message is of the expected format
                if (internalNode != null && !internalNode.isNull()) {
                    String reviewText = internalNode.asText();
                    JsonNode innerNode = objectMapper.readTree(reviewText);
                    JsonNode reviewTextNode = innerNode.get("reviewText");

                    // Once we have the review text, we convert it to text
                    if (reviewTextNode != null) {
                        return reviewTextNode.asText();
                    } else {
                        System.err.println("Review text not found within internal node.");
                        return null;
                    }
                } else {
                    System.err.println("Error parsing internal json node!");
                    return null;
                }
            } else {
                System.err.println("No fields found in the JSON message.");
                return null;
            }
        } catch (Exception e) {
            System.err.println("Error parsing JSON: " + e.getMessage());
            return null;
        }
    }

    /**
     * Validates if a string is a valid JSON format.
     *
     * @param str The string to check.
     * @return True if the string is valid JSON, false otherwise.
     */
    private static boolean isValidJson(String str) {
        try {
            objectMapper.readTree(str);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Splits a string of topics into an array of individual topics.
     *
     * @param topics A string containing topics separated by commas or whitespace.
     * @return An array of topic strings.
     */
    public static String[] extractTopics (String topics){
        return topics.split("[,\\s]+");
    }
}
