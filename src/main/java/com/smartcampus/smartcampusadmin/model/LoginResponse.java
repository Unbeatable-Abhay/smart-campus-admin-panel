package com.smartcampus.smartcampusadmin.model;

public class LoginResponse {

    private String token;
    private UserInfo user;

    public String getToken() {
        return token;
    }

    public UserInfo getUser() {
        return user;
    }

    public static class UserInfo {
        private int id;
        private String name;
        private String email;
        private String role;
        private String department;

        public int getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public String getEmail() {
            return email;
        }

        public String getRole() {
            return role;
        }

        public String getDepartment() {
            return department;
        }
    }
}