package ru.strategy48.ejudge.polygon2ejudge;

import org.apache.commons.cli.*;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;
import ru.strategy48.ejudge.polygon2ejudge.contest.exceptions.ConfigurationException;
import ru.strategy48.ejudge.polygon2ejudge.contest.exceptions.ContestException;
import ru.strategy48.ejudge.polygon2ejudge.contest.ContestUtils;
import ru.strategy48.ejudge.polygon2ejudge.polygon.exceptions.PolygonException;
import ru.strategy48.ejudge.polygon2ejudge.polygon.PolygonSession;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * @author Perveev Mike (perveev_m@mail.ru)
 */
public class Main {
    private static Path credentialsFile;

    public static void main(String[] args) {
        Option credentialsFileOption = new Option("c", "credentials",
                true, "Credentials file path");
        Option contestIdOption = new Option("i", "contest_id", true, "Polygon contest ID");
        Option defaultConfigFileOption = new Option("s", "config", true, "Default serve.cfg template");
        Option contestsDirOption = new Option("d", "contest_dir", true, "Contest directory");

        credentialsFileOption.setArgs(1);
        contestIdOption.setArgs(1);
        defaultConfigFileOption.setArgs(1);
        contestsDirOption.setArgs(1);

        credentialsFileOption.setOptionalArg(false);
        contestIdOption.setOptionalArg(false);
        defaultConfigFileOption.setOptionalArg(false);
        contestsDirOption.setOptionalArg(false);

        Options posixOptions = new Options();
        posixOptions.addOption(credentialsFileOption);
        posixOptions.addOption(contestIdOption);
        posixOptions.addOption(defaultConfigFileOption);
        posixOptions.addOption(contestsDirOption);

        CommandLineParser parser = new DefaultParser();
        CommandLine commandLine;

        try {
            commandLine = parser.parse(posixOptions, args);
        } catch (ParseException e) {
            System.out.println("Error parsing arguments!");
            e.printStackTrace();
            return;
        }

        credentialsFile = Paths.get(commandLine.getOptionValue("credentials"));
        int contestId = Integer.parseInt(commandLine.getOptionValue("contest_id"));
        Path defaultConfigFile = Paths.get(commandLine.getOptionValue("config"));
        Path contestDirectory = Paths.get(commandLine.getOptionValue("contest_dir"));

        try (PolygonSession session = getPolygonSession()) {
            ContestUtils.prepareContest(session, contestId, contestDirectory,
                    "Generic", defaultConfigFile);
        } catch (IOException e) {
            System.out.println("Error happened while reading credentials file!");
            System.out.println(e.getMessage());
            e.printStackTrace();
        } catch (PolygonException e) {
            System.out.println("Error happened while working with Polygon!");
            System.out.println(e.getMessage());
            e.printStackTrace();
        } catch (ContestException e) {
            System.out.println("Error happened while preparing contest files!");
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    private static PolygonSession getPolygonSession() throws ContestException {
        String key, secret;

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(credentialsFile.toFile());
            document.getDocumentElement().normalize();

            key = document.getElementsByTagName("key").item(0).getTextContent();
            secret = document.getElementsByTagName("secret").item(0).getTextContent();
        } catch (ParserConfigurationException | IOException | SAXException e) {
            throw new ConfigurationException(credentialsFile, e);
        }

        return new PolygonSession(key, secret);
    }
}
