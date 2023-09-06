package eu.ha3.presencefootsteps.sound.generator;

import eu.ha3.presencefootsteps.sound.Isolator;
import net.minecraft.entity.LivingEntity;

/**
 * Has the ability to generate footsteps based on a Player.
 *
 * @author Hurry
 */
public interface StepSoundGenerator {
    /**
     * Gets the motion tracker used to determine the direction and speed for an entity during simulation.
     */
    MotionTracker getMotionTracker();

    /**
     * Generate footsteps sounds of the Entity.
     */
    boolean generateFootsteps(LivingEntity ply);

    /**
     * Sets this engine to use the parameters from a new isolator.
     */
    void setIsolator(Isolator isolator);

    /**
     * Checks whether the sound engine is engaged and doing something.
     */
    boolean isInactive();
}
