package service;

import java.io.IOException;

public class GitService {
    public static void cloneRepository(String repoUrl, String destinationPath) {
        try {
            ProcessBuilder builder = new ProcessBuilder("git", "clone", repoUrl, destinationPath);
            builder.inheritIO().start().waitFor();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
