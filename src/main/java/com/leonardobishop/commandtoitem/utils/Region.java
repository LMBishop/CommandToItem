package com.leonardobishop.commandtoitem.utils;

import org.bukkit.Location;

public class Region {

    private Location locationOne;
    private Location locationTwo;

    public Region(Location locationOne, Location locationTwo) {
        this.locationOne = locationOne;
        this.locationTwo = locationTwo;
    }

    public boolean isInsideRegion(Location location) {
        if (locationOne == null || locationTwo == null || locationOne.getWorld() == null || locationTwo.getWorld() == null) {
            return false;
        }
        if (!location.getWorld().getName().equalsIgnoreCase(locationOne.getWorld().getName())) {
            return false;
        }
        int x1 = getLowestX();
        int z1 = getLowestZ();
        int x2 = getHighestX();
        int z2 = getHighestZ();

        return (location.getBlockX() >= x1 && location.getBlockX() <= x2) && (location.getBlockZ() >= z1 && location.getBlockZ() <= z2);
    }

    public int getLowestX() {
        return Math.min(locationOne.getBlockX(), locationTwo.getBlockX());
    }

    public int getLowestZ() {
        return Math.min(locationOne.getBlockZ(), locationTwo.getBlockZ());
    }

    public int getHighestX() {
        return Math.max(locationOne.getBlockX(), locationTwo.getBlockX());
    }

    public int getHighestZ() {
        return Math.max(locationOne.getBlockZ(), locationTwo.getBlockZ());
    }

}
