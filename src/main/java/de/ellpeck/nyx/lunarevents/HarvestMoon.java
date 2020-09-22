package de.ellpeck.nyx.lunarevents;

import de.ellpeck.nyx.Config;
import de.ellpeck.nyx.Nyx;
import de.ellpeck.nyx.capabilities.NyxWorld;
import net.minecraft.block.*;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;

import java.util.Iterator;

public class HarvestMoon extends LunarEvent {

    private final ConfigImpl config = new ConfigImpl(() -> Config.harvestMoon);

    public HarvestMoon(NyxWorld nyxWorld) {
        super("harvest_moon", nyxWorld);
    }

    @Override
    public ITextComponent getStartMessage() {
        return new TextComponentTranslation("info." + Nyx.ID + ".harvest_moon")
                .setStyle(new Style().setColor(TextFormatting.BLUE).setItalic(true));
    }

    @Override
    public boolean shouldStart(boolean lastDaytime) {
        if (Config.harvestMoonOnFull && this.world.getCurrentMoonPhaseFactor() < 1)
            return false;
        if (!lastDaytime || NyxWorld.isDaytime(this.world))
            return false;
        return this.config.canStart();
    }

    @Override
    public boolean shouldStop(boolean lastDaytime) {
        return NyxWorld.isDaytime(this.world);
    }

    @Override
    public int getSkyColor() {
        return 0x3f3fc0;
    }

    @Override
    public String getMoonTexture() {
        return "harvest_moon";
    }

    @Override
    public void update(boolean lastDaytime) {
        this.config.update(lastDaytime);

        if (this.world.isRemote || this.nyxWorld.currentEvent != this || Config.harvestMoonGrowAmount <= 0)
            return;
        if (this.world.getTotalWorldTime() % Config.harvestMoonGrowInterval != 0)
            return;
        Iterator<Chunk> chunks = this.world.getPersistentChunkIterable(((WorldServer) this.world).getPlayerChunkMap().getChunkIterator());
        while (chunks.hasNext()) {
            Chunk chunk = chunks.next();
            for (int i = 0; i < Config.harvestMoonGrowAmount; i++) {
                int x = this.world.rand.nextInt(16);
                int z = this.world.rand.nextInt(16);
                int y = chunk.getHeightValue(x, z);
                BlockPos pos = new BlockPos(chunk.x * 16 + x, y, chunk.z * 16 + z);
                IBlockState state = chunk.getBlockState(pos);
                Block block = state.getBlock();
                if (!(block instanceof IGrowable) || block instanceof BlockGrass || block instanceof BlockTallGrass || block instanceof BlockDoublePlant)
                    continue;
                try {
                    IGrowable growable = (IGrowable) block;
                    if (growable.canGrow(this.world, pos, state, false))
                        growable.grow(this.world, this.world.rand, pos, state);
                } catch (Exception ignored) {
                }
            }
        }
    }

    @Override
    public NBTTagCompound serializeNBT() {
        return this.config.serializeNBT();
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
        this.config.deserializeNBT(nbt);
    }
}
