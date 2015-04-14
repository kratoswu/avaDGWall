package com.avadesign.util;

public class StringUtil {
    public static boolean isEmptyString(String param) {
        return param == null || param.trim().isEmpty();
    }

    public static String getSwitchTypePattern() {
        return ".*[Ss][Ww][Ii][Tt][Cc][Hh].*";
    }

    public static String getSceneTypePattern() {
        return ".*[Ss][Cc][Ee][Nn][Ee].*";
    }

    public static String getSensorTypePattern() {
       return ".*[Ss][Ee][Nn][Ss][Oo][Rr].*";
    }

}
