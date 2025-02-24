package net.arathain.charter.block.entity;

import net.arathain.charter.Charter;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animatable.instance.InstancedAnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;

public class CharterStoneEntity extends BlockEntity implements GeoAnimatable {
    private final AnimatableInstanceCache factory = new InstancedAnimatableInstanceCache(this);
    public static final RawAnimation IDLE = RawAnimation.begin().thenPlay("animation.model.idle");

    public CharterStoneEntity(BlockPos pos, BlockState state) {
        super(Charter.CHARTER_STONE_ENTITY, pos, state);
    }


    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
    }

    @Override
    public void writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar animationData) {
        animationData.add(new AnimationController<>(this, "controller", 2, animationEvent -> {
            RawAnimation anime = IDLE;
            animationEvent.getController().setAnimation(anime);
            return PlayState.CONTINUE;
        }));

    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return factory;
    }

    @Override
    public double getTick(Object object) {
        return 0;
    }

}
