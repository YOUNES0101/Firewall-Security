package com.firewall.persistence;

import com.firewall.persistence.entity.CapturedPacket;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

import java.util.List;

/**
 * Repository for storing and retrieving captured packets.
 */
public class PacketRepository {

    private static final String PERSISTENCE_UNIT = "firewall-pu";
    private static EntityManagerFactory emf;

    /**
     * Initializes the database connection.
     */
    public static void initialize() {
        if (emf == null) {
            emf = Persistence.createEntityManagerFactory(PERSISTENCE_UNIT);
        }
    }

    /**
     * Closes the database connection.
     */
    public static void shutdown() {
        if (emf != null && emf.isOpen()) {
            emf.close();
        }
    }

    /**
     * Saves a captured packet to the database.
     */
    public void save(CapturedPacket packet) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            em.persist(packet);
            em.getTransaction().commit();
        } finally {
            em.close();
        }
    }

    /**
     * Saves multiple packets in a batch.
     */
    public void saveAll(List<CapturedPacket> packets) {
        if (packets.isEmpty())
            return;

        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            for (int i = 0; i < packets.size(); i++) {
                em.persist(packets.get(i));
                if (i % 50 == 0) { // Flush every 50 packets
                    em.flush();
                    em.clear();
                }
            }
            em.getTransaction().commit();
        } finally {
            em.close();
        }
    }

    /**
     * Gets all packets for a session.
     */
    public List<CapturedPacket> findBySession(String sessionId) {
        EntityManager em = emf.createEntityManager();
        try {
            return em.createQuery(
                    "SELECT p FROM CapturedPacket p WHERE p.sessionId = :sessionId ORDER BY p.capturedAt DESC",
                    CapturedPacket.class)
                    .setParameter("sessionId", sessionId)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    /**
     * Gets packets by source IP.
     */
    public List<CapturedPacket> findBySourceIp(String ip) {
        EntityManager em = emf.createEntityManager();
        try {
            return em.createQuery(
                    "SELECT p FROM CapturedPacket p WHERE p.sourceIp = :ip ORDER BY p.capturedAt DESC",
                    CapturedPacket.class)
                    .setParameter("ip", ip)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    /**
     * Gets recent packets (last N).
     */
    public List<CapturedPacket> findRecent(int limit) {
        EntityManager em = emf.createEntityManager();
        try {
            return em.createQuery(
                    "SELECT p FROM CapturedPacket p ORDER BY p.capturedAt DESC",
                    CapturedPacket.class)
                    .setMaxResults(limit)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    /**
     * Counts total packets captured.
     */
    public long count() {
        EntityManager em = emf.createEntityManager();
        try {
            return em.createQuery("SELECT COUNT(p) FROM CapturedPacket p", Long.class)
                    .getSingleResult();
        } finally {
            em.close();
        }
    }
}
