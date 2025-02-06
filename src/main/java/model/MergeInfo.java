package model;

public class MergeInfo {
    private String mergeHash;
    private String parent1;
    private String parent2;

    public MergeInfo(String mergeHash, String parent1, String parent2) {
        this.mergeHash = mergeHash;
        this.parent1 = parent1;
        this.parent2 = parent2;
    }

    public String getMergeHash() {
        return mergeHash;
    }

    public String getParent1() {
        return parent1;
    }

    public String getParent2() {
        return parent2;
    }

    @Override
    public String toString() {
        return "MergeInfo{" +
                "mergeHash='" + mergeHash + '\'' +
                ", parent1='" + parent1 + '\'' +
                ", parent2='" + parent2 + '\'' +
                '}';
    }
}
