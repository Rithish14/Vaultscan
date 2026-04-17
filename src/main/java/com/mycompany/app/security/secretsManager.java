package com.mycompany.app.security;

public class SecretsManager {

    private static final String PRIVATE_KEY = "-----BEGIN PRIVATE KEY-----\nMIIEvQIBADANBgkqhkiG9w0BAQEFAASCBK...\n-----END PRIVATE KEY-----";
    private static final String GITHUB_TOKEN = "ghp_16charactersfakeGitHubtokenforTesting123";

    public String fetchPrivateKey() {
        return PRIVATE_KEY;
    }

    public String getGithubToken() {
        return GITHUB_TOKEN;
    }
}
