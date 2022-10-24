package com.spleefleague.core.util;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.BlockPosition;
import com.comphenix.protocol.wrappers.ChunkCoordIntPair;
import com.comphenix.protocol.wrappers.MultiBlockChangeInfo;
import com.comphenix.protocol.wrappers.WrappedBlockData;
import com.mojang.authlib.GameProfile;
import com.spleefleague.core.Core;
import com.spleefleague.core.logger.CoreLogger;
import com.spleefleague.core.player.CoreOfflinePlayer;
import com.spleefleague.core.util.packet.BlockPalette;
import com.spleefleague.core.util.packet.ByteBufferReader;
import com.spleefleague.core.util.packet.ChunkData;
import com.spleefleague.core.util.packet.ChunkSection;
import com.spleefleague.core.util.variable.MultiBlockChange;
import com.spleefleague.core.world.ChunkCoord;
import com.spleefleague.core.world.FakeBlock;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoPacket;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.Bukkit;
import org.bukkit.block.data.BlockData;
import org.bukkit.craftbukkit.v1_19_R1.CraftServer;
import org.bukkit.craftbukkit.v1_19_R1.CraftWorld;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * @author NickM13 and Jonas
 * @since 4/21/2020
 */
public class PacketUtils {

    private static ServerPlayer mapToServerPlayer(UUID uuid, String name) {
        return new ServerPlayer(
                ((CraftServer) Bukkit.getServer()).getServer(),
                ((CraftWorld) Core.OVERWORLD).getHandle(),
                new GameProfile(
                        uuid,
                        name
                ),
                null
        );
    }

    public static PacketContainer createAddPlayerPacket(List<CoreOfflinePlayer> corePlayers) {
        ClientboundPlayerInfoPacket playerInfoPacket = new ClientboundPlayerInfoPacket(
                ClientboundPlayerInfoPacket.Action.ADD_PLAYER,
                corePlayers.stream()
                        .map(cp -> mapToServerPlayer(cp.getUniqueId(), cp.getName()))
                        .collect(Collectors.toList()));
        return new PacketContainer(PacketType.Play.Server.PLAYER_INFO, playerInfoPacket);
    }

    public static PacketContainer createRemovePlayerPacket(List<UUID> uuids) {
        ClientboundPlayerInfoPacket playerInfoPacket = new ClientboundPlayerInfoPacket(
                ClientboundPlayerInfoPacket.Action.REMOVE_PLAYER,
                uuids.stream()
                        .map(uuid -> mapToServerPlayer(uuid, null))
                        .collect(Collectors.toList()));
        return new PacketContainer(PacketType.Play.Server.PLAYER_INFO, playerInfoPacket);
    }

    public static PacketContainer createBlockChangePacket(BlockPosition blockPos, FakeBlock fakeBlock) {
        PacketContainer packetContainer = new PacketContainer(PacketType.Play.Server.BLOCK_CHANGE);

        packetContainer.getBlockPositionModifier().write(0, blockPos);
        packetContainer.getBlockData().write(0, WrappedBlockData.createData(fakeBlock.blockData().getMaterial()));

        return packetContainer;
    }

    public static PacketContainer createMultiBlockChangePacket(ChunkCoord chunkCoord, List<MultiBlockChange> fakeChunkBlocks) {
        PacketContainer packetContainer = new PacketContainer(PacketType.Play.Server.MULTI_BLOCK_CHANGE);

        ChunkCoordIntPair chunkCoordIntPair = chunkCoord.toChunkCoordIntPair();
        packetContainer.getChunkCoordIntPairs().write(0, chunkCoordIntPair);

        MultiBlockChangeInfo[] multiBlockChangeInfoArray = new MultiBlockChangeInfo[fakeChunkBlocks.size()];
        int i = 0;
        for (MultiBlockChange change : fakeChunkBlocks) {
            multiBlockChangeInfoArray[i] = new MultiBlockChangeInfo(
                    (short) (((change.pos & 0x000F) << 12) | ((change.pos & 0x00F0) << 4) | ((change.pos & 0xFF00) >> 8)),
                    change.blockData,
                    chunkCoordIntPair);
            i++;
        }

        packetContainer.getMultiBlockChangeInfoArrays().write(0, multiBlockChangeInfoArray);

        return packetContainer;
    }

    public static void writeFakeChunkDataPacket(PacketContainer mapChunkPacket, Map<Short, FakeBlock> fakeBlocks) {
        if (fakeBlocks.isEmpty()) return;
        Map<Integer, Map<Short, FakeBlock>> sectionMap = toSectionMap(fakeBlocks);
        try {
            byte[] bytes = mapChunkPacket.getByteArrays().read(0);
            int bitmask = mapChunkPacket.getIntegers().read(2);
            int originalMask = bitmask;
            for (int i : sectionMap.keySet()) {
                bitmask |= 1 << i;
            }
            ChunkData chunkData = splitToChunkSections(bitmask, originalMask, bytes);
            insertFakeBlocks(chunkData.getSections(), sectionMap);

            byte[] data = toByteArray(chunkData);
            mapChunkPacket.getByteArrays().write(0, data);
            mapChunkPacket.getIntegers().write(2, bitmask);
        } catch (NullPointerException | IOException exception) {
            CoreLogger.logError(exception);
        }
    }

