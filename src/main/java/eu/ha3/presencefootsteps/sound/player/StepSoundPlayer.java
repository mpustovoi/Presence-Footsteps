package eu.ha3.presencefootsteps.sound.player;

import eu.ha3.presencefootsteps.sound.Options;
import eu.ha3.presencefootsteps.sound.State;
import eu.ha3.presencefootsteps.world.Association;

/**
 * Can generate footsteps using the default Minecraft function.
 */
public interface StepSoundPlayer {
    /**
     * Play a step sound from a block
     */
    boolean playStep(Association assos, State eventType, Options options);
}
