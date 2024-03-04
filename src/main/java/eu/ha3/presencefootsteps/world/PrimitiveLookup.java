package eu.ha3.presencefootsteps.world;

import java.io.IOException;
import java.util.Locale;
import java.util.Map;
import eu.ha3.presencefootsteps.util.JsonObjectWriter;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.Identifier;

public class PrimitiveLookup extends AbstractSubstrateLookup<BlockSoundGroup> {
    @Override
    protected Identifier getId(BlockSoundGroup key) {
        return key.getStepSound().getId();
    }

    @Override
    public void writeToReport(boolean full, JsonObjectWriter writer, Map<String, BlockSoundGroup> groups) throws IOException {
        writer.each(groups.values(), group -> {
            String substrate = String.format(Locale.ENGLISH, "%.2f_%.2f", group.volume, group.pitch);
            if (full || !contains(group)) {
                writer.field(group.getStepSound().getId().toString() + "@" + substrate, getAssociation(group, substrate));
            }
        });
    }
}
