package org.kafka.load_balancing_algorithms;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Least Response Time (En Az Yanıt Süresi) Algoritması
 * İstekleri en düşük gecikme süresine sahip sunucuya yönlendirir.
 * Routes requests to the server with the lowest latency/response time.
 */
public class LeastResponseTime {
    private List<String> servers;
    private List<Double> responseTimes; // Yanıt sürelerini tutan liste | List to store response times

    public LeastResponseTime(List<String> servers) {
        this.servers = servers;
        this.responseTimes = new ArrayList<>(servers.size());
        for (int i = 0; i < servers.size(); i++)
            responseTimes.add(0.0); // Başlangıçta tüm süreler 0 | Initialize all times to 0
    }

    /**
     * En hızlı yanıt veren sunucuyu bulur.
     * Finds the server with the fastest response time.
     */
    public String getNextServer() {
        double minResponseTime = responseTimes.get(0);
        int minIndex = 0;
        for (int i = 1; i < responseTimes.size(); i++) {
            if (responseTimes.get(i) < minResponseTime) {
                minResponseTime = responseTimes.get(i);
                minIndex = i;
            }
        }
        return servers.get(minIndex);
    }

    /**
     * Sunucunun son yanıt süresini günceller.
     * Updates the last response time of a specific server.
     */
    public void updateResponseTime(String server, double responseTime) {
        int index = servers.indexOf(server);
        if (index != -1) {
            responseTimes.set(index, responseTime);
        }
    }

    /**
     * Simülasyon: Rastgele bir gecikme süresi üretir.
     * Simulation: Generates a random delay to mimic real-world response.
     */
    public static double simulateResponseTime(String server) {
        Random random = new Random();
        double delay = 0.1 + (1.0 - 0.1) * random.nextDouble();
        try {
            // Gerçek dünyayı taklit etmek için kısa bir bekleme
            // Short sleep to mimic real-world latency
            Thread.sleep((long) (delay * 1000));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return delay;
    }

    public static void main(String[] args) {
        List<String> servers = List.of("Server1", "Server2", "Server3");
        LeastResponseTime leastResponseTimeLB = new LeastResponseTime(servers);

        System.out.println("--- Hız Odaklı Dağıtım Başlıyor | Speed-Based Distribution Starting ---");
        for (int i = 0; i < 6; i++) {
            String server = leastResponseTimeLB.getNextServer();
            System.out.println("Request " + (i + 1) + " -> Directed to: " + server);

            double responseTime = simulateResponseTime(server);
            leastResponseTimeLB.updateResponseTime(server, responseTime);

            System.out.println("Actual Response Time: " + String.format("%.2f", responseTime) + "s");
            System.out.println("-------------------------------------------------");
        }
    }
}