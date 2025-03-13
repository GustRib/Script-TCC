package service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
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

    public List<String> processMerge(String mergeHash, boolean automaticMode) throws IOException {
        List<String> parents = gitHelper.getParentHashes(mergeHash);
        if (parents.size() < 2) return Collections.emptyList();

        parent1 = parents.get(0);
        parent2 = parents.get(1);

        System.out.println("\n🔄 Checkout para " + parent1);
        gitHelper.checkoutBranch(parent1);

        System.out.println("🔀 Tentando merge com " + parent2);
        List<String> output = gitHelper.mergeBranches(parent2);

        if (!gitHelper.didMergeFail(output)) {
            System.out.println("✅ Merge realizado com sucesso. Nenhum conflito encontrado.");
            return Collections.emptyList();
        }

        System.out.println("❌ Merge falhou. Analisando conflitos...");
        List<String> modifiedFilePaths = gitHelper.getGitStatus();

        List<String> conflictFiles = new ArrayList<>();
        for (String line : modifiedFilePaths) {
            if (line.length() > 3) {
                String status = line.substring(0, 2).trim();
                String filePath = line.substring(3).trim();

                if (status.matches("DD|AU|UD|UA|DU|AA|UU")) {
                    conflictFiles.add(filePath);
                }
            }
        }

        gitHelper.resetRepository();
        return conflictFiles;
    }



    private boolean userWantsToContinue() {
        System.out.print("Deseja continuar? (s/n): ");
        return scanner.nextLine().equalsIgnoreCase("s");
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

    public String getRepoPath() {
        return this.repoPath;
    }

}
