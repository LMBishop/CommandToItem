package com.leonardobishop.commandtoitem.utils;

import org.bukkit.Location;

import java.util.List;

public class RegionCondition {

    private List<Region> regions;
    private ConditionType type;

    public RegionCondition(List<Region> regions) {
        this.regions = regions;
    }

    public boolean validate(Location location) {
        boolean inRegion = false;

        for (Region region : regions) {
            if (region.isInsideRegion(location)) inRegion = true;
        }

        if (type == ConditionType.PERMIT) return inRegion;
        else if (type == ConditionType.EXCLUDE) return !inRegion;
        else return false;
    }

    enum ConditionType {
        EXCLUDE,
        PERMIT;
    }
}
