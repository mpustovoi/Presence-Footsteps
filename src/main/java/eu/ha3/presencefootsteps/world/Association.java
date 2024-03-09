package eu.ha3.presencefootsteps.world;

import java.util.Objects;

import org.jetbrains.annotations.Nullable;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.LivingEntity;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.math.BlockPos;

public record Association (
        BlockState state,
        BlockPos pos,
        @Nullable LivingEntity source,

        String acousticNames,
        String wetAcousticNames,
        String foliageAcousticNames
) {
    public static final Association NOT_EMITTER = new Association(Blocks.AIR.getDefaultState(), BlockPos.ORIGIN, null, Emitter.NOT_EMITTER, Emitter.NOT_EMITTER, Emitter.NOT_EMITTER);

    public static Association of(BlockState state, BlockPos pos, LivingEntity source, String dry, String wet, String foliage) {
        if (Emitter.isResult(dry) || Emitter.isResult(wet) || Emitter.isResult(foliage)) {
            return new Association(state, pos.toImmutable(), source, dry, wet, foliage);
        }
        return new Association(state, pos.toImmutable(), source, Emitter.UNASSIGNED, Emitter.UNASSIGNED, Emitter.UNASSIGNED);
    }

    public boolean isNull() {
        return !Emitter.isResult(acousticNames)
            && !Emitter.isResult(wetAcousticNames)
            && !Emitter.isResult(foliageAcousticNames);
    }

    public boolean hasCoreSound() {
        return Emitter.isResult(acousticNames);
    }

    public boolean hasWetSound() {
        return Emitter.isEmitter(wetAcousticNames);
    }

    public boolean hasFoliageSound() {
        return Emitter.isEmitter(foliageAcousticNames);
    }

    public boolean isSilent() {
        return this == NOT_EMITTER || (
               Emitter.isNotEmitter(acousticNames)
            && Emitter.isNotEmitter(wetAcousticNames)
            && Emitter.isNotEmitter(foliageAcousticNames)
        );
    }

    public BlockSoundGroup soundGroup() {
        return state.getSoundGroup();
    }

    public boolean dataEquals(Association other) {
        return Objects.equals(acousticNames, other.acousticNames);
    }
}
