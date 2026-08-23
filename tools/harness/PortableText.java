import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;

/** Canonicalizes tracked UTF-8 text while leaving binary inputs byte-exact. */
final class PortableText {
    private PortableText() { }

    static byte[] normalize(byte[] input) {
        for (byte value : input) if (value == 0) return input;
        String text;
        try {
            text = StandardCharsets.UTF_8.newDecoder().onMalformedInput(CodingErrorAction.REPORT)
                    .onUnmappableCharacter(CodingErrorAction.REPORT).decode(ByteBuffer.wrap(input)).toString();
        } catch (CharacterCodingException error) { return input; }
        text = text.replace("\r\n", "\n").replace('\r', '\n');
        return Normalizer.normalize(text, Normalizer.Form.NFC).getBytes(StandardCharsets.UTF_8);
    }
}
