package model;

public class MergeInfo {
    private String hash;
    private String parent1;
    private String parent2;
    private boolean hasTestConflict;

    public MergeInfo(String hash, String parent1, String parent2) {
        this.hash = hash;
        this.parent1 = parent1;
        this.parent2 = parent2;
        this.hasTestConflict = false;
    }

    public String getHash() {
        return hash;
    }

    public String getParent1() {
        return parent1;
    }

    public String getParent2() {
        return parent2;
    }

    public boolean hasTestConflict() {
        return hasTestConflict;
    }

    public void setTestConflict(boolean hasTestConflict) {
        this.hasTestConflict = hasTestConflict;
    }
}
