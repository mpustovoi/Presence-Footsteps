package eu.ha3.presencefootsteps.sound.player;

import java.util.Random;

import eu.ha3.presencefootsteps.sound.Options;
import net.minecraft.entity.LivingEntity;

public interface SoundPlayer {
    /**
     * Plays a sound.
     */
    void playSound(LivingEntity location, String soundName, float volume, float pitch, Options options);

    /**
     * Returns a random number generator.
     */
    Random getRNG();

    default void think() { }
}
