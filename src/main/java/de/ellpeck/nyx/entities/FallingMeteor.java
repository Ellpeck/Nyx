package de.ellpeck.nyx.entities;

import de.ellpeck.nyx.Nyx;
import de.ellpeck.nyx.Registry;
import de.ellpeck.nyx.capabilities.NyxWorld;
import net.minecraft.block.material.Material;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;

public class FallingMeteor extends FallingStar {
    public FallingMeteor(World worldIn) {
        super(worldIn);
        // meteor should be faster than falling star
        this.trajectoryX *= 2;
        this.trajectoryY *= 2;
        this.trajectoryZ *= 2;
    }

    @Override
    protected void customUpdate() {
        if (!this.world.isRemote) {
            if (this.collided) {
                // TODO dynamic size
                int size = 1;
                Explosion exp = this.world.createExplosion(null, this.posX + 0.5, this.posY + 0.5, this.posZ + 0.5, size * 4, true);
                for (BlockPos affected : exp.getAffectedBlockPositions()) {
                    if (!this.world.getBlockState(affected).getBlock().isReplaceable(this.world, affected) || !this.world.getBlockState(affected.down()).isFullBlock())
                        continue;
                    if (this.world.rand.nextInt(2) != 0)
                        continue;
                    this.world.setBlockState(affected, (this.world.rand.nextInt(5) == 0 ? Blocks.MAGMA : Registry.meteorRock).getDefaultState());
                }
                // TODO message to players in range
                this.setDead();
            }
        } else {
            // TODO particles
        }
    }

    public static void spawn(World world, BlockPos pos) {
        pos = world.getHeight(pos).up(MathHelper.getInt(world.rand, 64, 96));
        FallingMeteor meteor = new FallingMeteor(world);
        meteor.setPosition(pos.getX(), pos.getY(), pos.getZ());
        world.spawnEntity(meteor);
    }
}
