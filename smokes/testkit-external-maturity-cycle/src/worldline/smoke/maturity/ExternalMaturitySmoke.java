package worldline.smoke.maturity;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import worldline.testapi.ExternalConsumerUse;
import worldline.testkit.TestKitMaturityEvidence;
import worldline.testkit.TestKitMaturityFixture;

/** Assessment consumes actual external execution receipts and never backdates a new run. */
public final class ExternalMaturitySmoke {
    private ExternalMaturitySmoke() {
    }
    public static void main(String[] args) throws Exception {
        if (args.length != 4) {
            throw new IllegalArgumentException("observations as-of minimum-span-days output");
        }
        List<ExternalConsumerUse> uses = new ArrayList<>();
        for (String line : Files.readAllLines(Paths.get(args[0]), StandardCharsets.UTF_8)) {
            if (line.isEmpty() || line.startsWith("#")) {
                continue;
            }
            String[] row = line.split("\t", -1);
            if (row.length != 7 || !(row[5].equals("true") || row[5].equals("false"))) {
                throw new IllegalArgumentException("invalid consumer observation row");
            }
            uses.add(new ExternalConsumerUse(row[0], row[1], row[2], row[3], Integer.parseInt(row[4]),
                    Boolean.parseBoolean(row[5]), row[6]));
        }
        TestKitMaturityEvidence evidence = TestKitMaturityFixture.assess(uses, LocalDate.parse(args[1]),
                Integer.parseInt(args[2]));
        if (evidence.publicConsumers() != 2 || evidence.sustainedConsumers().size() != 1 || evidence.readyForOne()) {
            throw new IllegalStateException("frozen external maturity baseline differs");
        }
        boolean rejected = false;
        try {
            evidence.requireReadyForOne();
        } catch (IllegalStateException expected) {
            rejected = true;
        }
        if (!rejected) {
            throw new IllegalStateException("1.0 promotion ignored insufficient sustained public use");
        }
        Files.write(Paths.get(args[3]), evidence.canonical().getBytes(StandardCharsets.UTF_8));
        System.out.println("WORLDLINE_MATURITY_BASELINE=public-consumers:2,sustained-consumers:1,ready-for-1.0:false");
    }
}
