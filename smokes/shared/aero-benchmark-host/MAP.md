# Aero benchmark process scheduling

M792 and M794 explicitly fingerprint this original shared runtime source.
Before loading their controlled world, each client restricts its own process
to the four highest logical processors already permitted by its inherited
affinity and selects the Windows High process priority class. Both settings
are read back and recorded. A missing platform, insufficient processor set,
timeout or unsuccessful assignment fails before measured model rendering.

The settings last only for that client process. No other process, power plan,
global scheduler policy or library renderer is changed. The profile is a
Windows host qualification condition, not an all-platform performance claim.
All render, p99, allocation and hitch bounds remain independent of this setup;
no measured frame is discarded in response to host activity.
