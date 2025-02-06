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
                .filter(file -> {
                    boolean hasTestInName = file.getName().toLowerCase().contains("test");
                    boolean hasTestAnnotation = containsTestAnnotation(file);

                    System.out.println("Arquivo analisado: " + file.getName() +
                            " | Nome contém 'test'? " + hasTestInName +
                            " | Contém @Test? " + hasTestAnnotation);

                    return hasTestInName || hasTestAnnotation;
                })
                .collect(Collectors.toList());
    }

    private static boolean containsTestAnnotation(File file) {
        try {
            return Files.lines(file.toPath())
                    .map(String::trim)
                    .map(String::toLowerCase)
                    .anyMatch(line -> line.contains("@test"));
        } catch (IOException e) {
            System.err.println("Erro ao ler o arquivo: " + file.getName() + " - " + e.getMessage());
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
