public enum TrackedAnimalStatsType {
    childenCount,
    successorsCount,
    deathEpoch;

    public String toString() {
        switch (this) {
            case childenCount: return "Children count";
            case successorsCount: return "Successors count";
            case deathEpoch: return "Death epoch";
            default: return "";
        }
    }
}
