package com.lich.DarkArtifice;

import elucent.eidolon.Eidolon;
import net.minecraft.block.Block;
import net.minecraft.entity.EntityType;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.Item;
import net.minecraft.potion.Effect;
import net.minecraft.potion.Potion;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.HashMap;
import java.util.Map;

public class Registry {
    static Map<String, Block> BLOCK_MAP = new HashMap<>();
    static Map<String, Item> ITEM_MAP = new HashMap<>();
    private static Object darkart;
    static DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, DarkArtifice.MODID);
    static DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, DarkArtifice.MODID);
    static DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(ForgeRegistries.ENTITIES, DarkArtifice.MODID);
    static DeferredRegister<TileEntityType<?>> TILE_ENTITIES = DeferredRegister.create(ForgeRegistries.TILE_ENTITIES, DarkArtifice.MODID);
    static DeferredRegister<Effect> POTIONS = DeferredRegister.create(ForgeRegistries.POTIONS, DarkArtifice.MODID);
    static DeferredRegister<Potion> POTION_TYPES = DeferredRegister.create(ForgeRegistries.POTION_TYPES, DarkArtifice.MODID);
    static DeferredRegister<SoundEvent> SOUND_EVENTS = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, DarkArtifice.MODID);
    static DeferredRegister<ContainerType<?>> CONTAINERS = DeferredRegister.create(ForgeRegistries.CONTAINERS, DarkArtifice.MODID);

    static Item.Properties itemProps() {
        return new Item.Properties().group(Eidolon.TAB);
    }
}