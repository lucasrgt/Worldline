# M34 Server-authoritative Pose Correction

A byte fixture decodes native server Packet13 field order, normalizes stance to
the neutral feet pose, and requires the exact client acknowledgement order.

Two fresh official servers receive a movement into the center of a nearby solid
block selected from the decoded cache. The client pumps the correction, replaces
its local pose, acknowledges it, and must
return exactly to the initial authoritative pose while retaining the original
decoded chunk. Sending the invalid movement never counts as success.
