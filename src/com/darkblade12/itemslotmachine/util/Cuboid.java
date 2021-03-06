package com.darkblade12.itemslotmachine.util;

import java.util.Iterator;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;

public final class Cuboid implements Iterable<Block> {
    private String worldName;
    private int x1;
    private int y1;
    private int z1;
    private int x2;
    private int y2;
    private int z2;

    public Cuboid(Location l1, Location l2) {
        if (l1 == null || l2 == null) {
            throw new NullPointerException("Location cannot be null");
        } else if (l1.getWorld() == null) {
            throw new IllegalArgumentException("Cannot create a cuboid for an unloaded world");
        } else if (!l1.getWorld().getName().equals(l2.getWorld().getName())) {
            throw new IllegalArgumentException("Cannot create a cuboid between two different worlds");
        }

        worldName = l1.getWorld().getName();
        x1 = Math.min(l1.getBlockX(), l2.getBlockX());
        y1 = Math.min(l1.getBlockY(), l2.getBlockY());
        z1 = Math.min(l1.getBlockZ(), l2.getBlockZ());
        x2 = Math.max(l1.getBlockX(), l2.getBlockX());
        y2 = Math.max(l1.getBlockY(), l2.getBlockY());
        z2 = Math.max(l1.getBlockZ(), l2.getBlockZ());
    }

    public Cuboid(SafeLocation l1, SafeLocation l2) {
        this(l1.getBukkitLocation(), l2.getBukkitLocation());
    }

    public boolean isInside(Location location) {
        if (!location.getWorld().getName().equals(worldName)) {
            return false;
        }

        int x = location.getBlockX();
        int y = location.getBlockY();
        int z = location.getBlockZ();
        return x >= x1 && x <= x2 && y >= y1 && y <= y2 && z >= z1 && z <= z2;
    }

    public boolean contains(Material material) {
        if (material.isBlock()) {
            throw new IllegalArgumentException("'" + material.name() + "' is not a valid block material");
        }

        for (Block block : this) {
            if (block.getType() == material) {
                return true;
            }
        }

        return false;
    }

    public boolean isEmpty() {
        for (Block block : this) {
            if (block.getType() != Material.AIR) {
                return false;
            }
        }

        return true;
    }

    public void setBlocks(Material material) {
        if (material.isBlock()) {
            throw new IllegalArgumentException("'" + material.name() + "' is not a valid block material");
        }

        for (Block block : this) {
            block.setType(material);
        }
    }

    public int getSizeX() {
        return (x2 - x1) + 1;
    }

    public int getSizeY() {
        return (y2 - y1) + 1;
    }

    public int getSizeZ() {
        return (z2 - z1) + 1;
    }

    public int getVolume() {
        return getSizeX() * getSizeY() * getSizeZ();
    }

    public Location getLowerNE() {
        return new Location(this.getWorld(), this.x1, this.y1, this.z1);
    }

    public Location getUpperSW() {
        return new Location(this.getWorld(), this.x2, this.y2, this.z2);
    }

    public World getWorld() {
        World world = Bukkit.getWorld(worldName);
        if (world == null) {
            throw new IllegalStateException("World '" + worldName + "' is not loaded");
        }

        return world;
    }

    @Override
    public Iterator<Block> iterator() {
        return new CuboidIterator(getWorld(), x1, y1, z1, x2, y2, z2);
    }

    private final class CuboidIterator implements Iterator<Block> {
        private World world;
        private int baseX;
        private int baseY;
        private int baseZ;
        private int sizeX;
        private int sizeY;
        private int sizeZ;
        private int x;
        private int y;
        private int z;

        public CuboidIterator(World world, int x1, int y1, int z1, int x2, int y2, int z2) {
            this.world = world;
            baseX = x1;
            baseY = y1;
            baseZ = z1;
            sizeX = Math.abs(x2 - x1) + 1;
            sizeY = Math.abs(y2 - y1) + 1;
            sizeZ = Math.abs(z2 - z1) + 1;
        }

        @Override
        public boolean hasNext() {
            return x < sizeX && y < sizeY && z < sizeZ;
        }

        @Override
        public Block next() {
            Block block = world.getBlockAt(baseX + x, baseY + y, baseZ + z);
            if (++x >= sizeX) {
                x = 0;
                if (++y >= sizeY) {
                    y = 0;
                    ++z;
                }
            }

            return block;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }
}