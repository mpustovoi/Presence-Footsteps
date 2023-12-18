package eu.ha3.presencefootsteps.sound.generator;

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
     * Checks whether the sound engine is engaged and doing something.
     */
    boolean isInactive();
}
