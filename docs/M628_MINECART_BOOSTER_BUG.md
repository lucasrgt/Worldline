# M628-MINECART-BOOSTER-BUG minecart booster bug

<!-- Generated from smoke.properties by MilestoneNarrative. -->

## Claim

M628 freezes the classic Beta 1.7.3 side-by-side minecart booster. Two empty minecarts start at the same longitudinal coordinate on adjacent parallel regular rails 66. Packet7 attacks only the driver cart toward the south; both the attacked driver and the untouched parallel cart then move south. The TestKit contract requires distinct type-10 carts, a one-block lateral gap, and forward movement for both. This does not claim powered-rail speed, furnace-cart propulsion, riding, derailment, or arbitrary cart collisions.

## Qualification cycle

DataDrivenCycle rebuilds the raised parallel-track fixture in two fresh official dedicated-server JVMs. Each replica places two minecart items 328 behind north walls, emits one Packet7 attack against only the driver, and observes protocol-14 Packet31, Packet33, or Packet34 forward movement for both entities. MinecartBoosterFixture normalizes unstable entity IDs and movement packet choice into equatable forward-motion evidence. Headless B173WireClient only. No GUI. No Aero.

Expected signal: `driver=type10+forward,booster=type10+forward,parallel-gap=1,driver-rail=66:0,booster-rail=66:0,push=packet7-attack,clients=1,disconnect=clean`.

Frozen semantic SHA-256: `0cab886f076b9b7a6e9d9de70999dc4e1867ee6b970cbbe6444de3d6b65d4d57`.
