package eu.ha3.presencefootsteps.sound.acoustics;

import eu.ha3.presencefootsteps.sound.Options;
import eu.ha3.presencefootsteps.sound.State;
import eu.ha3.presencefootsteps.sound.player.SoundPlayer;
import eu.ha3.presencefootsteps.util.Period;
import eu.ha3.presencefootsteps.util.Range;
import net.minecraft.entity.LivingEntity;

record DelayedAcoustic(
        String soundName,
        Range volume,
        Range pitch,
        Period delay
) implements Acoustic {
    static final Serializer FACTORY = Serializer.ofJsObject((json, context) -> new DelayedAcoustic(
            context.getSoundName(json.get("name").getAsString()),
            context.defaultVolume().read("vol", json),
            context.defaultPitch().read("pitch", json),
            Period.fromJson(json, "delay")
    ));

    @Override
    public void playSound(SoundPlayer player, LivingEntity location, State event, Options inputOptions) {
        VaryingAcoustic.playSound(soundName, volume, pitch, delay, player, location, inputOptions);
    }
}