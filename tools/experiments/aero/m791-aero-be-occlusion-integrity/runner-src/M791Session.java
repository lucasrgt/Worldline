record M791Session(int index, M791Artifact off, M791Artifact on) {
    String summary() {
        return "session." + index + "," + off.summary() + "," + on.summary();
    }
}
