package de.ellpeck.nyx.entities;

import de.ellpeck.nyx.Nyx;
import de.ellpeck.nyx.Registry;
import de.ellpeck.nyx.capabilities.NyxWorld;
import de.ellpeck.nyx.lunarevents.HarvestMoon;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
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
    public float speedModifier;

    public FallingMeteor(World worldIn) {
        super(worldIn);
        this.dataManager.set(SIZE, worldIn.rand.nextInt(3) + 1);
        this.initTrajectory(2 * this.speedModifier);
    }

    @Override
    protected void entityInit() {
        this.dataManager.register(SIZE, 1);
    }

    @Override
    public void onAddedToWorld() {
        super.onAddedToWorld();
        // init some stuff that /summon wouldn't have
        if (this.dataManager.get(SIZE) <= 0)
            this.dataManager.set(SIZE, 2);
        if (this.speedModifier <= 0)
            this.speedModifier = 1;
        if (this.trajectoryX == 0 && this.trajectoryY == 0 && this.trajectoryZ == 0)
            this.initTrajectory(2 * this.speedModifier);
    }

    @Override
    public void setLocationAndAngles(double x, double y, double z, float yaw, float pitch) {
        // homing meteors should spawn higher up
        if (this.homing)
            y += 48;
        super.setLocationAndAngles(x, y, z, yaw, pitch);
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
                    this.trajectoryX = (float) motion.x * 2 * this.speedModifier;
                    if (motion.y < 0)
                        this.trajectoryY = (float) motion.y * 2 * this.speedModifier;
                    this.trajectoryZ = (float) motion.z * 2 * this.speedModifier;
                }
            }

            if (this.collided) {
                // if we removed trees, we want to continue flying after until we hit the ground
                if (this.removeTrees(this.getPosition()))
                    return;

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
        compound.setFloat("speed", this.speedModifier);
    }

    @Override
    protected void readEntityFromNBT(NBTTagCompound compound) {
        super.readEntityFromNBT(compound);
        this.dataManager.set(SIZE, compound.getInteger("size"));
        this.homing = compound.getBoolean("homing");
        this.disableMessage = compound.getBoolean("disable_message");
        this.speedModifier = compound.getFloat("speed");
    }

    private boolean removeTrees(BlockPos pos) {
        boolean any = false;
        for (int x = -1; x <= 1; x++) {
            for (int y = -1; y <= 1; y++) {
                for (int z = -1; z <= 1; z++) {
                    BlockPos offset = pos.add(x, y, z);
                    if (offset.distanceSq(this.posX, this.posY, this.posZ) >= 8 * 8)
                        continue;
                    IBlockState state = this.world.getBlockState(offset);
                    if (!state.getBlock().isLeaves(state, this.world, offset) && !state.getBlock().isWood(this.world, offset))
                        continue;
                    this.world.setBlockToAir(offset);
                    this.removeTrees(offset);
                    any = true;
                }
            }
        }
        return any;
    }

    public static FallingMeteor spawn(World world, BlockPos pos) {
        pos = world.getPrecipitationHeight(pos).up(MathHelper.getInt(world.rand, 64, 96));
        FallingMeteor meteor = new FallingMeteor(world);
        meteor.setPosition(pos.getX(), pos.getY(), pos.getZ());
        world.spawnEntity(meteor);
        return meteor;
    }
}
