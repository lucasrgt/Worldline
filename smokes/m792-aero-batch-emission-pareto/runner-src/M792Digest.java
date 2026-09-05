/** Public harness bridge for separately loaded external runners. */
final class M792Digest {
    private M792Digest() {}
    static String sha256Hex(byte[] bytes) throws Exception {
        return HexDigest.sha256Hex(bytes);
    }
}
