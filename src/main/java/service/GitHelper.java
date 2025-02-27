package service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
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
        return result.size() > 0 ? List.of(result.get(0).split(" ")).subList(1, result.get(0).split(" ").length) : new ArrayList<>();
    }

    public List<String> getGitStatus() throws IOException {
        return executeGitCommand("git", "status", "-s");
    }

    public boolean didMergeFail() throws IOException {
        List<String> result = executeGitCommand("git", "merge", "--no-ff", "--no-commit");
        return result.stream().anyMatch(line -> line.contains("Automatic merge failed; fix conflicts and then commit the result."));
    }

    public void resetRepository() throws IOException {
        executeGitCommand("git", "reset", "--hard");
        executeGitCommand("git", "merge", "--abort");
        executeGitCommand("git", "clean", "-fd");
    }

    public void executeGitCommandWithFeedback(String... command) throws IOException {
        List<String> output = executeGitCommand(command);
        output.forEach(System.out::println);
    }

    public List<String> getConflictingFiles() throws IOException {
        List<String> statusLines = getGitStatus();
        return statusLines.stream()
                .filter(line -> line.matches("^(D|A|U|UA|DU|AA|UU) .*"))  // Apenas arquivos conflitantes
                .map(line -> line.substring(3).trim())  // Remove código de status
                .collect(Collectors.toList());
    }


    private List<String> executeGitCommand(String... command) throws IOException {
        ProcessBuilder builder = new ProcessBuilder(command);
        builder.directory(new File(repoPath));
        builder.redirectErrorStream(true);
        Process process = builder.start();

        List<String> output = new ArrayList<>();
        try (var reader = new java.io.BufferedReader(new java.io.InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                output.add(line.trim());
            }
        }
        return output;
    }
}
