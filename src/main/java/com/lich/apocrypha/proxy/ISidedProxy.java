package com.lich.apocrypha.proxy;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;

public interface ISidedProxy
{
    PlayerEntity getPlayer();

    World getWorld();

    void init();
}
