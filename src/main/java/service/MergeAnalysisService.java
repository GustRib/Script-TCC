package service;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.util.io.DisabledOutputStream;
import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

public class MergeAnalysisService {

    public static List<RevCommit> findMergesAffectingTests(String repoPath, List<RevCommit> mergeCommits, List<File> testFiles) throws Exception {
        Git git = Git.open(new File(repoPath));

        return mergeCommits.stream()
                .filter(commit -> didMergeAffectTestFiles(git, commit, testFiles))
                .collect(Collectors.toList());
    }

    private static boolean didMergeAffectTestFiles(Git git, RevCommit commit, List<File> testFiles) {
        try {
            RevCommit parent1 = commit.getParent(0);
            RevCommit parent2 = commit.getParent(1);

            CanonicalTreeParser oldTree = new CanonicalTreeParser();
            oldTree.reset(git.getRepository().newObjectReader(), parent1.getTree());

            CanonicalTreeParser newTree = new CanonicalTreeParser();
            newTree.reset(git.getRepository().newObjectReader(), parent2.getTree());

            DiffFormatter df = new DiffFormatter(DisabledOutputStream.INSTANCE);
            df.setRepository(git.getRepository());
            List<DiffEntry> diffs = df.scan(oldTree, newTree);

            for (DiffEntry diff : diffs) {
                for (File testFile : testFiles) {
                    if (diff.getNewPath().endsWith(testFile.getName())) {
                        return true;
                    }
                }
            }
        } catch (Exception e) {
            return false;
        }
        return false;
    }
}
