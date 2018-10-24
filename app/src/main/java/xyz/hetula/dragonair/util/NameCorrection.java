package xyz.hetula.dragonair.util;

import java.util.HashMap;
import java.util.Map;

/**
 * Mapper for Scandinavic/Germanic characters like ä,ö,å,ü
 */
public class NameCorrection {
    private static final Map<String, String> sCorrections = new HashMap<>();
    static {
        sCorrections.put("Jyvaeskylae", "Jyväskylä");
    }

    public static String correctName(String originalName) {
        return sCorrections.getOrDefault(originalName, originalName);
    }

    private NameCorrection() {
        throw new IllegalStateException("No instances");
    }
}
