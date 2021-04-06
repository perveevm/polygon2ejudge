package ru.strategy48.ejudge.polygon2ejudge.contest;

import ru.strategy48.ejudge.polygon2ejudge.ConsoleLogger;
import ru.strategy48.ejudge.polygon2ejudge.contest.exceptions.ContestException;
import ru.strategy48.ejudge.polygon2ejudge.contest.exceptions.FileSystemException;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * @author Perveev Mike (perveev_m@mail.ru)
 * Provides methods for file system working
 */
public class FileUtils {
    static void copyFile(final Path from, final Path to) throws ContestException {
        ConsoleLogger.logInfo("Copying %s to %s", from.toString(), to.toString());

        try {
            Files.copy(from, to);
        } catch (IOException e) {
            throw new FileSystemException(from, to, e);
        }
    }

    static void moveFile(final Path from, final Path to) throws ContestException {
        ConsoleLogger.logInfo("Moving %s to %s", from.toString(), to.toString());

        try {
            Files.move(from, to);
        } catch (IOException e) {
            throw new FileSystemException(from, to, e);
        }
    }

    static void deleteFile(final Path path) throws ContestException {
        ConsoleLogger.logInfo("Deleting %s", path.toString());

        try {
            Files.deleteIfExists(path);
        } catch (IOException e) {
            throw new FileSystemException(path, e);
        }
    }

    static Path createFile(final Path path) throws ContestException {
        ConsoleLogger.logInfo("Creating file %s", path.toString());

        try {
            return Files.createFile(path);
        } catch (IOException e) {
            throw new FileSystemException(path, e);
        }
    }

    static void createDirectory(final Path path) throws ContestException {
        ConsoleLogger.logInfo("Creating directory %s", path.toString());

        try {
            Files.createDirectory(path);
        } catch (IOException e) {
            throw new FileSystemException(path, e);
        }
    }

    static String readFile(final Path path) throws ContestException {
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

    static void writeFile(final Path path, final String data) throws ContestException {
        ConsoleLogger.logInfo("Writing file %s", path.toString());

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(path.toFile()))) {
            writer.write(data);
        } catch (IOException e) {
            throw new FileSystemException(path, e);
        }
    }
}
