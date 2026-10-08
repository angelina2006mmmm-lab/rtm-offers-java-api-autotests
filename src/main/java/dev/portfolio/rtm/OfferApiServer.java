package dev.portfolio.rtm;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Set;

/**
 * Demo HTTP service, NOT an MTS internal API. Starts on a random free localhost port.
 * Designed for repeatable integration-style HTTP tests without external credentials.
 */
public final class OfferApiServer implements AutoCloseable {
    private static final String ROOT = "/api/v1/offers";
    private static final Set<String> CHANNELS = Set.of("SMS", "PUSH", "EMAIL");
    private static final Set<String> STATUSES = Set.of("ACTIVE", "PAUSED");

    private final ObjectMapper mapper = new ObjectMapper();
    private final OfferStore store = new OfferStore();
    private final HttpServer server;

    public OfferApiServer() throws IOException {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext(ROOT, this::handle);
    }

    public void start() {
        server.start();
    }

    public int port() {
        return server.getAddress().getPort();
    }

    private void handle(HttpExchange exchange) throws IOException {
        try {
            String path = exchange.getRequestURI().getPath();
            String method = exchange.getRequestMethod();

            if (ROOT.equals(path)) {
                handleCollection(exchange, method);
            } else if (path.startsWith(ROOT + "/")) {
                String suffix = path.substring((ROOT + "/").length());
                if (!suffix.matches("[0-9]+")) {
                    error(exchange, 400, "Invalid offer ID");
                    return;
                }
                long id;
                try {
                    id = Long.parseLong(suffix);
                } catch (NumberFormatException ex) {
                    error(exchange, 400, "Invalid offer ID");
                    return;
                }
                handleItem(exchange, method, id);
            } else {
                error(exchange, 404, "Endpoint not found");
            }
        } catch (JsonProcessingException ex) {
            error(exchange, 400, "Malformed JSON");
        } catch (IllegalArgumentException ex) {
            error(exchange, 400, ex.getMessage());
        } finally {
            exchange.close();
        }
    }

    private void handleCollection(HttpExchange exchange, String method) throws IOException {
        switch (method) {
            case "POST" -> {
                if (!isJsonRequest(exchange)) {
                    error(exchange, 415, "Content-Type must be application/json");
                    return;
                }
                JsonNode json = parseObject(exchange);
                String clientId = requiredString(json, "clientId");
                String campaignId = requiredString(json, "campaignId");
                String channel = requiredString(json, "channel");
                if (!CHANNELS.contains(channel)) {
                    throw new IllegalArgumentException("Unsupported channel");
                }
                sendJson(exchange, 201, store.create(clientId, campaignId, channel));
            }
            case "GET" -> sendJson(exchange, 200, store.list(queryParameter(exchange, "clientId")));
            default -> error(exchange, 405, "Method not allowed");
        }
    }

    private void handleItem(HttpExchange exchange, String method, long id) throws IOException {
        switch (method) {
            case "GET" -> {
                var result = store.get(id);
                if (result.isPresent()) sendJson(exchange, 200, result.get());
                else error(exchange, 404, "Offer not found");
            }
            case "PUT" -> {
                if (!isJsonRequest(exchange)) {
                    error(exchange, 415, "Content-Type must be application/json");
                    return;
                }
                String status = requiredString(parseObject(exchange), "status");
                if (!STATUSES.contains(status)) {
                    throw new IllegalArgumentException("Unsupported status");
                }
                var result = store.updateStatus(id, status);
                if (result.isPresent()) sendJson(exchange, 200, result.get());
                else error(exchange, 404, "Offer not found");
            }
            case "DELETE" -> {
                if (store.delete(id)) exchange.sendResponseHeaders(204, -1);
                else error(exchange, 404, "Offer not found");
            }
            default -> error(exchange, 405, "Method not allowed");
        }
    }

    private JsonNode parseObject(HttpExchange exchange) throws IOException {
        JsonNode node = mapper.readTree(exchange.getRequestBody());
        if (node == null || !node.isObject()) {
            throw new IllegalArgumentException("JSON object expected");
        }
        return node;
    }

    private String requiredString(JsonNode json, String key) {
        JsonNode field = json.get(key);
        if (field == null || !field.isTextual() || field.asText().isBlank()) {
            throw new IllegalArgumentException("Missing or invalid field: " + key);
        }
        return field.asText().trim();
    }

    private boolean isJsonRequest(HttpExchange exchange) {
        String type = exchange.getRequestHeaders().getFirst("Content-Type");
        return type != null && type.toLowerCase(java.util.Locale.ROOT).split(";", 2)[0].trim()
                .equals("application/json");
    }

    private String queryParameter(HttpExchange exchange, String name) {
        String rawQuery = exchange.getRequestURI().getRawQuery();
        if (rawQuery == null) return null;
        for (String pair : rawQuery.split("&")) {
            String[] parts = pair.split("=", 2);
            if (URLDecoder.decode(parts[0], StandardCharsets.UTF_8).equals(name)) {
                return parts.length == 2 ? URLDecoder.decode(parts[1], StandardCharsets.UTF_8) : "";
            }
        }
        return null;
    }

    private void error(HttpExchange exchange, int status, String message) throws IOException {
        sendJson(exchange, status, Map.of("error", message));
    }

    private void sendJson(HttpExchange exchange, int status, Object body) throws IOException {
        byte[] bytes = mapper.writeValueAsBytes(body);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        exchange.sendResponseHeaders(status, bytes.length);
        try (var output = exchange.getResponseBody()) {
            output.write(bytes);
        }
    }

    @Override
    public void close() {
        server.stop(0);
    }
}
