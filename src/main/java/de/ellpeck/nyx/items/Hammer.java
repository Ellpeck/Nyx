package de.ellpeck.nyx.items;

import de.ellpeck.nyx.Nyx;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.server.SPacketBlockChange;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeHooks;

public class Hammer extends Pickaxe {
    public Hammer(ToolMaterial material) {
        super(material);
        this.setMaxDamage(5500);
        this.attackDamage = 15;
        this.attackSpeed = -3.2F;
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand handIn) {
        playerIn.setActiveHand(handIn);
        return ActionResult.newResult(EnumActionResult.SUCCESS, playerIn.getHeldItem(handIn));
    }

    @Override
    public void onPlayerStoppedUsing(ItemStack stack, World worldIn, EntityLivingBase entityLiving, int timeLeft) {
        if (!entityLiving.onGround)
            return;
        int useTime = this.getMaxItemUseDuration(stack) - timeLeft;
        if (useTime < 20)
            return;
        // we have to be looking up
        if (entityLiving.rotationPitch > -10)
            return;
        float modifier = MathHelper.clamp((useTime - 20) / 5F, 1, 2.5F);
        entityLiving.motionX += -modifier * MathHelper.sin(entityLiving.rotationYaw * 0.017453292F);
        entityLiving.motionY += 0.625 * modifier;
        entityLiving.motionZ += modifier * MathHelper.cos(entityLiving.rotationYaw * 0.017453292F);
        entityLiving.getEntityData().setLong(Nyx.ID + ":leap_start", worldIn.getTotalWorldTime());
    }

    @Override
    public EnumAction getItemUseAction(ItemStack stack) {
        return EnumAction.BOW;
    }

    @Override
    public int getMaxItemUseDuration(ItemStack stack) {
        return 72000;
    }

    @Override
    public boolean onBlockStartBreak(ItemStack itemstack, BlockPos pos, EntityPlayer player) {
        if (player.isSneaking())
            return false;
        double dist = player.getEntityAttribute(EntityPlayer.REACH_DISTANCE).getAttributeValue();
        RayTraceResult ray = getMovingObjectPosWithReachDistance(player.world, player, dist, false, true, false);
        if (ray != null)
            return this.breakBlocks(itemstack, 1, player.world, pos, ray.sideHit, player);
        return false;
    }

    // The following methods are almost exact copies of Actually Additions' ItemDrill and WorldUtil

    private boolean breakBlocks(ItemStack stack, int radius, World world, BlockPos aPos, EnumFacing side, EntityPlayer player) {
        int xRange = radius;
        int yRange = radius;
        int zRange = 0;

        //Corrects Blocks to hit depending on Side of original Block hit
        if (side.getAxis() == EnumFacing.Axis.Y) {
            zRange = radius;
            yRange = 0;
        }
        if (side.getAxis() == EnumFacing.Axis.X) {
            xRange = 0;
            zRange = radius;
        }

        //Not defined later because main Block is getting broken below
        IBlockState state = world.getBlockState(aPos);
        float mainHardness = state.getBlockHardness(world, aPos);

        //Break Middle Block first
        if (!this.tryHarvestBlock(world, aPos, false, stack, player))
            return false;

        if (radius == 2 && side.getAxis() != EnumFacing.Axis.Y) {
            aPos = aPos.up();
            IBlockState theState = world.getBlockState(aPos);
            if (theState.getBlockHardness(world, aPos) <= mainHardness + 5.0F) {
                this.tryHarvestBlock(world, aPos, true, stack, player);
            }
        }

        //Break Blocks around
        if (radius > 0 && mainHardness >= 0.2F) {
            for (int xPos = aPos.getX() - xRange; xPos <= aPos.getX() + xRange; xPos++) {
                for (int yPos = aPos.getY() - yRange; yPos <= aPos.getY() + yRange; yPos++) {
                    for (int zPos = aPos.getZ() - zRange; zPos <= aPos.getZ() + zRange; zPos++) {
                        if (!(aPos.getX() == xPos && aPos.getY() == yPos && aPos.getZ() == zPos)) {
                            //Only break Blocks around that are (about) as hard or softer
                            BlockPos thePos = new BlockPos(xPos, yPos, zPos);
                            IBlockState theState = world.getBlockState(thePos);
                            if (theState.getBlockHardness(world, thePos) <= mainHardness + 5.0F) {
                                this.tryHarvestBlock(world, thePos, true, stack, player);
                            }
                        }
                    }
                }
            }
        }
        return true;
    }

