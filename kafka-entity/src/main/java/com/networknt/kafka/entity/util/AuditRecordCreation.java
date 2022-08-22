package com.networknt.kafka.entity.util;

import com.networknt.kafka.entity.AuditRecord;
import com.networknt.utility.ObjectUtils;
import com.networknt.utility.StringUtils;

import java.util.UUID;

public class AuditRecordCreation {

    public static AuditRecord createAuditRecord (
            String id,
            String serviceId,
            AuditRecord.AuditType auditType,
            String topic,
            int partition,
            long offset,
            String correlationId,
            String traceabilityId,
            String key,
            AuditRecord.AuditStatus auditStatus,
            String stacktrace,
            Long timestamp
    )throws RuntimeException {
        if(!StringUtils.isEmpty(topic) || !StringUtils.isEmpty(serviceId)
                || !ObjectUtils.isEmpty(auditType) || !ObjectUtils.isEmpty(auditStatus.toString())
                || !StringUtils.isEmpty(serviceId)) {
            AuditRecord auditRecord = new AuditRecord();
            auditRecord.setKey(key);
            auditRecord.setTopic(topic);
            auditRecord.setId(!StringUtils.isEmpty(id) ? id : UUID.randomUUID().toString());
            auditRecord.setAuditType(auditType);
            auditRecord.setAuditStatus(auditStatus);
            auditRecord.setTimestamp(timestamp != 0L ? timestamp : System.currentTimeMillis());
            auditRecord.setPartition(partition);
            auditRecord.setOffset(offset);
            auditRecord.setCorrelationId(!StringUtils.isEmpty(correlationId) ? correlationId : UUID.randomUUID().toString());
            auditRecord.setTraceabilityId(!StringUtils.isEmpty(traceabilityId) ? traceabilityId : UUID.randomUUID().toString());
            auditRecord.setStacktrace(stacktrace);
            auditRecord.setServiceId(serviceId);
            return auditRecord;
        }
        else{
            throw new RuntimeException("Topic , ServiceId, AuditType, AuditStatus, ServiceId are mandatory and can not be accepted as empty in audit record");
        }
    }
}
