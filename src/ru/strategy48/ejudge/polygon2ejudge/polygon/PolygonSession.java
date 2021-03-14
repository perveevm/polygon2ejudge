package ru.strategy48.ejudge.polygon2ejudge.polygon;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import ru.strategy48.ejudge.polygon2ejudge.polygon.exceptions.ConnectionError;
import ru.strategy48.ejudge.polygon2ejudge.polygon.exceptions.IncorrectParametersException;
import ru.strategy48.ejudge.polygon2ejudge.polygon.exceptions.PolygonException;
import ru.strategy48.ejudge.polygon2ejudge.polygon.exceptions.ResponseException;
import ru.strategy48.ejudge.polygon2ejudge.polygon.objects.Package;
import ru.strategy48.ejudge.polygon2ejudge.polygon.objects.Problem;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * @author Perveev Mike (perveev_m@mail.ru)
 * Provides some Polygon API methods
 */
public class PolygonSession implements AutoCloseable {
    private final String key;
    private final String secret;

    private final CloseableHttpClient client = HttpClients.createDefault();
    private final Random random = new Random();

    private static final String ALPHABET = "abcdefghijklmnopqrstuvwxyz";

    /**
     * Constructs Polygon session
     *
     * @param key    API key
     * @param secret API secret value
     */
    public PolygonSession(final String key, final String secret) {
        this.key = key;
        this.secret = secret;
    }

    @Override
    public void close() throws IOException {
        client.close();
    }

    /**
     * Saves problem package provided as .zip archive to given path
     *
     * @param problemId problem ID
     * @param packageId package ID
     * @param path      directory to save archive
     * @return {@link Path} to saved archive
     * @throws PolygonException if something went wrong while working with API
     */
    public Path saveProblemPackageToFile(final int problemId, final int packageId, final Path path)
            throws PolygonException {
        BufferedInputStream inputStream = getStreamResponse("problem.package", List.of(
                new BasicNameValuePair("problemId", String.valueOf(problemId)),
                new BasicNameValuePair("packageId", String.valueOf(packageId))));

        String fileName = "package-" + packageId + ".zip";
        Path filePath = Paths.get(path.toString(), fileName);

        try (BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(filePath.toFile()))) {
            int c;
            while ((c = inputStream.read()) != -1) {
                outputStream.write(c);
            }
        } catch (FileNotFoundException e) {
            throw new PolygonException("couldn't create archive file (" + e.getMessage() + ")", e);
        } catch (IOException e) {
            throw new PolygonException("couldn't write or close archive file (" + e.getMessage() + ")", e);
        }

        try {
            inputStream.close();
        } catch (IOException e) {
            throw new PolygonException("couldn't close input stream with archive file (" + e.getMessage() + ")", e);
        }

