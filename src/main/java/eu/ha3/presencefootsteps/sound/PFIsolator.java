package eu.ha3.presencefootsteps.sound;

import java.io.IOException;
import java.util.Map;

import eu.ha3.presencefootsteps.config.Variator;
import eu.ha3.presencefootsteps.sound.player.StepSoundPlayer;
import eu.ha3.presencefootsteps.util.JsonObjectWriter;
import eu.ha3.presencefootsteps.sound.acoustics.AcousticLibrary;
import eu.ha3.presencefootsteps.sound.acoustics.AcousticsPlayer;
import eu.ha3.presencefootsteps.sound.generator.Locomotion;
import eu.ha3.presencefootsteps.sound.player.ImmediateSoundPlayer;
import eu.ha3.presencefootsteps.sound.player.SoundPlayer;
import eu.ha3.presencefootsteps.world.GolemLookup;
import eu.ha3.presencefootsteps.world.Index;
import eu.ha3.presencefootsteps.world.LocomotionLookup;
import eu.ha3.presencefootsteps.world.Lookup;
import eu.ha3.presencefootsteps.world.PFSolver;
import eu.ha3.presencefootsteps.world.PrimitiveLookup;
import eu.ha3.presencefootsteps.world.Solver;
import eu.ha3.presencefootsteps.world.StateLookup;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.sound.BlockSoundGroup;

public class PFIsolator implements Isolator {
    private final Variator variator = new Variator();

    private final Index<Entity, Locomotion> locomotionMap = new LocomotionLookup();

    private final Lookup<EntityType<?>> golemMap = new GolemLookup();

    private final Lookup<BlockState> blockMap = new StateLookup();

    private final Lookup<BlockSoundGroup> primitiveMap = new PrimitiveLookup();

    private final ImmediateSoundPlayer player;
    private final AcousticsPlayer acoustics;

    private final Solver solver = new PFSolver(this);

    public PFIsolator(SoundEngine engine) {
        this.player = new ImmediateSoundPlayer(engine);
        this.acoustics = new AcousticsPlayer(player);
    }

    @Override
    public AcousticLibrary getAcoustics() {
        return acoustics;
    }

    @Override
    public Solver getSolver() {
        return solver;
    }

    @Override
    public Index<Entity, Locomotion> getLocomotionMap() {
        return locomotionMap;
    }

    @Override
    public Lookup<EntityType<?>> getGolemMap() {
        return golemMap;
    }

    @Override
    public Lookup<BlockState> getBlockMap() {
        return blockMap;
    }

    @Override
    public Lookup<BlockSoundGroup> getPrimitiveMap() {
        return primitiveMap;
    }

    @Override
    public SoundPlayer getSoundPlayer() {
        return player;
    }

    @Override
    public StepSoundPlayer getStepPlayer() {
        return player;
    }

    @Override
    public Variator getVariator() {
        return variator;
    }

    @Override
    public void writeToReport(boolean full, JsonObjectWriter writer, Map<String, BlockSoundGroup> groups) throws IOException {
        writer.object(() -> {
            writer.object("blocks", () -> {
                getBlockMap().writeToReport(full, writer, groups);
            });
            writer.object("entities", () -> {
                getLocomotionMap().writeToReport(full, writer, groups);
            });
            writer.object("primitives", () -> {
                getPrimitiveMap().writeToReport(full, writer, groups);
            });
        });
    }
}
