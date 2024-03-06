package eu.ha3.presencefootsteps;

import java.util.*;

import eu.ha3.presencefootsteps.api.DerivedBlock;
import eu.ha3.presencefootsteps.sound.SoundEngine;
import eu.ha3.presencefootsteps.sound.generator.Locomotion;
import eu.ha3.presencefootsteps.world.Emitter;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.registry.Registries;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.util.Formatting;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;

public class PFDebugHud {

    private final SoundEngine engine;

    private final List<String> list = new ArrayList<>();

    PFDebugHud(SoundEngine engine) {
        this.engine = engine;
    }

    public void render(HitResult blockHit, HitResult fluidHit, List<String> finalList) {
        MinecraftClient client = MinecraftClient.getInstance();

        list.add("");
        list.add(Formatting.UNDERLINE + "Presence Footsteps " + FabricLoader.getInstance().getModContainer("presencefootsteps").get().getMetadata().getVersion());

        PFConfig config = PresenceFootsteps.getInstance().getConfig();
        list.add(String.format("Enabled: %s, Multiplayer: %s", config.getEnabled(), config.getEnabledMP()));
        list.add(String.format("Volume: Global: %s%%, W: %s%%, Entities[H: %s%%, P: %s%%], Players[U: %s%%, T: %s%% ]",
                config.getGlobalVolume(),
                config.getWetSoundsVolume(),
                config.getHostileEntitiesVolume(),
                config.getPassiveEntitiesVolume(),
                config.getClientPlayerVolume(),
                config.getOtherPlayerVolume()
        ));
        list.add(String.format("Stepping Mode: %s, Targeting Mode: %s", config.getLocomotion() == Locomotion.NONE
                ? String.format("AUTO (%sDETECTED %s%s)", Formatting.BOLD, Locomotion.forPlayer(client.player, Locomotion.BIPED), Formatting.RESET)
                : config.getLocomotion().toString(), config.getEntitySelector()));

        insertAt(list, finalList, "Targeted Block: ", -1);

        if (blockHit.getType() == HitResult.Type.BLOCK) {
            BlockPos pos = ((BlockHitResult)blockHit).getBlockPos();
            BlockState state = client.world.getBlockState(pos);

            list.add("");
            list.add(Formatting.UNDERLINE + "Targeted Block Sounds Like");
            BlockState base = DerivedBlock.getBaseOf(state);
            if (!base.isAir()) {
                list.add(Registries.BLOCK.getId(base.getBlock()).toString());
            }
            list.add(String.format(Locale.ENGLISH, "Primitive Key: %s@%.2f_%.2f",
                    state.getSoundGroup().getStepSound().getId(),
                    state.getSoundGroup().volume,
                    state.getSoundGroup().pitch
            ));
            boolean hasRain = client.world.hasRain(pos) || state.getFluidState().isIn(FluidTags.WATER) || client.world.getBlockState(pos.up()).getFluidState().isIn(FluidTags.WATER);
            list.add("Has Wet Sound: " + hasRain);
            renderSoundList("Step Sounds[B]", engine.getIsolator().blocks().getAssociations(state), list);
            renderSoundList("Step Sounds[P]", engine.getIsolator().primitives().getAssociations(state.getSoundGroup()), list);
            list.add("");

            insertAt(list, finalList, "Targeted Block: ", 1);
        }

        if (client.crosshairTarget instanceof EntityHitResult ehr && ehr.getEntity() != null) {
            list.add(String.format("Targeted Entity Step Mode: %s", engine.getIsolator().locomotions().lookup(ehr.getEntity())));
            renderSoundList("Step Sounds[G]", engine.getIsolator().golems().getAssociations(ehr.getEntity().getType()), list);
            insertAt(list, finalList, "Targeted Entity", 2);
        }
    }

    private static void insertAt(List<String> values, List<String> destination, String target, int offset) {
        int i = 0;
        for (; i < destination.size(); i++) {
            if (destination.get(i).indexOf(target) != -1) {
                break;
            }
        }
        if (i < destination.size()) {
            destination.addAll(MathHelper.clamp(i + offset, 0, destination.size() - 1), values);
        } else {
            destination.addAll(values);
        }
        values.clear();
    }

    private void renderSoundList(String title, Map<String, String> sounds, List<String> list) {
        if (sounds.isEmpty()) {
            return;
        }
        StringBuilder combinedList = new StringBuilder(Formatting.UNDERLINE + title + Formatting.RESET + ": [ ");
        boolean first = true;
        for (var entry : sounds.entrySet()) {
            if (!first) {
                combinedList.append(" / ");
            }
            first = false;

            if (!entry.getKey().isEmpty()) {
                combinedList.append(entry.getKey()).append(":");
            }
            combinedList.append(entry.getValue());
        }
        combinedList.append(" ]");
        list.add(combinedList.toString());

        if (!list.isEmpty()) {
            return;
        }

        if (sounds.isEmpty()) {
            list.add(Emitter.UNASSIGNED);
        } else {
            sounds.forEach((key, value) -> {
                list.add((key.isEmpty() ? "default" : key) + ": " + value);
            });
        }
    }
}
