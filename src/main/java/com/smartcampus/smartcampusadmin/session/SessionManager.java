package com.smartcampus.smartcampusadmin.session;

public class SessionManager {

    private static String token;
    private static String userName;
    private static String userRole;

    public static void setToken(String newToken) {
        token = newToken;
    }

    public static String getToken() {
        return token;
    }

    public static void setUserName(String newUserName) {
        userName = newUserName;
    }

    public static String getUserName() {
        return userName;
    }

    public static void setUserRole(String newUserRole) {
        userRole = newUserRole;
    }

    public static String getUserRole() {
        return userRole;
    }

    public static boolean isLoggedIn() {
        return token != null;
    }

    public static void clear() {
        token = null;
        userName = null;
        userRole = null;
    }
}