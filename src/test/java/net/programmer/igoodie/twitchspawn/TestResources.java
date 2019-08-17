package net.programmer.igoodie.twitchspawn;

import com.google.common.io.Resources;
import jdk.nashorn.internal.runtime.regexp.joni.exception.InternalException;

import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class TestResources {

    public static String loadAsString(String filename) {
        try {
            URL resourceURL = Resources.getResource(filename);
            List<String> lines = Resources.readLines(resourceURL, StandardCharsets.UTF_8);
            return String.join("\n", lines);

        } catch (Exception e) {
            e.printStackTrace();
            throw new InternalException(e.getMessage());
        }
    }

}
