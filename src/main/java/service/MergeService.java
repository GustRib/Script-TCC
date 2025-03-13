package service;

import model.MergeInfo;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class MergeService {
    private final GitHelper gitHelper;
    private final MergeProcessor mergeProcessor;

    public MergeService(String repoPath) {
        this.gitHelper = new GitHelper(repoPath);
        this.mergeProcessor = new MergeProcessor(repoPath, gitHelper);
    }

    public List<MergeInfo> listMerges(boolean automaticMode) throws IOException {
        List<MergeInfo> testMerges = new ArrayList<>();
        List<String> mergeHashes = gitHelper.getMergeCommits();

        for (String mergeHash : mergeHashes) {
            try {
                resetRepository();
                gitHelper.checkoutBranch("master");

                System.out.println("\n🔄 Tentando refazer o merge: " + mergeHash);
                List<String> conflictFiles = mergeProcessor.processMerge(mergeHash, automaticMode);

                if (!conflictFiles.isEmpty()) {
                    List<File> unmergedTestFiles = conflictFiles.stream()
                            .filter(file -> file.toLowerCase().contains("test"))
                            .map(file -> new File(mergeProcessor.getRepoPath(), file)) // Obtém repoPath do mergeProcessor
                            .toList();

                    if (!unmergedTestFiles.isEmpty()) {
                        testMerges.add(new MergeInfo(mergeHash, mergeProcessor.getParent1(), mergeProcessor.getParent2()));
                        System.out.println("✅ Arquivos de teste conflitantes encontrados:");
                        unmergedTestFiles.forEach(file -> System.out.println(file.getName()));
                    }
                }
            } catch (IOException e) {
                System.err.println("Erro ao processar merge " + mergeHash + ": " + e.getMessage());
            }
        }
        return testMerges;
    }


    public void manualMerge(String mergeHash) throws IOException {
        gitHelper.resetRepository(); // Reset para evitar conflitos anteriores
        List<String> conflictFiles = mergeProcessor.processMerge(mergeHash, false); // Obtém lista de arquivos conflitantes

        if (conflictFiles.isEmpty()) {
            System.out.println("✅ Nenhum conflito detectado neste merge.");
            return;
        }
        int totalConflicts = conflictFiles.size();
        long testConflicts = conflictFiles.stream()
                .filter(file -> file.toLowerCase().contains("test"))
                .count();
        double testConflictPercentage = ((double) testConflicts / totalConflicts) * 100;

        System.out.println("\n📋 Arquivos em conflito:");
        conflictFiles.forEach(System.out::println);

        System.out.println("\n⚠️ Número total de arquivos em conflito: " + totalConflicts);
        System.out.println("🧪 Número de arquivos de teste em conflito: " + testConflicts);
        System.out.printf("📊 Percentual de arquivos de teste conflitantes: %.2f%%\n", testConflictPercentage);
    }

    private void resetRepository() throws IOException {
        System.out.println("🔄 Resetando para a master e limpando repositório...");
        gitHelper.resetRepository();
    }
}
