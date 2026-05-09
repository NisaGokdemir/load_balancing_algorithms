package org.kafka.load_balancing_algorithms;

import java.util.List;

/**
 * IP Hashing Algoritması
 * İstemcinin IP adresini kullanarak onu her zaman aynı sunucuya yönlendirir.
 * Uses the client's IP address to map them consistently to the same server.
 */
public class IPHash {
    private List<String> servers;

    public IPHash(List<String> servers) {
        this.servers = servers;
    }

    /**
     * IP adresine göre sunucu indeksini hesaplar.
     * Calculates the server index based on the IP address hash.
     */
    public String getNextServer(String clientIp) {
        // IP adresinden benzersiz bir sayı (hash) üretir
        // Generates a unique hash number from the IP address
        int hash = clientIp.hashCode();

        // Hash kodunun mutlak değerini sunucu sayısına böler (Mod işlemi)
        // Takes the absolute value of the hash and applies modulo by server size
        int serverIndex = Math.abs(hash) % servers.size();

        return servers.get(serverIndex);
    }

    public static void main(String[] args) {
        List<String> servers = List.of("Server1", "Server2", "Server3");
        IPHash ipHash = new IPHash(servers);

        // Farklı IP adreslerini test et | Testing different IP addresses
        List<String> clientIps = List.of("192.168.0.1", "192.168.0.2", "192.168.0.3");

        System.out.println("--- IP Tabanlı Eşleştirme | IP-Based Mapping ---");
        for (String ip : clientIps) {
            String assignedServer = ipHash.getNextServer(ip);
            System.out.println("Client IP: " + ip + " -> Assigned to: " + assignedServer);
        }

        // Aynı IP tekrar geldiğinde aynı sunucuya gittiğini doğrula
        // Verify that the same IP always goes to the same server
        System.out.println("\n--- Tekrar Testi | Persistence Test ---");
        System.out.println("192.168.0.1 again -> " + ipHash.getNextServer("192.168.0.1"));
    }
}