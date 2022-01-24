package xyz.oli.pathing.model.wrapper;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;

public class BukkitConverter {

    public static Location toLocation(PathLocation pathLocation) {
        return new Location(pathLocation.getPathWorld().getWorld(), pathLocation.getX(), pathLocation.getY(), pathLocation.getZ());
    }

    public static PathLocation toPathLocation(Location location) {
        return new PathLocation(new PathWorld(location.getWorld().getUID()), location.getBlockX(), location.getBlockY(), location.getBlockZ());
    }

    public static Block toBlock(PathBlock pathBlock) {
        return toLocation(pathBlock.getPathLocation()).getBlock();
    }

    public static PathBlock toPathBlock(Block block) {
        return new PathBlock(new PathLocation(new PathWorld(block.getWorld().getUID()), block.getX(), block.getY(), block.getZ()), toPathBlockType(block));
    }

    public static PathBlockType toPathBlockType(Block block) {

        if (block.isLiquid()) return PathBlockType.LIQUID;
        else if (block.isEmpty()) return PathBlockType.AIR;
        else if (block.isPassable()) return PathBlockType.OTHER;
        else if (block.getType().isSolid()) return PathBlockType.SOLID;
        else return PathBlockType.SOLID;
    }

    public static PathBlockType toPathBlockType(Material material) {

        if (material.isAir()) return PathBlockType.AIR;
        else if (material.isSolid()) return PathBlockType.SOLID;

        switch (material) {
            case WATER, LAVA -> {
                return PathBlockType.LIQUID;
            }
            case GRASS, TALL_GRASS -> {
                return PathBlockType.OTHER;
            }
        }

        return PathBlockType.SOLID;
    }
}
