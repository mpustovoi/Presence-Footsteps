package eu.ha3.presencefootsteps.mixins;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import eu.ha3.presencefootsteps.PresenceFootsteps;
import eu.ha3.presencefootsteps.api.DerivedBlock;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.StairsBlock;
import net.minecraft.block.AbstractBlock.Settings;
import net.minecraft.block.Block;


@Mixin(Block.class)
abstract class MAbstractBlock extends AbstractBlock implements DerivedBlock {
    MAbstractBlock() { super(null); }

    @Override
    public BlockState getBaseBlockState() {
        Block baseBlock = ((DerivedBlock.Settings)settings).getBaseBlock();
        if (baseBlock == null) {
            baseBlock = PresenceFootsteps.getInstance().getEngine().getIsolator().heuristics().getMostSimilar((Block)(Object)this);
        }
        return (baseBlock == null ? Blocks.AIR : baseBlock).getDefaultState();
    }
}

@Mixin(StairsBlock.class)
abstract class MStairsBlock implements DerivedBlock {
    @Accessor("baseBlockState")
    @Override
    public abstract BlockState getBaseBlockState();
}

@Mixin(Settings.class)
abstract class MBlockSettings implements DerivedBlock.Settings {
    @Nullable
    private Block baseBlock;

    @Override
    public void setBaseBlock(Block baseBlock) {
        this.baseBlock = baseBlock;
    }

    @Override
    @Nullable
    public Block getBaseBlock() {
        return baseBlock;
    }

    @Inject(method = "copy", at = @At("RETURN"))
    private static void onCopy(AbstractBlock block, CallbackInfoReturnable<Settings> info) {
        if (block instanceof Block b) {
            ((DerivedBlock.Settings)info.getReturnValue()).setBaseBlock(b);
        }
    }
}

@Pseudo
@Mixin(targets = "net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings")
abstract class MFabricBlockSettings {
    @Inject(method = "copyOf(Lnet/minecraft/block/AbstractBlock;)Lnet/fabricmc/fabric/api/object/builder/v1/block/FabricBlockSettings;", at = @At("RETURN"), require = 0)
    private static void onCopyOf(AbstractBlock block, CallbackInfoReturnable<?> info) {
        if (block instanceof Block b) {
            ((DerivedBlock.Settings)info.getReturnValue()).setBaseBlock(b);
        }
    }

    @Inject(method = "copyOf(Lnet/minecraft/block/AbstractBlock$Settings;)Lnet/fabricmc/fabric/api/object/builder/v1/block/FabricBlockSettings;", at = @At("RETURN"), require = 0)
    private static void onCopyOf(AbstractBlock.Settings settings, CallbackInfoReturnable<?> info) {
        ((DerivedBlock.Settings)info.getReturnValue()).setBaseBlock(((DerivedBlock.Settings)settings).getBaseBlock());
    }
}