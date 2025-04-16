/**
 * Autogenerated by Avro
 *
 * DO NOT EDIT DIRECTLY
 */
package com.networknt.kafka.common;

import org.apache.avro.specific.SpecificData;
import org.apache.avro.util.Utf8;
import org.apache.avro.message.BinaryMessageEncoder;
import org.apache.avro.message.BinaryMessageDecoder;
import org.apache.avro.message.SchemaStore;

@org.apache.avro.specific.AvroGenerated
public class EventId extends org.apache.avro.specific.SpecificRecordBase implements org.apache.avro.specific.SpecificRecord {
  private static final long serialVersionUID = -1405236215870335809L;


  public static final org.apache.avro.Schema SCHEMA$ = new org.apache.avro.Schema.Parser().parse("{\"type\":\"record\",\"name\":\"EventId\",\"namespace\":\"com.networknt.kafka.common\",\"fields\":[{\"name\":\"id\",\"type\":{\"type\":\"string\",\"avro.java.string\":\"String\"},\"doc\":\"a unique identifier for the event\"},{\"name\":\"userId\",\"type\":{\"type\":\"string\",\"avro.java.string\":\"String\"},\"doc\":\"the user who creates the event\"},{\"name\":\"hostId\",\"type\":{\"type\":\"string\",\"avro.java.string\":\"String\"},\"doc\":\"the host which is the event is created\"},{\"name\":\"nonce\",\"type\":\"long\",\"doc\":\"the number of the transactions for the user\"},{\"name\":\"timestamp\",\"type\":\"long\",\"doc\":\"time the event is recorded\",\"default\":0},{\"name\":\"derived\",\"type\":\"boolean\",\"doc\":\"indicate if the event is derived from event processor\",\"default\":false}]}");
  public static org.apache.avro.Schema getClassSchema() { return SCHEMA$; }

  private static final SpecificData MODEL$ = new SpecificData();

  private static final BinaryMessageEncoder<EventId> ENCODER =
      new BinaryMessageEncoder<>(MODEL$, SCHEMA$);

  private static final BinaryMessageDecoder<EventId> DECODER =
      new BinaryMessageDecoder<>(MODEL$, SCHEMA$);

  /**
   * Return the BinaryMessageEncoder instance used by this class.
   * @return the message encoder used by this class
   */
  public static BinaryMessageEncoder<EventId> getEncoder() {
    return ENCODER;
  }

  /**
   * Return the BinaryMessageDecoder instance used by this class.
   * @return the message decoder used by this class
   */
  public static BinaryMessageDecoder<EventId> getDecoder() {
    return DECODER;
  }

  /**
   * Create a new BinaryMessageDecoder instance for this class that uses the specified {@link SchemaStore}.
   * @param resolver a {@link SchemaStore} used to find schemas by fingerprint
   * @return a BinaryMessageDecoder instance for this class backed by the given SchemaStore
   */
  public static BinaryMessageDecoder<EventId> createDecoder(SchemaStore resolver) {
    return new BinaryMessageDecoder<>(MODEL$, SCHEMA$, resolver);
  }

  /**
   * Serializes this EventId to a ByteBuffer.
   * @return a buffer holding the serialized data for this instance
   * @throws java.io.IOException if this instance could not be serialized
   */
  public java.nio.ByteBuffer toByteBuffer() throws java.io.IOException {
    return ENCODER.encode(this);
  }

  /**
   * Deserializes a EventId from a ByteBuffer.
   * @param b a byte buffer holding serialized data for an instance of this class
   * @return a EventId instance decoded from the given buffer
   * @throws java.io.IOException if the given bytes could not be deserialized into an instance of this class
   */
  public static EventId fromByteBuffer(
      java.nio.ByteBuffer b) throws java.io.IOException {
    return DECODER.decode(b);
  }

  /** a unique identifier for the event */
  private java.lang.String id;
  /** the user who creates the event */
  private java.lang.String userId;
  /** the host which is the event is created */
  private java.lang.String hostId;
  /** the number of the transactions for the user */
  private long nonce;
  /** time the event is recorded */
  private long timestamp;
  /** indicate if the event is derived from event processor */
  private boolean derived;

  /**
   * Default constructor.  Note that this does not initialize fields
   * to their default values from the schema.  If that is desired then
   * one should use <code>newBuilder()</code>.
   */
  public EventId() {}

