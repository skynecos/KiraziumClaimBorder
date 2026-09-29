# KiraziumClaimBorder

Small Paper addon for SimpleClaimSystem that replaces the native `/claim see` particle preview with a temporary thin purple wall.

## Target

- Paper 26.1.2
- Java 25
- SimpleClaimSystem 1.13.1

## v1.0.2

- `/claim see` on your own claim: purple border.
- `/claim see` in unclaimed land: white border.
- Another player's claim: red border.
- All three materials are configurable.

## v1.0.1

- Default wall height increased from 4 blocks to 16 blocks.
- Existing v1.0.0 configs are migrated automatically.

## Install

1. Install SimpleClaimSystem.
2. Put `KiraziumClaimBorder.jar` into `plugins/`.
3. Restart the server.
4. Use `/claim see`.

No ProtocolLib, Nexo, ModelEngine, or resource pack is required.

## Behaviour

- Intercepts only the player's own `/claim see` preview.
- Uses SimpleClaimSystem's API to obtain the current claim.
- Draws only exposed outer chunk edges.
- Uses temporary `BlockDisplay` entities with `PURPLE_STAINED_GLASS`.
- Displays are non-colliding and do not modify world blocks.
- Displays are visible only to the player who ran the command.
- Re-running the command replaces the previous preview.
- Preview is cleaned up on timeout, logout, world change, and plugin disable.
- If the current chunk is unclaimed, the current chunk boundary is previewed, matching SimpleClaimSystem's basic behaviour.

## Configuration

`plugins/KiraziumClaimBorder/config.yml`:

- `wall.materials.own`
- `wall.materials.empty`
- `wall.materials.other`
- `wall.height`
- `wall.thickness`
- `wall.duration-ticks`
- `wall.y-offset`
- `wall.brightness`
