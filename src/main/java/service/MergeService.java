package service;

import model.MergeInfo;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class MergeService {

    public static List<MergeInfo> listMerges(String repoPath, List<String> testFilePatterns) {
        List<MergeInfo> testMerges = new ArrayList<>();

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

                        if (doesMergeAffectTestFiles(repoPath, parent1, parent2, testFilePatterns)) {
                            testMerges.add(new MergeInfo(mergeHash, parent1, parent2));
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("Merges envolvendo arquivos de teste: " + testMerges.size());
        testMerges.forEach(System.out::println);

        return testMerges;
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

    private static boolean doesMergeAffectTestFiles(String repoPath, String parent1, String parent2, List<String> testFilePatterns) {
        try {
            executeGitCommand(repoPath, "git", "checkout", parent1);
            executeGitCommand(repoPath, "git", "merge", parent2, "--no-ff", "--no-commit");

            List<String> modifiedFiles = new ArrayList<>();
            ProcessBuilder builder = new ProcessBuilder("git", "status", "--porcelain");
            builder.directory(new File(repoPath));
            builder.redirectErrorStream(true);
            Process process = builder.start();

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] parts = line.trim().split(" ");
                    if (parts.length > 1) {
                        modifiedFiles.add(parts[1]);
                    }
                }
            }

            cleanupAfterMerge(repoPath);

            return modifiedFiles.stream().anyMatch(file -> isTestFile(file, testFilePatterns));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    private static void cleanupAfterMerge(String repoPath) {
        try {
            executeGitCommand(repoPath, "git", "merge", "--abort");
            executeGitCommand(repoPath, "git", "checkout", "master");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void resetRepository() throws IOException {
        executeGitCommand("git reset --hard");
        executeGitCommand("git merge --abort");
        executeGitCommand("git clean -fd");
        executeGitCommand("git checkout master");
        executeGitCommand("git pull origin master");
    }

    private void cleanupRepository() throws IOException {
        executeGitCommand("git reset --hard");
        executeGitCommand("git merge --abort");
        executeGitCommand("git clean -fd");
        executeGitCommand("git checkout master");
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
