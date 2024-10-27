package org.os;
import java.util.Arrays;
import java.io.*;
import java.nio.file.*;
import java.util.Scanner;

public class CLI {

    public static Path currentDirectory = Paths.get("").toAbsolutePath();
    public static boolean running = true;

    public static Path getCurrentDirectory() {
        return currentDirectory;
    }

    public static void setCurrentDirectory(Path path) {
        currentDirectory = path;
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        while (running) {
            System.out.print(currentDirectory + "> ");
            String input = scanner.nextLine().trim();
            processInput(input);
        }

        scanner.close();
    }

    public static void processInput(String input) {
        String[] tokens = input.split("\\s+");

        switch (tokens[0]) {
            case "pwd":
                pwd();
                break;
            case "cd":
                if (tokens.length > 1) {
                    cd(tokens[1]);
                } else {
                    System.out.println("cd: missing operand");
                }
                break;
            case "ls":
                ls(tokens);
                break;
            case "mkdir":
                if (tokens.length > 1) {
                    mkdir(tokens[1]);
                } else {
                    System.out.println("mkdir: missing operand");
                }
                break;
            case "rmdir":
                if (tokens.length > 1) {
                    rmdir(tokens[1]);
                } else {
                    System.out.println("rmdir: missing operand");
                }
                break;
            case "touch":
                if (tokens.length > 1) {
                    touch(tokens[1]);
                } else {
                    System.out.println("touch: missing operand");
                }
                break;
            case "rm":
                if (tokens.length > 1) {
                    rm(tokens[1]);
                } else {
                    System.out.println("rm: missing operand");
                }
                break;
            case "cat":

                cat(Arrays.copyOfRange(tokens, 1, tokens.length));

                break;
            case "exit":
                exitCLI();
                break;
            case "help":
                displayHelp();
                break;
            default:
                System.out.println("Command not found: " + tokens[0]);
        }
    }

    public static void pwd() {
        System.out.println(currentDirectory);
    }

    public static void cd(String path) {
        Path newPath = currentDirectory.resolve(path).normalize();
        if (Files.exists(newPath) && Files.isDirectory(newPath)) {
            currentDirectory = newPath.toAbsolutePath();
        } else {
            System.out.println("cd: no such file or directory: " + path);
        }
    }

    public static void ls(String[] args) {
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(currentDirectory)) {
            for (Path entry : stream) {
                System.out.print(entry.getFileName() + " ");
            }
            System.out.println();
        } catch (IOException e) {
            System.out.println("ls: error reading directory");
        }
    }

    public static void mkdir(String dirName) {
        Path dirPath = currentDirectory.resolve(dirName);
        try {
            Files.createDirectory(dirPath);
            System.out.println("Directory created: " + dirName);
        } catch (IOException e) {
            System.out.println("mkdir: cannot create directory '" + dirName + "': " + e.getMessage());
        }
    }

    public static void rmdir(String dirName) {
        Path dirPath = currentDirectory.resolve(dirName);
        try {
            Files.delete(dirPath);
            System.out.println("Directory removed: " + dirName);
        } catch (IOException e) {
            System.out.println("rmdir: failed to remove '" + dirName + "': " + e.getMessage());
        }
    }

    public static void touch(String fileName) {
        Path filePath = currentDirectory.resolve(fileName);
        try {
            Files.createFile(filePath);
            System.out.println("File created: " + fileName);
        } catch (IOException e) {
            System.out.println("touch: cannot create file '" + fileName + "': " + e.getMessage());
        }
    }

    public static void rm(String fileName) {
        Path filePath = currentDirectory.resolve(fileName);
        try {
            Files.delete(filePath);
            System.out.println("File removed: " + fileName);
        } catch (IOException e) {
            System.out.println("rm: failed to remove '" + fileName + "': " + e.getMessage());
        }
    }

    public static void cat(String... args) {
        Scanner scanner = new Scanner(System.in);

        if (args.length == 0) {
            // Case 1: No arguments provided, act as an interactive `cat` command.
            System.out.println("Enter text (type 'EOF' on a new line to finish):");

            StringBuilder content = new StringBuilder();
            String line;

            // Capture multi-line input until 'EOF' is typed.
            while (!(line = scanner.nextLine()).equals("EOF")) {
                content.append(line).append(System.lineSeparator());
            }

            // Print the captured content to the terminal.
            System.out.println("\nYou entered:\n" + content.toString());

        } else if (args.length == 1) {
            // Case 2: File name provided as an argument.
            String fileName = args[0];
            Path filePath = currentDirectory.resolve(fileName);

            // Check if the file exists; if not, create it.
            if (!Files.exists(filePath)) {
                try {
                    Files.createFile(filePath);
                    System.out.println("File created: " + fileName);
                } catch (IOException e) {
                    System.out.println("cat: cannot create file '" + fileName + "': " + e.getMessage());
                    return;
                }
            }

            System.out.println("Enter text to write to " + fileName + " (type 'EOF' on a new line to finish):");

            try (BufferedWriter writer = Files.newBufferedWriter(filePath, StandardOpenOption.TRUNCATE_EXISTING)) {
                String line;
                // Capture multi-line input until 'EOF' is typed.
                while (!(line = scanner.nextLine()).equals("EOF")) {
                    writer.write(line);
                    writer.newLine();
                }
                System.out.println("Text written to file: " + fileName);
            } catch (IOException e) {
                System.out.println("cat: error writing to file '" + fileName + "': " + e.getMessage());
            }
        } else {
            System.out.println("cat: too many arguments");
        }
    }

    public static void exitCLI() {
        System.out.println("Exiting the CLI...");
        running = false;
    }

    public static void displayHelp() {
        System.out.println("Supported commands:");
        System.out.println("  pwd: Print current working directory.");
        System.out.println("  cd <dir>: Change directory.");
        System.out.println("  ls: List files in the current directory.");
        System.out.println("  mkdir <dir>: Create a new directory.");
        System.out.println("  rmdir <dir>: Remove an empty directory.");
        System.out.println("  touch <file>: Create an empty file.");
        System.out.println("  rm <file>: Remove a file.");
        System.out.println("  cat <file>: Display the contents of a file.");
        System.out.println("  exit: Terminate the CLI.");
        System.out.println("  help: Display this help message.");
    }
}
