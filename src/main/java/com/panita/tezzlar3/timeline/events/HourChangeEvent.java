package com.panita.tezzlar3.timeline.events;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class HourChangeEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private final int newHour;

    public HourChangeEvent(int newHour) {
        this.newHour = newHour;
    }

    public int getNewHour() {
        return newHour;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
