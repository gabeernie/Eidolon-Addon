package com.lich.apocrypha.common.content.wand.lightning;

import com.lich.apocrypha.Apocrypha;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.DisplayEffectsScreen;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.LightningBoltEntity;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.EffectType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.extensions.IForgeEffect;

import java.util.Random;

public class LightningMagnetEffect extends Effect implements IForgeEffect
{
    protected static final ResourceLocation EFFECT_TEXTURE = new ResourceLocation(Apocrypha.MODID, "textures/mob_effect/lightning_magnet.png");

    static int packColor(int alpha, int red, int green, int blue)
    {
        return alpha << 24 | red << 16 | green << 8 | blue;
    }

    private final Random rand = new Random();

    public LightningMagnetEffect()
    {
        super(EffectType.HARMFUL, packColor(255, 214, 214, 120));
    }

    @Override
    public boolean isReady(int duration, int amplifier)
    {
        return true;
    }

    @Override
    public void performEffect(LivingEntity entity, int amplifier)
    {
        super.performEffect(entity, amplifier);

        if (entity.getEntityWorld().isThundering() && rand.nextInt(100) == 50)
        {
            LightningBoltEntity bolt = EntityType.LIGHTNING_BOLT.create(entity.getEntityWorld());
            bolt.moveForced(Vector3d.copyCenteredHorizontally(entity.getPosition()));
            bolt.setEffectOnly(false);
            entity.getEntityWorld().addEntity(bolt);
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void renderInventoryEffect(EffectInstance effect, DisplayEffectsScreen<?> gui, MatrixStack mStack, int x, int y, float z)
    {
        Minecraft mc = Minecraft.getInstance();
        mc.getTextureManager().bindTexture(EFFECT_TEXTURE);
        gui.blit(mStack, x, y, 0, 0, 18, 18);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void renderHUDEffect(EffectInstance effect, AbstractGui gui, MatrixStack mStack, int x, int y, float z, float alpha)
    {
        Minecraft mc = Minecraft.getInstance();
        mc.getTextureManager().bindTexture(EFFECT_TEXTURE);
        gui.blit(mStack, x, y, 0, 0, 18, 18);
    }
}
