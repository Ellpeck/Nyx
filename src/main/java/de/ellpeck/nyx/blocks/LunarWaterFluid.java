package de.ellpeck.nyx.blocks;

import de.ellpeck.nyx.Nyx;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.Fluid;

public class LunarWaterFluid extends Fluid {
    public LunarWaterFluid() {
        super("lunar_water", new ResourceLocation(Nyx.ID, "blocks/lunar_water_still"), new ResourceLocation(Nyx.ID, "blocks/lunar_water_flowing"));
    }

    @Override
    public String getUnlocalizedName() {
        return "fluid." + Nyx.ID + "." + this.unlocalizedName;
    }
}
