package de.ellpeck.nyx.entities;

import com.google.common.base.Predicate;
import de.ellpeck.nyx.capabilities.NyxWorld;
import de.ellpeck.nyx.lunarevents.FullMoon;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAITargetNonTamed;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.passive.EntityWolf;
import net.minecraft.entity.player.EntityPlayer;

public class WolfAiFullMoon extends EntityAITargetNonTamed<EntityLivingBase> {
    public WolfAiFullMoon(EntityTameable entityIn) {
        super(entityIn, EntityLivingBase.class, false, e -> {
            if (e instanceof EntityWolf)
                return false;
            return e instanceof EntityPlayer || e instanceof EntityAnimal || e instanceof EntitySkeleton;
        });
    }

    @Override
    public boolean shouldExecute() {
        if (super.shouldExecute()) {
            NyxWorld nyx = NyxWorld.get(this.taskOwner.world);
            return nyx != null && nyx.currentEvent instanceof FullMoon;
        }
        return false;
    }

    @Override
    public boolean shouldContinueExecuting() {
        if (super.shouldContinueExecuting()) {
            NyxWorld nyx = NyxWorld.get(this.taskOwner.world);
            return nyx != null && nyx.currentEvent instanceof FullMoon;
        }
        return false;
    }
}
