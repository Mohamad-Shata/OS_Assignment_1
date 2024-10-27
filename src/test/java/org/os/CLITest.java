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
