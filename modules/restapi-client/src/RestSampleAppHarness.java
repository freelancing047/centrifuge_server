import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.MissingArgumentException;
import org.apache.commons.cli.MissingOptionException;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.apache.commons.cli.UnrecognizedOptionException;

import sample.RestSampleApp;

/**
 * The RestSampleAppHarness class is the main Java application to run the sample RestAPI application.
 * There are four command line arguments that can be specified that are for identifying the host and
 * port of the Centrifuge Server and the userid and password logon credentials.
 */
public class RestSampleAppHarness
{
    
    /**
     * The main method reads the command line arguments, parsing them into Apache Options collection.
     * The arguments (or the associated default values) will be passed to the RestSampleApp.runSamples()
     * method.
     * <br><br>
     * The command line arguments are:<br><br>
     * <ul><li>hostname (default=localhost)</li>
     * <li>port (default=9090)</li>
     * <li>username (default=admin)</li>
     * <li>password (default=changeme)</li></ul>
     *
     * @param args the arguments
     * @throws Exception the exception
     */
    public static void main(String[] args)
        throws Exception {
        Options options = createOptions();

        CommandLine cmd = parseCommandLine(args, options);
        if (cmd == null) {
            System.exit(1);
        }

        String hostname = cmd.getOptionValue("host");
        if (hostname == null || "".equals(hostname)) {
            hostname = "localhost";
        }

        int port = 9090;
        try {
            port = Integer.parseInt(cmd.getOptionValue("port"));
        } catch (NumberFormatException e) {
        }

        String username = cmd.getOptionValue("user");
        if (username == null || "".equals(username)) {
            username = "admin";
        }

        String password = cmd.getOptionValue("password");
        if (password == null) {
            password = "changeme";
        }

        RestSampleApp app = new RestSampleApp();
        app.runSamples(hostname, port, username, password);
    }

    /**
     * Parses the command line arguments.
     *
     * @param argv the argv
     * @param options the options
     * @return the command line
     * @throws ParseException the parse exception
     */
    private static CommandLine parseCommandLine(String[] argv, Options options)
        throws ParseException {
        CommandLine cmd = null;
        try {
            CommandLineParser parser = new PosixParser();
            cmd = parser.parse(options, argv);
        } catch (MissingOptionException e1) {
            System.err.println("Missing required option(s) " + e1.getMissingOptions() + "\n");
            printHelp(options);
            System.exit(1);
        } catch (UnrecognizedOptionException e2) {
            System.err.println("Unrecognized option " + e2.getOption() + "\n");
            printHelp(options);
            System.exit(1);
        } catch (MissingArgumentException e3) {
            System.err.println(e3.getMessage() + "\n");
            printHelp(options);
            System.exit(1);
        }

        if (cmd.hasOption("help")) {
            printHelp(options);
            System.exit(1);
        }
        return cmd;
    }

    /**
     * Prints the help.
     *
     * @param options the options
     */
    private static void printHelp(Options options) {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("RestSampleApp [options]", options, false);
    }

    /**
     * Creates the Apache Options collection for the sample command line arguments.
     *
     * @return the options
     */
    private static Options createOptions() {
        Options options = new Options();

        Option host = OptionBuilder.hasArg(true).withArgName("hostname or IP")
                .withDescription("Centrifuge server hostname.  Defaults to \"localhost\" if not specified.")
                .create("host");
        options.addOption(host);

        Option port = OptionBuilder.hasArg(true).withArgName("port")
                .withDescription("Centrifuge server port.  Defaults to \"9090\" if not specified.").create("port");
        options.addOption(port);

        Option username = OptionBuilder.hasArg(true).withArgName("username")
                .withDescription("Username to use for authentication.  Defaults to \"admin\" if not specified.")
                .create("user");
        options.addOption(username);

        Option password = OptionBuilder
                .hasArg(true)
                .withArgName("password")
                .withDescription(
                        "Password to use for authentication.  Uses the default \"admin\" password if not specified.")
                .create("password");
        options.addOption(password);

        options.addOption(new Option("help", "Prints this help message."));

        return options;
    }
}