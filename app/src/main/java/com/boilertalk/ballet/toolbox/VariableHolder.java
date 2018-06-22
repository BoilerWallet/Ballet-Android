package com.boilertalk.ballet.toolbox;

public class VariableHolder {
    private static String password;

    public static String getPassword() {
        return password;
    }

    public static void setPassword(String password) {
        VariableHolder.password = password;
    }

}
