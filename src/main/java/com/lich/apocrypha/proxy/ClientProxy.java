package com.lich.apocrypha.proxy;

import com.lich.apocrypha.Apocrypha;
import com.lich.apocrypha.common.APRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

public class ClientProxy implements ISidedProxy
{
    @Override
    public PlayerEntity getPlayer()
    {
        return Minecraft.getInstance().player;
    }

    @Override
    public World getWorld()
    {
        return Minecraft.getInstance().world;
    }

    @Override
    public void init()
    {
        APRegistry.clientInit();
        FMLJavaModLoadingContext.get().getModEventBus().addListener(Apocrypha::clientSetup);
    }
}