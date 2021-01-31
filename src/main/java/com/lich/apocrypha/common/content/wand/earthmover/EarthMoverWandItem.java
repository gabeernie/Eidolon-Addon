package com.lich.apocrypha.common.content.wand.earthmover;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.lich.apocrypha.common.APRegistry;
import com.lich.apocrypha.mixin.PlayerControllerInvoker;
import com.mojang.datafixers.util.Pair;
import elucent.eidolon.item.WandItem;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SimpleSound;
import net.minecraft.entity.ai.attributes.Attribute;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.UseAction;
import net.minecraft.network.play.client.CPlayerDiggingPacket;
import net.minecraft.particles.BlockParticleData;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceContext.FluidMode;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.common.ToolType;
import net.minecraftforge.eventbus.api.Event.Result;

import javax.annotation.Nullable;
import java.util.UUID;

public class EarthMoverWandItem extends WandItem
{
    private static final UUID WAND_REACH_MODIFIER = UUID.randomUUID();

    private Multimap<Attribute, AttributeModifier> attributeModifiers;

    private final ItemStack diamondPickaxe = new ItemStack(Items.DIAMOND_PICKAXE);

    private final Object2ObjectLinkedOpenHashMap<Pair<BlockPos, CPlayerDiggingPacket.Action>, Vector3d> unacknowledgedDiggingPackets = new Object2ObjectLinkedOpenHashMap<>();

    private float curBlockDamageMP;
    private float stepSoundTickCounter;

    public EarthMoverWandItem(Properties properties)
    {
        super(properties);
    }

    @Override
    public float getDestroySpeed(ItemStack stack, BlockState state)
    {
        return Items.DIAMOND_PICKAXE.getDestroySpeed(diamondPickaxe, state) * 2;
    }

    @Override
    public boolean canHarvestBlock(ItemStack stack, BlockState state)
    {
        return Items.DIAMOND_PICKAXE.canHarvestBlock(diamondPickaxe, state);
    }

    @Override
    public int getHarvestLevel(ItemStack stack, ToolType tool, @Nullable PlayerEntity player, @Nullable BlockState blockState)
    {
        return Items.DIAMOND_PICKAXE.getHarvestLevel(diamondPickaxe, ToolType.PICKAXE, player, blockState);
    }

    @Override
    public UseAction getUseAction(ItemStack stack)
    {
        return UseAction.NONE;
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, PlayerEntity entity, Hand hand)
    {
        ItemStack stack = entity.getHeldItem(hand);

        BlockRayTraceResult rayTrace = rayTrace(world, entity, FluidMode.NONE);
        BlockPos miningPos = rayTrace.getPos();

        if (world.isRemote())
        {
            if (((PlayerControllerInvoker) Minecraft.getInstance().playerController).invokeIsHittingPosition(miningPos))
            {
                BlockState blockstate = world.getBlockState(miningPos);
                if (blockstate.isAir(world, miningPos))
                    return ActionResult.resultPass(stack);
                else
                {
                    curBlockDamageMP += blockstate.getPlayerRelativeBlockHardness(entity, world, miningPos);
                    if (stepSoundTickCounter % 4.0F == 0.0F)
                    {
                        SoundType soundtype = blockstate.getSoundType(world, miningPos, entity);
                        Minecraft.getInstance().getSoundHandler().play(new SimpleSound(soundtype.getHitSound(), SoundCategory.BLOCKS, (soundtype.getVolume() + 1.0F) / 8.0F, soundtype.getPitch() * 0.5F, miningPos));
                    }

                    ++stepSoundTickCounter;
                    if (ForgeHooks.onLeftClickBlock(entity, miningPos, rayTrace.getFace()).getUseItem() == Result.DENY)
                        return ActionResult.resultPass(stack);
                    if (curBlockDamageMP >= 1.0F)
                    {
                        ((PlayerControllerInvoker) Minecraft.getInstance().playerController).invokeSendDiggingPacket(CPlayerDiggingPacket.Action.STOP_DESTROY_BLOCK, miningPos, rayTrace.getFace());
                        Minecraft.getInstance().playerController.onPlayerDestroyBlock(miningPos);
                        curBlockDamageMP = 0.0F;
                        stepSoundTickCounter = 0.0F;
                    }

                    for (int i = 0; i < 4; i++)
                    {
                        world.addParticle(new BlockParticleData(APRegistry.DIG_PARTICLE.get(), blockstate),
                                miningPos.getX() + 0.5,
                                miningPos.getY() + 0.5,
                                miningPos.getZ() + 0.5,
                                entity.getPosX(), entity.getPosY() + entity.getHeight() / 2, entity.getPosZ());
                    }

                    world.sendBlockBreakProgress(entity.getEntityId(), miningPos, (int) (curBlockDamageMP * 10.0F) - 1);
                    return ActionResult.resultFail(stack);
                }
            }
            else
                Minecraft.getInstance().playerController.clickBlock(miningPos, rayTrace.getFace());
        }
        return ActionResult.resultFail(stack);
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
