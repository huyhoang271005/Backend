package com.example.hello.Middleware;

public class Constant {
    public static class VALIDATION {
        public final static String USERNAME = "^[a-zA-Z0-9@._]{3,50}$";
        public final static String FULL_NAME = "^(?=.*\\p{Lu})(?=.*\\s)[\\p{L}\\s'’-]{3,50}$";
        public final static String PASSWORD = "^(?=.*[a-z])(?=.*[A-Z])(?=.*[0-9])(?=.*[!@#$%^&*_+])\\S{8,}$";
        public final static String DATE = "^\\d{4}-(0[1-9]|1[0-2])-(0[1-9]|[12]\\d|3[01])$";
        public final static String TIME = "^([01]\\d|2[0-3]):([0-5]\\d):([0-5]\\d)$";
        public final static String MONEY = "^[1-9]\\d{2,17}$";
    }
}
