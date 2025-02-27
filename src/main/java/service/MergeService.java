package service;

import model.MergeInfo;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class MergeService {
    private final String repoPath;
    private final GitHelper gitHelper;
    private final MergeProcessor mergeProcessor;

    public MergeService(String repoPath) {
        this.repoPath = repoPath;
        this.gitHelper = new GitHelper(repoPath);
        this.mergeProcessor = new MergeProcessor(repoPath, gitHelper);
    }

    public List<MergeInfo> listMerges(boolean automaticMode) throws IOException {
        List<MergeInfo> testMerges = new ArrayList<>();
        Scanner scanner = new Scanner(System.in);

        // Obter todos os hashes de commits de merge
        List<String> mergeHashes = gitHelper.getMergeCommits();

        for (String mergeHash : mergeHashes) {
            // Sempre voltar para a master e limpar o repositório antes do próximo merge
            System.out.println("🔄 Resetando para a master e limpando repositório...");
            gitHelper.executeGitCommandWithFeedback("git", "checkout", "master");
            gitHelper.executeGitCommandWithFeedback("git", "reset", "--hard");
            gitHelper.executeGitCommandWithFeedback("git", "clean", "-df");

            System.out.println("\n🔄 Tentando refazer o merge: " + mergeHash);
            boolean mergeSuccess = mergeProcessor.processMerge(mergeHash, automaticMode);

            if (!mergeSuccess) {
                System.out.println("❌ Merge falhou. Aplicando heurística para encontrar arquivos de teste conflitantes.");
                if (!mergeProcessor.getUnmergedTestFiles().isEmpty()) {
                    testMerges.add(new MergeInfo(mergeHash, mergeProcessor.getParent1(), mergeProcessor.getParent2()));
                    System.out.println("✅ Arquivos de teste conflitantes encontrados:");
                    mergeProcessor.getUnmergedTestFiles().forEach(file -> System.out.println(file.getName()));

                } else {
                    System.out.println("⚠️ Nenhum arquivo de teste conflitante encontrado.");
                }
            } else {
                System.out.println("✅ Merge aplicado sem conflitos.");
            }

            if (!automaticMode) {
                System.out.print("Deseja continuar para o próximo merge? (s/n): ");
                String resposta = scanner.nextLine();
                if (!resposta.equalsIgnoreCase("s")) {
                    System.out.println("Processo interrompido pelo usuário.");
                    break;
                }
            }
        }
        return testMerges;
    }
}
