package com.telepathicgrunt.worldblender.entities;

import com.telepathicgrunt.worldblender.WorldBlender;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class WBEntities {
    public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(ForgeRegistries.ENTITIES, WorldBlender.MODID);

    public static final RegistryObject<EntityType<ItemClearingEntity>> ITEM_CLEARING_ENTITY = ENTITIES.register("honey_slime", () ->
            EntityType.Builder.<ItemClearingEntity>create(ItemClearingEntity::new, EntityClassification.MISC)
                    .size(0, 0).trackingRange(0).build("item_clearing_entity"));
}
