package com.panita.tezzlar3.timeline.events;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class DayChangeEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private final int oldDay;
    private final int newDay;

    public DayChangeEvent(int oldDay, int newDay) {
        this.oldDay = oldDay;
        this.newDay = newDay;
    }

    public int getOldDay() {
        return oldDay;
    }

    public int getNewDay() {
        return newDay;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
