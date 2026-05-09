package org.kafka.load_balancing_algorithms;

import java.util.List;

/**
 * Weighted Round Robin (Ağırlıklı Sıralı Dağıtım)
 * Dağıtımı sunucuların işleme kapasitelerine (ağırlıklarına) göre yapar.
 * Distributes load based on the processing capacities (weights) of the servers.
 */
public class WeightedRoundRobin {
    private List<String> servers;  // Sunucu listesi | Server list
    private List<Integer> weights; // Sunucu ağırlıkları | Server weights
    private int currentIndex;      // Mevcut sunucu indeksi | Current server index
    private int currentWeight;     // Mevcut ağırlık seviyesi | Current weight level

    public WeightedRoundRobin(List<String> servers, List<Integer> weights) {
        this.servers = servers;
        this.weights = weights;
        this.currentIndex = -1;
        this.currentWeight = 0;
    }

    /**
     * Ağırlıklara göre bir sonraki sunucuyu seçer.
     * Selects the next server based on assigned weights.
     */
    public String getNextServer() {
        while (true) {
            currentIndex = (currentIndex + 1) % servers.size();

            // Liste başına dönüldüğünde ağırlık seviyesini bir azaltır
            // Decrement the current weight level when looping back to the start
            if (currentIndex == 0) {
                currentWeight--;
                // Seviye sıfıra indiyse en yüksek ağırlıktan tekrar başlar
                // If weight level hits zero, reset to the maximum weight
                if (currentWeight <= 0) {
                    currentWeight = getMaxWeight();
                }
            }

            // Mevcut sunucunun ağırlığı, kontrol edilen seviyeden büyük veya eşitse seç
            // Select server if its weight is greater than or equal to current weight level
            if (weights.get(currentIndex) >= currentWeight) {
                return servers.get(currentIndex);
            }
        }
    }

    private int getMaxWeight() {
        return weights.stream().max(Integer::compare).orElse(0);
    }

    public static void main(String[] args) {
        List<String> servers = List.of("Server1", "Server2", "Server3");
        // Server1: High Capacity (5), Others: Standard Capacity (1)
        List<Integer> weights = List.of(5, 1, 1);
        WeightedRoundRobin weightedRoundRobinLB = new WeightedRoundRobin(servers, weights);

        System.out.println("--- Dağıtım Başlıyor | Distribution Starting ---");
        for (int i = 0; i < 7; i++) {
            System.out.println("Request " + (i + 1) + " -> " + weightedRoundRobinLB.getNextServer());
        }
    }
}