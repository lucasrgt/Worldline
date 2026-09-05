/** Public harness bridge for separately loaded external runners. */
final class M793Digest {
    private M793Digest() {}
    static String sha256Hex(byte[] bytes) throws Exception {
        return HexDigest.sha256Hex(bytes);
    }
}
