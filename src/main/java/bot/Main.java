package bot;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;

import com.sun.net.httpserver.HttpServer;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {
    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        Options options = new Options();

        options.addOption(new Option("h", "help", false, "Displays this help message"));
        options.addOption(new Option("t", "token", true, "Provide the token during startup."));

        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();

        try {
            CommandLine cmd = parser.parse(options, args);

            if (cmd.hasOption("help")) {
                formatter.printHelp("Help Menu", options);
                System.exit(0);
            }

            String token = cmd.getOptionValue("token");
            if (token == null) {
                LOGGER.error("No token provided, please provide a token using the -t or --token flag.");
                formatter.printHelp("", options);
                System.exit(0);
            }

            // Start HTTP server for health checks
            startHealthCheckServer();

            CodeforcesBot.selfBot = new CodeforcesBot(token);
        } catch (ParseException e) {
            LOGGER.error("Failed to parse command line arguments: {}", e.getMessage());
            formatter.printHelp("CodeforcesBot", options);
            System.exit(1);
        } catch (IOException e) {
            LOGGER.error("Failed to start health check server: {}", e.getMessage());
            System.exit(1);
        }
    }

    private static void startHealthCheckServer() throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(8000), 0);
        server.createContext("/health", httpExchange -> {
            String response = "OK";
            httpExchange.sendResponseHeaders(200, response.length());
            OutputStream os = httpExchange.getResponseBody();
            os.write(response.getBytes());
            os.close();
        });
        server.setExecutor(null);
        server.start();
        LOGGER.info("Health check server started on port 8000");
    }
}
