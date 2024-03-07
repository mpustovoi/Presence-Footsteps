package eu.ha3.presencefootsteps.world;

import java.util.Objects;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.LivingEntity;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.math.BlockPos;

public record Association (
        BlockState state,
        BlockPos pos,
        LivingEntity source,

        String acousticNames,
        String wetAcousticNames
) {
    public static final Association NOT_EMITTER = new Association(Blocks.AIR.getDefaultState(), BlockPos.ORIGIN, null, Emitter.NOT_EMITTER, Emitter.NOT_EMITTER);

    public static Association of(BlockState state, BlockPos pos, LivingEntity source, String dry, String wet) {
        if (Emitter.isResult(dry) || Emitter.isResult(wet)) {
            return new Association(state, pos, source, dry, wet);
        }
        return NOT_EMITTER;
    }

    public boolean isNull() {
        return this == NOT_EMITTER;
    }

    public boolean isNotEmitter() {
        return isNull() || (Emitter.isNonEmitter(acousticNames) && Emitter.isNonEmitter(wetAcousticNames));
    }

    public boolean hasAssociation() {
        return !isNotEmitter() && isResult();
    }

    public BlockSoundGroup soundGroup() {
        return state.getSoundGroup();
    }

    public boolean isResult() {
        return Emitter.isResult(acousticNames) || Emitter.isResult(wetAcousticNames);
    }

    public boolean dataEquals(Association other) {
        return hasAssociation() && Objects.equals(acousticNames, other.acousticNames);
    }
}
