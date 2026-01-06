package com.example.hello.Middleware;

public class Constant {
    public static class VALIDATION {
        public final static String USERNAME = "^[a-zA-Z0-9@._]{3,50}$";
        public final static String FULL_NAME = "^(?=.*\\p{Lu})(?=.*\\s)[\\p{L}\\s'’-]{3,50}$";
        public final static String PASSWORD = "^(?=.*[a-z])(?=.*[A-Z])(?=.*[0-9])(?=.*[!@#$%^&*_+])\\S{8,}$";
        public final static String PHONE = "^0\\d{9}$";
    }
}
