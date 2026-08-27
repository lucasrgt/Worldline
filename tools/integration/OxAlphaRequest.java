/** Parsed identity and limits for one supervised Ox Alpha attempt. */
record OxAlphaRequest(String id, String goal, String base, String controlBase, String phase,
        int attempt, String session, int timeoutSeconds, String adoptionReceipt, String adoptionSha) {
    static OxAlphaRequest parse(String[] arguments) {
        String id = null;
        String goal = null;
        String base = null;
        String controlBase = null;
        String phase = null;
        String session = null;
        String adoptionReceipt = null;
        String adoptionSha = null;
        int attempt = 0;
        int timeout = 3600;
        for (int index = 0; index < arguments.length; index += 2) {
            require(index + 1 < arguments.length, "missing value for " + arguments[index]);
            String value = arguments[index + 1];
            switch (arguments[index]) {
                case "--id" -> id = value;
                case "--goal" -> goal = value;
                case "--base" -> base = value;
                case "--control-base" -> controlBase = value;
                case "--phase" -> phase = value;
                case "--attempt" -> attempt = Integer.parseInt(value);
                case "--session" -> session = value;
                case "--timeout-seconds" -> timeout = Integer.parseInt(value);
                case "--adoption-receipt" -> adoptionReceipt = value;
                case "--adoption-sha256" -> adoptionSha = value;
                default -> throw new IllegalArgumentException("unknown argument: " + arguments[index]);
            }
        }
        require(controlBase != null, "--control-base is required");
        require((adoptionReceipt == null) == (adoptionSha == null),
                "legacy adoption receipt and SHA-256 must be supplied together");
        return new OxAlphaRequest(id, goal, base, controlBase, phase, attempt, session, timeout,
                adoptionReceipt, adoptionSha);
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new IllegalArgumentException(message);
        }
    }
}
