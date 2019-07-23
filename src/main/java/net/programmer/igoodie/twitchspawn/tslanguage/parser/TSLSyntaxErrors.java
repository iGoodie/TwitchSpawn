package net.programmer.igoodie.twitchspawn.tslanguage.parser;

import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Cumulative syntax errors exception
 * used to show more than one syntax error
 * to the user.
 */
public class TSLSyntaxErrors extends Exception {

    List<TSLSyntaxError> errors;

    public TSLSyntaxErrors(List<TSLSyntaxError> errors) {
        super(errors.stream().map(err -> err.getMessage()).collect(Collectors.joining("\n")));
        this.errors = errors;
    }

    public TSLSyntaxErrors(TSLSyntaxError...errors) {
        this(Arrays.asList(errors));
    }

    public List<TSLSyntaxError> getErrors() {
        return errors;
    }

}
