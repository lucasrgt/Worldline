record M789Pair(int index, boolean baselineFirst, M789Artifact baseline,
                M789Artifact reuse) {
    String summary() {
        return "pair." + index + ".baselineFirst=" + baselineFirst + ","
            + baseline.summary() + "," + reuse.summary();
    }
}
