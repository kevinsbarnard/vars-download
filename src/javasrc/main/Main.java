package javasrc.main;

/**
 * Main class
 *
 * @author Kevin Barnard
 * @since 6/14/2018
 */
public class Main {

    private static String[] DEFAULT_PARAMS = {
            System.getProperty("user.home") + "/genera.txt",
            System.getProperty("user.home") + "/Desktop/Images/",
            "false"
    };

    public static void main(String[] args) {

        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
        } catch (ClassNotFoundException e) {
            System.err.println("FATAL: SQL Server Driver not found.");
            return;
        }

        new App(parseArgs(args));

    }

    private static String[] parseArgs(String[] args) {

        String[] params = DEFAULT_PARAMS;

        try {

            for (int i = 0; i < args.length; i++) {
                if (args[i].equals("-g")) params[2] = "true";
            }

            if (!Boolean.parseBoolean(params[2])) params[0] = args[ args.length - 2 ]; // Genus list file
            params[1] = args[ args.length - 1 ]; // Output directory

        } catch (Exception e) {
            System.out.println("Usage error: Follow\n\tjava -jar vars-download.jar [OPTIONS] path/to/genera.txt path/to/output/directory/");
        }

        return params;

    }

}
