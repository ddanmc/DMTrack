package com.paquito.primerspring.services;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
public class WeatherService {

    private final RestTemplate http = new RestTemplate();

    // Bogotá por defecto
    public static final double DEF_LAT = 4.7110;
    public static final double DEF_LON = -74.0721;
    public static final String DEF_NAME = "Bogotá, CO";

    /** Clima actual para lat/lon. Keys: temperature, windspeed, weathercode, time */
    public Map<String, Object> obtenerClima(double lat, double lon) {
        String url = "https://api.open-meteo.com/v1/forecast"
                + "?latitude=" + lat
                + "&longitude=" + lon
                + "&current_weather=true"
                + "&timezone=auto";
        Map<String, Object> out = new HashMap<>();
        try {
            Map<?,?> resp = http.getForObject(url, Map.class);
            if (resp != null && resp.get("current_weather") instanceof Map<?,?> cur) {
                out.put("temperature", cur.get("temperature"));
                out.put("windspeed",   cur.get("windspeed"));
                out.put("weathercode", cur.get("weathercode"));
                out.put("time",        cur.get("time"));
            }
        } catch (Exception e) {
            System.out.println("[WeatherService] error clima: " + e.getMessage());
        }
        out.putIfAbsent("temperature", "--");
        out.putIfAbsent("windspeed", "--");
        out.putIfAbsent("weathercode", null);
        out.putIfAbsent("time", "--");
        return out;
    }

    public Map<String, Object> obtenerClima() {
        return obtenerClima(DEF_LAT, DEF_LON);
    }

    /** Geocoding: busca ciudad por nombre. */
    public List<Map<String, Object>> buscarCiudad(String name) {
        String url = "https://geocoding-api.open-meteo.com/v1/search"
                + "?name=" + name.replace(" ", "%20")
                + "&count=5&language=es&format=json";
        try {
            Map<?, ?> resp = http.getForObject(url, Map.class);
            if (resp != null && resp.get("results") instanceof List<?> list) {
                List<Map<String, Object>> out = new ArrayList<>();
                for (Object o : list) {
                    if (o instanceof Map<?, ?> m) {
                        Map<String, Object> mm = (Map<String, Object>) m; // casteo
                        Map<String, Object> row = new HashMap<>();
                        row.put("name", mm.getOrDefault("name", ""));
                        row.put("country", mm.getOrDefault("country_code", ""));
                        row.put("lat", mm.get("latitude"));
                        row.put("lon", mm.get("longitude"));
                        out.add(row);
                    }
                }
                return out;
            }
        } catch (Exception e) {
            System.out.println("[WeatherService] error geocoding: " + e.getMessage());
        }
        return List.of();
    }

    /**
     * Pronóstico diario con:
     * - tmax/tmin (°C)
     * - precipitación total (mm)
     * - probabilidad de precipitación máxima (%)
     * - viento máximo a 10m (km/h)
     */
    public List<Map<String, Object>> pronosticoDiario(double lat, double lon, int days) {
        String url = "https://api.open-meteo.com/v1/forecast"
                + "?latitude=" + lat
                + "&longitude=" + lon
                + "&daily=temperature_2m_max,temperature_2m_min,precipitation_sum,"
                + "precipitation_probability_max,wind_speed_10m_max"
                + "&forecast_days=" + days
                + "&timezone=auto";

        List<Map<String, Object>> out = new ArrayList<>();
        try {
            Map<?,?> resp = http.getForObject(url, Map.class);
            if (resp != null && resp.get("daily") instanceof Map<?,?> daily) {
                List<?> dates = (List<?>) daily.get("time");
                List<?> tmax  = (List<?>) daily.get("temperature_2m_max");
                List<?> tmin  = (List<?>) daily.get("temperature_2m_min");
                List<?> prec  = (List<?>) daily.get("precipitation_sum");
                List<?> prob  = (List<?>) daily.get("precipitation_probability_max");
                List<?> wind  = (List<?>) daily.get("wind_speed_10m_max");

                for (int i = 0; i < dates.size(); i++) {
                    Map<String, Object> d = new HashMap<>();
                    d.put("date", dates.get(i));
                    d.put("tmax", (i < tmax.size()) ? tmax.get(i) : null);
                    d.put("tmin", (i < tmin.size()) ? tmin.get(i) : null);
                    d.put("prec", (i < prec.size()) ? prec.get(i) : null);
                    d.put("prob", (i < prob.size()) ? prob.get(i) : null);
                    d.put("wind", (i < wind.size()) ? wind.get(i) : null);
                    out.add(d);
                }
            }
        } catch (Exception e) {
            System.out.println("[WeatherService] error daily: " + e.getMessage());
        }
        return out;
    }

    /** Temperatura horaria para próximas N horas (labels ISO y valores °C). */
    public Map<String, List<?>> horarioTemperatura(double lat, double lon, int hours) {
        // Pedimos hasta 48h y luego recortamos a 'hours'
        String url = "https://api.open-meteo.com/v1/forecast"
                + "?latitude=" + lat
                + "&longitude=" + lon
                + "&hourly=temperature_2m"
                + "&forecast_days=2"
                + "&timezone=auto";

        Map<String, List<?>> out = new HashMap<>();
        out.put("times", java.util.Collections.emptyList());
        out.put("temps", java.util.Collections.emptyList());

        try {
            Map<?, ?> resp = http.getForObject(url, Map.class);
            if (resp != null && resp.get("hourly") instanceof Map<?, ?> hourly) {
                List<?> times = (List<?>) hourly.get("time");
                List<?> temps = (List<?>) hourly.get("temperature_2m");

                if (times != null && temps != null) {
                    int n = Math.min(hours, Math.min(times.size(), temps.size()));
                    out.put("times", new ArrayList<>(times.subList(0, n)));
                    out.put("temps", new ArrayList<>(temps.subList(0, n)));
                }
            }
        } catch (Exception e) {
            System.out.println("[WeatherService] error hourly: " + e.getMessage());
        }
        return out;
    }
}