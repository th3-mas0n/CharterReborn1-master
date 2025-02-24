package net.arathain.charter.block;


import net.arathain.charter.block.entity.AelpecyemIsCool;
import net.arathain.charter.block.entity.SoulVesselBlockEntity;
import net.arathain.charter.item.SoulVesselItem;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import org.jetbrains.annotations.Nullable;
import java.util.Objects;


public class SoulVesselBlock extends Block implements BlockEntityProvider, Waterloggable {
    public SoulVesselBlock(Settings settings) {
        super(settings);
        setDefaultState(getDefaultState().with(Properties.LIT, false).with(Properties.FACING, Direction.NORTH).with(Properties.WATERLOGGED, false));
    }


    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return createCuboidShape(4, 0, 4, 12, 23, 12);
    }


    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new SoulVesselBlockEntity(pos, state);
    }


    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        boolean client = world.isClient;
        if (!client) {
            ItemStack stack = player.getStackInHand(hand);
            SoulVesselBlockEntity press = (SoulVesselBlockEntity) world.getBlockEntity(pos);
            assert press != null;
            if (press.getSoulVessel() == ItemStack.EMPTY && SoulVesselItem.isViable(stack)) {
                world.setBlockState(pos, state.with(Properties.LIT, true));
                world.playSound(null, pos, SoundEvents.ITEM_FLINTANDSTEEL_USE, SoundCategory.BLOCKS, 1, 1);
                press.setStack(0, stack);
                press.markDirty();
                return ActionResult.CONSUME;
            } else if (!press.getItems().isEmpty() && stack.isEmpty() && SoulVesselItem.isViable(press.getSoulVessel())) {
                assert press.getSoulVessel() != null;
                world.spawnEntity(new ItemEntity(world, player.getX(), player.getY() + 0.5, player.getZ(), press.getSoulVessel()));
                ((AelpecyemIsCool) press).clear();
                press.removeStack(0);
                world.setBlockState(pos, state.with(Properties.LIT, false));
                return ActionResult.SUCCESS;
            }
        }
        return ActionResult.success(client);
    }


    @Override
    public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState newState, WorldAccess world, BlockPos pos, BlockPos posFrom) {
        if (state.contains(Properties.WATERLOGGED) && state.get(Properties.WATERLOGGED)) {
            world.scheduleFluidTick(pos, Fluids.WATER, Fluids.WATER.getTickRate(world));
        }
        return super.getStateForNeighborUpdate(state, direction, newState, world, pos, posFrom);
    }


    @Override
    public boolean tryFillWithFluid(WorldAccess world, BlockPos pos, BlockState state, FluidState fluidState) {
        if (!state.get(Properties.WATERLOGGED) && fluidState.getFluid() == Fluids.WATER) {
            if (!world.isClient()) {
                world.setBlockState(pos, state.with(Properties.WATERLOGGED, true), 3);
                world.scheduleFluidTick(pos, Fluids.WATER, Fluids.WATER.getTickRate(world));
            }
            return true;
        }
        return false;
    }


    @Override
    public FluidState getFluidState(BlockState state) {
        return state.contains(Properties.WATERLOGGED) && state.get(Properties.WATERLOGGED) ? Fluids.WATER.getStill(false) : super.getFluidState(state);
    }


    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        return (tickerWorld, pos, tickerState, blockEntity) -> SoulVesselBlockEntity.tick(tickerWorld, pos, tickerState, (SoulVesselBlockEntity) blockEntity);
    }


    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        final BlockState state = this.getDefaultState().with(Properties.FACING, ctx.getHorizontalPlayerFacing());
        if (state.contains(Properties.WATERLOGGED)) {
            final FluidState fluidState = ctx.getWorld().getFluidState(ctx.getBlockPos());
            final boolean source = fluidState.isIn(FluidTags.WATER) && fluidState.getLevel() == 8;
            return state.with(Properties.WATERLOGGED, source);
        }
        return state;
    }


    @Override
    public void afterBreak(World world, PlayerEntity player, BlockPos pos, BlockState state, @Nullable BlockEntity blockEntity, ItemStack stack) {
        SoulVesselBlockEntity press = (SoulVesselBlockEntity) blockEntity;
        if (press != null && press.getSoulVessel().getItem() instanceof SoulVesselItem && world.getPlayerByUuid(SoulVesselItem.getSoulUUID(press.getSoulVessel())) != null) {
            Objects.requireNonNull(world.getPlayerByUuid(SoulVesselItem.getSoulUUID(press.getSoulVessel()))).kill();
        }
        super.afterBreak(world, player, pos, state, blockEntity, stack);
    }


    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        super.appendProperties(builder.add(Properties.LIT, Properties.FACING, Properties.WATERLOGGED));
    }
}