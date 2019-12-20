public enum MapStatsType {
    animalsNumber,
    plantsNumber,
    averageEnergy,
    averageLifespan,
    averageChildCount;

    public String toString() {
        switch (this) {
            case animalsNumber: return "Number of animals";
            case plantsNumber: return "Number of plants";
            case averageEnergy: return "Average energy";
            case averageLifespan: return "Average lifespan";
            case averageChildCount: return "Average child count";
            default: return "";
        }
    }
}
