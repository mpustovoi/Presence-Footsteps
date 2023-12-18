package eu.ha3.presencefootsteps.sound.acoustics;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import eu.ha3.presencefootsteps.PresenceFootsteps;
import eu.ha3.presencefootsteps.util.Range;
import java.io.Reader;
import java.util.function.BiConsumer;

/**
 * A JSON parser that creates a Library of Acoustics.
 *
 * @author Hurry
 */
public record AcousticsFile (
        Range defaultVolume,
        Range defaultPitch,
        String soundRoot
) {
    private static final int ENGINE_VERSION = 1;

    public static void read(Reader reader, BiConsumer<String, Acoustic> consumer) {
        try {
            JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
            AcousticsFile context = read(json);
            json.getAsJsonObject("contents").entrySet().forEach(element -> {
                consumer.accept(element.getKey(), Acoustic.read(context, element.getValue(), "events"));
            });
        } catch (JsonParseException e) {
            PresenceFootsteps.logger.error("Error whilst loading acoustics", e);
        }
    }

    private static AcousticsFile read(JsonObject json) {
        expect("library".equals(json.get("type").getAsString()), "Invalid type: Expected \"library\" got \"" + json.get("type").getAsString() + "\"");
        expect(json.get("engineversion").getAsInt() == ENGINE_VERSION, "Unrecognised Engine version: " + ENGINE_VERSION + " expected, got " + json.get("engineversion").getAsInt());
        expect(json.has("contents"), "Empty contents");

        String soundRoot = "";

        if (json.has("soundroot")) {
            soundRoot = json.get("soundroot").getAsString();
        }

        if (json.has("defaults")) {
            JsonObject defaults = json.getAsJsonObject("defaults");
            return new AcousticsFile(
                    Range.DEFAULT.read("vol", defaults),
                    Range.DEFAULT.read("pitch", defaults),
                    soundRoot
            );
        }

        return new AcousticsFile(
                Range.DEFAULT,
                Range.DEFAULT,
                soundRoot
        );
    }

    private static void expect(boolean condition, String message) {
        if (!condition) {
            throw new JsonParseException(message);
        }
    }

    public String getSoundName(String soundName) {
        if (soundName.charAt(0) != '@') {
            return soundRoot + soundName;
        }

        return soundName.replace("@", "");
    }
}
