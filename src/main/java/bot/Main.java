package bot;

import com.sun.net.httpserver.HttpServer;

import bot.cache.RedisCache;
import redis.clients.jedis.JedisPool;

import org.apache.commons.cli.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;

public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        Options options = new Options();

        options.addOption(new Option("h", "help", false, "Displays this help message"));
        options.addOption(new Option("t", "token", true, "Provide the token during startup."));

        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();

        RedisCache.initializePool();

        try {
            CommandLine cmd = parser.parse(options, args);

            if (cmd.hasOption("help")) {
                formatter.printHelp("Help Menu", options);
                System.exit(0);
            }

            String token = cmd.getOptionValue("token");
            if (token == null) {
                logger.error("No token provided, please provide a token using the -t or --token flag.");
                formatter.printHelp("", options);
                System.exit(0);
            }

            // Start HTTP server for health checks
            startHealthCheckServer();

            CodeforcesBot.selfBot = new CodeforcesBot(token);
        } catch (ParseException e) {
            logger.error("Failed to parse command line arguments: {}", e.getMessage());
            formatter.printHelp("CodeforcesBot", options);
            System.exit(1);
        } catch (IOException e) {
            logger.error("Failed to start health check server: {}", e.getMessage());
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
        logger.info("Health check server started on port 8000");
    }
}
