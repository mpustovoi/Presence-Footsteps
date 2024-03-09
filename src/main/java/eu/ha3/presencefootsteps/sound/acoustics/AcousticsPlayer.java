package eu.ha3.presencefootsteps.sound.acoustics;

import eu.ha3.presencefootsteps.PresenceFootsteps;
import eu.ha3.presencefootsteps.sound.Options;
import eu.ha3.presencefootsteps.sound.State;
import eu.ha3.presencefootsteps.sound.player.SoundPlayer;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import eu.ha3.presencefootsteps.world.SoundsKey;
import net.minecraft.entity.LivingEntity;

import java.util.Map;

public class AcousticsPlayer implements AcousticLibrary {
    private final Map<String, Acoustic> acoustics = new Object2ObjectOpenHashMap<>();

    private final SoundPlayer player;

    public AcousticsPlayer(SoundPlayer player) {
        this.player = player;
    }

    @Override
    public void addAcoustic(String name, Acoustic acoustic) {
        if (acoustics.put(name, acoustic) != null) {
            PresenceFootsteps.logger.info("Duplicate acoustic: " + name);
        }
    }

    @Override
    public void playAcoustic(LivingEntity location, SoundsKey sounds, State event, Options inputOptions) {
        for (String acousticName : sounds.names()) {
            Acoustic acoustic = acoustics.get(acousticName);
            if (acoustic == null) {
                PresenceFootsteps.logger.warn("Tried to play a missing acoustic: " + acousticName);
            } else {
                acoustic.playSound(player, location, event, inputOptions);
            }
        }
    }
}