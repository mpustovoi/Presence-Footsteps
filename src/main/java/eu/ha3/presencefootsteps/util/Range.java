package eu.ha3.presencefootsteps.util;

import java.io.IOException;
import java.util.Random;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import net.minecraft.util.math.MathHelper;

public record Range (float min, float max) {
    public static final Range DEFAULT = exactly(1);

    public static Range exactly(float value) {
        return new Range(value, value);
    }

    public Range read(String name, JsonObject json) {
        if (json.has(name)) {
            JsonElement element = json.get(name);
            if (element.isJsonObject()) {
                return new Range(
                    getPercentage(element.getAsJsonObject(), "min", min),
                    getPercentage(element.getAsJsonObject(), "max", max)
                );
            }
            return exactly(getPercentage(json, name, min));
        }

        return new Range(
                getPercentage(json, name + "_min", min),
                getPercentage(json, name + "_max", max)
        );
    }

    public void write(JsonObjectWriter writer) throws IOException {
        if (MathHelper.approximatelyEquals(min, max)) {
            writer.writer().value(min);
        } else {
            writer.object(() -> {
                writer.field("min", min);
                writer.field("max", max);
            });
        }
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
