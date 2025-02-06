package service;

import model.MergeInfo;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class MergeService {

    public static List<MergeInfo> listMerges(String repoPath, List<String> testFilePatterns) throws IOException {
        List<MergeInfo> testMerges = new ArrayList<>();
        List<String> allTestFilesAffected = new ArrayList<>();
        int totalMerges = 0;

        resetRepository(repoPath);

        try {
            ProcessBuilder builder = new ProcessBuilder(
                    "git", "log", "--oneline", "--merges", "--all", "--pretty=format:%h");
            builder.directory(new File(repoPath));
            Process process = builder.start();

            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String mergeHash = line.trim();
                    List<String> parents = getParentHashes(repoPath, mergeHash);

                    if (parents.size() == 2) {
                        String parent1 = parents.get(0);
                        String parent2 = parents.get(1);

                        // Converte List<String> para List<File>
                        List<File> testFiles = testFilePatterns.stream()
                                .map(File::new)
                                .collect(Collectors.toList());

                        List<String> affectedFiles = getTestFilesAffectedByMerge(repoPath, parent1, parent2, testFiles);


                        if (!affectedFiles.isEmpty()) {
                            testMerges.add(new MergeInfo(mergeHash, parent1, parent2));
                            allTestFilesAffected.addAll(affectedFiles);
                        }
                    }
                    totalMerges++;
                }
            }
            finally {
                cleanupAfterMerge(repoPath);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("Número total de merges analisados: " + totalMerges);
        System.out.println("Merges que afetaram arquivos de teste: " + testMerges.size());

        if (allTestFilesAffected.isEmpty()) {
            System.out.println("Nenhum arquivo de teste sofreu conflito durante os merges.");
        } else {
            System.out.println("Lista de arquivos de teste que sofreram conflito:");
            allTestFilesAffected.forEach(System.out::println);
        }

        System.out.println("Quantidade total de arquivos de teste com conflito: " + allTestFilesAffected.size());

        return testMerges;
    }

    private static String getFirstTestFileAffectedByMerge(List<String> modifiedFiles, List<File> testFiles) {
        for (File testFile : testFiles) {
            String testFilePath = testFile.getPath();

            for (String modifiedFile : modifiedFiles) {
                if (modifiedFile.endsWith(testFilePath)) {
                    System.out.println("Arquivo de teste encontrado: " + modifiedFile);
                    return modifiedFile;
                }
            }
        }
        return null;
    }

    private static List<String> getParentHashes(String repoPath, String mergeHash) {
        List<String> parents = new ArrayList<>();
        try {
            ProcessBuilder builder = new ProcessBuilder(
                    "git", "rev-list", "-n", "1", "--parents", mergeHash);
            builder.directory(new File(repoPath));
            Process process = builder.start();

            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()))) {
                String line = reader.readLine();
                if (line != null) {
                    String[] parts = line.split(" ");
                    for (int i = 1; i < parts.length; i++) {
                        parents.add(parts[i]);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return parents;
    }

    private static List<String> getTestFilesAffectedByMerge(String repoPath, String parent1, String parent2, List<File> testFiles) throws IOException {
        List<String> affectedTestFiles = new ArrayList<>();

        try {
            System.out.println("🔄 Fazendo checkout para " + parent1);
            executeGitCommand(repoPath, "git", "checkout", parent1);

            System.out.println("🔀 Tentando merge com " + parent2);
            executeGitCommand(repoPath, "git", "merge", parent2, "--no-ff", "--no-commit");

            //Capturar arquivos modificados via `git status -s`
            List<String> modifiedFiles = executeGitStatus(repoPath);

            for (File testFile : testFiles) {
                String testFilePath = testFile.getPath();
                for (String modifiedFile : modifiedFiles) {
                    if (modifiedFile.endsWith(testFilePath)) {
                        affectedTestFiles.add(modifiedFile);
                    }
                }
            }

            if (affectedTestFiles.isEmpty()) {
                System.out.println("⚠️ Nenhum arquivo de teste foi alterado.");
            } else {
                System.out.println("✅ Arquivos de teste afetados:");
                affectedTestFiles.forEach(System.out::println);
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            cleanupAfterMerge(repoPath);
        }

        return affectedTestFiles;
    }



    private static List<String> executeGitStatus(String repoPath) throws IOException {
        List<String> modifiedFiles = new ArrayList<>();

        ProcessBuilder builder = new ProcessBuilder("git", "status", "-s");
        builder.directory(new File(repoPath));
        builder.redirectErrorStream(true);
        Process process = builder.start();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String filePath = line.substring(3).trim();
                modifiedFiles.add(new File(repoPath, filePath).getAbsolutePath());
            }
        }

        return modifiedFiles;
    }


    private static void resetRepository(String repoPath) throws IOException {
        executeGitCommand(repoPath, "git", "reset", "--hard");
        executeGitCommand(repoPath, "git", "merge", "--abort");
        executeGitCommand(repoPath, "git", "clean", "-fd");
        executeGitCommand(repoPath, "git", "checkout", "master");
        executeGitCommand(repoPath, "git", "pull", "origin", "master");
    }

    private static void cleanupAfterMerge(String repoPath) throws IOException {
        executeGitCommand(repoPath, "git", "reset", "--hard");
        executeGitCommand(repoPath, "git", "merge", "--abort");
        executeGitCommand(repoPath, "git", "clean", "-fd");
        executeGitCommand(repoPath, "git", "checkout", "master");
    }

    private static boolean isTestFile(String filePath, List<String> testFilePatterns) {
        return testFilePatterns.stream().anyMatch(filePath::contains);
    }

    private static void executeGitCommand(String repoPath, String... command) throws IOException {
        ProcessBuilder builder = new ProcessBuilder(command);
        builder.directory(new File(repoPath));
        builder.redirectErrorStream(true);
        Process process = builder.start();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
        }

        try {
            if (!process.waitFor(10, java.util.concurrent.TimeUnit.SECONDS)) { // Timeout de 10s
                process.destroy();
                System.err.println("Comando travado: " + String.join(" ", command));
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
            Thread.currentThread().interrupt();
        }
    }


}
