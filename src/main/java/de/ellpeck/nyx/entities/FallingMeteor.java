package de.ellpeck.nyx.entities;

import de.ellpeck.nyx.Nyx;
import de.ellpeck.nyx.Registry;
import de.ellpeck.nyx.capabilities.NyxWorld;
import net.minecraft.block.material.Material;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;

public class FallingMeteor extends FallingStar {

    public int size;

    public FallingMeteor(World worldIn) {
        super(worldIn);
        this.size = worldIn.rand.nextInt(3) + 1;

        // meteor should be faster than falling star
        this.trajectoryX *= 2;
        this.trajectoryY *= 2;
        this.trajectoryZ *= 2;
    }

    @Override
    protected void customUpdate() {
        if (!this.world.isRemote) {
            // falling into the void
            if (this.posY <= -64)
                this.setDead();

            if (this.collided) {
                Explosion exp = this.world.createExplosion(null, this.posX + 0.5, this.posY + 0.5, this.posZ + 0.5, this.size * 4, true);
                for (BlockPos affected : exp.getAffectedBlockPositions()) {
                    if (!this.world.getBlockState(affected).getBlock().isReplaceable(this.world, affected) || !this.world.getBlockState(affected.down()).isFullBlock())
                        continue;
                    if (this.world.rand.nextInt(2) != 0)
                        continue;
                    this.world.setBlockState(affected, (this.world.rand.nextInt(5) == 0 ? Blocks.MAGMA : Registry.meteorRock).getDefaultState());
                }
                this.setDead();

                // send "I spawned" message
                ITextComponent text = new TextComponentTranslation("info." + Nyx.ID + ".meteor").setStyle(new Style().setColor(TextFormatting.GRAY).setItalic(true));
                for (EntityPlayer player : this.world.playerEntities) {
                    if (player.getDistanceSq(this.posX, this.posY, this.posZ) <= 256 * 256)
                        player.sendMessage(text);
                }
            }
        } else {
            // TODO particles
        }
    }

    @Override
    protected void writeEntityToNBT(NBTTagCompound compound) {
        super.writeEntityToNBT(compound);
        compound.setInteger("size", this.size);
    }

    @Override
    protected void readEntityFromNBT(NBTTagCompound compound) {
        super.readEntityFromNBT(compound);
        this.size = compound.getInteger("size");
    }

    public static FallingMeteor spawn(World world, BlockPos pos) {
        pos = world.getHeight(pos).up(MathHelper.getInt(world.rand, 64, 96));
        FallingMeteor meteor = new FallingMeteor(world);
        meteor.setPosition(pos.getX(), pos.getY(), pos.getZ());
        world.spawnEntity(meteor);
        return meteor;
    }
}
