package javasrc.main;

import javasrc.sql.VARSQuery;
import scalasrc.PNGFetcher;

import javax.imageio.ImageIO;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 * Class holding helper functions for main application
 *
 * @author Kevin Barnard
 * @since 6/14/2018
 */
class Functions {

    /**
     * Reads lines as Strings from provided file.
     *
     * @param file File to read
     * @return String[] of lines from file
     */
    static String[] readLines(File file) {
        // Try read file
        BufferedReader fileReader;
        try {
            fileReader = new BufferedReader(new FileReader(file));
        } catch (IOException e) {
            System.err.println("Error: Could not read file at '" + file.getAbsolutePath() + "'");
            return null;
        }

        // Translate Objects to Strings
        Object[] lineObjects = fileReader.lines().toArray();
        String[] lines = new String[lineObjects.length];
        for (int i = 0; i < lines.length; i++) lines[i] = String.valueOf(lineObjects[i]);

        return lines;
    }

    /**
     * Run given statement on list of genera
     *
     * @param genera String array of genera to be queried
     * @param statement SQL statement to be executed
     * @return Array of ResultSets from queries parallel to genera list
     */
    static ResultSet[] runQueries(String[] genera, String statement) {
        ResultSet[] resultSets = new ResultSet[genera.length];

        // Run queries on each genus
        VARSQuery genusQuery = new VARSQuery();
        for (int i = 0; i < genera.length; i++) {
            System.out.println("Querying genus '" + genera[i] + "'");
            genusQuery.setStatement(insertConcept(statement, genera[i]));
            resultSets[i] = genusQuery.executeStatement();
        }

        return resultSets;
    }

    /**
     * Read data in from ResultSet
     * Structure: ConceptName Image RovName DiveNumber
     *
     * @param resultSet ResultSet to read
     * @return String[][] of read data
     * @throws SQLException Exception when result set reading fails
     */
    private static String[][] readResultSet(ResultSet resultSet) throws SQLException {
        // Get result set size and initialize data array
        ArrayList<String[]> data = new ArrayList<>();

        // Read data
        while (resultSet.next()) {
            String[] row = new String[4];
            row[0] = resultSet.getString("ConceptName");
            row[1] = resultSet.getString("Image");
            row[2] = resultSet.getString("RovName");
            row[3] = resultSet.getString("DiveNumber");
            data.add(row);
        }

        String[][] rowData = new String[data.size()][4];
        for (int i = 0; i < data.size(); i++) {
            rowData[i] = data.get(i);
        }
        return rowData;
    }

    /**
     * Fetch images given array of ResultSets and genera, and write to the output directory
     *
     * @param resultSets Array of ResultSets to read
     * @param genera Parallel array of genera corresponding to resultSets
     * @param outputDirectory Base output directory
     */
    static void fetchImages(ResultSet[] resultSets, String[] genera, File outputDirectory) {

        int failed = 0; // Count for images without valid .png counterpart

        for (int i = 0; i < resultSets.length; i++) {

            ResultSet genusSet = resultSets[i];
            String genus = genera[i];

            try {

                String[][] data = readResultSet(genusSet);
                final int numImages = data.length;
                System.out.println("Fetching " + numImages + " images for concept '" + genus + "' ");

                for (int j = 0; j < numImages; j++) {

                    String[] entry = data[j];

                    // Generate output file for row
                    File writeFile = generateFile(outputDirectory, genus, entry);

                    if (writeFile == null) { // Bad URL, increment failed
                        System.err.println("ERROR: Error fetching/writing image from " + entry[1]);
                        failed++;
                        continue;
                    }

                    if (writeFile.exists()) continue; // If already cached, skip

                    // Download and write
                    try {

                        URL pngURL = new URL(PNGFetcher.extractPngURL(entry[1]));
                        ImageIO.write(ImageIO.read(pngURL), "png", writeFile);

                        System.out.print("Copied " + pngURL.toString() + " to " + writeFile.getAbsolutePath());
                        System.out.println(" (" + j + "/" + numImages + ") " + Math.round((double)(j) / numImages * 1000) / 10 + "%");

                    } catch (Exception e) { // Failure in download/write, increment failed
                        System.err.println("ERROR: Error fetching/writing image from " + entry[1]);
                        failed++;
                    }
                }

            } catch (SQLException e) {
                System.err.println("CRITICAL: Error reading genus result set for genus '" + genus + "'");
            }

        }
        System.out.println(failed + " link" + ((failed == 1)?"":"s") + " failed.");
    }

    /**
     * Generate file following format
     *
     * @param outputDirectory Base output directory
     * @param genus Base genus
     * @param rowData Data for entry
     * @return File at formatted location
     */
    private static File generateFile(File outputDirectory, String genus, String[] rowData) {

        File folder = new File(String.join("/", outputDirectory.getAbsolutePath(), genus, rowData[0]));

        if (!folder.exists()) folder.mkdirs();

        try {
            return PNGFetcher.urlToFile(folder, PNGFetcher.extractPngURL(rowData[1]), rowData[2], rowData[3]);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Insert a substring into another string at a given index
     *
     * @param str String to be modified
     * @param insert Substring to be inserted
     * @param index Index of str to start substring
     * @return Modified String
     */
    private static String insertAt(String str, String insert, int index) {
        return (new StringBuilder(str).insert(index, insert)).toString();
    }

    /**
     * Modify generalized SQL query to fit search for concept
     *
     * @param str Query to be modified
     * @param concept Concept to be inserted
     * @return Modified query
     */
    private static String insertConcept(String str, String concept) {
        String first = insertAt(
                str,
                concept,
                str.indexOf('\'') + 1
        );
        return insertAt(
                first,
                concept,
                first.indexOf('%') - 1
        );
    }

    /**
     * Run specific statement on list of concepts
     *
     * @param concepts String array of concepts to be queried
     * @param statement SQL statement to be executed
     * @return Array of ResultSets from queries parallel to concept list
     */
    static ResultSet[] runSpecificQueries(String[] concepts, String statement) {
        ResultSet[] resultSets = new ResultSet[concepts.length];

        VARSQuery geologyQuery = new VARSQuery();
        for (int i = 0; i < concepts.length; i++) {
            System.out.println("Querying specific concept '" + concepts[i] + "'");
            geologyQuery.setStatement(insertAt(statement, concepts[i], statement.indexOf('\'') + 1));
            resultSets[i] = geologyQuery.executeStatement();
        }

        return resultSets;
    }

}
