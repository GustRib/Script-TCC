package model;

public class TestFile {
    private String filePath;
    private boolean containsTestAnnotation;

    public TestFile(String filePath, boolean containsTestAnnotation) {
        this.filePath = filePath;
        this.containsTestAnnotation = containsTestAnnotation;
    }

    public String getFilePath() {
        return filePath;
    }

    public boolean containsTestAnnotation() {
        return containsTestAnnotation;
    }
}