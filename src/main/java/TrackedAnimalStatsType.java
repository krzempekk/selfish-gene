public enum TrackedAnimalStatsType {
    childrenCount,
    successorsCount,
    deathEpoch;

    public String toString() {
        switch (this) {
            case childrenCount: return "Children count";
            case successorsCount: return "Successors count";
            case deathEpoch: return "Death epoch";
            default: return "";
        }
    }
}
