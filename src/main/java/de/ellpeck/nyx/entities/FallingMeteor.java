package de.ellpeck.nyx.entities;

import de.ellpeck.nyx.Nyx;
import de.ellpeck.nyx.Registry;
import de.ellpeck.nyx.capabilities.NyxWorld;
import de.ellpeck.nyx.lunarevents.HarvestMoon;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.network.play.server.SPacketSoundEffect;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;

public class FallingMeteor extends FallingStar {

    public static final DataParameter<Integer> SIZE = EntityDataManager.createKey(FallingMeteor.class, DataSerializers.VARINT);
    public boolean homing;
    public boolean disableMessage;

    public FallingMeteor(World worldIn) {
        super(worldIn);
        this.dataManager.set(SIZE, worldIn.rand.nextInt(3) + 1);

        // meteor should be faster than falling star
        this.trajectoryX *= 2;
        this.trajectoryY *= 2;
        this.trajectoryZ *= 2;
    }

    @Override
    protected void entityInit() {
        this.dataManager.register(SIZE, 1);
    }

    @Override
    protected void customUpdate() {
        if (!this.world.isRemote) {
            // falling into the void
            if (this.posY <= -64)
                this.setDead();

            // move towards the closest player if we're homing
            if (this.homing) {
                EntityPlayer player = this.world.getClosestPlayer(this.posX, this.posY, this.posZ, 128, false);
                if (player != null && player.getDistanceSq(this) >= 32 * 32) {
                    Vec3d motion = new Vec3d(player.posX - this.posX, player.posY - this.posY, player.posZ - this.posZ);
                    motion = motion.normalize();
                    this.trajectoryX = (float) motion.x * 2F;
                    this.trajectoryY = (float) motion.y * 2F;
                    this.trajectoryZ = (float) motion.z * 2F;
                }
            }

            if (this.collided) {
                NyxWorld data = NyxWorld.get(this.world);
                Explosion exp = this.world.createExplosion(null, this.posX + 0.5, this.posY + 0.5, this.posZ + 0.5, this.dataManager.get(SIZE) * 4, true);
                for (BlockPos affected : exp.getAffectedBlockPositions()) {
                    if (!this.world.getBlockState(affected).getBlock().isReplaceable(this.world, affected) || !this.world.getBlockState(affected.down()).isFullBlock())
                        continue;
                    if (this.world.rand.nextInt(2) != 0)
                        continue;
                    if (this.world.rand.nextInt(5) == 0) {
                        this.world.setBlockState(affected, Blocks.MAGMA.getDefaultState());
                    } else {
                        if (data.currentEvent instanceof HarvestMoon && this.world.rand.nextInt(10) == 0) {
                            this.world.setBlockState(affected, Registry.gleaningMeteorRock.getDefaultState());
                        } else {
                            this.world.setBlockState(affected, Registry.meteorRock.getDefaultState());
                        }
                        data.meteorLandingSites.add(affected);
                    }
                }
                data.sendToClients();
                this.setDead();

                // send "I spawned" message
                if (!this.disableMessage) {
                    ITextComponent text = new TextComponentTranslation("info." + Nyx.ID + ".meteor").setStyle(new Style().setColor(TextFormatting.GRAY).setItalic(true));
                    for (EntityPlayer player : this.world.playerEntities) {
                        SoundEvent sound;
                        float pitch;
                        double dist = player.getDistanceSq(this.posX, this.posY, this.posZ);
                        if (dist <= 256 * 256) {
                            if (dist > 16 * 16)
                                player.sendMessage(text);
                            sound = SoundEvents.ENTITY_GENERIC_EXPLODE;
                            pitch = 0.15F;
                        } else {
                            sound = Registry.fallingMeteorImpactSound;
                            pitch = 1;
                        }
                        if (player instanceof EntityPlayerMP && player.dimension == this.world.provider.getDimension())
                            ((EntityPlayerMP) player).connection.sendPacket(new SPacketSoundEffect(sound, SoundCategory.AMBIENT, player.posX, player.posY, player.posZ, 0.5F, pitch));
                    }
                }
            } else {
                if (this.world.getTotalWorldTime() % 35 == 0)
                    this.world.playSound(null, this.posX, this.posY, this.posZ, Registry.fallingMeteorSound, SoundCategory.AMBIENT, 5, 1);

            }
        } else if (this.isLoaded()) {
            // we only want to display particles if we're loaded
            float size = this.dataManager.get(SIZE) / 2F + 1;
            for (int i = 0; i < 60; i++) {
                double x = this.posX + MathHelper.nextDouble(this.world.rand, -size, size);
                double y = this.posY + MathHelper.nextDouble(this.world.rand, -size, size);
                double z = this.posZ + MathHelper.nextDouble(this.world.rand, -size, size);
                double mX = -this.motionX + this.world.rand.nextGaussian() * 0.02;
                double mY = -this.motionY + this.world.rand.nextGaussian() * 0.02;
                double mZ = -this.motionZ + this.world.rand.nextGaussian() * 0.02;

                EnumParticleTypes type;
                float f = this.world.rand.nextFloat();
                if (f >= 0.65F) {
                    type = EnumParticleTypes.FLAME;
                } else if (f >= 0.45F) {
                    type = EnumParticleTypes.LAVA;
                } else if (f >= 0.3F) {
                    type = EnumParticleTypes.SMOKE_NORMAL;
                } else {
                    type = EnumParticleTypes.SMOKE_LARGE;
                }
                this.world.spawnParticle(type, true, x, y, z, mX, mY, mZ);
            }
        }
    }

    @Override
    protected void writeEntityToNBT(NBTTagCompound compound) {
        super.writeEntityToNBT(compound);
        compound.setInteger("size", this.dataManager.get(SIZE));
        compound.setBoolean("homing", this.homing);
        compound.setBoolean("disable_message", this.disableMessage);
    }

    @Override
    protected void readEntityFromNBT(NBTTagCompound compound) {
        super.readEntityFromNBT(compound);
        this.dataManager.set(SIZE, compound.getInteger("size"));
        this.homing = compound.getBoolean("homing");
        this.disableMessage = compound.getBoolean("disable_message");
    }

    public static FallingMeteor spawn(World world, BlockPos pos) {
        pos = world.getPrecipitationHeight(pos).up(MathHelper.getInt(world.rand, 64, 96));
        FallingMeteor meteor = new FallingMeteor(world);
        meteor.setPosition(pos.getX(), pos.getY(), pos.getZ());
        world.spawnEntity(meteor);
        return meteor;
    }
}
