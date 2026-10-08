package dev.portfolio.rtm;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/** Isolated in-memory storage: each test starts with a clean instance. */
final class OfferStore {
    private final AtomicLong nextId = new AtomicLong(1);
    private final ConcurrentHashMap<Long, Offer> offers = new ConcurrentHashMap<>();

    Offer create(String clientId, String campaignId, String channel) {
        long id = nextId.getAndIncrement();
        Offer offer = new Offer(id, clientId, campaignId, channel, "ACTIVE");
        offers.put(id, offer);
        return offer;
    }

    Optional<Offer> get(long id) {
        return Optional.ofNullable(offers.get(id));
    }

    List<Offer> list(String clientId) {
        return offers.values().stream()
                .filter(offer -> clientId == null || offer.clientId().equals(clientId))
                .sorted(Comparator.comparingLong(Offer::id))
                .toList();
    }

    Optional<Offer> updateStatus(long id, String status) {
        return Optional.ofNullable(offers.computeIfPresent(id, (key, previous) -> previous.withStatus(status)));
    }

    boolean delete(long id) {
        return offers.remove(id) != null;
    }
}