  /**
   * All-args constructor.
   * @param id a unique identifier for the event
   * @param userId the user who creates the event
   * @param hostId the host which is the event is created
   * @param nonce the number of the transactions for the user
   * @param timestamp time the event is recorded
   * @param derived indicate if the event is derived from event processor
   */
  public EventId(java.lang.String id, java.lang.String userId, java.lang.String hostId, java.lang.Long nonce, java.lang.Long timestamp, java.lang.Boolean derived) {
    this.id = id;
    this.userId = userId;
    this.hostId = hostId;
    this.nonce = nonce;
    this.timestamp = timestamp;
    this.derived = derived;
  }

  @Override
  public org.apache.avro.specific.SpecificData getSpecificData() { return MODEL$; }

  @Override
  public org.apache.avro.Schema getSchema() { return SCHEMA$; }

  // Used by DatumWriter.  Applications should not call.
  @Override
  public java.lang.Object get(int field$) {
    switch (field$) {
    case 0: return id;
    case 1: return userId;
    case 2: return hostId;
    case 3: return nonce;
    case 4: return timestamp;
    case 5: return derived;
    default: throw new IndexOutOfBoundsException("Invalid index: " + field$);
    }
  }

  // Used by DatumReader.  Applications should not call.
  @Override
  @SuppressWarnings(value="unchecked")
  public void put(int field$, java.lang.Object value$) {
    switch (field$) {
    case 0: id = value$ != null ? value$.toString() : null; break;
    case 1: userId = value$ != null ? value$.toString() : null; break;
    case 2: hostId = value$ != null ? value$.toString() : null; break;
    case 3: nonce = (java.lang.Long)value$; break;
    case 4: timestamp = (java.lang.Long)value$; break;
    case 5: derived = (java.lang.Boolean)value$; break;
    default: throw new IndexOutOfBoundsException("Invalid index: " + field$);
    }
  }

  /**
   * Gets the value of the 'id' field.
   * @return a unique identifier for the event
   */
  public java.lang.String getId() {
    return id;
  }


  /**
   * Sets the value of the 'id' field.
   * a unique identifier for the event
   * @param value the value to set.
   */
  public void setId(java.lang.String value) {
    this.id = value;
  }

  /**
   * Gets the value of the 'userId' field.
   * @return the user who creates the event
   */
  public java.lang.String getUserId() {
    return userId;
  }


  /**
   * Sets the value of the 'userId' field.
   * the user who creates the event
   * @param value the value to set.
   */
  public void setUserId(java.lang.String value) {
    this.userId = value;
  }

  /**
   * Gets the value of the 'hostId' field.
   * @return the host which is the event is created
   */
  public java.lang.String getHostId() {
    return hostId;
  }


  /**
   * Sets the value of the 'hostId' field.
   * the host which is the event is created
   * @param value the value to set.
   */
  public void setHostId(java.lang.String value) {
    this.hostId = value;
  }

  /**
   * Gets the value of the 'nonce' field.
   * @return the number of the transactions for the user
   */
  public long getNonce() {
    return nonce;
  }


  /**
   * Sets the value of the 'nonce' field.
   * the number of the transactions for the user
   * @param value the value to set.
   */
  public void setNonce(long value) {
    this.nonce = value;
  }

  /**
   * Gets the value of the 'timestamp' field.
   * @return time the event is recorded
   */
  public long getTimestamp() {
    return timestamp;
  }


  /**
   * Sets the value of the 'timestamp' field.
   * time the event is recorded
   * @param value the value to set.
   */
  public void setTimestamp(long value) {
    this.timestamp = value;
  }

  /**
   * Gets the value of the 'derived' field.
   * @return indicate if the event is derived from event processor
   */
  public boolean getDerived() {
    return derived;
  }


  /**
   * Sets the value of the 'derived' field.
   * indicate if the event is derived from event processor
   * @param value the value to set.
   */
  public void setDerived(boolean value) {
    this.derived = value;
  }

  /**
   * Creates a new EventId RecordBuilder.
   * @return A new EventId RecordBuilder
   */
  public static com.networknt.kafka.common.EventId.Builder newBuilder() {
    return new com.networknt.kafka.common.EventId.Builder();
  }

  /**
   * Creates a new EventId RecordBuilder by copying an existing Builder.
   * @param other The existing builder to copy.
   * @return A new EventId RecordBuilder
   */
  public static com.networknt.kafka.common.EventId.Builder newBuilder(com.networknt.kafka.common.EventId.Builder other) {
    if (other == null) {
      return new com.networknt.kafka.common.EventId.Builder();
    } else {
      return new com.networknt.kafka.common.EventId.Builder(other);
    }
  }

