package de.ellpeck.nyx.entities;

import de.ellpeck.nyx.Config;
import de.ellpeck.nyx.Registry;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

public class FallingStar extends Entity {

    private float trajectoryX;
    private float trajectoryY;
    private float trajectoryZ;

    public FallingStar(World worldIn) {
        super(worldIn);
        this.setEntityBoundingBox(null);

        this.trajectoryX = MathHelper.nextFloat(this.world.rand, 0.5F, 1.25F);
        if (this.world.rand.nextBoolean())
            this.trajectoryX *= -1;
        this.trajectoryY = MathHelper.nextFloat(this.world.rand, -0.5F, -0.85F);
        this.trajectoryZ = MathHelper.nextFloat(this.world.rand, 0.5F, 1.25F);
        if (this.world.rand.nextBoolean())
            this.trajectoryZ *= -1;
    }

    @Override
    public void onEntityUpdate() {
        if (!this.world.isRemote) {
            this.move(MoverType.SELF, this.trajectoryX, this.trajectoryY, this.trajectoryZ);

            if (this.collided) {
                Item result = this.world.rand.nextDouble() <= Config.cometShardChance ? Registry.cometShard : Registry.fallenStar;
                EntityItem item = new EntityItem(this.world, this.posX, this.posY, this.posZ, new ItemStack(result));
                this.world.spawnEntity(item);
                this.setDead();
            }
        } else {
            for (int i = 0; i < 2; i++) {
                double mX = -this.motionX + this.world.rand.nextGaussian() * 0.05;
                double mY = -this.motionY + this.world.rand.nextGaussian() * 0.05;
                double mZ = -this.motionZ + this.world.rand.nextGaussian() * 0.05;
                this.world.spawnParticle(EnumParticleTypes.FIREWORKS_SPARK, true, this.posX, this.posY, this.posZ, mX, mY, mZ);
            }
        }
        super.onEntityUpdate();
    }

    @Override
    protected void entityInit() {

    }

    @Override
    protected void readEntityFromNBT(NBTTagCompound compound) {
        this.trajectoryX = compound.getFloat("trajectory_x");
        this.trajectoryY = compound.getFloat("trajectory_y");
        this.trajectoryZ = compound.getFloat("trajectory_z");
    }

    @Override
    protected void writeEntityToNBT(NBTTagCompound compound) {
        compound.setFloat("trajectory_x", this.trajectoryX);
        compound.setFloat("trajectory_y", this.trajectoryY);
        compound.setFloat("trajectory_z", this.trajectoryZ);
    }
}
