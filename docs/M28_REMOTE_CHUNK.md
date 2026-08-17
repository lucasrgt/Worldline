# M28 Remote Chunk Observation

M28 adds `ChunkMultiplayerSession` and immutable `RemoteChunkObservation`.
The neutral value contains region origin, width, height, depth, and bounded
payload size. It never exposes protocol bytes or mapped Minecraft classes.

The b1.7.3 inbound codec now recognizes native `Packet51MapChunk`, reads its
official header, validates each encoded dimension, bounds the compressed
payload to four million bytes, consumes it fully, and returns the neutral
observation. Other qualified packets continue through the fail-closed pump.

Two fresh official servers each deliver a complete `16 x 128 x 16` envelope.
Origins and compressed byte counts remain observational because spawn and
compression output are outside the frozen semantic trace.

## Non-claims

M28 does not decompress or interpret block arrays, create a client world,
correlate prechunk lifecycle, maintain an asynchronous chunk cache, render the
received chunk, claim multiplayer determinism, or externally step server ticks.
