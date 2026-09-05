/** Public harness bridge for separately loaded external runners. */
final class M794Digest {
  private M794Digest() {}
  static String sha256Hex(byte[] bytes) throws Exception {
    return HexDigest.sha256Hex(bytes);
  }
}
