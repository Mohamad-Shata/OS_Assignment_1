package org.os;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.io.*;
import java.nio.file.*;

import static org.junit.jupiter.api.Assertions.*;

class CLITest {

    @TempDir
    Path tempDir;
    private final ByteArrayOutputStream outputStreamCaptor = new ByteArrayOutputStream();

    @BeforeEach
    public void setUp() {
        // Redirect system output to capture CLI output.
        System.setOut(new PrintStream(outputStreamCaptor));

        // Set the current directory of CLI to the temporary directory.
        CLI.setCurrentDirectory(tempDir);
    }

    @AfterEach
    public void tearDown() {
        System.setOut(System.out);
    }

    @Test
    public void testPwd() {
        CLI.pwd();
        assertTrue(outputStreamCaptor.toString().contains(tempDir.toString()), "pwd command failed");
    }

    @Test
    public void testCd() {
        Path newDir = tempDir.resolve("testDir");
        CLI.mkdir("testDir");
        CLI.cd("testDir");
        assertEquals(newDir.toAbsolutePath(), CLI.getCurrentDirectory());
    }

    @Test
    public void testMkdirAndLs() {
        CLI.mkdir("newDir");
        CLI.ls(new String[]{});
        assertTrue(outputStreamCaptor.toString().contains("newDir"), "mkdir or ls command failed");
    }

    @Test
    public void testTouchAndLs() {
        CLI.touch("testFile.txt");
        CLI.ls(new String[]{});
        assertTrue(outputStreamCaptor.toString().contains("testFile.txt"), "touch or ls command failed");
    }

    @Test
    public void testRm() {
        CLI.touch("fileToDelete.txt");
        assertTrue(Files.exists(tempDir.resolve("fileToDelete.txt")), "File was not created properly");

        CLI.rm("fileToDelete.txt");
        assertFalse(Files.exists(tempDir.resolve("fileToDelete.txt")), "rm command failed to delete the file");
    }

    @Test
    public void testCatDisplayFileContents() throws IOException {
        String filename = "displayFile.txt";
        Path filePath = tempDir.resolve(filename);
        Files.write(filePath, "Hello, World!".getBytes());

        CLI.handleCat(new String[]{"cat", filename});
        assertTrue(outputStreamCaptor.toString().contains("Hello, World!"), "cat command failed to display file content");
    }

    @Test
    public void testCatOverwriteOperator() throws IOException {
        String filename = "overwriteFile.txt";
        Path filePath = tempDir.resolve(filename);
        Files.write(filePath, "Original content\n".getBytes());

        // Simulate user input for overwriting content
        System.setIn(new ByteArrayInputStream("New content\nEOF\n".getBytes()));
        CLI.handleCat(new String[]{"cat", ">", filename});

        // Verify file content was overwritten
        String fileContent = Files.readString(filePath).replace("\r\n", "\n").replace("\r", "\n");
        assertEquals("New content\n", fileContent, "cat > command failed to overwrite file content");
    }

    @Test
    public void testCatAppendOperator() throws IOException {
        String filename = "appendFile.txt";
        Path filePath = tempDir.resolve(filename);
        Files.write(filePath, "Initial content\n".getBytes());

        // Simulate user input for appending content
        System.setIn(new ByteArrayInputStream("Appended content\nEOF\n".getBytes()));
        CLI.handleCat(new String[]{"cat", ">>", filename});

        // Verify file content was appended
        String fileContent = Files.readString(filePath).replace("\r\n", "\n").replace("\r", "\n");
        assertEquals("Initial content\nAppended content\n", fileContent, "cat >> command failed to append to file content");
    }

    @Test
    public void testCatWithMultipleFilesDisplay() throws IOException {
        String filename1 = "file1.txt";
        String filename2 = "file2.txt";
        Path filePath1 = tempDir.resolve(filename1);
        Path filePath2 = tempDir.resolve(filename2);

        Files.write(filePath1, "Content of file 1\n".getBytes());
        Files.write(filePath2, "Content of file 2\n".getBytes());

        CLI.handleCat(new String[]{"cat", filename1, filename2});
        String output = outputStreamCaptor.toString();

        assertTrue(output.contains("Content of file 1"), "cat command failed to display content of file1");
        assertTrue(output.contains("Content of file 2"), "cat command failed to display content of file2");
    }

    @Test
    public void testCatWithMultipleFilesAndRedirectOverwrite() throws IOException {
        String filename1 = "file1.txt";
        String filename2 = "file2.txt";
        String redirectFile = "outputFile.txt";

        Path filePath1 = tempDir.resolve(filename1);
        Path filePath2 = tempDir.resolve(filename2);
        Path redirectPath = tempDir.resolve(redirectFile);

        Files.write(filePath1, "First file content\n".getBytes());
        Files.write(filePath2, "Second file content\n".getBytes());

        CLI.handleCat(new String[]{"cat", filename1, filename2, ">", redirectFile});

        String outputContent = Files.readString(redirectPath).replace("\r\n", "\n").replace("\r", "\n");
        assertEquals("First file content\nSecond file content\n", outputContent, "cat > command failed to overwrite with multiple files");
    }

    @Test
    public void testCatWithMultipleFilesAndRedirectAppend() throws IOException {
        String filename1 = "file1.txt";
        String filename2 = "file2.txt";
        String redirectFile = "outputFile.txt";

        Path filePath1 = tempDir.resolve(filename1);
        Path filePath2 = tempDir.resolve(filename2);
        Path redirectPath = tempDir.resolve(redirectFile);

        // Initial content in redirect file
        Files.write(redirectPath, "Initial output\n".getBytes());
        Files.write(filePath1, "First file content\n".getBytes());
        Files.write(filePath2, "Second file content\n".getBytes());

        CLI.handleCat(new String[]{"cat", filename1, filename2, ">>", redirectFile});

        String outputContent = Files.readString(redirectPath).replace("\r\n", "\n").replace("\r", "\n");
        assertEquals("Initial output\nFirst file content\nSecond file content\n", outputContent, "cat >> command failed to append with multiple files");
    }




    @Test
    public void testInvalidCommand() {
        CLI.processInput("invalidCommand");
        assertTrue(outputStreamCaptor.toString().contains("Command not found"), "Invalid command not handled correctly");
    }

    @Test
    public void testHelpCommand() {
        CLI.displayHelp();
        String helpOutput = outputStreamCaptor.toString();
        assertTrue(helpOutput.contains("Supported commands"), "Help command output does not contain expected text");
        assertTrue(helpOutput.contains("pwd"), "Help command output does not list 'pwd'");
        assertTrue(helpOutput.contains("cd <dir>"), "Help command output does not list 'cd'");
        assertTrue(helpOutput.contains("ls"), "Help command output does not list 'ls'");
    }
}
