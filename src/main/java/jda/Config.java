package jda;

import io.github.cdimascio.dotenv.Dotenv;

public class Config {
    private static final Dotenv dotenv = Dotenv.configure().filename("env").load(); // instead of '.env', use 'env'

    public static String get(String key) {
        return (dotenv).get(key.toUpperCase());
    }
}

