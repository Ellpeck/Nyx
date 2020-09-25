package de.ellpeck.nyx.entities;

import de.ellpeck.nyx.Nyx;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.Entity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;

public class MeteorRenderer extends Render<FallingMeteor> {

    private static final ResourceLocation RES = new ResourceLocation(Nyx.ID, "textures/models/meteor.png");
    private final ModelOverlay model = new ModelOverlay();

    public MeteorRenderer(RenderManager renderManager) {
        super(renderManager);
    }

    @Override
    protected ResourceLocation getEntityTexture(FallingMeteor entity) {
        return TextureMap.LOCATION_BLOCKS_TEXTURE;
    }

    @Override
    public void doRender(FallingMeteor entity, double x, double y, double z, float entityYaw, float partialTicks) {
        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, z);
        GlStateManager.disableLighting();
        float size = entity.getDataManager().get(FallingMeteor.SIZE) / 2F;
        GlStateManager.scale(size, size, size);
        GlStateManager.translate(-1, -1, -1);
        this.bindTexture(RES);
        this.model.render();
        GlStateManager.enableLighting();
        GlStateManager.popMatrix();
    }

    @Override
    public boolean shouldRender(FallingMeteor livingEntity, ICamera camera, double camX, double camY, double camZ) {
        return true;
    }

    private static class ModelOverlay extends ModelBase {

        private final ModelRenderer box;

        public ModelOverlay() {
            this.box = new ModelRenderer(this, 0, 0);
            this.box.setTextureSize(128, 128);
            this.box.addBox(0, 0, 0, 32, 32, 32);
        }

        public void render() {
            this.box.render(1 / 16F);
        }
    }
}
