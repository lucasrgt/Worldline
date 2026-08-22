# Piston extend smoke map

A piston facing east is powered by a standing torch on its west face. The
backend steps `updateEntities` then `World.tick` so piston tile entities can
finish. After eight ticks the piston is extended and the official server JAR
matches. BUD, sticky retraction, and entity-driven plates remain outside.