    private static byte[] toByteArray(ChunkData data) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        for (ChunkSection section : data.getSections()) {
            if (section != null) {
                writeChunkSectionData(baos, section);
            }
        }
        baos.write(data.getAdditionalData());
        return baos.toByteArray();
    }

    private static void writeChunkSectionData(ByteArrayOutputStream baos, ChunkSection section) throws IOException {
        BlockData[] used = section.getContainedBlocks();
        BlockPalette palette;
        if (used == null) {
            palette = BlockPalette.GLOBAL;
        } else {
            palette = BlockPalette.createPalette(used);
        }
        short nonAirCount = section.getNonAirCount();
        byte bpb = (byte) palette.getBitsPerBlock();
        int paletteLength = palette.getLength();
        int[] paletteInfo;
        if (paletteLength == 0) {
            paletteInfo = new int[0];
        } else {
            paletteInfo = palette.getPaletteData();
        }
        baos.write((nonAirCount >> 8) & 0xFF);
        baos.write(nonAirCount & 0xFF);
        baos.write(bpb);
        if (palette.includePaletteLength()) {
            ByteBufferReader.writeVarIntToByteArrayOutputStream(paletteLength, baos);
        }
        for (int p : paletteInfo) {
            ByteBufferReader.writeVarIntToByteArrayOutputStream(p, baos);
        }
        byte[] blockdata = palette.encode(section.getBlockData());
        ByteBufferReader.writeVarIntToByteArrayOutputStream(blockdata.length / 8/*it's represented as a long array*/, baos);
        baos.write(blockdata);
    }

    private static void insertFakeBlocks(ChunkSection[] sections, Map<Integer, Map<Short, FakeBlock>> blocks) {
        for (Map.Entry<Integer, Map<Short, FakeBlock>> sectionEntry : blocks.entrySet()) {
            int id = sectionEntry.getKey();
            ChunkSection section = sections[id];
            short airChange = 0;
            for (Map.Entry<Short, FakeBlock> blockEntry : sectionEntry.getValue().entrySet()) {
                int sectionPos = blockEntry.getKey() & 0xFFF;
                boolean previouslyAir = section.getBlockRelative(sectionPos).getMaterial().isAir();
                section.setBlockRelative(blockEntry.getValue().blockData(), sectionPos);
                if (previouslyAir) {
                    if (!blockEntry.getValue().blockData().getMaterial().isAir()) {
                        airChange++;
                    }
                } else {
                    if (blockEntry.getValue().blockData().getMaterial().isAir()) {
                        airChange--;
                    }
                }
            }
            section.setNonAirCount((short) (section.getNonAirCount() + airChange));
        }
    }

    private static ChunkData splitToChunkSections(int bitmask, int originalMask, byte[] data) {
        ChunkSection[] sections = new ChunkSection[16];
        ByteBuffer buffer = ByteBuffer.wrap(data);
        ByteBufferReader bbr = new ByteBufferReader(buffer);
        for (int i = 0; i < 16; i++) {
            if ((bitmask & 0x8000 >> (15 - i)) != 0) {
                if ((originalMask & 0x8000 >> (15 - i)) != 0) {
                    short nonAirCount = buffer.getShort();
                    short bpb = (short) Byte.toUnsignedInt(buffer.get());
                    BlockPalette palette;
                    if (bpb <= 8) {
                        int paletteLength = bbr.readVarInt();
                        int[] paletteData = new int[paletteLength];
                        for (int j = 0; j < paletteLength; j++) {
                            paletteData[j] =
                                    bbr.readVarInt();
                        }
                        palette = BlockPalette.createPalette(paletteData, bpb);
                    } else {
                        palette = BlockPalette.GLOBAL;
                    }
                    int dataLength = bbr.readVarInt();
                    byte[] blockData = new byte[dataLength * 8];
                    buffer.get(blockData);
                    sections[i] = new ChunkSection(blockData, nonAirCount, palette);
                } else {
                    sections[i] = new ChunkSection(true);
                }
            }
        }

        byte[] additional = new byte[data.length - buffer.position()];
        buffer.get(additional);
        return new ChunkData(sections, additional);
    }

    private static Map<Integer, Map<Short, FakeBlock>> toSectionMap(Map<Short, FakeBlock> fakeBlocks) {
        Map<Integer, Map<Short, FakeBlock>> sectionMap = new HashMap<>();
        for (Map.Entry<Short, FakeBlock> fb : fakeBlocks.entrySet()) {
            int section = (fb.getKey() >> 12) & 0xF;
            if (!sectionMap.containsKey(section)) {
                sectionMap.put(section, new HashMap<>());
            }
            sectionMap.get(section).put(fb.getKey(), fb.getValue());
        }
        return sectionMap;
    }

}
