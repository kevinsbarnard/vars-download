package javasrc.main;

import javasrc.sql.VARSQuery;

import java.io.File;
import java.io.InputStream;
import java.sql.ResultSet;

/**
 * Main application
 *
 * @author Kevin Barnard
 * @since 6/14/2018
 */
class App {

    App(String[] params) {
        init(params);
        run(params);
    }

    private static void init(String[] params) {

        // TODO Initialization

    }

    private static void run(String[] params) {

        boolean geology = Boolean.parseBoolean(params[2]);

        // Set up write directory
        File outputDirectory = new File(params[1]);
        if (!outputDirectory.exists()) outputDirectory.mkdirs();

        if (!geology) {
            // Read in genus list
            String[] genera = Functions.readLines(new File(params[0]));
            assert genera != null;
            // Load genus query
            InputStream statementStream = ClassLoader.getSystemResourceAsStream("Query.sql");
            // Run queries to fetch ResultSets
            ResultSet[] resultSets = Functions.runQueries(
                    genera,
                    VARSQuery.readFromStream(statementStream)
            );
            // Fetch genera images
            Functions.fetchImages(resultSets, genera, outputDirectory);
        } else {
            // Load geology query
            InputStream geologyStatementStream = ClassLoader.getSystemResourceAsStream("ExplicitQuery.sql");
            // Run queries to fetch geology ResultSets
            String[] geologyList = {"pillow lava", "cobble", "gravel"};
            ResultSet[] geologyResultSets = Functions.runSpecificQueries(
                    geologyList,
                    VARSQuery.readFromStream(geologyStatementStream)
            );
            // Fetch geology images
            Functions.fetchImages(geologyResultSets, geologyList, outputDirectory);
        }

    }

}
