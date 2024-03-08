package eu.ha3.presencefootsteps.world;

import net.minecraft.entity.EntityType;
import net.minecraft.registry.Registries;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.Identifier;

import java.io.IOException;
import java.util.Map;
import eu.ha3.presencefootsteps.util.JsonObjectWriter;

public class GolemLookup extends AbstractSubstrateLookup<EntityType<?>> {
    @Override
    public String getAssociation(EntityType<?> key, String substrate) {
        return getSubstrateMap(getId(key), substrate).getOrDefault(EntityType.getId(key), Emitter.UNASSIGNED);
    }

    @Override
    protected Identifier getId(EntityType<?> key) {
        return EntityType.getId(key);
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
