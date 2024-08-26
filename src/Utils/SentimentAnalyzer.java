package Utils;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations;
import edu.stanford.nlp.util.CoreMap;

import java.util.List;
import java.util.Properties;

/**
 * Provides sentiment analysis for a given text using the Stanford NLP library.
 *
 * Methods:
 * - analyzeSentiment: Returns the sentiment of the input text (e.g., Positive, Negative, Neutral, Very Positive).
 */
public class SentimentAnalyzer {
    private StanfordCoreNLP pipeline;

    public SentimentAnalyzer() {
        Properties props = new Properties();
        props.setProperty("annotators", "tokenize, ssplit, parse, sentiment");
        this.pipeline = new StanfordCoreNLP(props);
    }
    /**
     * Analyzes the sentiment of the given review text.
     *
     * @param reviewText The input text to analyze.
     * @return The sentiment class of the first sentence (e.g., Positive, Negative, Neutral), or null if input is invalid.
     */
    public String analyzeSentiment(String reviewText) {
        if (reviewText != null) {
            Annotation annotation = new Annotation(reviewText);
            pipeline.annotate(annotation);

            List<CoreMap> sentences = annotation.get(CoreAnnotations.SentencesAnnotation.class);
            int sentimentScore = 0;

            for (CoreMap sentence : sentences) {
                String sentiment = sentence.get(SentimentCoreAnnotations.SentimentClass.class);
                sentimentScore += sentimentToScore(sentiment);
            }

            int averageSentimentScore = Math.round((float) sentimentScore / sentences.size());

            return scoreToSentiment(averageSentimentScore);
        } else {
            return null;
        }
    }

    /**
     * Converts a sentiment class to a numeric score.
     *
     * @param sentiment Sentiment class as a string (e.g., Very Negative, Negative, Neutral, Positive, Very Positive).
     * @return The corresponding numeric score.
     */
    private int sentimentToScore(String sentiment) {
        switch (sentiment) {
            case "Very Negative":
                return 0;
            case "Negative":
                return 1;
            case "Neutral":
                return 2;
            case "Positive":
                return 3;
            case "Very Positive":
                return 4;
            default:
                return 2;
        }
    }

    /**
     * Converts a numeric sentiment score back to a sentiment class.
     *
     * @param score The numeric score (0 to 4).
     * @return The corresponding sentiment class as a string.
     */
    private String scoreToSentiment(int score) {
        switch (score) {
            case 0:
                return "Very Negative";
            case 1:
                return "Negative";
            case 2:
                return "Neutral";
            case 3:
                return "Positive";
            case 4:
                return "Very Positive";
            default:
                return "Neutral";
        }
    }
}
