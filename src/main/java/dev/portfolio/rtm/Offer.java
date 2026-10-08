package dev.portfolio.rtm;

/** An intentionally small domain model for the demo marketing offers API. */
public record Offer(long id, String clientId, String campaignId, String channel, String status) {
    public Offer withStatus(String newStatus) {
        return new Offer(id, clientId, campaignId, channel, newStatus);
    }
}
