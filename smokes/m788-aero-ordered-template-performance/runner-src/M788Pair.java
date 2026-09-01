record M788Pair(int index, boolean directFirst, M788Artifact direct,
                M788Artifact template) {
    String summary() {
        return "pair." + index + ".directFirst=" + directFirst + ","
            + direct.summary() + "," + template.summary();
    }
}
