package xyz.hetula.dragonair;

public class Constants {

    public static class Api {
        public static final String CURRENT_WEATHER_API_URL = "https://api.openweathermap.org/data/2.5/weather?id=%d&APPID=%s";

        private Api() {
            throw new IllegalStateException("No instances");
        }
    }

    public static class Pref {
        public static final String KEY_CURRENT_CITY = "conf.CURRENT_CITY";

        private Pref() {
            throw new IllegalStateException("No instances");
        }
    }

    public static class Notification {
        public static final int WEATHER_NOTIFICATION_ID = 40;
        public static final String WEATHER_CHANNEL_ID = "weather.main";
        public static final int WEATHER_CHANNEL_NAME = R.string.channel_name;

        private Notification() {
            throw new IllegalStateException("No instances");
        }
    }

    public static class Weather {
        public static final double KELVIN_TO_CELSIUS = 273.15;

        private Weather() {
            throw new IllegalStateException("No instances");
        }
    }

    public static class Intents {
        public static final String ACTION_UPDATE_WEATHER = "xyz.hetula.UPDATE_WEATHER";

        private Intents() {
            throw new IllegalStateException("No instances");
        }
    }


    private Constants() {
        throw new IllegalStateException("No instances");
    }
}
