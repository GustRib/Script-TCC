package service;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.MergeResult;
import org.eclipse.jgit.revwalk.RevCommit;

import java.io.File;
import java.util.List;

public class MergeRedoService {

    public static void redoMerges(String repoPath, List<RevCommit> merges) throws Exception {
        try (Git git = Git.open(new File(repoPath))) {

            for (RevCommit merge : merges) {
                System.out.println("Refazendo merge do commit: " + merge.getId().getName());

                if (merge.getParentCount() < 2) {  // CORRIGIDO: usar merge em vez de commit
                    System.out.println("Commit não é um merge válido: " + merge.getId().getName());
                    continue;  // CORRIGIDO: usar continue em vez de break para não interromper o loop
                }

                // Checkout para o pai 1
                git.checkout().setName(merge.getParent(0).getName()).call();
                System.out.println("Checkout para o commit " + merge.getParent(0).getName());

                // Tenta fazer merge com o pai 2
                MergeResult result = git.merge().include(merge.getParent(1)).call();

                // Verifica conflitos
                if (result.getConflicts() != null) {
                    System.out.println("Conflitos detectados nos arquivos: " + result.getConflicts().keySet());
                } else {
                    System.out.println("Merge realizado sem conflitos.");
                }

                // Retorna para a branch principal
                git.checkout().setName("main").call();
            }
        }
    }
}
