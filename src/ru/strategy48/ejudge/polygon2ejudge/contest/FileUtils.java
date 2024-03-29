package ru.strategy48.ejudge.polygon2ejudge.contest;

import ru.strategy48.ejudge.polygon2ejudge.ConsoleLogger;
import ru.strategy48.ejudge.polygon2ejudge.contest.exceptions.ContestException;
import ru.strategy48.ejudge.polygon2ejudge.contest.exceptions.FileSystemException;
import ru.strategy48.ejudge.polygon2ejudge.contest.exceptions.ScriptException;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * @author Perveev Mike (perveev_m@mail.ru)
 * Provides methods for file system working
 */
public class FileUtils {
    public static void copyFile(final Path from, final Path to) throws ContestException {
        ConsoleLogger.logInfo("Copying %s to %s", from.toString(), to.toString());

        try {
            Files.copy(from, to);
        } catch (IOException e) {
            throw new FileSystemException(from, to, e);
        }
    }

    public static void copyFileCorrectingLineBreaks(final Path from, final Path to) throws ContestException {
        ConsoleLogger.logInfo("Copying %s to %s correcting line breaks", from.toString(), to.toString());

        try (BufferedReader reader = new BufferedReader(new FileReader(from.toFile()))) {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(to.toFile()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    writer.write(line);
                    writer.write(System.lineSeparator());
                }
            } catch (IOException e) {
                throw new FileSystemException(from, to, e);
            }
        } catch (IOException e) {
            throw new FileSystemException(from, to, e);
        }
    }

    public static void moveFile(final Path from, final Path to) throws ContestException {
        ConsoleLogger.logInfo("Moving %s to %s", from.toString(), to.toString());

        try {
            Files.move(from, to);
        } catch (IOException e) {
            throw new FileSystemException(from, to, e);
        }
    }

    public static boolean exists(final Path p) {
        ConsoleLogger.logInfo("Checking if file %s exists", p.toString());
        return Files.exists(p);
    }

    public static void deleteFile(final Path path) throws ContestException {
        ConsoleLogger.logInfo("Deleting %s", path.toString());

        try {
            Files.deleteIfExists(path);
        } catch (IOException e) {
            throw new FileSystemException(path, e);
        }
    }

    public static Path createFile(final Path path) throws ContestException {
        ConsoleLogger.logInfo("Creating file %s", path.toString());

        try {
            return Files.createFile(path);
        } catch (IOException e) {
            throw new FileSystemException(path, e);
        }
    }

    public static void createDirectory(final Path path) throws ContestException {
        ConsoleLogger.logInfo("Creating directory %s", path.toString());

        try {
            Files.createDirectory(path);
        } catch (IOException e) {
            throw new FileSystemException(path, e);
        }
    }

    public static String readFile(final Path path) throws ContestException {
        ConsoleLogger.logInfo("Reading file %s", path.toString());

        StringBuilder res = new StringBuilder();

        try (BufferedReader reader = new BufferedReader(new FileReader(path.toFile()))) {
            char[] buffer = new char[1 << 16];
            int cnt;
            while ((cnt = reader.read(buffer)) != -1) {
                res.append(buffer, 0, cnt);
            }
        } catch (IOException e) {
            throw new FileSystemException(path, e);
        }

        return res.toString();
    }

    public static void writeFile(final Path path, final String data) throws ContestException {
        ConsoleLogger.logInfo("Writing file %s", path.toString());

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(path.toFile()))) {
            writer.write(data);
        } catch (IOException e) {
            throw new FileSystemException(path, e);
        }
    }

    public static Path removeExtension(final Path path) {
        String s = path.toString();
        return Path.of(s.substring(0, s.lastIndexOf('.')));
    }

    public static void makeExecutable(final Path path) throws ContestException {
        String cmd = String.format("sudo chmod +x %s", path);
        try {
            Process chmod = Runtime.getRuntime().exec(cmd);
            int exitCode = chmod.waitFor();
            if (exitCode != 0) {
                throw new ScriptException(cmd);
            }
        } catch (IOException | InterruptedException e) {
            throw new ScriptException(cmd, e);
        }
    }
}
