# M656-SERVER-ADMISSION-MATRIX server admission matrix

<!-- Generated from smoke.properties by MilestoneNarrative. -->

## Claim

M656 freezes the official Beta 1.7.3 server entry policy across whitelist membership and a one-player capacity. With whitelist enabled and no connected players, an unlisted identity receives a login rejection while a listed identity is admitted. While that listed identity occupies the sole slot, a second listed identity receives the distinct full-server rejection. After a clean restart with whitelist disabled, the same previously unlisted identity is admitted. This does not claim bans, IP bans, online-mode authentication, exact rejection wording, arbitrary capacity values, duplicate-login policy, or post-Beta protocols.

## Qualification cycle

DataDrivenCycle runs two fresh replicas of a bounded two-boot official dedicated-server scenario. B173AdmissionServer writes an exact offline-mode profile with max-players one and a two-identity white-list.txt. The scenario first probes the unlisted identity while capacity is empty, admits one listed identity and confirms the official player census, then probes the other listed identity while full. It stops cleanly, restarts the same world with whitelist disabled, and confirms that the originally unlisted identity now completes protocol-14 synchronization. ServerEntryPolicyFixture normalizes the two Packet255 login rejections and both positive censuses into equatable causal evidence. Headless only. No GUI. No Aero.

Expected signal: `whitelist=unlisted-rejected+listed-accepted,capacity=listed-overflow-rejected,disabled=unlisted-accepted,max=1,identities=3,disconnect=clean`.

Frozen semantic SHA-256: `db5e065110c25d43b1f4ffa771901bc029d97a485b44c663be6cd75052f30513`.
