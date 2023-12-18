package eu.ha3.presencefootsteps.sound.acoustics;

import eu.ha3.presencefootsteps.sound.Options;
import eu.ha3.presencefootsteps.sound.State;
import eu.ha3.presencefootsteps.sound.player.SoundPlayer;
import net.minecraft.entity.LivingEntity;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * An acoustic that can play different acoustics depending on a specific event type.
 *
 * @author Hurry
 */
record EventSelectorAcoustics(Map<State, Acoustic> pairs) implements Acoustic {
    static final Serializer FACTORY = Serializer.ofJsObject((json, context) -> new EventSelectorAcoustics(Arrays.stream(State.values())
        .filter(i -> json.has(i.getName()))
        .collect(Collectors.toMap(
                Function.identity(),
                i -> Acoustic.read(context, json.get(i.getName()))
        ))));

    @Override
    public void playSound(SoundPlayer player, LivingEntity location, State event, Options inputOptions) {
        if (pairs.containsKey(event)) {
            pairs.get(event).playSound(player, location, event, inputOptions);
        } else if (event.canTransition()) {
            playSound(player, location, event.getTransitionDestination(), inputOptions);
            // the possibility of a resonance cascade scenario is extremely unlikely
        }
    }
}