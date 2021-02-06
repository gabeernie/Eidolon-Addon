package com.lich.apocrypha.common.content.wand.earthmover;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.lich.apocrypha.common.APRegistry;
import com.lich.apocrypha.mixin.PlayerControllerInvoker;
import elucent.eidolon.item.WandItem;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SimpleSound;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.Attribute;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.item.Items;
import net.minecraft.item.UseAction;
import net.minecraft.network.play.client.CPlayerDiggingPacket;
import net.minecraft.particles.BlockParticleData;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceContext.FluidMode;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.common.ToolType;
import net.minecraftforge.eventbus.api.Event.Result;

import javax.annotation.Nullable;
import java.util.UUID;

import static java.lang.Math.max;

public class EarthMoverWandItem extends WandItem
{
    private static final UUID WAND_REACH_MODIFIER = UUID.randomUUID();

    private Multimap<Attribute, AttributeModifier> attributeModifiers;

    private final ItemStack diamondPickaxe = new ItemStack(Items.DIAMOND_PICKAXE);
    private final ItemStack diamondShovel  = new ItemStack(Items.DIAMOND_SHOVEL);

    private float curBlockDamageMP;
    private float stepSoundTickCounter;

    public EarthMoverWandItem(Properties properties)
    {
        super(properties);
    }

    @Override
    public float getDestroySpeed(ItemStack stack, BlockState state)
    {
        return max(Items.DIAMOND_PICKAXE.getDestroySpeed(diamondPickaxe, state) * 0.5F,
                Items.DIAMOND_SHOVEL.getDestroySpeed(diamondShovel, state) * 0.5F);
    }

    @Override
    public boolean canHarvestBlock(ItemStack stack, BlockState state)
    {
        return Items.DIAMOND_PICKAXE.canHarvestBlock(diamondPickaxe, state) || Items.DIAMOND_SHOVEL.canHarvestBlock(diamondShovel, state);
    }

    @Override
    public int getHarvestLevel(ItemStack stack, ToolType tool, @Nullable PlayerEntity player, @Nullable BlockState blockState)
    {
        return max(Items.DIAMOND_PICKAXE.getHarvestLevel(diamondPickaxe, ToolType.PICKAXE, player, blockState),
                Items.DIAMOND_SHOVEL.getHarvestLevel(diamondShovel, ToolType.PICKAXE, player, blockState));
    }

    @Override
    public UseAction getUseAction(ItemStack stack)
    {
        return UseAction.CROSSBOW;
    }

    @Override
    public int getUseDuration(ItemStack stack)
    {
        return 72000;
    }

    @Override
    public ActionResultType onItemUse(ItemUseContext context)
    {
        context.getPlayer().setActiveHand(context.getHand());
        return ActionResultType.CONSUME;
    }

    @Override
    public void onUsingTick(ItemStack stack, LivingEntity player, int count)
    {
        if (!player.world.isRemote() || !(player instanceof PlayerEntity))
            return;

        BlockRayTraceResult rayTrace = rayTrace(player.world, (PlayerEntity) player, FluidMode.NONE);
        BlockPos miningPos = rayTrace.getPos();
        if (((PlayerControllerInvoker) Minecraft.getInstance().playerController).invokeIsHittingPosition(miningPos))
        {
            BlockState blockstate = player.world.getBlockState(miningPos);
            if (!blockstate.isAir(player.world, miningPos))
            {
                curBlockDamageMP += blockstate.getPlayerRelativeBlockHardness((PlayerEntity) player, player.world, miningPos);
                if (stepSoundTickCounter % 4.0F == 0.0F)
                {
                    SoundType soundtype = blockstate.getSoundType(player.world, miningPos, player);
                    Minecraft.getInstance().getSoundHandler().play(new SimpleSound(soundtype.getHitSound(), SoundCategory.BLOCKS, (soundtype.getVolume() + 1.0F) / 8.0F, soundtype.getPitch() * 0.5F, miningPos));
                }

                ++stepSoundTickCounter;
                if (ForgeHooks.onLeftClickBlock((PlayerEntity) player, miningPos, rayTrace.getFace()).getUseItem() == Result.DENY)
                    return;
                if (curBlockDamageMP >= 1.0F)
                {
                    ((PlayerControllerInvoker) Minecraft.getInstance().playerController).invokeSendDiggingPacket(CPlayerDiggingPacket.Action.STOP_DESTROY_BLOCK, miningPos, rayTrace.getFace());
                    Minecraft.getInstance().playerController.onPlayerDestroyBlock(miningPos);
                    curBlockDamageMP = 0.0F;
                    stepSoundTickCounter = 0.0F;
                }

                for (int i = 0; i < 3; i++)
                {
                    player.world.addParticle(new BlockParticleData(APRegistry.DIG_PARTICLE.get(), blockstate),
                            miningPos.getX() + 0.5,
                            miningPos.getY() + 0.5,
                            miningPos.getZ() + 0.5,
                            player.getPosX(), player.getPosY() + player.getHeight() / 1.5, player.getPosZ());
                }

                player.world.sendBlockBreakProgress(player.getEntityId(), miningPos, (int) (curBlockDamageMP * 10.0F) - 1);
            }
        }
        else
            Minecraft.getInstance().playerController.clickBlock(miningPos, rayTrace.getFace());
    }

    @Override
    public void onPlayerStoppedUsing(ItemStack stack, World world, LivingEntity entity, int timeLeft)
    {
        if (world.isRemote())
        {
            Minecraft.getInstance().playerController.resetBlockRemoving();
        }
        super.onPlayerStoppedUsing(stack, world, entity, timeLeft);
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(EquipmentSlotType equipmentSlot)
    {
        return equipmentSlot == EquipmentSlotType.MAINHAND ? getOrCreateModifiers() : super.getAttributeModifiers(equipmentSlot);
    }

    private Multimap<Attribute, AttributeModifier> getOrCreateModifiers()
    {
        if (attributeModifiers == null)
        {
            attributeModifiers = ImmutableMultimap.<Attribute, AttributeModifier>builder()
                    .put(ForgeMod.REACH_DISTANCE.get(), new AttributeModifier(WAND_REACH_MODIFIER, "Wand Reach modifier", 10, Operation.ADDITION))
                    .build();
        }
        return attributeModifiers;
    }
}
