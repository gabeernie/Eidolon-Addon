package com.lich.apocrypha.mixin;

import net.minecraft.client.multiplayer.PlayerController;
import net.minecraft.network.play.client.CPlayerDiggingPacket;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(PlayerController.class)
public interface PlayerControllerInvoker
{
    @Invoker("sendDiggingPacket")
    public void invokeSendDiggingPacket(CPlayerDiggingPacket.Action action, BlockPos pos, Direction dir);

    @Invoker("isHittingPosition")
    public boolean invokeIsHittingPosition(BlockPos pos);
}