package ru.strategy48.ejudge.polygon2ejudge.polygon.exceptions;

import org.apache.http.NameValuePair;

import java.util.List;
import java.util.stream.Collectors;

public class IncorrectParametersException extends PolygonException {
    private final List<NameValuePair> parameters;

    public IncorrectParametersException(final List<NameValuePair> parameters) {
        super("incorrect parameters: " + parameters.stream().map((p) -> "(" + p.getName() + ", " + p.getValue() + ")").
                collect(Collectors.joining(", ", "{", "}")));
        this.parameters = parameters;
    }

    public IncorrectParametersException(final List<NameValuePair> parameters, final Throwable cause) {
        super("incorrect parameters: " + parameters.stream().map((p) -> "(" + p.getName() + ", " + p.getValue() + ")").
                collect(Collectors.joining(", ", "{", "}")), cause);
        this.parameters = parameters;
    }

    public List<NameValuePair> getParameters() {
        return parameters;
    }
}
