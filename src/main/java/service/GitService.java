package service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class GitService {
    public static boolean cloneRepository(String repoUrl, String destinationPath) {
        try {
            ProcessBuilder builder = new ProcessBuilder("git", "clone", repoUrl, destinationPath);
            builder.redirectErrorStream(true);

            Process process = builder.start();

            // Captura e imprime a saída do comando
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    System.out.println(line);
                }
            }

            int exitCode = process.waitFor(); // Aguarda a finalização do processo

            if (exitCode == 0) {
                System.out.println("Repositório clonado com sucesso.");
                return true;
            } else {
                System.err.println("Erro ao clonar repositório. Código de saída: " + exitCode);
                return false;
            }
        } catch (IOException | InterruptedException e) {
            System.err.println("Falha ao executar o comando git clone: " + e.getMessage());
            Thread.currentThread().interrupt(); // Restaura o estado de interrupção
            return false;
        }
    }
}
