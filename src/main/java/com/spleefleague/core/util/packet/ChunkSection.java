package com.spleefleague.core.util.packet;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.bukkit.Material;
import org.bukkit.block.data.BlockData;

/**
 * @author Jonas
 */
public class ChunkSection {

    private final BlockData[] blocks;
    private boolean modified = false;
    private BlockData[] paletteBlocks;//Cached palette blocks array
    private final Set<BlockData> paletteBlockSet;
    private short nonAirCount;

    /**
     * @param blockdata Block data array described as in http://wiki.vg/Chunk_Format
     * @param palette   Block palette object
     */
    public ChunkSection(byte[] blockdata, short nonAirCount, BlockPalette palette) {
        this.blocks = palette.decode(blockdata);
        this.nonAirCount = nonAirCount;
        paletteBlocks = palette.getBlocks();//Null for the global palette
        if (paletteBlocks != null) {
            paletteBlockSet = new HashSet<>();
            Collections.addAll(paletteBlockSet, paletteBlocks);
        } else {
            paletteBlockSet = null;
        }
    }

    public ChunkSection(boolean overworld) {
        BlockData air = Material.AIR.createBlockData();
        blocks = new BlockData[4096];
        Arrays.fill(blocks, air);
        this.nonAirCount = 0;
        //An empty, unsent chunksection contains air blocks
        paletteBlocks = new BlockData[]{air};
        paletteBlockSet = new HashSet<>();
        paletteBlockSet.add(air);
    }

    public short getNonAirCount() {
        return nonAirCount;
    }

    public void setNonAirCount(short nonAirCount) {
        this.nonAirCount = nonAirCount;
    }

    public BlockData getBlockRelative(int pos) {
        return blocks[pos];
    }

    public void setBlockRelative(BlockData data, int pos) {
        blocks[pos] = data;
        modified = true;
        if (paletteBlockSet != null) {
            if (paletteBlockSet.add(data)) {
                paletteBlocks = null;
            }
        }
    }

    public BlockData[] getBlockData() {
        return blocks;
    }

    public boolean isModified() {
        return modified;
    }

    public BlockData[] getContainedBlocks() {
        if (paletteBlocks == null) {
            if (paletteBlockSet == null) {
                return null;
            }
            paletteBlocks = paletteBlockSet.toArray(new BlockData[0]);
        }
        return paletteBlocks;
    }
}
