package xyz.hetula.w.backend.util;

import com.google.gson.Gson;
import xyz.hetula.w.api.city.City;
import xyz.hetula.w.api.weather.Weather;

import java.io.BufferedReader;
import java.util.Arrays;
import java.util.List;

/**
 * Java side helper to deal with Gson related stuff.
 * Should work from kotlin side nicely but some arrays etc
 * are much nicer to handle from Java side.
 */
public class GsonHelper {

    @SuppressWarnings("UnusedReturnValue") // Kotlin uses, lint is bad at detecting this.
    public static List<City> readCities(Gson gsonInstance, BufferedReader data) {
        City[] cities = gsonInstance.fromJson(data, City[].class);
        return Arrays.asList(cities);
    }

    @SuppressWarnings("UnusedReturnValue") // Kotlin uses, lint is bad at detecting this.
    public static Weather readWeather(Gson gsonInstance, BufferedReader data) {
        return gsonInstance.fromJson(data, Weather.class);
    }

    private GsonHelper() {
        throw new IllegalStateException("No instances");
    }
}
