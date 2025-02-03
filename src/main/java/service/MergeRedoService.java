package service;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.MergeResult;
import org.eclipse.jgit.revwalk.RevCommit;
import java.io.File;
import java.util.List;

public class MergeRedoService {

    public static void redoMerges(String repoPath, List<RevCommit> merges) throws Exception {
        Git git = Git.open(new File(repoPath));

        for (RevCommit merge : merges) {
            System.out.println("Refazendo merge do commit: " + merge.getId().getName());

            git.checkout().setName(merge.getParent(0).getName()).call();
            MergeResult result = git.merge().include(merge.getParent(1)).call();

            if (result.getConflicts() != null) {
                System.out.println("Conflitos detectados nos arquivos de teste: " + result.getConflicts().keySet());
            }

            git.checkout().setName("main").call();
        }
    }
}
