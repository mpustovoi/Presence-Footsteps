package eu.ha3.presencefootsteps.util;

import java.util.Random;

import com.google.gson.JsonObject;

public record Range (float min, float max) {
    public static final Range DEFAULT = exactly(1);

    public static Range exactly(float value) {
        return new Range(value, value);
    }

    public Range read(String name, JsonObject json) {
        if (json.has(name)) {
            return exactly(getPercentage(json, name, min));
        }

        return new Range(
                getPercentage(json, name + "_min", this.min),
                getPercentage(json, name + "_max", this.max)
        );
    }

    public float random(Random rand) {
        return MathUtil.randAB(rand, min, max);
    }

    public float on(float value) {
        return MathUtil.between(min, max, value);
    }

    private static float getPercentage(JsonObject object, String param, float fallback) {
        if (!object.has(param)) {
            return fallback;
        }
        return object.get(param).getAsFloat() / 100F;
    }
}
