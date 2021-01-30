package com.lich.apocrypha.common.content.wand.lightning;

import com.lich.apocrypha.common.APRegistry;
import elucent.eidolon.Registry;
import elucent.eidolon.entity.SpellProjectileEntity;
import elucent.eidolon.network.MagicBurstEffectPacket;
import elucent.eidolon.network.Networking;
import elucent.eidolon.particle.Particles;
import elucent.eidolon.util.ColorUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.potion.EffectInstance;
import net.minecraft.util.DamageSource;
import net.minecraft.util.IndirectEntityDamageSource;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3i;
import net.minecraft.world.World;

import java.util.UUID;

public class LightningBoltProjectileEntity extends SpellProjectileEntity
{
    private UUID subClassCasterID;

    private static final Vector3i secondaryColor = new Vector3i(224, 224, 96);

    public LightningBoltProjectileEntity(EntityType<?> type, World world)
    {
        super(type, world);

        setNoGravity(true);
    }

    @Override
    public Entity shoot(double x, double y, double z, double vx, double vy, double vz, UUID caster)
    {
        subClassCasterID = caster;
        return super.shoot(x, y, z, vx, vy, vz, caster);
    }

    @Override
    public void tick()
    {
        super.tick();
        Vector3d motion = getMotion();
        Vector3d pos = getPositionVec();
        Vector3d norm = motion.normalize().scale(0.02500000037252903D);

        for (int i = 0; i < 8; ++i)
        {
            double lerpX = MathHelper.lerp((float) i / 8.0F, prevPosX, pos.x);
            double lerpY = MathHelper.lerp((float) i / 8.0F, prevPosY, pos.y);
            double lerpZ = MathHelper.lerp((float) i / 8.0F, prevPosZ, pos.z);
            Particles.create(Registry.WISP_PARTICLE)
                    .addVelocity(-norm.x, -norm.y, -norm.z)
                    .setAlpha(0.0625F, 0.0F)
                    .setScale(0.625F, 0.0F)
                    .setColor(1, 1, 224 / 256F, secondaryColor.getX() / 256F, secondaryColor.getY() / 256F, secondaryColor.getZ() / 256F)
                    .setLifetime(5)
                    .spawn(world, lerpX, lerpY, lerpZ);
            Particles.create(Registry.WISP_PARTICLE)
                    .addVelocity(-norm.x, -norm.y, -norm.z)
                    .setAlpha(0.125F, 0.0F)
                    .setScale(0.25F, 0.125F)
                    .setColor(214 / 256F, 214 / 256F, 120 / 256F, secondaryColor.getX() / 256F, secondaryColor.getY() / 256F, secondaryColor.getZ() / 256F)
                    .setLifetime(20)
                    .spawn(world, lerpX, lerpY, lerpZ);
        }

    }

    @Override
    protected void onImpact(RayTraceResult ray, Entity target)
    {
        target.attackEntityFrom(new IndirectEntityDamageSource(DamageSource.LIGHTNING_BOLT.getDamageType(), this, world.getPlayerByUuid(subClassCasterID)), 4.0F);
        if (target instanceof LivingEntity)
            ((LivingEntity) target).addPotionEffect(new EffectInstance(APRegistry.LIGHTNING_MAGNET_EFFECT.get(), 300, 0));

        onImpact(ray);
    }

    @Override
    protected void onImpact(RayTraceResult ray)
    {
        setDead();
        if (!world.isRemote)
        {
            Vector3d pos = ray.getHitVec();
            world.playSound(null, pos.x, pos.y, pos.z, APRegistry.SPLASH_LIGHTNING_BOLT_EVENT.get(), SoundCategory.NEUTRAL, 0.5F, rand.nextFloat() * 0.2F + 0.9F);
            Networking.sendToTracking(world, getPosition(), new MagicBurstEffectPacket(pos.x, pos.y, pos.z, ColorUtil.packColor(255, 224, 224, 128), ColorUtil.packColor(255, secondaryColor.getX(), secondaryColor.getY(), secondaryColor.getZ())));
        }

    }
}
