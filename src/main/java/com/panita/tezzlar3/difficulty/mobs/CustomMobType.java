package com.panita.tezzlar3.difficulty.mobs;

import org.bukkit.Material;

public enum CustomMobType {
    ZOMBIE_BEEKEEPER(Material.ZOMBIE_HEAD, "&eZombi Apicultor", 30.0, "- Invoca abejas al recibir daño<newline>- Es inmune a Sharpness & Smite"),
    INFRARED_SKELETON(Material.SKELETON_SKULL, "&cEsqueleto Infrarrojo", 20.0, "Ataca con arco de Power III."),
    REALISTIC_SPIDER(Material.SPIDER_EYE, "&4Araña Realista", 4.0, "Araña pequeña que aparece en grupos grandes"),
    PARASITE_SILVERFISH(Material.SILVERFISH_SPAWN_EGG, "<#F03C57>Lepisma Parásito</#F03C57>", 16.0, "- Se monta en el jugador<newline>- Se puede duplicar<newline>- Hace daño exponencial"),
    SHINY_PIGLIN(Material.PIGLIN_HEAD, "&6Piglin Brillante", 100.0, "- Tiene mucha vida y buen arma<newline>- Suelta mucho oro al morir"),
    LIGHTNING_SKELETON(Material.SKELETON_SKULL, "<#894B0A>Esqueleto Relámpago</#894B0A>", 20.0, "Dispara flechas que invocan rayos eléctricos."),
    GIGA_MAGMA_CUBE(Material.MAGMA_CREAM, "<#E88331><b>Giga Magma Cube</b></#E88331>", 1000.0, "<red>No se recomienda invocar</red><newline>- Mini Jefe con múltiples ataques<newline>- Diseñado para multijugador"),
    SLIME_BANZAI(Material.SLIME_BALL, "<#7EF7B6>Slime Banzai</#7EF7B6>", 20.0, "Una variante muy resbalosa"),
    PERUVIAN_VINDICATOR(Material.VINDICATOR_SPAWN_EGG, "<gradient:#E43434:#FFFFFF>Vindi</gradient><gradient:#FFFFFF:#FFFFFF>cator Pe</gradient><gradient:#FFFFFF:#E43434>ruano</gradient>", 35.0, "- Montado en una llama<newline>- Con arma letal"),
    ZOMBIE_RATATOUILLE(Material.ZOMBIE_HEAD, "<#FFA35C>Zombie Ratatouille</#FFA35C>", 20.0, "Controlado por un conejo que lo hace inmune."),
    CHARGED_ZOMBIE(Material.CREEPER_HEAD, "<#00FFFF>Zombie Cargado</#00FFFF>", 40.0, "- Aura azul y Casco de Diamante<newline>- Otorga Lentitud IV al golpear"),
    APOCALYPTIC_ZOMBIE(Material.BLACK_BANNER, "<gradient:#DB7A56:#F84E49>Zombie Apocalíptico</gradient>", 20.0, "- Fuerza II permanente<newline>- Invoca horda al acercarse"),
    ENDER_GUARDIAN(Material.ENDERMAN_SPAWN_EGG, "<#6b54ff>EnderGuardian</#6b54ff>", 40.0, "- Enderman con un Guardián montado<newline>- El guardián se teletransporta con él"),
    ZOMBIE_CAMERAMAN(Material.PLAYER_HEAD, "&aZombie Camarografo", 40.0, "Te aplica un modificador aleatorio al golpearte."),
    PHANTOM_RIDER(Material.PHANTOM_SPAWN_EGG, "<#8A2BE2>Phantom Rider</#8A2BE2>", 20.0, "- Esqueleto volador que lanza flechas desde el cielo."),
    VAMPIRE_BAT(Material.BAT_SPAWN_EGG, "&4Murciélago Vampiro", 24.0, "Te ataca y te roba un contenedor de vida máxima."),
    ANCESTRAL_REMAINS(Material.PIGLIN_SPAWN_EGG, "<#803522>Restos Ancestrales</#803522>", 60.0, "Piglin con armadura de héroe caído."),
    PIGLIN_DJ(Material.PIGLIN_BRUTE_SPAWN_EGG, "&d&lPiglin DJ", 40.0, "- Lleva armadura completa de Netherite<newline>- Al morir suelta su disco y rockola"),
    PYROMANIAC_PIGLIN(Material.PIGLIN_SPAWN_EGG, "<#FFA500>Piglin Pirómano Demente</#FFA500>", 40.0, "- Sus flechas explotan (Rompe bloques)"),
    TOTEM_RAVAGER(Material.RAVAGER_SPAWN_EGG, "<#3A9451>Tótem Ravager</#3A9451>", 50.0, "- 5 Ravagers apilados<newline>- El de arriba suelta un tótem aleatorio"),
    ARAB_WANDERING_TRADER(Material.WANDERING_TRADER_SPAWN_EGG, "<#FFD700>Wandering Trader Árabe</#FFD700>", 20.0, "- Explota en 10 mins si no se le compra nada.");

    private final Material icon;
    private final String customName;
    private final double health;
    private final String description;

    CustomMobType(Material icon, String customName, double health, String description) {
        this.icon = icon;
        this.customName = customName;
        this.health = health;
        this.description = description;
    }

    public Material getIcon() {
        return icon;
    }

    public String getCustomName() {
        return customName;
    }

    public double getHealth() {
        return health;
    }

    public String getDescription() {
        return description;
    }
}
