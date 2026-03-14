package com.gtbs.observability.audit.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gtbs.observability.audit.api.AuditRecordResponse;
import com.gtbs.observability.audit.domain.AuditEventType;
import com.gtbs.observability.audit.domain.AuditRecord;
import com.gtbs.observability.audit.repository.AuditRecordRepository;
import com.gtbs.observability.ingest.BookingLifecycleEvent;
import com.gtbs.observability.ingest.PolicyChangeEvent;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuditLogService {

    private final AuditRecordRepository auditRecordRepository;
    private final ObjectMapper objectMapper;

    public AuditLogService(AuditRecordRepository auditRecordRepository, ObjectMapper objectMapper) {
        this.auditRecordRepository = auditRecordRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public void recordBookingEvent(BookingLifecycleEvent event) {
        auditRecordRepository.save(
            AuditRecord.create(
                AuditEventType.valueOf(event.status()),
                event.bookingId(),
                event.segmentId(),
                event.regionId(),
                event.sourceService(),
                event.correlationId(),
                event.mapVersion(),
                event.eventTimestamp(),
                toJson(event)
            )
        );
    }

    @Transactional
    public void recordPolicyEvent(PolicyChangeEvent event) {
        auditRecordRepository.save(
            AuditRecord.create(
                AuditEventType.valueOf(event.eventType()),
                null,
                event.segmentId(),
                event.regionId(),
                event.sourceService(),
                event.correlationId(),
                event.mapVersion(),
                event.eventTimestamp(),
                toJson(event)
            )
        );
    }

    @Transactional(readOnly = true)
    public List<AuditRecordResponse> findByBookingId(UUID bookingId) {
        return auditRecordRepository.findByBookingIdOrderByEventTimestampAsc(bookingId)
            .stream()
            .map(AuditRecordResponse::from)
            .toList();
    }

    @Transactional(readOnly = true)
    public List<AuditRecordResponse> findBySegmentId(String segmentId) {
        return auditRecordRepository.findBySegmentIdOrderByEventTimestampAsc(segmentId)
            .stream()
            .map(AuditRecordResponse::from)
            .toList();
    }

    private String toJson(Object payload) {
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException exception) {
            throw new IllegalArgumentException("Could not serialize audit payload", exception);
        }
    }
}
