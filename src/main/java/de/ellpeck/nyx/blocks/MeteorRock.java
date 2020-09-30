package de.ellpeck.nyx.blocks;

import de.ellpeck.nyx.Registry;
import de.ellpeck.nyx.capabilities.NyxWorld;
import net.minecraft.block.Block;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Random;
import java.util.function.Supplier;

public class MeteorRock extends Block {

    private final Supplier<Item> droppedItem;

    public MeteorRock(Supplier<Item> droppedItem) {
        super(Material.ROCK);
        this.droppedItem = droppedItem;

        this.setHarvestLevel("pickaxe", 3);
        this.setHardness(40);
        this.setResistance(3000);
    }

    @Override
    public void onBlockHarvested(World worldIn, BlockPos pos, IBlockState state, EntityPlayer player) {
        if (!worldIn.isRemote) {
            NyxWorld data = NyxWorld.get(worldIn);
            if (data != null) {
                data.meteorLandingSites.remove(pos);
                data.sendToClients();
            }
        }
    }

    @Override
    public Item getItemDropped(IBlockState state, Random rand, int fortune) {
        return this.droppedItem.get();
    }
}