        return filePath;
    }

    /**
     * Gets {@link List} of problems in given contest
     *
     * @param contestId contest ID
     * @return {@link List} of problems
     * @throws PolygonException if something went wrong while working with API
     */
    public List<Problem> getContestProblems(final int contestId) throws PolygonException {
        JSONObject response = getJSONObjectResponse("contest.problems", List.of(
                new BasicNameValuePair("contestId", String.valueOf(contestId))));

        List<Problem> problems = new ArrayList<>();
        for (String key : response.keySet()) {
            problems.add(JSONUtils.problemFromJSON(response.getJSONObject(key)));
        }

        return problems;
    }

    /**
     * Gets {@link List} of packages for given problem
     *
     * @param problemId problem ID
     * @return {@link List} of packages
     * @throws PolygonException if something went wrong while working with API
     */
    public List<Package> getProblemPackages(final int problemId) throws PolygonException {
        JSONArray response = getJSONArrayResponse("problem.packages", List.of(
                new BasicNameValuePair("problemId", String.valueOf(problemId))));

        List<Package> packages = new ArrayList<>();
        for (int i = 0; i < response.length(); i++) {
            packages.add(JSONUtils.packageFromJSON(response.getJSONObject(i)));
        }

        return packages;
    }

    private JSONArray getJSONArrayResponse(final String method, final List<NameValuePair> parameters)
            throws PolygonException {
        HttpEntity response = getAPIResponse(method, parameters);
        try {
            JSONObject jsonResponse = new JSONObject(EntityUtils.toString(response));
            checkJSONResponse(jsonResponse);
            return jsonResponse.getJSONArray("result");
        } catch (IOException e) {
            throw new ResponseException(method, e);
        }
    }

    private JSONObject getJSONObjectResponse(final String method, final List<NameValuePair> parameters)
            throws PolygonException {
        HttpEntity response = getAPIResponse(method, parameters);
        try {
            JSONObject jsonResponse = new JSONObject(EntityUtils.toString(response));
            checkJSONResponse(jsonResponse);
            return jsonResponse.getJSONObject("result");
        } catch (IOException e) {
            throw new ResponseException(method, e);
        }
    }

    private void checkJSONResponse(final JSONObject jsonResponse) throws PolygonException {
        if (jsonResponse == null) {
            throw new ResponseException("Given JSON response is null", "null");
        }
        if (!jsonResponse.keySet().contains("status")) {
            throw new ResponseException("Given JSON doesn't have status field", jsonResponse.toString());
        }
        if (!jsonResponse.get("status").equals("OK")) {
            throw new ResponseException("Given JSON status is " + jsonResponse.getString("status"), jsonResponse.toString());
        }
        if (!jsonResponse.keySet().contains("result")) {
            throw new ResponseException("Given JSON doesn't have result field", jsonResponse.toString());
        }
    }

    private BufferedInputStream getStreamResponse(final String method, final List<NameValuePair> parameters)
            throws PolygonException {
        HttpEntity response = getAPIResponse(method, parameters);
        try {
            return new BufferedInputStream(response.getContent());
        } catch (IOException e) {
            throw new ResponseException(method, e);
        }
    }

    private HttpEntity getAPIResponse(final String method, final List<NameValuePair> parameters)
            throws PolygonException {
        List<NameValuePair> allParameters = new ArrayList<>();

        allParameters.add(new BasicNameValuePair("apiKey", key));
        allParameters.add(new BasicNameValuePair("time", String.valueOf(System.currentTimeMillis() / 1000)));
        allParameters.addAll(parameters);
        allParameters.add(new BasicNameValuePair("apiSig", generateApiSig(method, allParameters)));

        return sendPost("https://polygon.codeforces.com/api/" + method, allParameters);
    }

    private HttpEntity sendPost(final String url, final List<NameValuePair> parameters) throws PolygonException {
        HttpPost post = new HttpPost(url);

        try {
            post.setEntity(new UrlEncodedFormEntity(parameters));
        } catch (UnsupportedEncodingException e) {
            throw new IncorrectParametersException(parameters);
        }

        try {
            CloseableHttpResponse response = client.execute(post);
            return response.getEntity();
        } catch (IOException e) {
            throw new ConnectionError(url, e);
        }
    }

    private String generateApiSig(final String method, final List<NameValuePair> parameters) {
        StringBuilder rand = new StringBuilder();
        StringBuilder apiSig = new StringBuilder();

        for (int i = 0; i < 6; i++) {
            char c = ALPHABET.charAt(random.nextInt(26));
            rand.append(c);
            apiSig.append(c);
        }

        apiSig.append('/').append(method);

        parameters.sort((p1, p2) -> {
            if (p1.getName().equals(p2.getName())) {
                return p1.getValue().compareTo(p2.getValue());
            } else {
                return p1.getName().compareTo(p2.getName());
            }
        });

        for (int i = 0; i < parameters.size(); i++) {
            if (i == 0) {
                apiSig.append('?');
            } else {
                apiSig.append('&');
            }
            apiSig.append(parameters.get(i).getName()).append('=').append(parameters.get(i).getValue());
        }

        apiSig.append('#').append(secret);

        return rand.toString() + DigestUtils.sha512Hex(apiSig.toString());
    }
}
