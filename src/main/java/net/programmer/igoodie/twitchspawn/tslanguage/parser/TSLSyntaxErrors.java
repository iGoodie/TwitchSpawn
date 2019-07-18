package net.programmer.igoodie.twitchspawn.tslanguage.parser;

import java.util.List;

/**
 * Cumulative syntax errors exception
 * used to show more than one syntax error
 * to the user.
 */
public class TSLSyntaxErrors extends Exception {

    List<TSLSyntaxError> errors;

    public TSLSyntaxErrors(List<TSLSyntaxError> errors) {
        this.errors = errors;
    }

    public List<TSLSyntaxError> getErrors() {
        return errors;
    }

}
