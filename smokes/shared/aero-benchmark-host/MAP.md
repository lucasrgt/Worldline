# Aero benchmark process scheduling

M792 and M794 explicitly fingerprint this original shared runtime source.
Before loading their controlled world, each client restricts its own process
to one permitted logical processor from each of four distinct physical cores
and selects the Windows High process priority class. Windows topology is queried
through GetLogicalProcessorInformation; selection walks logical processors from
highest to lowest without taking two siblings from the same core. Both settings
are read back and recorded. A missing platform, insufficient processor set,
timeout or unsuccessful assignment fails before measured model rendering.

The settings last only for that client process. No other process, power plan,
global scheduler policy or library renderer is changed. The profile is a
Windows host qualification condition, not an all-platform performance claim.
The JVM must start with ActiveProcessorCount=4 so its compiler and collector
ergonomics match the permitted execution width before world loading.
All render, p99, allocation and hitch bounds remain independent of this setup;
no measured frame is discarded in response to host activity.
