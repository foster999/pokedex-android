package com.example.pokedex;

public class Util {
    public static String capitalize(String str) {
        if (str == null) return str;
        return str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase();
    }
}
