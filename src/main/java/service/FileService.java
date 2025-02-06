package service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class FileService {

    public static List<File> listJavaFiles(String projectPath) {
        File root = new File(projectPath);
        List<File> javaFiles = new ArrayList<>();
        findFilesRecursively(root, javaFiles, ".java");
        return javaFiles;
    }

    public static int countJavaFiles(String projectPath) {
        return listJavaFiles(projectPath).size();
    }

    public static List<File> findTestFiles(List<File> javaFiles) {
        return javaFiles.stream()
                .filter(file -> file.getName().toLowerCase().contains("test") || containsTestAnnotation(file))
                .collect(Collectors.toList());
    }

    public static List<File> findFilesWithTestAnnotation(List<File> testFiles) {
        return testFiles.stream()
                .filter(FileService::containsTestAnnotation)
                .collect(Collectors.toList());
    }

    private static boolean containsTestAnnotation(File file) {
        try {
            return Files.lines(file.toPath()).anyMatch(line -> line.trim().contains("@Test"));
        } catch (IOException e) {
            return false;
        }
    }

    private static void findFilesRecursively(File dir, List<File> fileList, String extension) {
        if (dir != null && dir.listFiles() != null) {
            for (File file : dir.listFiles()) {
                if (file.isDirectory()) {
                    findFilesRecursively(file, fileList, extension);
                } else if (file.getName().endsWith(extension)) {
                    fileList.add(file);
                }
            }
        }
    }
}
