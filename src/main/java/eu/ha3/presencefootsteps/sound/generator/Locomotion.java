package eu.ha3.presencefootsteps.sound.generator;

import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import eu.ha3.presencefootsteps.sound.SoundEngine;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;

public enum Locomotion {
    NONE,
    BIPED((entity, engine) -> new TerrestrialStepSoundGenerator(entity, engine, new Modifier<>())),
    QUADRUPED((entity, engine) -> new TerrestrialStepSoundGenerator(entity, engine, new QuadrupedModifier())),
    FLYING((entity, engine) -> new WingedStepSoundGenerator(entity, engine, new QuadrupedModifier())),
    FLYING_BIPED((entity, engine) -> new WingedStepSoundGenerator(entity, engine, new Modifier<>()));

    private static final Map<String, Locomotion> registry = new Object2ObjectOpenHashMap<>();

    static {
        for (Locomotion i : values()) {
            registry.put(i.name(), i);
            registry.put(String.valueOf(i.ordinal()), i);
        }
    }

    private final BiFunction<LivingEntity, SoundEngine, Optional<StepSoundGenerator>> constructor;

    private static final String AUTO_TRANSLATION_KEY = "menu.pf.stance.auto";
    private final String translationKey = "menu.pf.stance." + name().toLowerCase();

    Locomotion() {
        constructor = (entity, engine) -> Optional.empty();
    }

    Locomotion(BiFunction<LivingEntity, SoundEngine, StepSoundGenerator> gen) {
        constructor = (entity, engine) -> Optional.of(gen.apply(entity, engine));
    }

    public Optional<StepSoundGenerator> supplyGenerator(LivingEntity entity, SoundEngine engine) {
        return constructor.apply(entity, engine);
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
