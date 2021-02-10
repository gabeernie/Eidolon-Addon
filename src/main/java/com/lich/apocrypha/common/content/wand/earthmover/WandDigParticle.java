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

import static java.lang.Math.*;

public class WandDigParticle extends SpriteTexturedParticle
{
    private final BlockState sourceState;
    private       BlockPos   sourcePos;

    private final Vector3d destination;
    private       Vector3d direction;
    private       double   movementLength;

    private final float minU;
    private final float minV;
    private final float maxU;
    private final float maxV;

    public WandDigParticle(ClientWorld world, double x, double y, double z, double motionX, double motionY, double motionZ, BlockState state)
    {
        super(world, x, y, z, motionX, motionY, motionZ);

        sourcePos = new BlockPos(x - 0.5, y - 0.5, z - 0.5);
        sourceState = state;
        destination = new Vector3d(motionX, motionY, motionZ);

        maxAge = (int) (25 + rand.nextGaussian() * 10);

        particleRed = 0.6F;
        particleGreen = 0.6F;
        particleBlue = 0.6F;
        particleScale /= 4.0F;

        updateSprite();

        float uDiff = (sprite.getMaxU() - sprite.getMinU()) / 4;
        float vDiff = (sprite.getMaxV() - sprite.getMinV()) / 4;

        int uSlice = rand.nextInt(4);
        minU = sprite.getMinU() + uDiff * uSlice;
        maxU = sprite.getMinU() + uDiff * (uSlice + 1);

        int vSlice = rand.nextInt(4);
        minV = sprite.getMinV() + vDiff * vSlice;
        maxV = sprite.getMinV() + vDiff * (vSlice + 1);

        Vector3d trajectory = new Vector3d(motionX - x, motionY - y, motionZ - z);
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
        return minU;
    }

    @Override
    protected float getMaxU()
    {
        return maxU;
    }

    @Override
    protected float getMinV()
    {
        return minV;
    }

    @Override
    protected float getMaxV()
    {
        return maxV;
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
        setSprite(Minecraft.getInstance().getBlockRendererDispatcher().getBlockModelShapes().getTexture(sourceState));
        return this;
    }

    @Override
    public void tick()
    {
        prevPosX = posX;
        prevPosY = posY;
        prevPosZ = posZ;
        if (age++ >= maxAge)
        {
            setExpired();
            return;
        }

        double delta = age / (double) maxAge;

        Vector3d rightDir = direction.mul(1, 0, 0);
        Vector3d upDir = rightDir.crossProduct(direction);
        double radius = 0.50;
        double pitch = 3 / (PI * 2);

        double t = movementLength / pitch * delta;

        double x = radius * cos(t) * rightDir.getX() + (radius * sin(t) * upDir.getX()) + (pitch * t * direction.getX());
        double y = radius * cos(t) * rightDir.getY() + (radius * sin(t) * upDir.getY()) + (pitch * t * direction.getY());
        double z = radius * cos(t) * rightDir.getZ() + (radius * sin(t) * upDir.getZ()) + (pitch * t * direction.getZ());

        x = sourcePos.getX() + 0.5 + x;
        y = sourcePos.getY() + 0.5 + y;
        z = sourcePos.getZ() + 0.5 + z;

        setPosition(x, y, z);
    }
}
