package eu.ha3.presencefootsteps.world;

import eu.ha3.presencefootsteps.PresenceFootsteps;
import eu.ha3.presencefootsteps.sound.SoundEngine;
import eu.ha3.presencefootsteps.sound.generator.Locomotion;
import eu.ha3.presencefootsteps.util.JsonObjectWriter;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.Registries;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.Identifier;

import java.io.IOException;
import java.util.Map;

public class LocomotionLookup implements Index<Entity, Locomotion> {
    private final Map<Identifier, Locomotion> values = new Object2ObjectLinkedOpenHashMap<>();

    private final SoundEngine engine;

    public LocomotionLookup(SoundEngine engine) {
        this.engine = engine;
    }

    @Override
    public Locomotion lookup(Entity key) {
        if (key instanceof PlayerEntity) {
            return Locomotion.forPlayer((PlayerEntity)key, engine.getConfig().getLocomotion());
        }
        return Locomotion.forLiving(key, values.getOrDefault(EntityType.getId(key.getType()), Locomotion.BIPED));
    }

    @Override
    public void add(String key, String value) {
        Identifier id = new Identifier(key);

        if (!Registries.ENTITY_TYPE.containsId(id)) {
            PresenceFootsteps.logger.warn("Locomotion registered for unknown entity type " + id);
        }

        values.put(id, Locomotion.byName(value.toUpperCase()));
    }

    @Override
    public boolean contains(Identifier key) {
        return values.containsKey(key);
    }

    @Override
    public void writeToReport(boolean full, JsonObjectWriter writer, Map<String, BlockSoundGroup> groups) throws IOException {
        writer.each(Registries.ENTITY_TYPE, type -> {
            Identifier id = EntityType.getId(type);
            if (full || !contains(id)) {
                if (type.create(MinecraftClient.getInstance().world) instanceof LivingEntity) {
                    writer.field(id.toString(), values.get(id).name());
                }
            }
        });
    }
}