package eu.ha3.presencefootsteps.sound.generator;

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import eu.ha3.presencefootsteps.sound.SoundEngine;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;

public enum Locomotion {
    NONE,
    BIPED(engine -> new TerrestrialStepSoundGenerator(engine, new Modifier<>())),
    QUADRUPED(engine -> new TerrestrialStepSoundGenerator(engine, new QuadrupedModifier())),
    FLYING(engine -> new WingedStepSoundGenerator(engine, new QuadrupedModifier())),
    FLYING_BIPED(engine -> new WingedStepSoundGenerator(engine, new Modifier<>()));

    private static final Map<String, Locomotion> registry = new Object2ObjectOpenHashMap<>();

    static {
        for (Locomotion i : values()) {
            registry.put(i.name(), i);
            registry.put(String.valueOf(i.ordinal()), i);
        }
    }

    private final Function<SoundEngine, Optional<StepSoundGenerator>> constructor;

    private static final String AUTO_TRANSLATION_KEY = "menu.pf.stance.auto";
    private final String translationKey = "menu.pf.stance." + name().toLowerCase();

    Locomotion() {
        constructor = engine -> Optional.empty();
    }

    Locomotion(Function<SoundEngine, StepSoundGenerator> gen) {
        constructor = engine -> Optional.of(gen.apply(engine));
    }

    public Optional<StepSoundGenerator> supplyGenerator(SoundEngine engine) {
        return constructor.apply(engine);
    }

    public Text getOptionName() {
        return Text.translatable("menu.pf.stance", Text.translatable(this == NONE ? AUTO_TRANSLATION_KEY : translationKey));
    }

    public Text getOptionTooltip() {
        return Text.translatable(translationKey + ".tooltip");
    }

    public String getDisplayName() {
        return I18n.translate("pf.stance", I18n.translate(translationKey));
    }

    public static Locomotion byName(String name) {
        return registry.getOrDefault(name, BIPED);
    }

    public static Locomotion forLiving(Entity entity, Locomotion fallback) {
        if (MineLP.hasPonies()) {
            return MineLP.getLocomotion(entity, fallback);
        }

        return fallback;
    }

    public static Locomotion forPlayer(PlayerEntity ply, Locomotion preference) {
        if (preference == NONE) {
            if (ply instanceof ClientPlayerEntity && MineLP.hasPonies()) {
                return MineLP.getLocomotion(ply);
            }

            return Locomotion.BIPED;
        }

        return preference;
    }
}
