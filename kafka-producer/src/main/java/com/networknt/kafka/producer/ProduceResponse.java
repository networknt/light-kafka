package com.networknt.kafka.producer;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.annotation.Nullable;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.Objects;

public class ProduceResponse {
    //Don't change the error codes, These are fixed for kafka-rest
    public static final int KAFKA_BAD_REQUEST_ERROR_CODE = 40002;
    public static final int KAFKA_AUTHENTICATION_ERROR_CODE = 40101;
    public static final int KAFKA_AUTHORIZATION_ERROR_CODE = 40301;
    public static final int TOPIC_NOT_FOUND_ERROR_CODE = 40401;
    public static final int PARTITION_NOT_FOUND_ERROR_CODE = 40402;
    public static final int KAFKA_UNKNOWN_TOPIC_PARTITION_CODE = 40403;
    public static final int KAFKA_ERROR_ERROR_CODE = 50002;
    public static final int KAFKA_RETRIABLE_ERROR_ERROR_CODE = 50003;
    public static final int BROKER_NOT_AVAILABLE_ERROR_CODE = 50302;

    List<PartitionOffset> offsets;
    Integer keySchemaId;
    Integer valueSchemaId;

    @JsonCreator
    public ProduceResponse(
            @JsonProperty("offsets") @Nullable List<PartitionOffset> offsets,
            @JsonProperty("key_schema_id") @Nullable Integer keySchemaId,
            @JsonProperty("value_schema_id") @Nullable Integer valueSchemaId
    ) {
        this.offsets = offsets;
        this.keySchemaId = keySchemaId;
        this.valueSchemaId = valueSchemaId;
    }

    @JsonProperty
    public List<PartitionOffset> getOffsets() {
        return offsets;
    }

    public void setOffsets(List<PartitionOffset> offsets) {
        this.offsets = offsets;
    }

    @JsonProperty("key_schema_id")
    public Integer getKeySchemaId() {
        return keySchemaId;
    }

    public void setKeySchemaId(Integer keySchemaId) {
        this.keySchemaId = keySchemaId;
    }

    @JsonProperty("value_schema_id")
    public Integer getValueSchemaId() {
        return valueSchemaId;
    }

    public void setValueSchemaId(Integer valueSchemaId) {
        this.valueSchemaId = valueSchemaId;
    }


    @JsonIgnore
    public int getStatusCode() {
        for (PartitionOffset partitionOffset : offsets) {
            if (partitionOffset.getErrorCode() == null) {
                continue;
            }

            if (partitionOffset.getErrorCode() == KAFKA_AUTHENTICATION_ERROR_CODE) {
                return 401;
            } else if (partitionOffset.getErrorCode() == KAFKA_AUTHORIZATION_ERROR_CODE) {
                return 403;
            }
        }
        return 200;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProduceResponse that = (ProduceResponse) o;
        return Objects.equals(offsets, that.offsets) && Objects.equals(keySchemaId, that.keySchemaId) && Objects.equals(valueSchemaId, that.valueSchemaId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(offsets, keySchemaId, valueSchemaId);
    }

    public Response.Status getRequestStatus() {
        for (PartitionOffset partitionOffset : offsets) {
            if (partitionOffset.getErrorCode() == null) {
                continue;
            }

            if (partitionOffset.getErrorCode() == KAFKA_AUTHENTICATION_ERROR_CODE) {
                return Response.Status.UNAUTHORIZED;
            } else if (partitionOffset.getErrorCode() == KAFKA_AUTHORIZATION_ERROR_CODE) {
                return Response.Status.FORBIDDEN;
            }
        }
        return Response.Status.OK;
    }


}
