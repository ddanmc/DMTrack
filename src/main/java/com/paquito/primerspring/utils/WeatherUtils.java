package com.paquito.primerspring.utils;

public class WeatherUtils {

    /** Convierte el código de Open-Meteo a texto legible. */
    public static String getCondition(Integer code) {
        if (code == null) return "N/D";
        return switch (code) {
            case 0 -> "Despejado";
            case 1,2,3 -> "Parcialmente nublado";
            case 45,48 -> "Niebla";
            case 51,53,55,56,57 -> "Llovizna";
            case 61,63,65,66,67 -> "Lluvia";
            case 71,73,75,77 -> "Nieve";
            case 80,81,82 -> "Chubascos";
            case 85,86 -> "Chubascos de nieve";
            case 95,96,99 -> "Tormenta";
            default -> "Desconocido (" + code + ")";
        };
    }

    /** True si el código implica lluvia/tormenta (afecta logística). */
    public static boolean isRainy(Integer code) {
        if (code == null) return false;
        return (code >= 51 && code <= 57)  // llovizna
            || (code >= 61 && code <= 67)  // lluvia
            || (code >= 80 && code <= 82)  // chubascos
            || code == 95 || code == 96 || code == 99; // tormenta
    }
}