  /**
   * Creates a new EventId RecordBuilder by copying an existing EventId instance.
   * @param other The existing instance to copy.
   * @return A new EventId RecordBuilder
   */
  public static com.networknt.kafka.common.EventId.Builder newBuilder(com.networknt.kafka.common.EventId other) {
    if (other == null) {
      return new com.networknt.kafka.common.EventId.Builder();
    } else {
      return new com.networknt.kafka.common.EventId.Builder(other);
    }
  }

  /**
   * RecordBuilder for EventId instances.
   */
  @org.apache.avro.specific.AvroGenerated
  public static class Builder extends org.apache.avro.specific.SpecificRecordBuilderBase<EventId>
    implements org.apache.avro.data.RecordBuilder<EventId> {

    /** a unique identifier for the event */
    private java.lang.String id;
    /** the user who creates the event */
    private java.lang.String userId;
    /** the host which is the event is created */
    private java.lang.String hostId;
    /** the number of the transactions for the user */
    private long nonce;
    /** time the event is recorded */
    private long timestamp;
    /** indicate if the event is derived from event processor */
    private boolean derived;

    /** Creates a new Builder */
    private Builder() {
      super(SCHEMA$, MODEL$);
    }

    /**
     * Creates a Builder by copying an existing Builder.
     * @param other The existing Builder to copy.
     */
    private Builder(com.networknt.kafka.common.EventId.Builder other) {
      super(other);
      if (isValidValue(fields()[0], other.id)) {
        this.id = data().deepCopy(fields()[0].schema(), other.id);
        fieldSetFlags()[0] = other.fieldSetFlags()[0];
      }
      if (isValidValue(fields()[1], other.userId)) {
        this.userId = data().deepCopy(fields()[1].schema(), other.userId);
        fieldSetFlags()[1] = other.fieldSetFlags()[1];
      }
      if (isValidValue(fields()[2], other.hostId)) {
        this.hostId = data().deepCopy(fields()[2].schema(), other.hostId);
        fieldSetFlags()[2] = other.fieldSetFlags()[2];
      }
      if (isValidValue(fields()[3], other.nonce)) {
        this.nonce = data().deepCopy(fields()[3].schema(), other.nonce);
        fieldSetFlags()[3] = other.fieldSetFlags()[3];
      }
      if (isValidValue(fields()[4], other.timestamp)) {
        this.timestamp = data().deepCopy(fields()[4].schema(), other.timestamp);
        fieldSetFlags()[4] = other.fieldSetFlags()[4];
      }
      if (isValidValue(fields()[5], other.derived)) {
        this.derived = data().deepCopy(fields()[5].schema(), other.derived);
        fieldSetFlags()[5] = other.fieldSetFlags()[5];
      }
    }

    /**
     * Creates a Builder by copying an existing EventId instance
     * @param other The existing instance to copy.
     */
    private Builder(com.networknt.kafka.common.EventId other) {
      super(SCHEMA$, MODEL$);
      if (isValidValue(fields()[0], other.id)) {
        this.id = data().deepCopy(fields()[0].schema(), other.id);
        fieldSetFlags()[0] = true;
      }
      if (isValidValue(fields()[1], other.userId)) {
        this.userId = data().deepCopy(fields()[1].schema(), other.userId);
        fieldSetFlags()[1] = true;
      }
      if (isValidValue(fields()[2], other.hostId)) {
        this.hostId = data().deepCopy(fields()[2].schema(), other.hostId);
        fieldSetFlags()[2] = true;
      }
      if (isValidValue(fields()[3], other.nonce)) {
        this.nonce = data().deepCopy(fields()[3].schema(), other.nonce);
        fieldSetFlags()[3] = true;
      }
      if (isValidValue(fields()[4], other.timestamp)) {
        this.timestamp = data().deepCopy(fields()[4].schema(), other.timestamp);
        fieldSetFlags()[4] = true;
      }
      if (isValidValue(fields()[5], other.derived)) {
        this.derived = data().deepCopy(fields()[5].schema(), other.derived);
        fieldSetFlags()[5] = true;
      }
    }

    /**
      * Gets the value of the 'id' field.
      * a unique identifier for the event
      * @return The value.
      */
    public java.lang.String getId() {
      return id;
    }


    /**
      * Sets the value of the 'id' field.
      * a unique identifier for the event
      * @param value The value of 'id'.
      * @return This builder.
      */
    public com.networknt.kafka.common.EventId.Builder setId(java.lang.String value) {
      validate(fields()[0], value);
      this.id = value;
      fieldSetFlags()[0] = true;
      return this;
    }

    /**
      * Checks whether the 'id' field has been set.
      * a unique identifier for the event
      * @return True if the 'id' field has been set, false otherwise.
      */
    public boolean hasId() {
      return fieldSetFlags()[0];
    }


    /**
      * Clears the value of the 'id' field.
      * a unique identifier for the event
      * @return This builder.
      */
    public com.networknt.kafka.common.EventId.Builder clearId() {
      id = null;
      fieldSetFlags()[0] = false;
      return this;
    }

    /**
      * Gets the value of the 'userId' field.
      * the user who creates the event
      * @return The value.
      */
    public java.lang.String getUserId() {
      return userId;
    }


    /**
      * Sets the value of the 'userId' field.
      * the user who creates the event
      * @param value The value of 'userId'.
      * @return This builder.
      */
    public com.networknt.kafka.common.EventId.Builder setUserId(java.lang.String value) {
      validate(fields()[1], value);
      this.userId = value;
      fieldSetFlags()[1] = true;
      return this;
    }

    /**
      * Checks whether the 'userId' field has been set.
      * the user who creates the event
      * @return True if the 'userId' field has been set, false otherwise.
      */
    public boolean hasUserId() {
      return fieldSetFlags()[1];
    }


    /**
      * Clears the value of the 'userId' field.
      * the user who creates the event
      * @return This builder.
      */
    public com.networknt.kafka.common.EventId.Builder clearUserId() {
      userId = null;
      fieldSetFlags()[1] = false;
      return this;
    }

    /**
      * Gets the value of the 'hostId' field.
      * the host which is the event is created
      * @return The value.
      */
    public java.lang.String getHostId() {
      return hostId;
    }


    /**
      * Sets the value of the 'hostId' field.
      * the host which is the event is created
      * @param value The value of 'hostId'.
      * @return This builder.
      */
    public com.networknt.kafka.common.EventId.Builder setHostId(java.lang.String value) {
      validate(fields()[2], value);
      this.hostId = value;
      fieldSetFlags()[2] = true;
      return this;
    }

    /**
      * Checks whether the 'hostId' field has been set.
      * the host which is the event is created
      * @return True if the 'hostId' field has been set, false otherwise.
      */
    public boolean hasHostId() {
      return fieldSetFlags()[2];
    }


    /**
      * Clears the value of the 'hostId' field.
      * the host which is the event is created
      * @return This builder.
      */
    public com.networknt.kafka.common.EventId.Builder clearHostId() {
      hostId = null;
      fieldSetFlags()[2] = false;
      return this;
    }

    /**
      * Gets the value of the 'nonce' field.
      * the number of the transactions for the user
      * @return The value.
      */
    public long getNonce() {
      return nonce;
    }


    /**
      * Sets the value of the 'nonce' field.
      * the number of the transactions for the user
      * @param value The value of 'nonce'.
      * @return This builder.
      */
    public com.networknt.kafka.common.EventId.Builder setNonce(long value) {
      validate(fields()[3], value);
      this.nonce = value;
      fieldSetFlags()[3] = true;
      return this;
    }

    /**
      * Checks whether the 'nonce' field has been set.
      * the number of the transactions for the user
      * @return True if the 'nonce' field has been set, false otherwise.
      */
    public boolean hasNonce() {
      return fieldSetFlags()[3];
    }


    /**
      * Clears the value of the 'nonce' field.
      * the number of the transactions for the user
      * @return This builder.
      */
    public com.networknt.kafka.common.EventId.Builder clearNonce() {
      fieldSetFlags()[3] = false;
      return this;
    }

    /**
      * Gets the value of the 'timestamp' field.
      * time the event is recorded
      * @return The value.
      */
    public long getTimestamp() {
      return timestamp;
    }


    /**
      * Sets the value of the 'timestamp' field.
      * time the event is recorded
      * @param value The value of 'timestamp'.
      * @return This builder.
      */
    public com.networknt.kafka.common.EventId.Builder setTimestamp(long value) {
      validate(fields()[4], value);
      this.timestamp = value;
      fieldSetFlags()[4] = true;
      return this;
    }

    /**
      * Checks whether the 'timestamp' field has been set.
      * time the event is recorded
      * @return True if the 'timestamp' field has been set, false otherwise.
      */
    public boolean hasTimestamp() {
      return fieldSetFlags()[4];
    }


    /**
      * Clears the value of the 'timestamp' field.
      * time the event is recorded
      * @return This builder.
      */
    public com.networknt.kafka.common.EventId.Builder clearTimestamp() {
      fieldSetFlags()[4] = false;
      return this;
    }

    /**
      * Gets the value of the 'derived' field.
      * indicate if the event is derived from event processor
      * @return The value.
      */
    public boolean getDerived() {
      return derived;
    }


    /**
      * Sets the value of the 'derived' field.
      * indicate if the event is derived from event processor
      * @param value The value of 'derived'.
      * @return This builder.
      */
    public com.networknt.kafka.common.EventId.Builder setDerived(boolean value) {
      validate(fields()[5], value);
      this.derived = value;
      fieldSetFlags()[5] = true;
      return this;
    }

    /**
      * Checks whether the 'derived' field has been set.
      * indicate if the event is derived from event processor
      * @return True if the 'derived' field has been set, false otherwise.
      */
    public boolean hasDerived() {
      return fieldSetFlags()[5];
    }


    /**
      * Clears the value of the 'derived' field.
      * indicate if the event is derived from event processor
      * @return This builder.
      */
    public com.networknt.kafka.common.EventId.Builder clearDerived() {
      fieldSetFlags()[5] = false;
      return this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public EventId build() {
      try {
        EventId record = new EventId();
        record.id = fieldSetFlags()[0] ? this.id : (java.lang.String) defaultValue(fields()[0]);
        record.userId = fieldSetFlags()[1] ? this.userId : (java.lang.String) defaultValue(fields()[1]);
        record.hostId = fieldSetFlags()[2] ? this.hostId : (java.lang.String) defaultValue(fields()[2]);
        record.nonce = fieldSetFlags()[3] ? this.nonce : (java.lang.Long) defaultValue(fields()[3]);
        record.timestamp = fieldSetFlags()[4] ? this.timestamp : (java.lang.Long) defaultValue(fields()[4]);
        record.derived = fieldSetFlags()[5] ? this.derived : (java.lang.Boolean) defaultValue(fields()[5]);
        return record;
      } catch (org.apache.avro.AvroMissingFieldException e) {
        throw e;
      } catch (java.lang.Exception e) {
        throw new org.apache.avro.AvroRuntimeException(e);
      }
    }
  }

  @SuppressWarnings("unchecked")
  private static final org.apache.avro.io.DatumWriter<EventId>
    WRITER$ = (org.apache.avro.io.DatumWriter<EventId>)MODEL$.createDatumWriter(SCHEMA$);

  @Override public void writeExternal(java.io.ObjectOutput out)
    throws java.io.IOException {
    WRITER$.write(this, SpecificData.getEncoder(out));
  }

  @SuppressWarnings("unchecked")
  private static final org.apache.avro.io.DatumReader<EventId>
    READER$ = (org.apache.avro.io.DatumReader<EventId>)MODEL$.createDatumReader(SCHEMA$);

  @Override public void readExternal(java.io.ObjectInput in)
    throws java.io.IOException {
    READER$.read(this, SpecificData.getDecoder(in));
  }

  @Override protected boolean hasCustomCoders() { return true; }

  @Override public void customEncode(org.apache.avro.io.Encoder out)
    throws java.io.IOException
  {
    out.writeString(this.id);

    out.writeString(this.userId);

    out.writeString(this.hostId);

    out.writeLong(this.nonce);

    out.writeLong(this.timestamp);

    out.writeBoolean(this.derived);

  }

  @Override public void customDecode(org.apache.avro.io.ResolvingDecoder in)
    throws java.io.IOException
  {
    org.apache.avro.Schema.Field[] fieldOrder = in.readFieldOrderIfDiff();
    if (fieldOrder == null) {
      this.id = in.readString();

      this.userId = in.readString();

      this.hostId = in.readString();

      this.nonce = in.readLong();

      this.timestamp = in.readLong();

      this.derived = in.readBoolean();

    } else {
      for (int i = 0; i < 6; i++) {
        switch (fieldOrder[i].pos()) {
        case 0:
          this.id = in.readString();
          break;

        case 1:
          this.userId = in.readString();
          break;

        case 2:
          this.hostId = in.readString();
          break;

        case 3:
          this.nonce = in.readLong();
          break;

        case 4:
          this.timestamp = in.readLong();
          break;

        case 5:
          this.derived = in.readBoolean();
          break;

        default:
          throw new java.io.IOException("Corrupt ResolvingDecoder.");
        }
      }
    }
  }
}
