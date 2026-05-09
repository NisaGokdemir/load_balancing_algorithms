package org.kafka.load_balancing_algorithms;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Least Connections (En Az Bağlantı) Algoritması
 * İstekleri o an en az aktif bağlantıya sahip olan sunucuya yönlendirir.
 * Directs requests to the server with the lowest number of active connections.
 */
public class LeastConnections {
    private Map<String, Integer> serverConnections; // Sunucu ve aktif bağlantı sayısı | Servers and active connection counts

    public LeastConnections(List<String> servers) {
        serverConnections = new HashMap<>();
        for (String server : servers) {
            serverConnections.put(server, 0); // Başlangıçta tüm bağlantılar 0 | Initialize all connections to 0
        }
    }

    /**
     * En az bağlantısı olan sunucuyu seçer ve bağlantı sayısını artırır.
     * Selects the server with least connections and increments its count.
     */
    public String getNextServer() {
        String bestServer = serverConnections.entrySet().stream()
                .min(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);

        if (bestServer != null) {
            // Yeni istek geldiği için bağlantı sayısını artırıyoruz
            // Incrementing connection count for the new request
            serverConnections.put(bestServer, serverConnections.get(bestServer) + 1);
        }
        return bestServer;
    }

    /**
     * İşlem bitince sunucunun bağlantı sayısını azaltır.
     * Decrements the connection count once the task is finished.
     */
    public void releaseConnection(String server) {
        serverConnections.computeIfPresent(server, (k, v) -> v > 0 ? v - 1 : 0);
    }

    public static void main(String[] args) {
        List<String> servers = List.of("Server1", "Server2", "Server3");
        LeastConnections leastConnectionsLB = new LeastConnections(servers);

        System.out.println("--- İstekler Dağıtılıyor | Distributing Requests ---");
        for (int i = 0; i < 6; i++) {
            String server = leastConnectionsLB.getNextServer();
            System.out.println("Request redirected to: " + server +
                    " (Current Load: " + leastConnectionsLB.getConnectionCount(server) + ")");

            // Gerçek senaryoda bu işlem asenkron biter
            // In a real scenario, this would be released asynchronously
            if (i % 2 == 0) {
                leastConnectionsLB.releaseConnection(server);
                System.out.println("Connection released for: " + server);
            }
        }
    }

    // Test amaçlı bağlantı sayısını görme | Helper for testing
    private int getConnectionCount(String server) {
        return serverConnections.getOrDefault(server, 0);
    }
}