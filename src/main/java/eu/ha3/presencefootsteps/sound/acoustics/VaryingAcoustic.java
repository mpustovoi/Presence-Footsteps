package eu.ha3.presencefootsteps.sound.acoustics;

import com.google.gson.JsonObject;
import eu.ha3.presencefootsteps.sound.Options;
import eu.ha3.presencefootsteps.sound.State;
import eu.ha3.presencefootsteps.sound.player.SoundPlayer;
import eu.ha3.presencefootsteps.util.Range;
import net.minecraft.entity.LivingEntity;

/**
 * The simplest form of an acoustic. Plays one sound with a set volume and pitch range.
 *
 * @author Hurry
 */
record VaryingAcoustic(
        String soundName,
        Range volume,
        Range pitch
) implements Acoustic {
    static final Serializer FACTORY = (json, context) -> {
        if (json.isJsonPrimitive()) {
            return new VaryingAcoustic(
                context.getSoundName(json.getAsString()),
                context.defaultVolume(),
                context.defaultPitch()
            );
        }
        JsonObject jso = json.getAsJsonObject();
        return new VaryingAcoustic(
                context.getSoundName(jso.get("name").getAsString()),
                context.defaultVolume().read("vol", jso),
                context.defaultPitch().read("pitch", jso)
        );
    };

    @Override
    public void playSound(SoundPlayer player, LivingEntity location, State event, Options inputOptions) {
        playSound(soundName, volume, pitch, Options.EMPTY, player, location, inputOptions);
    }

    // shared code between VaryingAcoustic & DelayedAcoustic since
    // in the old implementation DelayedAcoustic extended VaryingAcoustic
    static void playSound(String soundName, Range volume, Range pitch, Options options, SoundPlayer player, LivingEntity location, Options inputOptions) {
        if (soundName.isEmpty()) {
            // Special case for intentionally empty sounds (as opposed to fall back sounds)
            return;
        }

        final float finalVolume = inputOptions.containsKey("gliding_volume")
                ? volume.on(inputOptions.get("gliding_volume"))
                : volume.random(player.getRNG());

        final float finalPitch = inputOptions.containsKey("gliding_pitch")
                ? pitch.on(inputOptions.get("gliding_pitch"))
                : pitch.random(player.getRNG());

        player.playSound(location, soundName, finalVolume, finalPitch, options.and(inputOptions));
    }
}