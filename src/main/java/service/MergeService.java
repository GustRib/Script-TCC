package service;

import model.MergeInfo;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

public class MergeService {
    public static List<MergeInfo> listMerges(String repoPath, List<File> testFiles) throws IOException {
        List<MergeInfo> testMerges = new ArrayList<>();
        Scanner scanner = new Scanner(System.in);

        resetRepository(repoPath);

        List<String> mergeHashes = getMergeCommits(repoPath);
        for (String mergeHash : mergeHashes) {
            List<String> parents = getParentHashes(repoPath, mergeHash);
            if (parents.size() != 2) continue;

            String parent1 = parents.get(0);
            String parent2 = parents.get(1);

            System.out.println("\n🔄 Fazendo checkout para " + parent1);
            executeGitCommand(repoPath, "git", "checkout", parent1);

            System.out.println("🔀 Tentando merge com " + parent2);
            executeGitCommand(repoPath, "git", "merge", parent2, "--no-ff", "--no-commit");

            // Obter status do Git
            List<String> modifiedFilePaths = getGitStatus(repoPath);

            // Convertendo para objetos File
            List<File> modifiedFiles = modifiedFilePaths.stream()
                    .map(filePath -> new File(repoPath, filePath))
                    .collect(Collectors.toList());

            // Filtrar arquivos não mesclados que contêm "test" ou "Test" no nome
            List<File> unmergedTestFiles = modifiedFilePaths.stream()
                    .filter(filePath -> filePath.matches("^(D|A|U|UA|DU|AA|UU) .*"))  // Captura todas as combinações de unmerged
                    .map(filePath -> new File(repoPath, filePath.substring(3).trim())) // Remove os três primeiros caracteres (código + espaço)
                    .filter(file -> file.getName().toLowerCase().contains("test"))
                    .collect(Collectors.toList());


            System.out.println("Número total de arquivos modificados: " + modifiedFiles.size());
            System.out.println("Número de arquivos de teste não mesclados: " + unmergedTestFiles.size());

            if (!unmergedTestFiles.isEmpty()) {
                testMerges.add(new MergeInfo(mergeHash, parent1, parent2));
                System.out.println("✅ Arquivos de teste não mesclados:");
                unmergedTestFiles.forEach(file -> System.out.println(file.getName()));
            } else {
                System.out.println("⚠️ Nenhum arquivo de teste não mesclado encontrado.");
            }

            System.out.print("Deseja continuar para o próximo merge? (s/n): ");
            String resposta = scanner.nextLine();
            if (!resposta.equalsIgnoreCase("s")) {
                System.out.println("Processo interrompido pelo usuário.");
                break;
            }

            cleanupAfterMerge(repoPath);
        }

        return testMerges;
    }

    private static List<String> getMergeCommits(String repoPath) throws IOException {
        List<String> mergeHashes = new ArrayList<>();
        ProcessBuilder builder = new ProcessBuilder("git", "log", "--oneline", "--merges", "--all", "--pretty=format:%H");
        builder.directory(new File(repoPath));
        Process process = builder.start();
        try (var reader = new java.io.BufferedReader(new java.io.InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                mergeHashes.add(line.trim());
            }
        }
        return mergeHashes;
    }

    private static List<String> getParentHashes(String repoPath, String mergeHash) throws IOException {
        List<String> parents = new ArrayList<>();
        ProcessBuilder builder = new ProcessBuilder("git", "rev-list", "-n", "1", "--parents", mergeHash);
        builder.directory(new File(repoPath));
        Process process = builder.start();
        try (var reader = new java.io.BufferedReader(new java.io.InputStreamReader(process.getInputStream()))) {
            String line = reader.readLine();
            if (line != null) {
                String[] parts = line.split(" ");
                for (int i = 1; i < parts.length; i++) {
                    parents.add(parts[i]);
                }
            }
        }
        return parents;
    }

    private static List<String> getGitStatus(String repoPath) throws IOException {
        List<String> statusList = new ArrayList<>();
        ProcessBuilder builder = new ProcessBuilder("git", "status", "-s");
        builder.directory(new File(repoPath));
        Process process = builder.start();

        try (var reader = new java.io.BufferedReader(new java.io.InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                statusList.add(line.trim()); // Armazena saída do git status
            }
        }

        return statusList;
    }

    private static void executeGitCommand(String repoPath, String... command) throws IOException {
        ProcessBuilder builder = new ProcessBuilder(command);
        builder.directory(new File(repoPath));
        builder.redirectErrorStream(true);
        Process process = builder.start();
        try (var reader = new java.io.BufferedReader(new java.io.InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
        }
        try {
            if (!process.waitFor(10, java.util.concurrent.TimeUnit.SECONDS)) {
                process.destroy();
                System.err.println("Comando travado: " + String.join(" ", command));
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private static void resetRepository(String repoPath) throws IOException {
        executeGitCommand(repoPath, "git", "reset", "--hard");
        executeGitCommand(repoPath, "git", "merge", "--abort");
        executeGitCommand(repoPath, "git", "clean", "-fd");
    }

    private static void cleanupAfterMerge(String repoPath) throws IOException {
        executeGitCommand(repoPath, "git", "reset", "--hard");
        executeGitCommand(repoPath, "git", "merge", "--abort");
        executeGitCommand(repoPath, "git", "clean", "-fd");
    }
}
