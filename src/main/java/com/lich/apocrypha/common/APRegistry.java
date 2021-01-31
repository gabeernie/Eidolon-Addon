package com.lich.apocrypha.common;

import com.lich.apocrypha.Apocrypha;
import com.lich.apocrypha.common.content.wand.earthmover.EarthMoverWandItem;
import com.lich.apocrypha.common.content.wand.earthmover.WandDigParticleType;
import com.lich.apocrypha.common.content.wand.lightning.LightningBoltProjectileEntity;
import com.lich.apocrypha.common.content.wand.lightning.LightningMagnetEffect;
import com.lich.apocrypha.common.content.wand.lightning.LightningWandItem;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EntityType.Builder;
import net.minecraft.entity.EntityType.IFactory;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.particles.ParticleType;
import net.minecraft.potion.Effect;
import net.minecraft.potion.Potion;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ParticleFactoryRegisterEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.HashMap;
import java.util.Map;

public class APRegistry
{
    final static Map<String, Block> BLOCK_MAP = new HashMap<>();
    final static Map<String, Item>  ITEM_MAP  = new HashMap<>();

    static DeferredRegister<Block>             BLOCKS        = DeferredRegister.create(ForgeRegistries.BLOCKS, Apocrypha.MODID);
    static DeferredRegister<Item>              ITEMS         = DeferredRegister.create(ForgeRegistries.ITEMS, Apocrypha.MODID);
    static DeferredRegister<EntityType<?>>     ENTITIES      = DeferredRegister.create(ForgeRegistries.ENTITIES, Apocrypha.MODID);
    static DeferredRegister<TileEntityType<?>> TILE_ENTITIES = DeferredRegister.create(ForgeRegistries.TILE_ENTITIES, Apocrypha.MODID);
    static DeferredRegister<Effect>            POTIONS       = DeferredRegister.create(ForgeRegistries.POTIONS, Apocrypha.MODID);
    static DeferredRegister<Potion>            POTION_TYPES  = DeferredRegister.create(ForgeRegistries.POTION_TYPES, Apocrypha.MODID);
    static DeferredRegister<SoundEvent>        SOUND_EVENTS  = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, Apocrypha.MODID);
    static DeferredRegister<ContainerType<?>>  CONTAINERS    = DeferredRegister.create(ForgeRegistries.CONTAINERS, Apocrypha.MODID);
    static DeferredRegister<ParticleType<?>>   PARTICLES     = DeferredRegister.create(ForgeRegistries.PARTICLE_TYPES, Apocrypha.MODID);

    public static final RegistryObject<EntityType<LightningBoltProjectileEntity>> LIGHTNING_BOLT_PROJECTILE = addEntity("lightningbolt_projectile", 0.4F, 0.4F, LightningBoltProjectileEntity::new, EntityClassification.MISC);

    public static final RegistryObject<SoundEvent> CAST_LIGHTNING_BOLT_EVENT   = addSound("cast_lightning_bolt");
    public static final RegistryObject<SoundEvent> SPLASH_LIGHTNING_BOLT_EVENT = addSound("splash_lightning_bolt");

    public static final RegistryObject<Effect> LIGHTNING_MAGNET_EFFECT = POTIONS.register("lightning_magnet", LightningMagnetEffect::new);

    public static final RegistryObject<Item> LIGHTNING_WAND_ITEM  = addItem(new LightningWandItem(itemProps()), "lightning_wand");
    public static final RegistryObject<Item> EARTHMOVER_WAND_ITEM = addItem(new EarthMoverWandItem(itemProps()), "earthmover_wand");

    public static final RegistryObject<WandDigParticleType> DIG_PARTICLE = PARTICLES.register("dig_particle", WandDigParticleType::new);

    static Item.Properties itemProps()
    {
        return new Item.Properties().group(Apocrypha.TAB);
    }

    static RegistryObject<SoundEvent> addSound(String name)
    {
        SoundEvent event = new SoundEvent(new ResourceLocation(Apocrypha.MODID, name));
        return SOUND_EVENTS.register(name, () -> event);
    }

    static <T extends Entity> RegistryObject<EntityType<T>> addEntity(String name, float width, float height, IFactory<T> factory, EntityClassification kind)
    {
        EntityType<T> type = Builder.create(factory, kind).setTrackingRange(64).setUpdateInterval(1).size(width, height).build(Apocrypha.MODID + ":" + name);
        return ENTITIES.register(name, () -> type);
    }

    static <T extends Entity> RegistryObject<EntityType<T>> addEntity(String name, int primaryColor, int secondaryColor, float width, float height, IFactory<T> factory, EntityClassification kind)
    {
        EntityType<T> type = Builder.create(factory, kind).setTrackingRange(64).setUpdateInterval(1).size(width, height).build(Apocrypha.MODID + ":" + name);
        ITEMS.register("spawn_" + name, () -> new SpawnEggItem(type, primaryColor, secondaryColor, itemProps().group(ItemGroup.MISC)));
        return ENTITIES.register(name, () -> type);
    }

    static RegistryObject<Item> addItem(String name)
    {
        Item item = new Item(itemProps());
        ITEM_MAP.put(name, item);
        return ITEMS.register(name, () -> item);
    }

    static RegistryObject<Item> addItem(Item item, String name)
    {
        ITEM_MAP.put(name, item);
        return ITEMS.register(name, () -> item);
    }

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public void registerFactories(ParticleFactoryRegisterEvent event)
    {
        Minecraft.getInstance().particles.registerFactory(DIG_PARTICLE.get(), WandDigParticleType.Factory::new);
    }

    public static void init()
    {
        BLOCKS.register(FMLJavaModLoadingContext.get().getModEventBus());
        ITEMS.register(FMLJavaModLoadingContext.get().getModEventBus());
        ENTITIES.register(FMLJavaModLoadingContext.get().getModEventBus());
        POTIONS.register(FMLJavaModLoadingContext.get().getModEventBus());
        POTION_TYPES.register(FMLJavaModLoadingContext.get().getModEventBus());
        TILE_ENTITIES.register(FMLJavaModLoadingContext.get().getModEventBus());
        SOUND_EVENTS.register(FMLJavaModLoadingContext.get().getModEventBus());
        CONTAINERS.register(FMLJavaModLoadingContext.get().getModEventBus());
        PARTICLES.register(FMLJavaModLoadingContext.get().getModEventBus());
    }

    public static void clientInit()
    {

    }
}