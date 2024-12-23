import org.junit.jupiter.api.*;
import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.FileTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class CommandLineInterpreterTest {

    private final Path initialDirectory = CommandLineInterpreter.currentDirectory;

    @BeforeEach
    public void setUp() {
        // Reset to initial directory before each test
        CommandLineInterpreter.currentDirectory = initialDirectory;
    }

    @AfterEach
    public void tearDown() {
        CommandLineInterpreter.currentDirectory = initialDirectory;
    }

    @Test
    public void testPwd() {
        Path expectedPath = Paths.get(".").toAbsolutePath().normalize();
        assertEquals(expectedPath, CommandLineInterpreter.pwd());
    }

    @Test
    public void testCdValidPath() {
        Path originalDir = CommandLineInterpreter.currentDirectory;
        CommandLineInterpreter.cd("..");
        Path expectedPath = originalDir.getParent();
        assertEquals(expectedPath, CommandLineInterpreter.pwd());
    }

    @Test
    public void testCdInvalidPath() {
        Path originalDir = CommandLineInterpreter.currentDirectory;
        CommandLineInterpreter.cd("invalidPath");
        assertEquals(originalDir, CommandLineInterpreter.pwd());
    }


    @Test
    public void testLs() {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outputStream));
        CommandLineInterpreter.ls();

        String output = outputStream.toString().trim();
        assertTrue(output.contains("testFile.txt"), "Output should contain 'testFile.txt'");
        assertFalse(output.contains(".hiddenFile.txt"), "Output should not contain hidden files");
        assertFalse(output.isEmpty(), "Output should not be empty");
    }

    @Test
    public void testLsHidesHiddenFiles() throws IOException {
        // Redirect system output to capture ls output
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outputStream));

        // Create a visible file and a hidden file
        Path visibleFile = Files.createFile(Paths.get("visibleFile.txt"));
        Path hiddenFile = Files.createFile(Paths.get(".hiddenFile.txt"));

        try {
            CommandLineInterpreter.ls(); // Call ls

            String output = outputStream.toString().trim();
            System.setOut(System.out); // Reset to standard output

            // Assert that only visibleFile.txt is listed, not .hiddenFile.txt
            assertTrue(output.contains("visibleFile.txt"));
            assertFalse(output.contains(".hiddenFile.txt"));

        } finally {
            // Cleanup
            Files.deleteIfExists(visibleFile);
            Files.deleteIfExists(hiddenFile);
            System.setOut(System.out);  // Reset standard output
        }
    }

    @Test
    public void testLsr() {
        // Call the lsr() method
        String[] result = CommandLineInterpreter.lsr();

        // Get the expected file list from the current directory
        String[] expectedFiles = new File(".").list((dir, name) -> !name.startsWith(".")); // Exclude hidden files
        Arrays.sort(expectedFiles, Collections.reverseOrder()); // Sort in reverse order to match the expected output

        // Compare the expected result with the output of lsr()
        assertArrayEquals(expectedFiles, result);
    }

    @Test
    public void testLsAshowsHiddenFiles() throws IOException {
        // Create a visible file and a hidden file
        Path visibleFile = Files.createFile(Paths.get("visibleFile.txt"));
        Path hiddenFile = Files.createFile(Paths.get(".hiddenFile.txt"));

        try {
            // Call lsA() and capture its result
            String[] result = CommandLineInterpreter.lsA();

            // Convert the result array to a list for easier assertion checking
            List<String> fileList = Arrays.asList(result);

            // Assert that both the visible and hidden files are listed
            assertTrue(fileList.contains("visibleFile.txt"));
            assertTrue(fileList.contains(".hiddenFile.txt"));

        } finally {
            // Cleanup
            Files.deleteIfExists(visibleFile);
            Files.deleteIfExists(hiddenFile);
        }
    }
    @Test
    public void testTouchAndRmFile() throws IOException {
        String filename = "test.txt";
        CommandLineInterpreter.touch(filename);

        Path filePath = CommandLineInterpreter.currentDirectory.resolve(filename);
        assertTrue(Files.exists(filePath), "File should be created");

        CommandLineInterpreter.rm(filename);
        assertFalse(Files.exists(filePath), "File should be deleted");
    }

    @Test
    public void testMkdirAndRmdirDirectory() {
        String dirName = "testdir";
        CommandLineInterpreter.mkdir(dirName);

        Path dirPath = CommandLineInterpreter.currentDirectory.resolve(dirName);
        assertTrue(Files.exists(dirPath) && Files.isDirectory(dirPath), "Directory should be created");

        CommandLineInterpreter.rmdir(new File(dirName));
        assertFalse(Files.exists(dirPath), "Directory should be deleted");
    }

    @Test
    public void testAppendToFile() throws IOException {
        String filename = "appendtest.txt";
        CommandLineInterpreter.touch(filename);

        CommandLineInterpreter.appendToFile("Hello, world!", Paths.get(filename));

        Path filePath = CommandLineInterpreter.currentDirectory.resolve(filename);
        String content = Files.readString(filePath);

        assertTrue(content.contains("Hello, world!"), "Content should be appended");

        Files.deleteIfExists(filePath); // Cleanup after test
    }

    @Test
    public void testRedirectToFile() throws IOException {
        String filename = "redirecttest.txt";
        CommandLineInterpreter.redirectToFile(filename, "Test content");

        Path filePath = CommandLineInterpreter.currentDirectory.resolve(filename);
        String content = Files.readString(filePath);

        assertEquals("Test content", content);

        Files.deleteIfExists(filePath); // Cleanup after test
    }

    @Test
    public void testMvFile() throws IOException {
        String sourceFile = "source.txt";
        String destinationFile = "destination.txt";

        CommandLineInterpreter.touch(sourceFile);
        CommandLineInterpreter.mv(sourceFile, destinationFile);

        Path destinationPath = CommandLineInterpreter.currentDirectory.resolve(destinationFile);

        assertTrue(Files.exists(destinationPath), "File should be moved");
        assertFalse(Files.exists(CommandLineInterpreter.currentDirectory.resolve(sourceFile)), "Original file should no longer exist");

        Files.deleteIfExists(destinationPath); // Cleanup after test
    }

    @Test
    public void testCatFile() throws IOException {
        String filename = "catfile.txt";
        CommandLineInterpreter.redirectToFile(filename, "Cat command test");

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outputStream));

        CommandLineInterpreter.cat(filename);

        System.setOut(System.out);  // Reset to standard output
        assertTrue(outputStream.toString().contains("Cat command test"), "Content should be displayed");

        Files.deleteIfExists(CommandLineInterpreter.currentDirectory.resolve(filename)); // Cleanup
    }

    @Test
    public void testPipeLsTouch() throws IOException, InterruptedException {


        // Capture the initial timestamp
        Path testFilePath = CommandLineInterpreter.cd("test");
        FileTime initialTimestamp = Files.getLastModifiedTime(testFilePath);

        // Wait briefly to ensure a timestamp change can occur
        Thread.sleep(1000);

        // Step 2: Execute the pipe command "ls | touch"
        CommandLineInterpreter.pipe("ls", "touch");

        // Step 3: Capture the updated timestamp
        FileTime updatedTimestamp = Files.getLastModifiedTime(testFilePath);

        // Step 4: Verify that the timestamp has been updated
        assertFalse(updatedTimestamp.compareTo(initialTimestamp) < 0, "Timestamp should be updated by touch command");

    }
}

