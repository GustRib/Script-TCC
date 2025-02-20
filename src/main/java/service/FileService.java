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

    public static List<String> getModifiedFiles(String repoPath) throws IOException {
        return executeGitCommand(repoPath, "git", "diff", "--name-only", "--diff-filter=AM");
    }

    public static List<String> getUnmergedTestFiles(String repoPath) throws IOException {
        List<String> unmergedFiles = executeGitCommand(repoPath, "git", "ls-files", "--unmerged");
        return unmergedFiles.stream()
                .filter(file -> file.toLowerCase().contains("test"))
                .collect(Collectors.toList());
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

    private static List<String> executeGitCommand(String repoPath, String... command) throws IOException {
        List<String> output = new ArrayList<>();
        ProcessBuilder builder = new ProcessBuilder(command);
        builder.directory(new File(repoPath));
        Process process = builder.start();
        try (var reader = new java.io.BufferedReader(new java.io.InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                output.add(line.trim());
            }
        }
        return output;
    }
}
