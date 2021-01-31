package com.lich.apocrypha;

import com.lich.apocrypha.common.APRegistry;
import com.lich.apocrypha.proxy.ClientProxy;
import com.lich.apocrypha.proxy.ISidedProxy;
import com.lich.apocrypha.proxy.ServerProxy;
import elucent.eidolon.Registry;
import elucent.eidolon.entity.EmptyRenderer;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(Apocrypha.MODID)
public class Apocrypha
{
    public static       ISidedProxy proxy = DistExecutor.safeRunForDist(() -> ClientProxy::new, () -> ServerProxy::new);
    public static final String      MODID = "apocrypha";

    public static final ItemGroup TAB = new ItemGroup(MODID)
    {
        @Override
        public ItemStack createIcon()
        {
            return new ItemStack(Registry.SHADOW_GEM.get(), 1);
        }
    };

    public Apocrypha()
    {
        proxy.init();
        APRegistry.init();

        FMLJavaModLoadingContext.get().getModEventBus().register(new APRegistry());
    }

    @OnlyIn(Dist.CLIENT)
    public static void clientSetup(FMLClientSetupEvent event)
    {
        RenderingRegistry.registerEntityRenderingHandler(APRegistry.LIGHTNING_BOLT_PROJECTILE.get(), EmptyRenderer::new);
    }
}