# DrakesArcana

Magia elemental y progresión survival de DrakesCraft para Paper/Purpur 1.21.11.

## Alcance inicial

- Seis afinidades primarias aleatorias: Fuego, Tierra, Aire, Agua, Hielo y Electro.
- Un origen aleatorio ligado a la afinidad: Fénix/Blaze, Guardianes, Breeze/Phantom, Guardianes marinos, Stray o Caminante Ender, sin disfraces permanentes ni copia de criaturas.
- Luz y Sombra nacen como disciplinas avanzadas desde el lanzamiento, no como un séptimo u octavo sorteo.
- Persistencia SQLite, corrección de afinidad por staff, rangos, experiencia, Sigilos y Códice entregable.
- Pulso y Dominio como base visual: trayectorias, ondas, telegráficos, sonido y partículas sin modificar bloques.
- Daño solo contra monstruos en `world` y `boss_arena`; nunca contra jugadores mientras el PvP Arcana esté desactivado.

## Comandos

| Comando | Uso |
| --- | --- |
| `/arcana info` | Muestra afinidad y disciplinas. |
| `/arcana book` | Entrega el Códice. |
| `/arcana cast pulso` | Ataque visual de corto alcance. |
| `/arcana cast dominio` | Ultimate de área con cooldown. |
| `/arcana staff set <jugador> <afinidad>` | Corrección y pruebas de staff. |

## Límites de seguridad

Arcana no rompe ni coloca bloques. Los dominios tienen presupuesto global, cooldown y duración corta. La integración con ProtectionStones y WorldGuard será de denegación por defecto antes de habilitar PvP Arcana en claims.

## Integraciones

- **DiosesDrakes:** lore, bendiciones y afinidades de panteón mediante API, no dependencia dura.
- **Odysseia:** `boss_arena` acepta hechizos ofensivos para los bosses de `/bosswarp`.
- **ElementManipulation:** permanece como addon Slimefun independiente y fuente de referencia; Arcana no duplica sus ítems.
- **AuraSkills:** estadísticas base, sin duplicar experiencia.

## Progresión survival

Arcana tendrá Minas Arcanas regenerables por capas, con bloques temporales y recompensas de experiencia/Sigilos. Los aldeanos arcanos usarán Sigilos y materiales de exploración para intercambiar mejoras. El equipo por encima de netherite se construirá por piezas, con requisitos de rango y costes; nunca aparecerá como compra directa ni reemplazará el endgame de Slimefun.
