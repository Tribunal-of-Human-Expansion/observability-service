package com.gtbs.observability.ingest;

import java.time.Instant;
import java.util.UUID;

public record BookingLifecycleEvent(
    UUID bookingId,
    String status,
    String segmentId,
    String regionId,
    String sourceService,
    String correlationId,
    String mapVersion,
    Instant eventTimestamp
) {
}
