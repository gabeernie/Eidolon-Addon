package com.lich.apocrypha.common.content.wand.earthmover;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.IParticleRenderType;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.SpriteTexturedParticle;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;

import javax.annotation.Nonnull;

import static java.lang.Math.abs;

public class WandDigParticle extends SpriteTexturedParticle
{
    private final BlockState sourceState;
    private       BlockPos   sourcePos;

    private final Vector3d destination;
    private       Vector3d direction;
    private       double   movementLength;

    private final float uCoord;
    private final float vCoord;

    public WandDigParticle(ClientWorld world, double x, double y, double z, double motionX, double motionY, double motionZ, BlockState state)
    {
        super(world, x, y, z, motionX, motionY, motionZ);

        sourcePos = new BlockPos(x - 0.5, y - 0.5, z - 0.5);
        sourceState = state;
        destination = new Vector3d(motionX, motionY, motionZ);

        particleRed = 0.6F;
        particleGreen = 0.6F;
        particleBlue = 0.6F;
        particleScale /= 2.0F;
        uCoord = rand.nextFloat() * 3.0F;
        vCoord = rand.nextFloat() * 3.0F;

        updateSprite();

        Vector3d trajectory = new Vector3d(x - motionX, y - motionY, z - motionZ);
        movementLength = trajectory.length();
        direction = trajectory.normalize();

        if (!sourceState.isIn(Blocks.GRASS_BLOCK))
            multiplyColor();
    }

    @Nonnull
    @Override
    public IParticleRenderType getRenderType()
    {
        return IParticleRenderType.TERRAIN_SHEET;
    }

    @Override
    protected float getMinU()
    {
        return sprite.getInterpolatedU((uCoord + 1.0F) / 4.0F * 16.0F);
    }

    @Override
    protected float getMaxU()
    {
        return sprite.getInterpolatedU(uCoord / 4.0F * 16.0F);
    }

    @Override
    protected float getMinV()
    {
        return sprite.getInterpolatedV(vCoord / 4.0F * 16.0F);
    }

    @Override
    protected float getMaxV()
    {
        return sprite.getInterpolatedV((vCoord + 1.0F) / 4.0F * 16.0F);
    }

    @Override
    public int getBrightnessForRender(float partialTick)
    {
        int i = super.getBrightnessForRender(partialTick);
        int j = 0;
        if (world.isBlockLoaded(sourcePos))
        {
            j = WorldRenderer.getCombinedLight(world, sourcePos);
        }

        return i == 0 ? j : i;
    }

    protected void multiplyColor()
    {
        int i = Minecraft.getInstance().getBlockColors().getColor(sourceState, world, sourcePos, 0);
        particleRed *= (float) (i >> 16 & 255) / 255.0F;
        particleGreen *= (float) (i >> 8 & 255) / 255.0F;
        particleBlue *= (float) (i & 255) / 255.0F;
    }

    private Particle updateSprite()
    {
        setSprite(Minecraft.getInstance().getBlockRendererDispatcher().getBlockModelShapes().getTexture(sourceState, world, sourcePos));
        return this;
    }

    @Override
    public void tick()
    {
        prevPosX = posX;
        prevPosY = posY;
        prevPosZ = posZ;
        if (age++ >= maxAge || (isNearDestination()))
        {
            setExpired();
            return;
        }

        double delta = age / (double) maxAge;

        double x = sourcePos.getX() + 0.5 + (direction.getX() * (movementLength * delta));
        double y = sourcePos.getY() + 0.5 + (direction.getY() * (movementLength * delta));
        double z = sourcePos.getZ() + 0.5 + (direction.getZ() * (movementLength * delta));

        setPosition(x, y, z);
    }

    private boolean isNearDestination()
    {
        return abs(posX - motionX) < 0.05 && abs(posY - motionY) < 0.05 && abs(posZ - motionZ) < 0.05;
    }
}
