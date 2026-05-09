package org.kafka.load_balancing_algorithms;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Round Robin Yük Dengeleme Algoritması
 * İstekleri sunucu listesi üzerinden sırayla dağıtır.
 */
public class RoundRobin {
    private List<String> servers; // Mevcut sunucuların listesi | List of available servers
    private AtomicInteger index;  // Sıradaki sunucuyu belirlemek için atomik sayaç | Atomic counter to track next server

    public RoundRobin(List<String> servers) {
        this.servers = servers;
        this.index = new AtomicInteger(-1);
    }

    /**
     * Bir sonraki sunucuyu seçer.
     * Selects the next server in a circular fashion.
     */
    public String getNextServer() {
        // Sayacı artır ve sunucu sayısına göre mod al (Dairesel döngü sağlar)
        // Increment index and take modulo by server list size for circular loop
        int currentIndex = Math.abs(index.incrementAndGet() % servers.size());
        return servers.get(currentIndex);
    }

    public static void main(String[] args) {
        List<String> servers = List.of("Server1", "Server2", "Server3");
        RoundRobin roundRobinLB = new RoundRobin(servers);

        // Örnek: 6 istek gönderildiğinde sunucuların nasıl seçildiğini gör
        // Example: See how servers are selected for 6 requests
        for (int i = 0; i < 6; i++) {
            System.out.println("İstek " + (i + 1) + " yönlendirilen sunucu: " + roundRobinLB.getNextServer());
        }
    }
}