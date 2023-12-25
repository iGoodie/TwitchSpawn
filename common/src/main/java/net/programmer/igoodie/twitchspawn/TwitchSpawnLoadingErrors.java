package net.programmer.igoodie.twitchspawn;


import java.util.LinkedList;
import java.util.List;

import net.programmer.igoodie.twitchspawn.tslanguage.parser.TSLSyntaxErrors;

public class TwitchSpawnLoadingErrors extends Exception {

    List<Exception> exceptions;

    public TwitchSpawnLoadingErrors() {
        this.exceptions = new LinkedList<>();
    }

    public void addException(Exception exception) {
        if (exception instanceof TSLSyntaxErrors)
            this.exceptions.addAll(((TSLSyntaxErrors) exception).getErrors());
        else
            this.exceptions.add(exception);
    }

    public boolean isEmpty() {
        return exceptions.isEmpty();
    }


    /**
     * Returns the list of exceptions
     * @return The exception list.
     */
    public List<Exception> getExceptions() {
        return this.exceptions;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        String delimiter = "";
        for (Exception exception : exceptions) {
            sb.append(delimiter);
            sb.append(exception.getMessage());
            delimiter = "\n";
        }
        return sb.toString();
    }

}
