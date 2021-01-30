package com.lich.apocrypha.common.content.wand.lightning;

import com.lich.apocrypha.common.APRegistry;
import elucent.eidolon.item.WandItem;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

public class LightningWandItem extends WandItem
{
    public LightningWandItem(Properties properties)
    {
        super(properties);
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, PlayerEntity entity, Hand hand)
    {
        ItemStack stack = entity.getHeldItem(hand);
        if (!entity.isSwingInProgress)
        {
            if (!world.isRemote)
            {
                Vector3d pos = entity.getPositionVec().add(entity.getLookVec().scale(0.5D)).add(0.5D * Math.sin(Math.toRadians(225.0F - entity.rotationYawHead)), entity.getHeight() * 2.0F / 3.0F, 0.5D * Math.cos(Math.toRadians(225.0F - entity.rotationYawHead)));
                Vector3d vel = entity.getEyePosition(0.0F).add(entity.getLookVec().scale(40)).subtract(pos).scale(0.05D);
                world.addEntity((new LightningBoltProjectileEntity(APRegistry.LIGHTNING_BOLT_PROJECTILE.get(), world)).shoot(pos.x, pos.y, pos.z, vel.x, vel.y, vel.z, entity.getUniqueID()));
                world.playSound(null, pos.x, pos.y, pos.z, APRegistry.CAST_LIGHTNING_BOLT_EVENT.get(), SoundCategory.NEUTRAL, 0.75F, random.nextFloat() * 0.2F + 0.9F);
                stack.damageItem(1, entity, player -> player.sendBreakAnimation(hand));
            }

            entity.swingArm(hand);
            return ActionResult.resultSuccess(stack);
        }
        else
        {
            return ActionResult.resultPass(stack);
        }
    }
}
