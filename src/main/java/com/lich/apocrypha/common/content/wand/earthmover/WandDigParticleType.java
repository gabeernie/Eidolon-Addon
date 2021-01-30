package com.lich.apocrypha.common.content.wand.earthmover;

import com.mojang.serialization.Codec;
import net.minecraft.client.particle.IAnimatedSprite;
import net.minecraft.client.particle.IParticleFactory;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particles.BlockParticleData;
import net.minecraft.particles.ParticleType;

import javax.annotation.Nonnull;

public class WandDigParticleType extends ParticleType<BlockParticleData>
{
    public WandDigParticleType()
    {
        super(false, BlockParticleData.DESERIALIZER);
    }

    @Nonnull
    @Override
    public Codec<BlockParticleData> func_230522_e_()
    {
        return BlockParticleData.func_239800_a_(this);
    }

    public static class Factory implements IParticleFactory<BlockParticleData>
    {
        private final IAnimatedSprite sprite;

        public Factory(IAnimatedSprite sprite)
        {
            this.sprite = sprite;
        }

        @Override
        public Particle makeParticle(BlockParticleData data, ClientWorld world, double x, double y, double z, double mx, double my, double mz)
        {
            WandDigParticle ret = new WandDigParticle(world, x, y, z, mx, my, mz, data.getBlockState());
            ret.selectSpriteRandomly(sprite);
            return ret;
        }
    }
}