package org.kafka.consistent_hashing;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

/**
 * Consistent Hashing (Tutarlı Karma) Algoritması
 * Sunucu ekleme/çıkarma durumunda minimum veri taşınmasını sağlayan gelişmiş hash yapısı.
 * Advanced hashing that minimizes data movement when servers are added or removed.
 */
public class ConsistentHashing {
    private final int numReplicas; // Her sunucu için sanal düğüm sayısı | Virtual nodes per server
    private final TreeMap<Long, String> ring; // Hash halkası | The hash ring
    private final Set<String> servers; // Fiziksel sunucular | Physical servers

    public ConsistentHashing(List<String> servers, int numReplicas) {
        this.numReplicas = numReplicas;
        this.ring = new TreeMap<>();
        this.servers = new HashSet<>();

        for (String server : servers) {
            addServer(server);
        }
    }

    /**
     * Anahtarı MD5 kullanarak hash'ler ve bir sayıya dönüştürür.
     * Hashes the key using MD5 and converts it to a long value.
     */
    private long hash(String key) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(key.getBytes());
            byte[] digest = md.digest();
            // İlk 4 byte'ı alarak sayıya çeviriyoruz | Convert first 4 bytes to long
            return ((long) (digest[0] & 0xFF) << 24) |
                    ((long) (digest[1] & 0xFF) << 16) |
                    ((long) (digest[2] & 0xFF) << 8) |
                    ((long) (digest[3] & 0xFF));
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("MD5 algorithm not found", e);
        }
    }

    /**
     * Halka üzerine yeni bir sunucu ve sanal kopyalarını ekler.
     * Adds a new server and its virtual replicas to the ring.
     */
    public void addServer(String server) {
        servers.add(server);
        for (int i = 0; i < numReplicas; i++) {
            long hash = hash(server + "-" + i); // Her kopya için eşsiz hash | Unique hash for each replica
            ring.put(hash, server);
        }
    }

    /**
     * Sunucuyu ve ona bağlı tüm sanal düğümleri halkadan kaldırır.
     * Removes the server and all its virtual nodes from the ring.
     */
    public void removeServer(String server) {
        if (servers.remove(server)) {
            for (int i = 0; i < numReplicas; i++) {
                long hash = hash(server + "-" + i);
                ring.remove(hash);
            }
        }
    }

    /**
     * İstek anahtarını halka üzerinde saat yönünde en yakın sunucuya eşler.
     * Maps the request key to the nearest server clockwise on the ring.
     */
    public String getServer(String key) {
        if (ring.isEmpty()) return null;

        long hash = hash(key);
        // Saat yönünde en yakın düğümü bul | Find the closest node clockwise
        Map.Entry<Long, String> entry = ring.ceilingEntry(hash);

        // Eğer halkanın sonundaysak, başa dön (Dairesel yapı)
        // If we exceed the highest node, wrap around to the first node
        if (entry == null) {
            entry = ring.firstEntry();
        }
        return entry.getValue();
    }

    public static void main(String[] args) {
        List<String> servers = Arrays.asList("S0", "S1", "S2");
        ConsistentHashing ch = new ConsistentHashing(servers, 3);

        System.out.println("--- İlk Atamalar | Initial Assignments ---");
        System.out.println("UserA -> " + ch.getServer("UserA"));
        System.out.println("UserB -> " + ch.getServer("UserB"));

        System.out.println("\n--- Sunucu S6 Ekleniyor | Adding Server S6 ---");
        ch.addServer("S6");
        System.out.println("UserA (Yeni Durum) -> " + ch.getServer("UserA"));

        System.out.println("\n--- Sunucu S2 Kaldırılıyor | Removing Server S2 ---");
        ch.removeServer("S2");
        System.out.println("UserB (Yeni Durum) -> " + ch.getServer("UserB"));
    }
}
