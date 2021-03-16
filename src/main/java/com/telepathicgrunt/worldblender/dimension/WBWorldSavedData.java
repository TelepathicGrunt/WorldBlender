package com.telepathicgrunt.worldblender.dimension;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.types.templates.CompoundList;
import com.telepathicgrunt.worldblender.WorldBlender;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ClassInheritanceMultiMap;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunk;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.DimensionSavedDataManager;
import net.minecraft.world.storage.WorldSavedData;
import net.minecraftforge.event.TickEvent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;


public class WBWorldSavedData extends WorldSavedData {
    private static final String ALTAR_DATA = WorldBlender.MODID + "AltarMade";
    private static final WBWorldSavedData CLIENT_DUMMY = new WBWorldSavedData();
    private boolean wbAltarMade;

    public WBWorldSavedData() {
        super(ALTAR_DATA);
    }

    public static WBWorldSavedData get(World world) {
        if (!(world instanceof ServerWorld)) {
            return CLIENT_DUMMY;
        }

        DimensionSavedDataManager storage = ((ServerWorld) world).getSavedData();
        return storage.getOrCreate(WBWorldSavedData::new, ALTAR_DATA);
    }

    @Override
    public void read(CompoundNBT data) {
        wbAltarMade = data.getBoolean("WBAltarMade");
    }

    @Override
    public CompoundNBT write(CompoundNBT data) {
        data.putBoolean("WBAltarMade", wbAltarMade);
        return data;
    }

    public void setWBAltarState(boolean state) {
        this.wbAltarMade = state;
    }

    public boolean getWBAltarState() {
        return this.wbAltarMade;
    }
}