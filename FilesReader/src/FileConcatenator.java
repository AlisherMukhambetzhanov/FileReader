import java.io.*;
import java.nio.file.*;
import java.util.*;

public class FileConcatenator {

    private static Map<String, Set<String>> dependencies = new HashMap<>();
    private static Map<String, String> fileContents = new HashMap<>();
    private static List<String> sortedFiles = new ArrayList<>();

    public static void main(String[] args) {
        String rootDirectory = "C:\\My files\\FilesReader\\TestData";

        // Шаг 1: Обход файловой системы и чтение файлов
        try {
            Files.walk(Paths.get(rootDirectory))
                    .filter(Files::isRegularFile)
                    .forEach(path -> processFile(path.toFile(), rootDirectory));
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Шаг 2: Построение и сортировка списка файлов
        try {
            sortFiles();
        } catch (Exception e) {
            System.out.println("Ошибка: " + e.getMessage());
            return;
        }

        // Шаг 3: Конкатенация файлов
        concatenateFiles();
    }

    private static void processFile(File file, String rootDirectory) {
        try {
            List<String> lines = Files.readAllLines(file.toPath());
            StringBuilder contentBuilder = new StringBuilder();
            for (String line : lines) {
                if (line.startsWith("require")) {
                    String dependencyRelativePath = line.substring(line.indexOf("‘") + 1, line.lastIndexOf("’"));
                    String dependencyAbsolutePath = rootDirectory + File.separator + dependencyRelativePath;
                    dependencies.computeIfAbsent(file.getPath(), k -> new HashSet<>()).add(dependencyAbsolutePath.replace("/", File.separator));
                } else {
                    contentBuilder.append(line).append("\n");
                }
            }
            fileContents.put(file.getPath(), contentBuilder.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void sortFiles() throws Exception {
        // Топологическая сортировка файлов на основе зависимостей
        Set<String> visited = new HashSet<>();
        Set<String> recStack = new HashSet<>();

        for (String file : fileContents.keySet()) {
            if (!visited.contains(file)) {
                if (!topologicalSort(file, visited, recStack)) {
                    throw new Exception("Обнаружена циклическая зависимость");
                }
            }
        }
    }

    private static boolean topologicalSort(String file, Set<String> visited, Set<String> recStack) {
        if (recStack.contains(file)) {
            return false; // Обнаружен цикл
        }
        if (visited.contains(file)) {
            return true; // Уже посещен
        }

        visited.add(file);
        recStack.add(file);

        Set<String> fileDependencies = dependencies.get(file);
        if (fileDependencies != null) {
            for (String dep : fileDependencies) {
                if (!topologicalSort(dep, visited, recStack)) {
                    return false;
                }
            }
        }

        recStack.remove(file);
        sortedFiles.add(file);
        return true;
    }

    private static void concatenateFiles() {
        StringBuilder concatenatedContent = new StringBuilder();
        sortedFiles.forEach(file -> concatenatedContent.append(fileContents.get(file)));
        System.out.println(concatenatedContent.toString());
    }
}
