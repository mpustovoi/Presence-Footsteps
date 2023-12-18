package eu.ha3.presencefootsteps.sound.acoustics;

import eu.ha3.presencefootsteps.sound.Options;
import eu.ha3.presencefootsteps.sound.State;
import eu.ha3.presencefootsteps.sound.player.SoundPlayer;
import net.minecraft.entity.LivingEntity;

record ChanceAcoustic(
        Acoustic acoustic,
        float probability
) implements Acoustic {
    static final Serializer FACTORY = Serializer.ofJsObject((json, context) -> new ChanceAcoustic(
        Acoustic.read(context, json.get("acoustic")),
        json.get("probability").getAsFloat()
    ));

    @Override
    public void playSound(SoundPlayer player, LivingEntity location, State event, Options inputOptions) {
        final float rand = player.getRNG().nextFloat();

        if (rand * 100 <= probability) {
            acoustic.playSound(player, location, event, inputOptions);
        }
    }
}