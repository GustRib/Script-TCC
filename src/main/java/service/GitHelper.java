package service;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public class GitHelper {
    private final String repoPath;

    public GitHelper(String repoPath) {
        this.repoPath = repoPath;
    }

    public List<String> getMergeCommits() throws IOException {
        return executeGitCommand("git", "log", "--oneline", "--merges", "--all", "--pretty=format:%H");
    }

    public List<String> getParentHashes(String mergeHash) throws IOException {
        List<String> result = executeGitCommand("git", "rev-list", "-n", "1", "--parents", mergeHash);
        return result.isEmpty() ? List.of() : List.of(result.get(0).split(" ")).subList(1, result.get(0).split(" ").length);
    }

    public List<String> getGitStatus() throws IOException {
        return executeGitCommand("git", "status", "-s");
    }

    public boolean didMergeFail(List<String> result) {
        return result.stream().anyMatch(line -> line.contains("Automatic merge failed"));
    }

    public void resetRepository() throws IOException {
        executeGitCommand("git", "reset", "--hard");
        executeGitCommand("git", "merge", "--abort");
        executeGitCommand("git", "clean", "-fd");
    }

    public void checkoutBranch(String branch) throws IOException {
        executeGitCommand("git", "checkout", branch);
    }

    public List<String> mergeBranches(String branch) throws IOException {
        return executeGitCommand("git", "merge", branch, "--no-ff", "--no-commit");
    }

    private List<String> executeGitCommand(String... command) throws IOException {
        ProcessBuilder builder = new ProcessBuilder(command);
        builder.directory(new File(repoPath));
        builder.redirectErrorStream(true);
        Process process = builder.start();

        return new java.io.BufferedReader(new java.io.InputStreamReader(process.getInputStream()))
                .lines()
                .map(String::trim)
                .collect(Collectors.toList());
    }
}