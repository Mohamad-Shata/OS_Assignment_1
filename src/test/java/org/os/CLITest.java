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
    public void testLsRecursiveListsFilesInSubdirectories() throws IOException {
    
        Path subDir1 = tempDir.resolve("subdir1");
        Path subDir2 = tempDir.resolve("subdir2");
        Files.createDirectory(subDir1);
        Files.createDirectory(subDir2);
        
        Path file1 = subDir1.resolve("file1.txt");
        Path file2 = subDir1.resolve("file2.txt");
        Path file3 = subDir2.resolve("file3.txt");
        Files.createFile(file1);
        Files.createFile(file2);
        Files.createFile(file3);
    
        
        CLI.lsr();
    
        String expectedOutput = "Listing files recursively in: " + tempDir + "\n" +
                                "subdir1\n" +
                                "subdir1/file1.txt\n" +
                                "subdir1/file2.txt\n" +
                                "subdir2\n" +
                                "subdir2/file3.txt";
        assertEquals(expectedOutput.trim(), outputStreamCaptor.toString().trim());
    }
    @Test
    public void testLsRecursiveHandlesMultipleFileTypes() throws IOException {
    
    Path dir1 = tempDir.resolve("dir1");
    Path dir2 = tempDir.resolve("dir2");
    Files.createDirectory(dir1);
    Files.createDirectory(dir2);
    
    Path textFile1 = dir1.resolve("file1.txt");
    Path textFile2 = dir1.resolve("file2.txt");
    Path emptyFile = dir2.resolve("emptyFile.txt");
    Path nestedDir = dir1.resolve("nestedDir");
    Files.createFile(textFile1);
    Files.createFile(textFile2);
    Files.createFile(emptyFile);
    Files.createDirectory(nestedDir);
    
   
    Path nestedFile = nestedDir.resolve("nestedFile.txt");
    Files.createFile(nestedFile);
    
  
    CLI.lsr();
    
   
    String expectedOutput = "Listing files recursively in: " + tempDir + "\n" +
                            "dir1\n" +
                            "dir1/nestedDir\n" +
                            "dir1/nestedDir/nestedFile.txt\n" +
                            "dir1/file1.txt\n" +
                            "dir1/file2.txt\n" +
                            "dir2\n" +
                            "dir2/emptyFile.txt";
    
    assertEquals(expectedOutput.trim(), outputStreamCaptor.toString().trim());
    }

    @Test
    public void testLsShowsHiddenFilesWhenFlagIsTrue() throws IOException {
    
    Path hiddenFile = tempDir.resolve(".hiddenFile.txt");
    Files.createFile(hiddenFile);

    CLI.lsa(true);
    
    String expectedOutput = "Listing files in: " + tempDir + "\n" + ".hiddenFile.txt";
    assertEquals(expectedOutput.trim(), outputStreamCaptor.toString().trim());
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
    public void testMv_rename() {
        // Create the original file
        Path oldFilePath = tempDir.resolve("file1.txt");
        Path newFilePath = tempDir.resolve("file2.txt");

        // Create the file using your CLI touch method
        CLI.touch(oldFilePath.toString());

        // Now call your mv method to rename/move the file
        CLI.mv(oldFilePath.toString(), newFilePath.toString());

        // Assert that the new file exists
        assertTrue(Files.exists(newFilePath), "File rename failed");
        assertFalse(Files.exists(oldFilePath), "Old file should not exist after rename");

    }
    @Test
    public void testMv_move() {
        // Setup paths
        Path sourceFilePath = tempDir.resolve("file1.txt");
        Path targetDirPath = tempDir.resolve("targetDir");
        Path targetFilePath = targetDirPath.resolve("file1.txt");

        // Create the source file using your CLI touch method
        CLI.touch(sourceFilePath.toString());

       CLI.mkdir(targetDirPath.toString());

        // Move the file using your CLI mv method
        CLI.mv(sourceFilePath.toString(), targetDirPath.toString());

        // Assert that the file has been moved
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
