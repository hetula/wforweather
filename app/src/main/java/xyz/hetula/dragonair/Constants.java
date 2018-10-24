package xyz.hetula.dragonair;

public class Constants {

    public static class Api {
        public static final String CURRENT_WEATHER_API_URL = "https://api.openweathermap.org/data/2.5/weather?id=%d&APPID=%s";
    }

    public static class Conf {
        public static final String KEY_CURRENT_CITY = "conf.CURRENT_CITY";
    }


    private Constants() {
        throw new IllegalStateException("No instances");
    }
}
