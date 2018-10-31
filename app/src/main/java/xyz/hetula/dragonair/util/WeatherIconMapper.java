package xyz.hetula.dragonair.util;

import androidx.annotation.DrawableRes;
import androidx.annotation.Nullable;
import xyz.hetula.dragonair.R;
import xyz.hetula.dragonair.weather.Condition;

public class WeatherIconMapper {

    private WeatherIconMapper() {
        throw new IllegalStateException("Static only");
    }


    @DrawableRes
    @SuppressWarnings("UnusedReturnValue") // Kotlin uses, lint is bad at detecting this.
    public static int mapWeatherToIconRes(@Nullable String weatherCondition, boolean night) {
        if (weatherCondition == null || weatherCondition.isEmpty()) {
            return R.drawable.ic_weather_icon_missing;
        }
        Condition currentCondition = Condition.UNKNOWN;
        for (Condition condition : Condition.values()) {
            if (condition.matches(weatherCondition)) {
                currentCondition = condition;
                break;
            }
        }

        switch (currentCondition) {
            case CLEAR_SKY:
                return night ? R.drawable.ic_night : R.drawable.ic_day;
            case FEW_CLOUDS: // TODO Own for 'Few Clouds'
            case SCATTERED_CLOUDS:
                return R.drawable.ic_clouds;
            case BROKEN_CLOUDS:
                return R.drawable.ic_cloudy;
            case SHOWER_RAIN: // TODO Proper Rain icons
            case RAIN:
                return R.drawable.ic_rainy;
            case THUNDERSTORM:
                return R.drawable.ic_thunder;
            case SNOW:
                return R.drawable.ic_snow;
            case MIST:
                return R.drawable.ic_mist;
            case UNKNOWN:
            default:
                return R.drawable.ic_weather_icon_missing;
        }
    }

    @DrawableRes
    @SuppressWarnings("UnusedReturnValue") // Kotlin uses, lint is bad at detecting this.
    public static int mapTemperatureToIconRes(int currentTempCelsius) {
        switch (currentTempCelsius) {
            case -2:
                return R.drawable.ic_temp_minus_2;
            case -1:
                return R.drawable.ic_temp_minus_1;
            case 0:
                return R.drawable.ic_temp_0;
            case 1:
                return R.drawable.ic_temp_1;
            case 2:
                return R.drawable.ic_temp_2;
            default:
                return R.drawable.ic_weather_placeholder;
        }
    }
}
