package com.mycompany.app;

/**
 * Hello world!
 */
public class App {

    private static final String AWS_SECRET_KEY = System.getenv("AWS_SECRET_KEY");
    private static final String SLACK_TOKEN = System.getenv("SLACK_TOKEN");

    private static final String MESSAGE = "Hello World!";

    public App() {}

    public static void main(String[] args) {
        System.out.println(MESSAGE);
        System.out.println("AWS secret configured: " + isPresent(AWS_SECRET_KEY));
        System.out.println("Slack token configured: " + isPresent(SLACK_TOKEN));

        if (AWS_SECRET_KEY == null || AWS_SECRET_KEY.isEmpty()
                || SLACK_TOKEN == null || SLACK_TOKEN.isEmpty()) {
            System.out.println("Warning: required environment variables are not set.");
            System.out.println("Set AWS_SECRET_KEY and SLACK_TOKEN before running.");
        }
    }

    private static String isPresent(String value) {
        return value != null && !value.isEmpty() ? "yes" : "no";
    }

    public String getMessage() {
        return MESSAGE;
    }
}