    private boolean tryHarvestBlock(World world, BlockPos pos, boolean isExtra, ItemStack stack, EntityPlayer player) {
        IBlockState state = world.getBlockState(pos);
        Block block = state.getBlock();
        float hardness = state.getBlockHardness(world, pos);
        boolean canHarvest = (ForgeHooks.canHarvestBlock(block, player, world, pos) || this.canHarvestBlock(state, stack)) && (!isExtra || this.getDestroySpeed(stack, world.getBlockState(pos)) > 1.0F);
        if (hardness >= 0.0F && (!isExtra || canHarvest && !block.hasTileEntity(world.getBlockState(pos)))) {
            if (!player.capabilities.isCreativeMode)
                stack.damageItem(1, player);
            //Break the Block
            return breakExtraBlock(stack, world, player, pos);
        }
        return false;
    }

    private static RayTraceResult getMovingObjectPosWithReachDistance(World world, EntityPlayer player, double distance, boolean stopOnLiquid, boolean ignoreBlockWithoutBoundingBox, boolean returnLastUncollidableBlock) {
        float f = player.rotationPitch;
        float f1 = player.rotationYaw;
        double d0 = player.posX;
        double d1 = player.posY + player.getEyeHeight();
        double d2 = player.posZ;
        Vec3d vec3 = new Vec3d(d0, d1, d2);
        float f2 = MathHelper.cos(-f1 * 0.017453292F - (float) Math.PI);
        float f3 = MathHelper.sin(-f1 * 0.017453292F - (float) Math.PI);
        float f4 = -MathHelper.cos(-f * 0.017453292F);
        float f5 = MathHelper.sin(-f * 0.017453292F);
        float f6 = f3 * f4;
        float f7 = f2 * f4;
        Vec3d vec31 = vec3.add(f6 * distance, f5 * distance, f7 * distance);
        return world.rayTraceBlocks(vec3, vec31, stopOnLiquid, ignoreBlockWithoutBoundingBox, returnLastUncollidableBlock);
    }

    private static boolean breakExtraBlock(ItemStack stack, World world, EntityPlayer player, BlockPos pos) {
        IBlockState state = world.getBlockState(pos);
        Block block = state.getBlock();

        if (player.capabilities.isCreativeMode) {
            if (block.removedByPlayer(state, world, pos, player, false)) {
                block.onPlayerDestroy(world, pos, state);
            }

            // send update to client
            if (!world.isRemote) {
                ((EntityPlayerMP) player).connection.sendPacket(new SPacketBlockChange(world, pos));
            }
            return true;
        }

        // callback to the tool the player uses. Called on both sides. This damages the tool n stuff.
        stack.onBlockDestroyed(world, state, pos, player);

        // server sided handling
        if (!world.isRemote) {
            // send the blockbreak event
            int xp = ForgeHooks.onBlockBreakEvent(world, ((EntityPlayerMP) player).interactionManager.getGameType(), (EntityPlayerMP) player, pos);
            if (xp == -1) return false;

            TileEntity tileEntity = world.getTileEntity(pos);
            if (block.removedByPlayer(state, world, pos, player, true)) { // boolean is if block can be harvested, checked above
                block.onPlayerDestroy(world, pos, state);
                block.harvestBlock(world, player, pos, state, tileEntity, stack);
                block.dropXpOnBlockBreak(world, pos, xp);
            }

            // always send block update to client
            ((EntityPlayerMP) player).connection.sendPacket(new SPacketBlockChange(world, pos));
        }
        // client sided handling
        else {
            // clientside we do a "this block has been clicked on long enough to be broken" call. This should not send any new packets
            // the code above, executed on the server, sends a block-updates that give us the correct state of the block we destroy.

            // following code can be found in PlayerControllerMP.onPlayerDestroyBlock
            world.playEvent(2001, pos, Block.getStateId(state));
            if (block.removedByPlayer(state, world, pos, player, true)) {
                block.onPlayerDestroy(world, pos, state);
            }
            // callback to the tool
            stack.onBlockDestroyed(world, state, pos, player);

            // send an update to the server, so we get an update back
            Nyx.proxy.sendBreakPacket(pos);
        }
        return true;
    }
}
