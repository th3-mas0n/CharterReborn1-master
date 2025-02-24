package net.arathain.charter.block;

import net.arathain.charter.components.CharterComponent;
import net.arathain.charter.util.CharterUtil;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.util.math.random.Random;


public class CharterVesselBlock extends Block {
    public CharterVesselBlock(Settings settings) {
        super(settings);
        setDefaultState(getDefaultState().with(Properties.LIT, false).with(Properties.FACING, Direction.NORTH));
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return createCuboidShape(4, 0, 4, 12, 23, 12);
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        return this.getDefaultState().with(Properties.FACING, ctx.getHorizontalPlayerFacing());
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        super.appendProperties(builder.add(Properties.LIT, Properties.FACING, Properties.WATERLOGGED));
    }

    @Override
    public void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        CharterComponent charter = CharterUtil.getCharterAtPos(pos, world);
        if (charter != null) {
            charter.getAreas().forEach(area -> {
                BlockPos charterPos = new BlockPos((int) area.getCenter().x, (int) area.getCenter().y, (int) area.getCenter().z);
                if (charterPos.equals(charter.getCharterStonePos()) && charter.getUses() > 0) {
                    charter.incrementUses(-1000);
                    if (random.nextInt(420) == 2) {
                        if (state.get(Properties.LIT)) {
                            world.breakBlock(pos, false);
                        } else {
                            world.setBlockState(pos, state.with(Properties.LIT, true));
                        }
                    }
                    if (charter.getUses() < 0) {
                        charter.incrementUses(-charter.getUses());
                    }
                }
            });
        }
    }
}
