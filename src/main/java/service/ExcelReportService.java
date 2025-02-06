package service;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

public class ExcelReportService {

    public static void generateTestFilesReport(String outputFilePath, List<File> javaFiles) {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Test Files Report");

            Row headerRow = sheet.createRow(0);
            headerRow.createCell(0).setCellValue("Nome do Arquivo");
            headerRow.createCell(1).setCellValue("Tem 'test' no nome?");
            headerRow.createCell(2).setCellValue("Contém @Test?");
            headerRow.createCell(3).setCellValue("Tem os dois?");

            int rowNum = 1;
            for (File file : javaFiles) {
                boolean hasTestInName = file.getName().toLowerCase().contains("test");
                boolean hasTestAnnotation = containsTestAnnotation(file);
                boolean hasBoth = hasTestInName && hasTestAnnotation;

                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(file.getName());
                row.createCell(1).setCellValue(hasTestInName ? "Sim" : "Não");
                row.createCell(2).setCellValue(hasTestAnnotation ? "Sim" : "Não");
                row.createCell(3).setCellValue(hasBoth ? "Sim" : "Não");
            }

            try (FileOutputStream fileOut = new FileOutputStream(outputFilePath)) {
                workbook.write(fileOut);
            }

            System.out.println("Relatório gerado com sucesso: " + outputFilePath);
        } catch (IOException e) {
            System.err.println("Erro ao gerar a planilha: " + e.getMessage());
        }
    }

    private static boolean containsTestAnnotation(File file) {
        try {
            return Files.lines(file.toPath())
                    .map(String::trim)
                    .map(String::toLowerCase)
                    .anyMatch(line -> line.contains("@test"));
        } catch (IOException e) {
            System.err.println("Erro ao ler o arquivo: " + file.getName());
            return false;
        }
    }
}
