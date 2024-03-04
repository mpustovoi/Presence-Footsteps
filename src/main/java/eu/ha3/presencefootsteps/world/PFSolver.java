package eu.ha3.presencefootsteps.world;

import eu.ha3.presencefootsteps.compat.ContraptionCollidable;
import eu.ha3.presencefootsteps.sound.Options;
import eu.ha3.presencefootsteps.sound.SoundEngine;
import eu.ha3.presencefootsteps.sound.State;
import eu.ha3.presencefootsteps.util.PlayerUtil;
import net.minecraft.block.BlockState;
import net.minecraft.client.network.OtherClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.World;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Locale;

public class PFSolver implements Solver {
    private static final Logger LOGGER = LogManager.getLogger("PFSolver");

    private static final double TRAP_DOOR_OFFSET = 0.1;

    private final SoundEngine engine;

    private boolean wasGolem = false;

    public PFSolver(SoundEngine engine) {
        this.engine = engine;
    }

    private BlockState getBlockStateAt(Entity entity, BlockPos pos) {
        World world = entity.getWorld();
        BlockState state = world.getBlockState(pos);

        if (state.isAir() && (entity instanceof ContraptionCollidable collidable)) {
            state = collidable.getCollidedStateAt(pos);
        }
        return state;
    }

    private Box getCollider(Entity player) {
        Box collider = player.getBoundingBox();
        // normalize to the bottom of the block
        // so we can detect carpets on top of fences
        collider = collider.offset(0, -(collider.minY - Math.floor(collider.minY)), 0);

        double expansionRatio = 0.1;

        // add buffer
        collider = collider.expand(expansionRatio);
        if (player.isSprinting()) {
            collider = collider.expand(0.3, 0.5, 0.3);
        }
        return collider;
    }

    @Override
    public boolean playAssociation(LivingEntity ply, Association assos, State eventType) {
        if (!assos.isResult()) {
            return false;
        }

        if (!assos.isNotEmitter()) {
            assos = assos.at(ply);

            if (assos.hasAssociation()) {
                engine.getIsolator().acoustics().playAcoustic(assos, eventType, Options.EMPTY);
            } else {
                engine.getIsolator().stepPlayer().playStep(assos);
            }
        }

        return true;
    }

    @Override
    public Association findAssociation(LivingEntity ply, BlockPos pos, String strategy) {
        if (!MESSY_FOLIAGE_STRATEGY.equals(strategy)) {
            return Association.NOT_EMITTER;
        }

        BlockState above = getBlockStateAt(ply, pos.up());

        String foliage = engine.getIsolator().blocks().getAssociation(above, Substrates.FOLIAGE);

        if (!Emitter.isEmitter(foliage)) {
            return Association.NOT_EMITTER;
        }

        // we discard the normal block association, and mark the foliage as detected
        if (Emitter.MESSY_GROUND.equals(engine.getIsolator().blocks().getAssociation(above, Substrates.MESSY))) {
            return new Association(above, pos.up()).withDry(foliage);
        }

        return Association.NOT_EMITTER;
    }

    @Override
    public Association findAssociation(LivingEntity ply, double verticalOffsetAsMinus, boolean isRightFoot) {

        double rot = Math.toRadians(MathHelper.wrapDegrees(ply.getYaw()));

        Vec3d pos = ply.getPos();

        float feetDistanceToCenter = 0.2f * (isRightFoot ? -1 : 1)
                * PlayerUtil.getScale(ply) // scale foot offset by the player's scale
        ;

        BlockPos footPos = BlockPos.ofFloored(
            pos.x + Math.cos(rot) * feetDistanceToCenter,
            ply.getBoundingBox().getMin(Axis.Y) - TRAP_DOOR_OFFSET - verticalOffsetAsMinus,
            pos.z + Math.sin(rot) * feetDistanceToCenter
        );

        if (!(ply instanceof OtherClientPlayerEntity)) {
            Vec3d vel = ply.getVelocity();

            if (vel.lengthSquared() != 0 && Math.abs(vel.y) < 0.004) {
                return Association.NOT_EMITTER; // Don't play sounds on every tiny bounce
            }
        }

        Box collider = getCollider(ply);

        BlockPos.Mutable mutableFootPos = footPos.mutableCopy();

        if (feetDistanceToCenter > 1) {
            for (BlockPos underfootPos : BlockPos.iterateOutwards(footPos, (int)feetDistanceToCenter, 2, (int)feetDistanceToCenter)) {
                mutableFootPos.set(underfootPos);
                Association assos = findAssociation(ply, collider, underfootPos, mutableFootPos);
                if (assos.hasAssociation()) {
                    return assos;
                }
            }
        }

        return findAssociation(ply, collider, footPos, mutableFootPos);
    }

