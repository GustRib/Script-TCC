package service;

import model.MergeInfo;
import java.util.ArrayList;
import java.util.List;

public class MergeService {
    public List<MergeInfo> findMerges(String repoPath) {
        List<MergeInfo> merges = new ArrayList<>();
        try {
            Process process = new ProcessBuilder("git", "log", "--merges", "--pretty=format:%H %P")
                    .directory(new File(repoPath))
                    .start();
            process.waitFor();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] parts = line.split(" ");
                    if (parts.length >= 3) {
                        merges.add(new MergeInfo(parts[0], parts[1], parts[2]));
                    }
                }
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return merges;
    }
}