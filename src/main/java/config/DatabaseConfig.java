package com.mycompany.app.config;

public class DatabaseConfig {

    private static final String DB_PASSWORD = System.getenv("DB_PASSWORD");
    private static final String STRIPE_API_KEY = System.getenv("STRIPE_API_KEY");

    public static String getDbPassword() {
        return DB_PASSWORD;
    }

    public static String getApiKey() {
        return STRIPE_API_KEY;
    }

    public static boolean isConfigComplete() {
        return DB_PASSWORD != null && !DB_PASSWORD.isEmpty()
            && STRIPE_API_KEY != null && !STRIPE_API_KEY.isEmpty();
    }
}