package eu.ha3.presencefootsteps.world;

import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import net.minecraft.entity.EntityType;
import net.minecraft.registry.Registries;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.Identifier;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

import eu.ha3.presencefootsteps.util.JsonObjectWriter;

public class GolemLookup implements Lookup<EntityType<?>> {
    private final Map<String, Map<Identifier, String>> substrates = new Object2ObjectLinkedOpenHashMap<>();

    @Override
    public String getAssociation(EntityType<?> key, String substrate) {
        Map<Identifier, String> primitives = substrates.get(substrate);

        if (primitives == null) {
            // Check for default
            primitives = substrates.get(EMPTY_SUBSTRATE);
        }

        if (primitives == null) {
            return Emitter.UNASSIGNED;
        }

        return primitives.getOrDefault(EntityType.getId(key), Emitter.UNASSIGNED);
    }

    @Override
    public Set<String> getSubstrates() {
        return substrates.keySet();
    }

    @Override
    public void add(String key, String value) {
        final String[] split = key.trim().split("@");
        final String primitive = split[0];
        final String substrate = split.length > 1 ? split[1] : EMPTY_SUBSTRATE;

        substrates
            .computeIfAbsent(substrate, s -> new Object2ObjectLinkedOpenHashMap<>())
            .put(new Identifier(primitive), value);
    }

    @Override
    public boolean contains(EntityType<?> key) {
        final Identifier primitive = EntityType.getId(key);

        for (Map<Identifier, String> primitives : substrates.values()) {
            if (primitives.containsKey(primitive)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void writeToReport(boolean full, JsonObjectWriter writer, Map<String, BlockSoundGroup> groups) throws IOException {
        writer.each(Registries.ENTITY_TYPE, type -> {
            if (full || !contains(type)) {
                writer.object(EntityType.getId(type).toString(), () -> {
                    writer.object("associations", () -> {
                        getSubstrates().forEach(substrate -> {
                            try {
                                String association = getAssociation(type, substrate);
                                if (Emitter.isResult(association)) {
                                    writer.field(substrate, association);
                                }
                            } catch (IOException ignore) {}
                        });
                    });
                });
            }
        });
    }
}
