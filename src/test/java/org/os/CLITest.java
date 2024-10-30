package org.os;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.io.*;
import java.nio.file.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import static org.junit.jupiter.api.Assertions.*;

class CLITest {

    @TempDir
    Path tempDir;
    private final ByteArrayOutputStream outputStreamCaptor = new ByteArrayOutputStream();

    @BeforeEach
    public void setUp() {
        System.setOut(new PrintStream(outputStreamCaptor));

        CLI.setCurrentDirectory(tempDir);
    }

    @AfterEach
    public void tearDown() {
        System.setOut(System.out);
    }


    @Test
    void testLsReverse() throws IOException {

        Files.createFile(tempDir.resolve("file1.txt"));
        Files.createFile(tempDir.resolve("file3.txt"));
        Files.createFile(tempDir.resolve("file2.txt"));


        CLI.lsReverse();

        // Assertions
        String[] outputLines = outputStreamCaptor.toString().split(System.lineSeparator());
        List<String> filenames = Stream.of(outputLines)
                .filter(line -> !line.startsWith("Listing files"))
                .map(String::trim).collect(Collectors.toList());

        assertEquals(3, filenames.size());
        assertEquals("file3.txt", filenames.get(0));
        assertEquals("file2.txt", filenames.get(1));
        assertEquals("file1.txt", filenames.get(2));

        outputStreamCaptor.reset();
    }


    @Test
    void testLsReverseEmptyDirectory() {
        CLI.lsReverse();


        String output = outputStreamCaptor.toString().trim();
        assertTrue(output.startsWith("Listing files") );

        outputStreamCaptor.reset();
    }

    @Test
    public void testLsGrep() throws IOException {
        Path testGrepFile = tempDir.resolve("test_grep.txt");
        Path otherFile = tempDir.resolve("other_file.txt");

        Files.createFile(testGrepFile);
        Files.createFile(otherFile);

        CLI.setCurrentDirectory(tempDir);

        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));

        CLI.lsGrep("test_grep");

        String output = outContent.toString().trim();
        assertEquals("test_grep.txt", output);

        System.setOut(System.out);
    }

    @Test
    public void testLsShowsHiddenFilesWhenFlagIsTrue() throws IOException {
        // Create a hidden file in the temporary directory
        Path hiddenFile = tempDir.resolve(".hiddenFile.txt");
        Files.createFile(hiddenFile);

        // Call the CLI method with the flag set to true
        CLI.lsa(true);

        // Prepare expected output
        String expectedOutput = "Listing files in: " + tempDir + "\n" + ".hiddenFile.txt";

        // Normalize line endings
        String actualOutput = outputStreamCaptor.toString().replace("\r\n", "\n");
        expectedOutput = expectedOutput.replace("\r\n", "\n");

        // Assert that the expected output matches the actual output
        assertEquals(expectedOutput.trim(), actualOutput.trim());
    }

    @Test
    public void testLsDoesNotShowHiddenFilesWhenFlagIsFalse() throws IOException {

    Path hiddenFile = tempDir.resolve(".hiddenFile.txt");
    Files.createFile(hiddenFile);

    CLI.lsa(false);

    String expectedOutput = "Listing files in: " + tempDir;
    assertEquals(expectedOutput.trim(), outputStreamCaptor.toString().trim());
    }

    
     
    @Test
    public void testLsEmptyDirectory() {
    CLI.ls();  
    String expectedOutput = "Listing files in: " + tempDir; 
    assertEquals(expectedOutput, outputStreamCaptor.toString().trim());
    }


    @Test
    public void testLsWithFiles() throws Exception {
     
        Files.createFile(tempDir.resolve("file1.txt"));
        Files.createFile(tempDir.resolve("file2.txt"));
        Files.createDirectory(tempDir.resolve("folder1"));

      
        CLI.ls();

        
        String expectedOutput = "Listing files in: " + tempDir + System.lineSeparator() +
                                 "file1.txt" + System.lineSeparator() +
                                 "file2.txt" + System.lineSeparator() +
                                 "folder1";
        assertEquals(expectedOutput.trim(), outputStreamCaptor.toString().trim());
    }

    @Test
    public void testLsHandlesIOException() throws Exception {
        
        Path notADirectory = Files.createFile(tempDir.resolve("not_a_directory.txt"));
        CLI.setCurrentDirectory(notADirectory);  

        CLI.ls(); 

      
        assertTrue(outputStreamCaptor.toString().contains("Error reading directory:"));
    }

    @Test
    public void test_pwd() {
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
    public void test_mkdir() {

        CLI.mkdir("newDir");
        CLI.ls(new String[]{});
        assertTrue(outputStreamCaptor.toString().contains("newDir"), "mkdir or ls command failed");
    }

    @Test
    public void test_rmdir()
    {
        Path dir = tempDir.resolve("dir");
        CLI.mkdir("dir");
        assertTrue(Files.exists(dir) && Files.isDirectory(dir), "Directory was not created properly");
    }

    @Test
    public void testTouchAndLs() {
        CLI.touch("testFile.txt");
        CLI.ls(new String[]{});
        assertTrue(outputStreamCaptor.toString().contains("testFile.txt"), "touch or ls command failed");
    }

    @Test
    public void testMv_rename() {
        Path oldFilePath = tempDir.resolve("file1.txt");
        Path newFilePath = tempDir.resolve("file2.txt");

        CLI.touch(oldFilePath.toString());

        CLI.mv(oldFilePath.toString(), newFilePath.toString());

        assertTrue(Files.exists(newFilePath), "File rename failed");
        assertFalse(Files.exists(oldFilePath), "Old file should not exist after rename");

    }
    @Test
    public void testMv_move() {
        Path sourceFilePath = tempDir.resolve("file1.txt");
        Path targetDirPath = tempDir.resolve("targetDir");
        Path targetFilePath = targetDirPath.resolve("file1.txt");

        CLI.touch(sourceFilePath.toString());

       CLI.mkdir(targetDirPath.toString());

        CLI.mv(sourceFilePath.toString(), targetDirPath.toString());

        assertTrue(Files.exists(targetFilePath), "File was  not moved to target directory  .");
        assertFalse(Files.exists(sourceFilePath), "Source file still exists.");
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

        System.setIn(new ByteArrayInputStream("New content\nEOF\n".getBytes()));
        CLI.handleCat(new String[]{"cat", ">", filename});

        String fileContent = Files.readString(filePath).replace("\r\n", "\n").replace("\r", "\n");
        assertEquals("New content\n", fileContent, "cat > command failed to overwrite file content");
    }

    @Test
    public void testCatAppendOperator() throws IOException {
        String filename = "appendFile.txt";
        Path filePath = tempDir.resolve(filename);
        Files.write(filePath, "Initial content\n".getBytes());

        System.setIn(new ByteArrayInputStream("Appended content\nEOF\n".getBytes()));
        CLI.handleCat(new String[]{"cat", ">>", filename});

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

    @Test
    void test_exit_CLI()
    {
        CLI.running = true;
        CLI.exitCLI();
        assertFalse(CLI.running, "CLI should not be running after exit");
    }

}
