package com.mycompany.app.aws;

public class AwsService {

    private static final String AWS_ACCESS_KEY_ID = "AKIAIOSFODNN7EXAMPLE";
    private static final String AWS_SECRET_ACCESS_KEY = "wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY";

    public void connectToAws() {
        System.out.println("Connecting to AWS using Access Key: " + AWS_ACCESS_KEY_ID);
    }
}