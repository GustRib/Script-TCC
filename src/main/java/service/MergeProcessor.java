package service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

public class MergeProcessor {
    private final String repoPath;
    private final GitHelper gitHelper;
    private final Scanner scanner;
    private String parent1, parent2;
    private final List<File> unmergedTestFiles;

    public MergeProcessor(String repoPath, GitHelper gitHelper) {
        this.repoPath = repoPath;
        this.gitHelper = gitHelper;
        this.scanner = new Scanner(System.in);
        this.unmergedTestFiles = new ArrayList<>();
    }

    public boolean processMerge(String mergeHash, boolean automaticMode) throws IOException {
        List<String> parents = gitHelper.getParentHashes(mergeHash);
        if (parents.size() < 2) return true;

        parent1 = parents.get(0);
        parent2 = parents.get(1);

        System.out.println("\n🔄 Checkout para " + parent1);
        gitHelper.executeGitCommandWithFeedback("git", "checkout", parent1);

        System.out.println("🔀 Tentando merge com " + parent2);
        gitHelper.executeGitCommandWithFeedback("git", "merge", parent2, "--no-ff", "--no-commit");

        if (!gitHelper.didMergeFail()) {
            System.out.println("✅ Merge realizado com sucesso. Nenhum conflito encontrado.");
            return true;
        }

        System.out.println("❌ Merge falhou. Analisando conflitos...");
        List<String> modifiedFilePaths = gitHelper.getGitStatus();

        List<File> unmergedTestFiles = modifiedFilePaths.stream()
                .filter(filePath -> filePath.matches("^(D|A|U|UA|DU|AA|UU) .*"))
                .map(filePath -> new File(repoPath, filePath.substring(3).trim()))
                .filter(file -> file.getName().toLowerCase().contains("test"))
                .collect(Collectors.toList());

        this.unmergedTestFiles.clear();
        this.unmergedTestFiles.addAll(unmergedTestFiles);

        unmergedTestFiles.forEach(file -> {
            String status = modifiedFilePaths.stream()
                    .filter(line -> line.contains(file.getName()))
                    .findFirst()
                    .map(line -> line.substring(0, 2))
                    .orElse("  ");
            System.out.println("Arquivo: " + file.getName() + " - Status: " + ConflictStatus.fromCode(status));
        });

        System.out.println("📌 Arquivos de teste não mesclados: " + unmergedTestFiles.size());
        unmergedTestFiles.forEach(file -> System.out.println(file.getName()));

        if (!automaticMode) {
            System.out.print("Deseja continuar? (s/n): ");
            String resposta = scanner.nextLine();
            if (!resposta.equalsIgnoreCase("s")) {
                return false;
            }
        }

        gitHelper.resetRepository();
        return true;
    }

    public List<File> getUnmergedTestFiles() {
        return unmergedTestFiles;
    }

    public String getParent1() {
        return parent1;
    }

    public String getParent2() {
        return parent2;
    }
}

enum ConflictStatus {
    DELETED_BOTH("DD"),
    ADDED_BY_US("AU"),
    DELETED_BY_THEM("UD"),
    ADDED_BY_THEM("UA"),
    DELETED_BY_US("DU"),
    BOTH_ADDED("AA"),
    BOTH_MODIFIED("UU"),
    UNDEFINED("  ");

    private final String code;

    ConflictStatus(String code) {
        this.code = code;
    }

    public static ConflictStatus fromCode(String code) {
        for (ConflictStatus status : values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        return UNDEFINED;
    }
}