package service;

import java.util.Scanner;

public class App {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        GitService gitService = new GitService();

        while (true) {
            System.out.println("\n=== MENU ===");
            System.out.println("1. Clonar repositório");
            System.out.println("2. Contar arquivos .java");
            System.out.println("3. Listar arquivos de teste");
            System.out.println("4. Analisar Merges");
            System.out.println("5. Sair");
            System.out.print("Escolha uma opção: ");

            int opcao = scanner.nextInt();
            scanner.nextLine();  // Consumir nova linha

            switch (opcao) {
                case 1:
                    System.out.print("Digite o link do repositório: ");
                    String repoUrl = scanner.nextLine();
                    System.out.print("Digite o diretório de destino: ");
                    String repoDir = scanner.nextLine();
                    GitService.cloneRepository(repoUrl, repoDir);
                    break;
                case 2:
                    System.out.print("Digite o diretório do repositório: ");
                    String dir = scanner.nextLine();
                    int count = FileService.listJavaFiles;
                    System.out.println("Total de arquivos .java: " + count);
                    break;

                case 3:
                    System.out.print("Digite o diretório do repositório: ");
                    String repoPath = scanner.nextLine();
                    MergeService.analyzeMerges(repoPath);
                    break;
                case 4:
                    System.out.println("Saindo...");
                    scanner.close();
                    return;
                default:
                    System.out.println("Opção inválida. Tente novamente.");
            }
        }
    }
}
