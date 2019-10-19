package de.ellpeck.nyx.entities;

import de.ellpeck.nyx.Registry;
import net.minecraft.block.Block;
import net.minecraft.block.BlockCauldron;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class CauldronTracker extends Entity {

    private BlockPos trackingPos;
    private int timer;

    public CauldronTracker(World worldIn) {
        super(worldIn);
        this.setEntityBoundingBox(null);
    }

    public void setTrackingPos(BlockPos pos) {
        this.setPosition(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
        this.trackingPos = pos;
    }

    @Override
    public void onUpdate() {
        this.onEntityUpdate();
    }

    @Override
    public void onEntityUpdate() {
        if (this.world.isRemote)
            return;

        IBlockState state = this.world.getBlockState(this.trackingPos);
        Block block = state.getBlock();
        if (!(block instanceof BlockCauldron)) {
            this.setDead();
            return;
        }
        if (this.world.isDaytime() || this.world.getCurrentMoonPhaseFactor() < 1 || !this.world.canSeeSky(this.trackingPos)) {
            this.timer = 0;
            return;
        }

        int level = state.getValue(BlockCauldron.LEVEL);
        if (level <= 0) {
            this.timer = 0;
            return;
        }

        this.timer++;
        if (this.timer >= 10000) {
            IBlockState newState = Registry.lunarWaterCauldron.getDefaultState().withProperty(BlockCauldron.LEVEL, level);
            this.world.setBlockState(this.trackingPos, newState);
            this.setDead();
        }
    }

    @Override
    protected void readEntityFromNBT(NBTTagCompound compound) {
        this.setTrackingPos(NBTUtil.getPosFromTag(compound.getCompoundTag("tracking_pos")));
        this.timer = compound.getInteger("timer");
    }

    @Override
    protected void writeEntityToNBT(NBTTagCompound compound) {
        compound.setTag("tracking_pos", NBTUtil.createPosTag(this.trackingPos));
        compound.setInteger("timer", this.timer);
    }

    @Override
    protected void entityInit() {

    }
}
