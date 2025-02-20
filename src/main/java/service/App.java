package service;

import model.MergeInfo;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

//horas gastas no script = 23
public class App {
    private static final String BASE_REPO_DIR = "repos";

    public static void main(String[] args) throws IOException {

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
            scanner.nextLine();

            switch (opcao) {
                case 1:
                    System.out.print("Digite o link do repositório: ");
                    String repoUrl = scanner.nextLine();
                    String repoName = extractRepoName(repoUrl);
                    String repoDir = BASE_REPO_DIR + File.separator + repoName;

                    File repoFolder = new File(repoDir);
                    if (repoFolder.exists() && repoFolder.isDirectory()) {
                        System.out.println("Repositório já clonado. Avançando...");
                    } else {
                        System.out.println("Clonando repositório para " + repoDir);
                        GitService.cloneRepository(repoUrl, repoDir);
                    }
                    break;

                case 2:
                    System.out.print("Digite o diretório do repositório: ");
                    String dir = scanner.nextLine();
                    int count = FileService.countJavaFiles(dir);
                    System.out.println("Total de arquivos .java: " + count);
                    break;

                case 3:
                    System.out.print("Digite o diretório do repositório: ");
                    String testDir = scanner.nextLine();
                    List<File> testFiles1 = FileService.findTestFiles(FileService.listJavaFiles(testDir));

                    System.out.println("Total de arquivos de teste encontrados: " + testFiles1.size());
                    testFiles1.forEach(file -> System.out.println(file.getName()));

/*
                    String outputPath = "test_report.xlsx";
                    // Gerar a planilha com os dados
                    ExcelReportService.generateTestFilesReport(outputPath, javaFiles);
                    System.out.println("Planilha gerada em: " + outputPath);
*/
                    break;

                case 4:
                    long startTime = System.nanoTime();
                    System.out.println("Digite o caminho do repositório Git:");
                    String repoPath = scanner.nextLine();
                    System.out.println("Repositório selecionado: " + repoPath);

                    // Obter os arquivos de teste não mesclados
                    List<String> unmergedTestFiles = FileService.getUnmergedTestFiles(repoPath);
                    List<File> testFiles = unmergedTestFiles.stream()
                            .map(filePath -> new File(filePath))
                            .collect(Collectors.toList());

                    // Chamar listMerges passando repoPath e a lista de arquivos de teste
                    List<MergeInfo> testMerges = MergeService.listMerges(repoPath, testFiles); // Passando os arquivos de teste
                    System.out.println("Quantidade de merges que afetam arquivos de teste: " + testMerges.size());
                    testMerges.forEach(merge -> System.out.println(merge));

                    long endTime = System.nanoTime();
                    double elapsedTimeInSeconds = (endTime - startTime) / 1_000_000_000.0;
                    System.out.println("Tempo total de execução: " + elapsedTimeInSeconds + " segundos");
                    break;

                case 5:
                    System.out.println("Saindo...");
                    scanner.close();
                    return;

                default:
                    System.out.println("Opção inválida. Tente novamente.");
            }
        }
    }

    private static String extractRepoName(String repoUrl) {
        return repoUrl.substring(repoUrl.lastIndexOf("/") + 1).replace(".git", "");
    }

}
