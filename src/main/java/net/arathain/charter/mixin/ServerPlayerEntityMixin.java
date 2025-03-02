package net.arathain.charter.mixin;

import com.mojang.authlib.GameProfile;
import net.arathain.charter.block.CharterStoneBlock;
import net.arathain.charter.block.WaystoneBlock;
import net.arathain.charter.block.particle.BindingAmbienceParticleEffect;
import net.arathain.charter.components.CharterComponent;
import net.arathain.charter.components.CharterComponents;
import net.arathain.charter.entity.Indebted;
import net.arathain.charter.entity.SlowFallEntity;
import net.arathain.charter.util.CharterUtil;
import net.minecraft.block.BlockState;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.encryption.PlayerPublicKey;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.UUID;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin extends PlayerEntity implements Indebted {
    public abstract ServerWorld getWorld();

    private UUID CharterUUID;

    public ServerPlayerEntityMixin(World world, BlockPos pos, float yaw, GameProfile profile, PlayerPublicKey key) {
        super(world, pos, yaw, profile);
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void timck(CallbackInfo ci) {
        if (CharterComponents.CHARTER_OWNER_COMPONENT.get(this).isShade()) {
            Vec3d rotVec = ((SlowFallEntity) this).charter$isSlowFalling() ? this.getRotationVec(1).multiply(0.5) : new Vec3d(0, 0, 0);
            if (((SlowFallEntity) this).charter$isSlowFalling()) {
                Vec3d eye1RotVec = this.getRotationVec(1).rotateY(15).multiply(0.2);
                Vec3d eye2RotVec = this.getRotationVec(1).rotateY(-15).multiply(0.2);
                this.getWorld().spawnParticles(new BindingAmbienceParticleEffect(1.0f, 1.0f, 1.0f, 0.0f, 0.0f, 0.0f, 0.09f), this.getX() + eye1RotVec.x, this.getEyeY() + eye1RotVec.y, this.getZ() + eye1RotVec.z, 1, 0, 0, 0, 0);
                this.getWorld().spawnParticles(new BindingAmbienceParticleEffect(1.0f, 1.0f, 1.0f, 0.0f, 0.0f, 0.0f, 0.09f), this.getX() + eye2RotVec.x, this.getEyeY() + eye2RotVec.y, this.getZ() + eye2RotVec.z, 1, 0, 0, 0, 0);
            }
            for (int i = 0; i < 16; i++) {
                this.getWorld().spawnParticles(ParticleTypes.LARGE_SMOKE, this.getX() - rotVec.x, this.getEyeY() - rotVec.y, this.getZ() - rotVec.z, 2, -this.getVelocity().x, -this.getVelocity().y, -this.getVelocity().z, 0.01);
            }
        }
    }

    @Inject(method = "onDeath", at = @At("HEAD"))
    private void death(DamageSource source, CallbackInfo ci) {
        CharterComponent charter = CharterUtil.getCharterAtPos(this.getPos(), this.getWorld());
        if (charter != null) {
            if (this.getUuid().equals(charter.getCharterOwnerUuid())) {
                charter.getAreas().forEach(area -> {
                    if (area.contains(this.getPos())) {
                        BlockState state = getWorld().getBlockState(new BlockPos((int) area.getCenter().x, (int) area.getCenter().y, (int) area.getCenter().z));
                        if (state.getBlock() instanceof WaystoneBlock) {
                            getWorld().breakBlock(new BlockPos((int) area.getCenter().x, (int) area.getCenter().y, (int) area.getCenter().z), false);
                        }
                        if (state.getBlock() instanceof CharterStoneBlock) {
                            getWorld().breakBlock(new BlockPos((int) area.getCenter().x, (int) area.getCenter().y, (int) area.getCenter().z), false);
                        }
                    }
                });
            } else {
                charter.getMembers().forEach(member -> {
                    if (member.equals(uuid)) {
                        charter.getAreas().forEach(area -> {
                            if (area.contains(this.getPos())) {
                                BlockState state = getWorld().getBlockState(new BlockPos((int) area.getCenter().x, (int) area.getCenter().y, (int) area.getCenter().z));
                                if (state.getBlock() instanceof WaystoneBlock) {
                                    getWorld().breakBlock(new BlockPos((int) area.getCenter().x, (int) area.getCenter().y, (int) area.getCenter().z), false);
                                }
                            }
                        });
                    }
                });

            }
        }
    }

    @Nullable
    @Override
    public UUID charter$getCharterUUID() {
        return CharterUUID;
    }

    @Override
    public void charter$setCharterUUID(UUID charterUUID) {
        CharterUUID = charterUUID;
    }
}
