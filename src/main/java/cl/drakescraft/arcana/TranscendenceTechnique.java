package cl.drakescraft.arcana;

import java.util.List;
import org.bukkit.ChatColor;
import org.bukkit.Material;

public enum TranscendenceTechnique {
    NONE("Ninguna", null, ArcanaRank.DESPERTAR, 0L, 0L, Material.BARRIER, List.of("&7Sin técnica equipada")),

    // DRAGON BALL
    GENKIDAMA(
        "Genkidama Cósmica",
        TranscendencePath.DRAGON_BALL,
        ArcanaRank.ASCENDIDO,
        45L,
        60L,
        Material.NETHER_STAR,
        List.of(
            "&7Reúne la energía vital ambiental sobre tu cabeza",
            "&7en una esfera estelar colosal.",
            "&e• Carga:&f 2.5 segundos de canalización.",
            "&c• Impacto:&f 24.0 de daño expansivo en 8 bloques.",
            "&b• Efecto:&f Onda de choque que desintegra hostiles.",
            "&d• Respetuoso con construcciones y claims."
        )
    ),
    ULTRA_INSTINCT(
        "Ultra Instinto (Migatte)",
        TranscendencePath.DRAGON_BALL,
        ArcanaRank.ASCENDIDO,
        50L,
        80L,
        Material.PRISMARINE_SHARD,
        List.of(
            "&7Despierta la doctrina de la evasión absoluta.",
            "&e• Duración:&f 15 segundos.",
            "&b• Evasión Divina:&f 75% de probabilidad de esquivar",
            "&7  cualquier golpe o proyectil con micro-teleportación",
            "&7  instantánea y cancelación total del daño recibido.",
            "&a• Bonificación:&f Velocidad III y Salto II."
        )
    ),
    KAIOKEN(
        "Golpe de Kaiō-ken",
        TranscendencePath.DRAGON_BALL,
        ArcanaRank.VANGUARDIA,
        25L,
        40L,
        Material.REDSTONE,
        List.of(
            "&7Multiplica el flujo de Ki en una violenta aura carmesí.",
            "&e• Duración:&f 12 segundos.",
            "&c• Bonificación:&f Fuerza II, Velocidad II y Resistencia I.",
            "&4• Costo Corporal:&f Drena 1 corazón cada 3 segundos.",
            "&7  (El cuerpo no puede morir por esta tensión)."
        )
    ),

    // NARUTO
    RASENGAN(
        "Orbe Espiral (Rasengan)",
        TranscendencePath.NARUTO,
        ArcanaRank.CATALIZADOR,
        20L,
        25L,
        Material.WIND_CHARGE,
        List.of(
            "&7Comprime un vórtice giratorio de viento en tu palma.",
            "&c• Daño:&f 14.0 en el impacto frontal.",
            "&b• Efecto:&f Vórtice cinético continuo que repele",
            "&7  al enemigo a gran distancia con estallido de viento."
        )
    ),
    CHIDORI(
        "Millar de Pájaros (Chidori)",
        TranscendencePath.NARUTO,
        ArcanaRank.CATALIZADOR,
        25L,
        30L,
        Material.LIGHTNING_ROD,
        List.of(
            "&7Concentra relámpagos cortantes de alta frecuencia.",
            "&e• Embestida:&f Dash sónico de 9 bloques al frente.",
            "&c• Daño Perforante:&f 16.0 a todos los enemigos atravesados.",
            "&9• Parálisis:&f Ralentización IV por 3 segundos."
        )
    ),
    SUSANOO(
        "Armadura Espectral (Susanoo)",
        TranscendencePath.NARUTO,
        ArcanaRank.VANGUARDIA,
        40L,
        70L,
        Material.AMETHYST_CLUSTER,
        List.of(
            "&7Invoca una caja torácica etérea de energía espiritual.",
            "&e• Duración:&f 10 segundos.",
            "&b• Protección Absoluta:&f Resistencia IV e inmunidad al empuje.",
            "&d• Pulso Repulsivo:&f Aleja violentamente a cualquier enemigo",
            "&7  que ose acercarse a menos de 3.5 bloques."
        )
    ),

    // ONE PIECE
    CONQUERORS_HAKI(
        "Haki del Conquistador",
        TranscendencePath.ONE_PIECE,
        ArcanaRank.ASCENDIDO,
        50L,
        75L,
        Material.DRAGON_HEAD,
        List.of(
            "&7Libera una abrumadora onda de fuerza de voluntad suprema.",
            "&e• Alcance:&f 12 bloques a la redonda con relámpagos negros.",
            "&c• Dominación:&f Mobs débiles (< 25 HP) caen fulminados.",
            "&b• Hostiles y Jefes:&f 12.0 de daño, Lentitud V y Debilidad V."
        )
    ),
    ARMAMENT_HAKI(
        "Haki de Armadura (Busoshoku)",
        TranscendencePath.ONE_PIECE,
        ArcanaRank.CATALIZADOR,
        30L,
        45L,
        Material.NETHERITE_INGOT,
        List.of(
            "&7Recubre tu armadura y armas con endurecimiento negro.",
            "&e• Duración:&f 20 segundos.",
            "&c• Daño Letal:&f +5 de daño extra en cada impacto físico.",
            "&7  Ignora la protección de armadura de los enemigos.",
            "&9• Defensa:&f Resistencia II constante."
        )
    ),
    GEAR_SECOND(
        "Segunda Marcha (Gear Second)",
        TranscendencePath.ONE_PIECE,
        ArcanaRank.EXPLORADOR,
        15L,
        35L,
        Material.FEATHER,
        List.of(
            "&7Acelera el flujo circulatorio expulsando vapor del cuerpo.",
            "&e• Duración:&f 15 segundos.",
            "&a• Agilidad Extrema:&f Velocidad IV, Salto II y Prisa Minera II.",
            "&d• Regeneración:&f Recuperación acelerada de vitalidad."
        )
    );

    private final String displayName;
    private final TranscendencePath path;
    private final ArcanaRank minRank;
    private final long spiritCost;
    private final long cooldownSeconds;
    private final Material icon;
    private final List<String> description;

    TranscendenceTechnique(String displayName, TranscendencePath path, ArcanaRank minRank,
                          long spiritCost, long cooldownSeconds, Material icon, List<String> description) {
        this.displayName = displayName;
        this.path = path;
        this.minRank = minRank;
        this.spiritCost = spiritCost;
        this.cooldownSeconds = cooldownSeconds;
        this.icon = icon;
        this.description = description;
    }

    public String displayName() { return displayName; }
    public TranscendencePath path() { return path; }
    public ArcanaRank minRank() { return minRank; }
    public long spiritCost() { return spiritCost; }
    public long cooldownSeconds() { return cooldownSeconds; }
    public Material icon() { return icon; }
    public List<String> description() { return description; }

    public static List<TranscendenceTechnique> byPath(TranscendencePath path) {
        return java.util.Arrays.stream(values())
                .filter(t -> t.path == path)
                .toList();
    }
}
