package com.panita.tezzlar3.missions.data;

import org.bukkit.configuration.ConfigurationSection;
import java.util.List;
import java.util.Map;

public class Mission {
    private final String id;
    private final String name;
    private final int startDay;
    private final int endDay;
    private final String scope;
    
    private final String objectiveType;
    private final String objectiveTarget;
    private final int objectiveAmount;
    private final String objectiveEnvironment;
    private final String objectiveLocation;
    private final int objectiveRadius;
    
    private final List<Map<?, ?>> rewards;
    private final List<Map<?, ?>> punishments;

    public Mission(String id, ConfigurationSection section) {
        this.id = id;
        this.name = section.getString("name");
        this.startDay = section.getInt("start_day");
        this.endDay = section.getInt("end_day");
        this.scope = section.getString("scope", "INDIVIDUAL");
        
        ConfigurationSection obj = section.getConfigurationSection("objective");
        this.objectiveType = obj.getString("type");
        this.objectiveTarget = obj.getString("target");
        this.objectiveAmount = obj.getInt("amount");
        this.objectiveEnvironment = obj.getString("environment");
        this.objectiveLocation = obj.getString("location");
        this.objectiveRadius = obj.getInt("radius", 20);
        
        this.rewards = section.getMapList("rewards");
        this.punishments = section.getMapList("punishments");
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public int getStartDay() { return startDay; }
    public int getEndDay() { return endDay; }
    public String getScope() { return scope; }
    public String getObjectiveType() { return objectiveType; }
    public String getObjectiveTarget() { return objectiveTarget; }
    public int getObjectiveAmount() { return objectiveAmount; }
    public String getObjectiveEnvironment() { return objectiveEnvironment; }
    public String getObjectiveLocation() { return objectiveLocation; }
    public int getObjectiveRadius() { return objectiveRadius; }
    public List<Map<?, ?>> getRewards() { return rewards; }
    public List<Map<?, ?>> getPunishments() { return punishments; }
}
