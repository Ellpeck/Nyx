package de.ellpeck.nyx.items;

import com.google.common.collect.Multimap;
import de.ellpeck.nyx.Registry;
import net.minecraft.block.Block;
import net.minecraft.block.BlockBush;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;

public class Scythe extends Item {

    public Scythe() {
        this.setMaxDamage(3450);
        this.setMaxStackSize(1);
    }

    @Override
    public boolean onBlockStartBreak(ItemStack itemstack, BlockPos pos, EntityPlayer player) {
        ItemStack stack = player.getHeldItem(EnumHand.MAIN_HAND);
        World worldIn = player.world;

        // only mass-break blocks that are allowed
        IBlockState state = worldIn.getBlockState(pos);
        Block block = state.getBlock();
        if (!(block instanceof IPlantable) && !block.isLeaves(state, worldIn, pos))
            return false;

        int range = 4;
        for (int x = -range; x <= range; x++) {
            for (int y = -range; y <= range; y++) {
                for (int z = -range; z <= range; z++) {
                    BlockPos offset = pos.add(x, y, z);
                    IBlockState offState = worldIn.getBlockState(offset);
                    Block offBlock = offState.getBlock();
                    if (!(offBlock instanceof IPlantable) && !offBlock.isLeaves(offState, worldIn, offset))
                        continue;

                    // modified version of Block.dropBlockAsItemWithChance since we want custom amounts
                    List<ItemStack> drops = offBlock.getDrops(worldIn, offset, offState, 0);
                    float chance = ForgeEventFactory.fireBlockHarvesting(drops, worldIn, offset, offState, 0, 1, false, player);
                    for (ItemStack drop : drops) {
                        if (worldIn.rand.nextFloat() > chance)
                            continue;
                        // increase drop amount by chance
                        if (worldIn.rand.nextFloat() <= 0.6F) {
                            multCount(drop, 2);
                        } else if (worldIn.rand.nextFloat() <= 0.4F) {
                            multCount(drop, 3);
                        } else if (worldIn.rand.nextFloat() <= 0.2F) {
                            multCount(drop, 4);
                        }
                        Block.spawnAsEntity(worldIn, offset, drop);
                    }

                    worldIn.destroyBlock(offset, false);
                    stack.damageItem(1, player);
                }
            }
        }
        return true;
    }

    @Override
    public boolean onBlockDestroyed(ItemStack stack, World worldIn, IBlockState state, BlockPos pos, EntityLivingBase entityLiving) {
        // returning true here triggers statistics
        return true;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean isFull3D() {
        return true;
    }

    @Override
    public boolean getIsRepairable(ItemStack toRepair, ItemStack repair) {
        return repair.getItem() == Item.getItemFromBlock(Registry.crystal);
    }

    @Override
    public Multimap<String, AttributeModifier> getItemAttributeModifiers(EntityEquipmentSlot equipmentSlot) {
        Multimap<String, AttributeModifier> multimap = super.getItemAttributeModifiers(equipmentSlot);
        if (equipmentSlot == EntityEquipmentSlot.MAINHAND) {
            multimap.put(SharedMonsterAttributes.ATTACK_DAMAGE.getName(), new AttributeModifier(ATTACK_DAMAGE_MODIFIER, "Weapon modifier", 10, 0));
            multimap.put(SharedMonsterAttributes.ATTACK_SPEED.getName(), new AttributeModifier(ATTACK_SPEED_MODIFIER, "Weapon modifier", -2.8, 0));
        }
        return multimap;
    }

    private static void multCount(ItemStack stack, int mult) {
        int newAmount = Math.min(stack.getMaxStackSize(), stack.getCount() * mult);
        stack.setCount(newAmount);
    }
}
