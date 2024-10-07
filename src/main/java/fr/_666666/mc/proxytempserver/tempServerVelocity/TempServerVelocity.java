package fr._666666.mc.proxytempserver.tempServerVelocity;

import com.google.inject.Inject;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.ProxyServer;
import org.slf4j.Logger;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

    @Plugin(id = "temp_server_velocity", name = "Temp Server Velocity", version = "1.0-SNAPSHOT")
    public class TempServerVelocity {

    private final ProxyServer server;
    private final Logger logger;
    private final Map<String, String> serverMap = new HashMap<>();

    @Inject
    public TempServerVelocity(ProxyServer server, Logger logger) {
        this.server = server;
        this.logger = logger;
        server.getCommandManager().register("ts", new ServerCommand(), "ts");
        loadServersFromFile();
    }

    private class ServerCommand implements SimpleCommand {

        @Override
        public void execute(Invocation invocation) {
            CommandSource source = invocation.source();
            String[] args = invocation.arguments();

            if (args.length < 2) {
                source.sendPlainMessage("Usage: /ts add <server name> <server ip> or /ts tp <server name>");
                return;
            }

            String action = args[0];
            String serverName = args[1];

            if (action.equalsIgnoreCase("add") && args.length == 3) {
                String serverIp = args[2];
                serverMap.put(serverName, serverIp);
                writeToFile(serverName, serverIp);
                source.sendPlainMessage("Server " + serverName + " added with IP " + serverIp);
            } else if (action.equalsIgnoreCase("tp")) {
                Optional<Player> player = source instanceof Player ? Optional.of((Player) source) : Optional.empty();
                player.ifPresent(p -> {
                    String serverIp = serverMap.get(serverName);
                    if (serverIp != null) {
                        server.getServer(serverIp).ifPresent(s -> p.createConnectionRequest(s).fireAndForget());
                        source.sendPlainMessage("Teleporting to server " + serverName);
                    } else {
                        source.sendPlainMessage("Server " + serverName + " not found.");
                    }
                });
            } else {
                source.sendPlainMessage("Invalid command. Usage: /ts add <server name> <server ip> or /ts tp <server name>");
            }
        }

        private void writeToFile(String serverName, String serverIp) {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter("temp.server", true))) {
                writer.write(serverName + " " + serverIp);
                writer.newLine();
            } catch (IOException e) {
                logger.error("Error writing to file", e);
            }
        }
    }

    private void loadServersFromFile() {
        try (BufferedReader reader = new BufferedReader(new FileReader("temp.server"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(" ");
                if (parts.length == 2) {
                    serverMap.put(parts[0], parts[1]);
                }
            }
        } catch (IOException e) {
            logger.error("Error reading from file", e);
        }
    }
}