package eu.ha3.presencefootsteps.sound.player;

import java.util.Random;

import eu.ha3.presencefootsteps.sound.Options;
import eu.ha3.presencefootsteps.sound.SoundEngine;
import eu.ha3.presencefootsteps.sound.State;
import eu.ha3.presencefootsteps.sound.acoustics.AcousticLibrary;
import eu.ha3.presencefootsteps.world.Association;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.LivingEntity;
import net.minecraft.sound.BlockSoundGroup;

public class PFSoundPlayer implements SoundPlayer, StepSoundPlayer {
    private final ImmediateSoundPlayer immediatePlayer;
    private final DelayedSoundPlayer delayedPlayer;

    private final SoundEngine engine;

    public PFSoundPlayer(SoundEngine engine) {
        this.engine = engine;
        immediatePlayer = new ImmediateSoundPlayer(engine);
        delayedPlayer = new DelayedSoundPlayer(this);
    }

    @Override
    public void think() {
        delayedPlayer.think();
    }

    @Override
    public Random getRNG() {
        return immediatePlayer.getRNG();
    }

    @SuppressWarnings("deprecation")
    @Override
    public void playStep(Association association, State event, Options options) {

        AcousticLibrary library = engine.getIsolator().acoustics();

        if (association.hasCoreSound()) {
            library.playAcoustic(association.source(), association.acousticNames(), event, options);
        } else if (!association.state().isLiquid()) {
            BlockSoundGroup soundType = association.soundGroup();
            BlockState above = association.source().getWorld().getBlockState(association.pos().up());

            if (above.isOf(Blocks.SNOW)) {
                soundType = above.getSoundGroup();
            }

            immediatePlayer.playSound(association.source(), soundType.getStepSound().getId().toString(), soundType.getVolume() * 0.15F, soundType.getPitch(), options);
        }

        if (association.hasWetSound() && Options.WET_VOLUME_OPTIONS.get("volume_percentage") > 0.1F) {
            library.playAcoustic(association.source(), association.wetAcousticNames(), event, options.and(Options.WET_VOLUME_OPTIONS));
        }

        if (association.hasFoliageSound() && Options.FOLIAGE_VOLUME_OPTIONS.get("volume_percentage") > 0.1F) {
            library.playAcoustic(association.source(), association.foliageAcousticNames(), event, options.and(Options.FOLIAGE_VOLUME_OPTIONS));
        }
    }

    @Override
    public void playSound(LivingEntity location, String soundName, float volume, float pitch, Options options) {
        if (options.containsKey("delay_min") && options.containsKey("delay_max")) {
            delayedPlayer.playSound(location, soundName, volume, pitch, options);
            return;
        }
        immediatePlayer.playSound(location, soundName, volume, pitch, options);
    }
}
