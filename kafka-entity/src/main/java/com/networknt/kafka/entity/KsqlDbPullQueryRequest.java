package com.networknt.kafka.entity;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonProperty;

public class KsqlDbPullQueryRequest  {

    
    
    public enum OffsetEnum {
        
        EARLIEST ("earliest"),
        
        LATEST ("latest");
        

        private final String value;

        OffsetEnum(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return String.valueOf(value);
        }

        public static OffsetEnum fromValue(String text) {
            for (OffsetEnum b : OffsetEnum.values()) {
                if (String.valueOf(b.value).equals(text)) {
                return b;
                }
            }
            return null;
        }
    }

    public enum QueryType {

        PULL ("pull"),

        PUSH ("push");


        private final String value;

        QueryType(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return String.valueOf(value);
        }

        public static QueryType fromValue(String text) {
            for (QueryType b : QueryType.values()) {
                if (String.valueOf(b.value).equals(text)) {
                    return b;
                }
            }
            return null;
        }
    }

    private OffsetEnum offset;

    private QueryType queryType;
    private Boolean deserializationError;
    private Boolean tableScanEnable;
    private String query;
    private Map<String, Object> properties = new HashMap<>();

    public KsqlDbPullQueryRequest () {
    }

    @JsonProperty("offset")
    public OffsetEnum getOffset() {
        return offset;
    }

    public void setOffset(String offset) {
        this.offset = OffsetEnum.fromValue(offset);
        properties.put("auto.offset.reset", offset);
    }

    @JsonProperty("queryType")
    public QueryType getQueryType() {
        return queryType;
    }

    public void setQueryType(String queryType) {
        this.queryType = QueryType.fromValue(queryType);
    }

    @JsonProperty("deserializationError")
    public Boolean getDeserializationError() {
        return deserializationError;
    }

    public void setDeserializationError(Boolean deserializationError) {
        this.deserializationError = deserializationError;
        properties.put("fail.on.deserialization.error", deserializationError);
    }

    @JsonProperty("tableScanEnable")
    public Boolean getTableScanEnable() {
        return tableScanEnable;
    }

    public void setTableScanEnable(Boolean tableScanEnable) {
        this.tableScanEnable = tableScanEnable;
        properties.put("ksql.query.pull.table.scan.enabled", tableScanEnable);
    }

    public Map<String, Object> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, Object> properties) {
        this.properties = properties;
    }

    @JsonProperty("query")
    public String getQuery() {
        return query.replace("\\'", "'");
    }

    public void setQuery(String query) {
        this.query = query;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        KsqlDbPullQueryRequest KsqlDbPullQueryRequest = (KsqlDbPullQueryRequest) o;

        return Objects.equals(offset, KsqlDbPullQueryRequest.offset) &&
               Objects.equals(deserializationError, KsqlDbPullQueryRequest.deserializationError) &&
               Objects.equals(query, KsqlDbPullQueryRequest.query);
    }

    @Override
    public int hashCode() {
        return Objects.hash(offset, deserializationError, query);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class KsqlDbPullQueryRequest {\n");
        sb.append("    offset: ").append(toIndentedString(offset)).append("\n");        sb.append("    deserializationError: ").append(toIndentedString(deserializationError)).append("\n");        sb.append("    query: ").append(toIndentedString(query)).append("\n");
        sb.append("}");
        return sb.toString();
    }

    /**
     * Convert the given object to string with each line indented by 4 spaces
     * (except the first line).
     */
    private String toIndentedString(Object o) {
        if (o == null) {
            return "null";
        }
        return o.toString().replace("\n", "\n    ");
    }
}