    private Association findAssociation(Entity player, Box collider, BlockPos originalFootPos, BlockPos.Mutable pos) {
        Association worked = findAssociation(player, pos, collider);

        // If it didn't work, the player has walked over the air on the border of a block.
        // ------ ------ --> z
        // | o | < player is here
        // wool | air |
        // ------ ------
        // |
        // V z
        if (!worked.isNull()) {
            return worked;
        }

        pos.set(originalFootPos);
        // Create a trigo. mark contained inside the block the player is over
        double xdang = (player.getX() - pos.getX()) * 2 - 1;
        double zdang = (player.getZ() - pos.getZ()) * 2 - 1;
        // -1 0 1
        // ------- -1
        // | o |
        // | + | 0 --> x
        // | |
        // ------- 1
        // |
        // V z

        // If the player is at the edge of that
        if (Math.max(Math.abs(xdang), Math.abs(zdang)) <= 0.2f) {
            return worked;
        }
        // Find the maximum absolute value of X or Z
        boolean isXdangMax = Math.abs(xdang) > Math.abs(zdang);
        // --------------------- ^ maxofZ-
        // | . . |
        // | . . |
        // | o . . |
        // | . . |
        // | . |
        // < maxofX- maxofX+ >
        // Take the maximum border to produce the sound
            // If we are in the positive border, add 1, else subtract 1
        worked = findAssociation(player, isXdangMax
                ? pos.move(Direction.EAST, xdang > 0 ? 1 : -1)
                : pos.move(Direction.SOUTH, zdang > 0 ? 1 : -1), collider);

        // If that didn't work, then maybe the footstep hit in the
        // direction of walking
        // Try with the other closest block
        if (!worked.isNull()) {
            return worked;
        }

        pos.set(originalFootPos);
        // Take the maximum direction and try with the orthogonal direction of it
        return findAssociation(player, isXdangMax
                ? pos.move(Direction.SOUTH, zdang > 0 ? 1 : -1)
                : pos.move(Direction.EAST, xdang > 0 ? 1 : -1), collider);
    }

    private Association findAssociation(Entity entity, BlockPos.Mutable pos, Box collider) {
        return findAssociation((p, state, substrate) -> {
            List<Entity> golems = entity.getWorld().getEntitiesByClass(Entity.class, new Box(p).expand(0.5, 0, 0.5), e -> !(e instanceof PlayerEntity));

            if (!golems.isEmpty()) {
                String golem = engine.getIsolator().golems().getAssociation(golems.get(0).getType(), substrate);

                if (Emitter.isEmitter(golem)) {
                    LOGGER.debug("Golem detected: " + golem);
                    wasGolem = true;
                    return golem;
                }
            }

            if (state.isAir()) {
                return Emitter.UNASSIGNED;
            }

            String association = engine.getIsolator().blocks().getAssociation(state, substrate);
            if (Emitter.isResult(association)) {
                return association;
            }

            BlockSoundGroup sounds = state.getSoundGroup();
            substrate = String.format(Locale.ENGLISH, "%.2f_%.2f", sounds.volume, sounds.pitch);
            return engine.getIsolator().primitives().getAssociation(sounds, substrate);
        }, entity, pos, collider);
    }

    private Association findAssociation(Associations associations, Entity entity, BlockPos.Mutable pos, Box collider) {
        wasGolem = false;
        BlockState in = getBlockStateAt(entity, pos);

        // Try to see if the block above is a carpet...
        pos.move(Direction.UP);
        boolean hasRain = entity.getWorld().hasRain(pos);
        BlockState above = getBlockStateAt(entity, pos);
        String association = associations.getAssociation(pos, above, Substrates.CARPET);

        if (Emitter.isEmitter(association)) {
            LOGGER.debug("Carpet detected: " + association);
            in = above;
        } else {
            pos.move(Direction.DOWN);
            // This condition implies that if the carpet is NOT_EMITTER, solving will
            // CONTINUE with the actual block surface the player is walking on
            if (in.isAir()) {
                pos.move(Direction.DOWN);
                BlockState below = getBlockStateAt(entity, pos);

                association = associations.getAssociation(pos, below, Substrates.FENCE);

                if (Emitter.isResult(association)) {
                    LOGGER.debug("Fence detected: " + association);
                    in = below;
                } else {
                    pos.move(Direction.UP);
                }
            }

            VoxelShape shape = in.getCollisionShape(entity.getWorld(), pos);
            if (shape.isEmpty()) {
                shape = in.getOutlineShape(entity.getWorld(), pos);
            }
            if (!shape.isEmpty() && !shape.getBoundingBox().offset(pos).intersects(collider)) {
                LOGGER.debug("Skipping due to hitbox miss");
                return Association.NOT_EMITTER;
            }

            if (!Emitter.isResult(association)) {
                association = associations.getAssociation(pos, in, Substrates.DEFAULT);
            }

            if (Emitter.isEmitter(association)) {
                // This condition implies that foliage over a NOT_EMITTER block CANNOT PLAY
                // This block most not be executed if the association is a carpet
                String foliage = associations.getAssociation(pos.move(Direction.UP), above, Substrates.FOLIAGE);
                pos.move(Direction.DOWN);

                if (Emitter.isEmitter(foliage)) {
                    LOGGER.debug("Foliage detected: " + foliage);
                    association += "," + foliage;
                }
            }
        }

        String wetAssociation = Emitter.NOT_EMITTER;

        if (Emitter.isEmitter(association) && (hasRain || (!wasGolem && (in.getFluidState().isIn(FluidTags.WATER) || above.getFluidState().isIn(FluidTags.WATER))))) {
            // Only if the block is open to the sky during rain
            // or the block is submerged
            // or the block is waterlogged
            // then append the wet effect to footsteps
            String wet = associations.getAssociation(pos, in, Substrates.WET);

            if (Emitter.isEmitter(wet)) {
                LOGGER.debug("Wet block detected: " + wet);
                wetAssociation = wet;
            }
        }

        // Player has stepped on a non-emitter block as defined in the blockmap
        if (Emitter.isNonEmitter(association) && Emitter.isNonEmitter(wetAssociation)) {
            return Association.NOT_EMITTER;
        }

        if (Emitter.isResult(association)) {
            return new Association(in, pos).withDry(association).withWet(wetAssociation);
        }

        return Association.NOT_EMITTER;
    }

}
