record M790Pair(int index, boolean baselineFirst, M790Artifact baseline,
                M790Artifact reuse) {
    String summary() {
        return "pair." + index + ".baselineFirst=" + baselineFirst + ","
            + baseline.summary() + "," + reuse.summary();
    }
}
