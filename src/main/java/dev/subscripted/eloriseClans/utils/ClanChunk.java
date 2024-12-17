package dev.subscripted.eloriseClans.utils;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@FieldDefaults(level = AccessLevel.PRIVATE)
public class ClanChunk {

    @Getter
    final String world;
    @Getter
    final int chunkX;
    @Getter
    final int chunkZ;
    @Getter
    final String clanPrefix;

    public ClanChunk(String world, int chunkX, int chunkZ, String clanPrefix) {
        this.world = world;
        this.chunkX = chunkX;
        this.chunkZ = chunkZ;
        this.clanPrefix = clanPrefix;
    }

    public String getChunkKey() {
        return chunkX + ";" + chunkZ;
    }

    @Override
    public String toString() {
        return "ClanChunk{" +
                "world='" + world + '\'' +
                ", chunkX=" + chunkX +
                ", chunkZ=" + chunkZ +
                ", clanPrefix='" + clanPrefix + '\'' +
                '}';
    }
}
