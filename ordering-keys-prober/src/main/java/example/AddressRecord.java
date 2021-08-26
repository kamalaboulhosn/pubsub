/**
 * Autogenerated by Avro
 *
 * DO NOT EDIT DIRECTLY
 */
package google.example;

import org.apache.avro.generic.GenericArray;
import org.apache.avro.specific.SpecificData;
import org.apache.avro.util.Utf8;
import org.apache.avro.message.BinaryMessageEncoder;
import org.apache.avro.message.BinaryMessageDecoder;
import org.apache.avro.message.SchemaStore;

@org.apache.avro.specific.AvroGenerated
public class AddressRecord extends org.apache.avro.specific.SpecificRecordBase implements org.apache.avro.specific.SpecificRecord {
  private static final long serialVersionUID = -237156364116896528L;
  public static final org.apache.avro.Schema SCHEMA$ = new org.apache.avro.Schema.Parser().parse("{\"type\":\"record\",\"name\":\"AddressRecord\",\"namespace\":\"google.example\",\"fields\":[{\"name\":\"street_number\",\"type\":\"int\"},{\"name\":\"street_name\",\"type\":\"string\"},{\"name\":\"city\",\"type\":\"string\"},{\"name\":\"state\",\"type\":\"string\"},{\"name\":\"country\",\"type\":\"string\"},{\"name\":\"zip\",\"type\":\"string\"}]}");
  public static org.apache.avro.Schema getClassSchema() { return SCHEMA$; }

  private static SpecificData MODEL$ = new SpecificData();

  private static final BinaryMessageEncoder<AddressRecord> ENCODER =
      new BinaryMessageEncoder<AddressRecord>(MODEL$, SCHEMA$);

  private static final BinaryMessageDecoder<AddressRecord> DECODER =
      new BinaryMessageDecoder<AddressRecord>(MODEL$, SCHEMA$);

  /**
   * Return the BinaryMessageEncoder instance used by this class.
   * @return the message encoder used by this class
   */
  public static BinaryMessageEncoder<AddressRecord> getEncoder() {
    return ENCODER;
  }

  /**
   * Return the BinaryMessageDecoder instance used by this class.
   * @return the message decoder used by this class
   */
  public static BinaryMessageDecoder<AddressRecord> getDecoder() {
    return DECODER;
  }

  /**
   * Create a new BinaryMessageDecoder instance for this class that uses the specified {@link SchemaStore}.
   * @param resolver a {@link SchemaStore} used to find schemas by fingerprint
   * @return a BinaryMessageDecoder instance for this class backed by the given SchemaStore
   */
  public static BinaryMessageDecoder<AddressRecord> createDecoder(SchemaStore resolver) {
    return new BinaryMessageDecoder<AddressRecord>(MODEL$, SCHEMA$, resolver);
  }

  /**
   * Serializes this AddressRecord to a ByteBuffer.
   * @return a buffer holding the serialized data for this instance
   * @throws java.io.IOException if this instance could not be serialized
   */
  public java.nio.ByteBuffer toByteBuffer() throws java.io.IOException {
    return ENCODER.encode(this);
  }

  /**
   * Deserializes a AddressRecord from a ByteBuffer.
   * @param b a byte buffer holding serialized data for an instance of this class
   * @return a AddressRecord instance decoded from the given buffer
   * @throws java.io.IOException if the given bytes could not be deserialized into an instance of this class
   */
  public static AddressRecord fromByteBuffer(
      java.nio.ByteBuffer b) throws java.io.IOException {
    return DECODER.decode(b);
  }

   private int street_number;
   private java.lang.CharSequence street_name;
   private java.lang.CharSequence city;
   private java.lang.CharSequence state;
   private java.lang.CharSequence country;
   private java.lang.CharSequence zip;

  /**
   * Default constructor.  Note that this does not initialize fields
   * to their default values from the schema.  If that is desired then
   * one should use <code>newBuilder()</code>.
   */
  public AddressRecord() {}

  /**
   * All-args constructor.
   * @param street_number The new value for street_number
   * @param street_name The new value for street_name
   * @param city The new value for city
   * @param state The new value for state
   * @param country The new value for country
   * @param zip The new value for zip
   */
  public AddressRecord(java.lang.Integer street_number, java.lang.CharSequence street_name, java.lang.CharSequence city, java.lang.CharSequence state, java.lang.CharSequence country, java.lang.CharSequence zip) {
    this.street_number = street_number;
    this.street_name = street_name;
    this.city = city;
    this.state = state;
    this.country = country;
    this.zip = zip;
  }

  public org.apache.avro.specific.SpecificData getSpecificData() { return MODEL$; }
  public org.apache.avro.Schema getSchema() { return SCHEMA$; }
  // Used by DatumWriter.  Applications should not call.
  public java.lang.Object get(int field$) {
    switch (field$) {
    case 0: return street_number;
    case 1: return street_name;
    case 2: return city;
    case 3: return state;
    case 4: return country;
    case 5: return zip;
    default: throw new IndexOutOfBoundsException("Invalid index: " + field$);
    }
  }

  // Used by DatumReader.  Applications should not call.
  @SuppressWarnings(value="unchecked")
  public void put(int field$, java.lang.Object value$) {
    switch (field$) {
    case 0: street_number = (java.lang.Integer)value$; break;
    case 1: street_name = (java.lang.CharSequence)value$; break;
    case 2: city = (java.lang.CharSequence)value$; break;
    case 3: state = (java.lang.CharSequence)value$; break;
    case 4: country = (java.lang.CharSequence)value$; break;
    case 5: zip = (java.lang.CharSequence)value$; break;
    default: throw new IndexOutOfBoundsException("Invalid index: " + field$);
    }
  }

  /**
   * Gets the value of the 'street_number' field.
   * @return The value of the 'street_number' field.
   */
  public int getStreetNumber() {
    return street_number;
  }


  /**
   * Sets the value of the 'street_number' field.
   * @param value the value to set.
   */
  public void setStreetNumber(int value) {
    this.street_number = value;
  }

  /**
   * Gets the value of the 'street_name' field.
   * @return The value of the 'street_name' field.
   */
  public java.lang.CharSequence getStreetName() {
    return street_name;
  }


  /**
   * Sets the value of the 'street_name' field.
   * @param value the value to set.
   */
  public void setStreetName(java.lang.CharSequence value) {
    this.street_name = value;
  }

  /**
   * Gets the value of the 'city' field.
   * @return The value of the 'city' field.
   */
  public java.lang.CharSequence getCity() {
    return city;
  }


  /**
   * Sets the value of the 'city' field.
   * @param value the value to set.
   */
  public void setCity(java.lang.CharSequence value) {
    this.city = value;
  }

  /**
   * Gets the value of the 'state' field.
   * @return The value of the 'state' field.
   */
  public java.lang.CharSequence getState() {
    return state;
  }


  /**
   * Sets the value of the 'state' field.
   * @param value the value to set.
   */
  public void setState(java.lang.CharSequence value) {
    this.state = value;
  }

  /**
   * Gets the value of the 'country' field.
   * @return The value of the 'country' field.
   */
  public java.lang.CharSequence getCountry() {
    return country;
  }


  /**
   * Sets the value of the 'country' field.
   * @param value the value to set.
   */
  public void setCountry(java.lang.CharSequence value) {
    this.country = value;
  }

  /**
   * Gets the value of the 'zip' field.
   * @return The value of the 'zip' field.
   */
  public java.lang.CharSequence getZip() {
    return zip;
  }


  /**
   * Sets the value of the 'zip' field.
   * @param value the value to set.
   */
  public void setZip(java.lang.CharSequence value) {
    this.zip = value;
  }

  /**
   * Creates a new AddressRecord RecordBuilder.
   * @return A new AddressRecord RecordBuilder
   */
  public static google.example.AddressRecord.Builder newBuilder() {
    return new google.example.AddressRecord.Builder();
  }

  /**
   * Creates a new AddressRecord RecordBuilder by copying an existing Builder.
   * @param other The existing builder to copy.
   * @return A new AddressRecord RecordBuilder
   */
  public static google.example.AddressRecord.Builder newBuilder(google.example.AddressRecord.Builder other) {
    if (other == null) {
      return new google.example.AddressRecord.Builder();
    } else {
      return new google.example.AddressRecord.Builder(other);
    }
  }

  /**
   * Creates a new AddressRecord RecordBuilder by copying an existing AddressRecord instance.
   * @param other The existing instance to copy.
   * @return A new AddressRecord RecordBuilder
   */
  public static google.example.AddressRecord.Builder newBuilder(google.example.AddressRecord other) {
    if (other == null) {
      return new google.example.AddressRecord.Builder();
    } else {
      return new google.example.AddressRecord.Builder(other);
    }
  }

  /**
   * RecordBuilder for AddressRecord instances.
   */
  @org.apache.avro.specific.AvroGenerated
  public static class Builder extends org.apache.avro.specific.SpecificRecordBuilderBase<AddressRecord>
    implements org.apache.avro.data.RecordBuilder<AddressRecord> {

    private int street_number;
    private java.lang.CharSequence street_name;
    private java.lang.CharSequence city;
    private java.lang.CharSequence state;
    private java.lang.CharSequence country;
    private java.lang.CharSequence zip;

    /** Creates a new Builder */
    private Builder() {
      super(SCHEMA$);
    }

    /**
     * Creates a Builder by copying an existing Builder.
     * @param other The existing Builder to copy.
     */
    private Builder(google.example.AddressRecord.Builder other) {
      super(other);
      if (isValidValue(fields()[0], other.street_number)) {
        this.street_number = data().deepCopy(fields()[0].schema(), other.street_number);
        fieldSetFlags()[0] = other.fieldSetFlags()[0];
      }
      if (isValidValue(fields()[1], other.street_name)) {
        this.street_name = data().deepCopy(fields()[1].schema(), other.street_name);
        fieldSetFlags()[1] = other.fieldSetFlags()[1];
      }
      if (isValidValue(fields()[2], other.city)) {
        this.city = data().deepCopy(fields()[2].schema(), other.city);
        fieldSetFlags()[2] = other.fieldSetFlags()[2];
      }
      if (isValidValue(fields()[3], other.state)) {
        this.state = data().deepCopy(fields()[3].schema(), other.state);
        fieldSetFlags()[3] = other.fieldSetFlags()[3];
      }
      if (isValidValue(fields()[4], other.country)) {
        this.country = data().deepCopy(fields()[4].schema(), other.country);
        fieldSetFlags()[4] = other.fieldSetFlags()[4];
      }
      if (isValidValue(fields()[5], other.zip)) {
        this.zip = data().deepCopy(fields()[5].schema(), other.zip);
        fieldSetFlags()[5] = other.fieldSetFlags()[5];
      }
    }

    /**
     * Creates a Builder by copying an existing AddressRecord instance
     * @param other The existing instance to copy.
     */
    private Builder(google.example.AddressRecord other) {
      super(SCHEMA$);
      if (isValidValue(fields()[0], other.street_number)) {
        this.street_number = data().deepCopy(fields()[0].schema(), other.street_number);
        fieldSetFlags()[0] = true;
      }
      if (isValidValue(fields()[1], other.street_name)) {
        this.street_name = data().deepCopy(fields()[1].schema(), other.street_name);
        fieldSetFlags()[1] = true;
      }
      if (isValidValue(fields()[2], other.city)) {
        this.city = data().deepCopy(fields()[2].schema(), other.city);
        fieldSetFlags()[2] = true;
      }
      if (isValidValue(fields()[3], other.state)) {
        this.state = data().deepCopy(fields()[3].schema(), other.state);
        fieldSetFlags()[3] = true;
      }
      if (isValidValue(fields()[4], other.country)) {
        this.country = data().deepCopy(fields()[4].schema(), other.country);
        fieldSetFlags()[4] = true;
      }
      if (isValidValue(fields()[5], other.zip)) {
        this.zip = data().deepCopy(fields()[5].schema(), other.zip);
        fieldSetFlags()[5] = true;
      }
    }

    /**
      * Gets the value of the 'street_number' field.
      * @return The value.
      */
    public int getStreetNumber() {
      return street_number;
    }


    /**
      * Sets the value of the 'street_number' field.
      * @param value The value of 'street_number'.
      * @return This builder.
      */
    public google.example.AddressRecord.Builder setStreetNumber(int value) {
      validate(fields()[0], value);
      this.street_number = value;
      fieldSetFlags()[0] = true;
      return this;
    }

    /**
      * Checks whether the 'street_number' field has been set.
      * @return True if the 'street_number' field has been set, false otherwise.
      */
    public boolean hasStreetNumber() {
      return fieldSetFlags()[0];
    }


    /**
      * Clears the value of the 'street_number' field.
      * @return This builder.
      */
    public google.example.AddressRecord.Builder clearStreetNumber() {
      fieldSetFlags()[0] = false;
      return this;
    }

    /**
      * Gets the value of the 'street_name' field.
      * @return The value.
      */
    public java.lang.CharSequence getStreetName() {
      return street_name;
    }


    /**
      * Sets the value of the 'street_name' field.
      * @param value The value of 'street_name'.
      * @return This builder.
      */
    public google.example.AddressRecord.Builder setStreetName(java.lang.CharSequence value) {
      validate(fields()[1], value);
      this.street_name = value;
      fieldSetFlags()[1] = true;
      return this;
    }

    /**
      * Checks whether the 'street_name' field has been set.
      * @return True if the 'street_name' field has been set, false otherwise.
      */
    public boolean hasStreetName() {
      return fieldSetFlags()[1];
    }


    /**
      * Clears the value of the 'street_name' field.
      * @return This builder.
      */
    public google.example.AddressRecord.Builder clearStreetName() {
      street_name = null;
      fieldSetFlags()[1] = false;
      return this;
    }

    /**
      * Gets the value of the 'city' field.
      * @return The value.
      */
    public java.lang.CharSequence getCity() {
      return city;
    }


    /**
      * Sets the value of the 'city' field.
      * @param value The value of 'city'.
      * @return This builder.
      */
    public google.example.AddressRecord.Builder setCity(java.lang.CharSequence value) {
      validate(fields()[2], value);
      this.city = value;
      fieldSetFlags()[2] = true;
      return this;
    }

    /**
      * Checks whether the 'city' field has been set.
      * @return True if the 'city' field has been set, false otherwise.
      */
    public boolean hasCity() {
      return fieldSetFlags()[2];
    }


    /**
      * Clears the value of the 'city' field.
      * @return This builder.
      */
    public google.example.AddressRecord.Builder clearCity() {
      city = null;
      fieldSetFlags()[2] = false;
      return this;
    }

    /**
      * Gets the value of the 'state' field.
      * @return The value.
      */
    public java.lang.CharSequence getState() {
      return state;
    }


    /**
      * Sets the value of the 'state' field.
      * @param value The value of 'state'.
      * @return This builder.
      */
    public google.example.AddressRecord.Builder setState(java.lang.CharSequence value) {
      validate(fields()[3], value);
      this.state = value;
      fieldSetFlags()[3] = true;
      return this;
    }

    /**
      * Checks whether the 'state' field has been set.
      * @return True if the 'state' field has been set, false otherwise.
      */
    public boolean hasState() {
      return fieldSetFlags()[3];
    }


    /**
      * Clears the value of the 'state' field.
      * @return This builder.
      */
    public google.example.AddressRecord.Builder clearState() {
      state = null;
      fieldSetFlags()[3] = false;
      return this;
    }

    /**
      * Gets the value of the 'country' field.
      * @return The value.
      */
    public java.lang.CharSequence getCountry() {
      return country;
    }


    /**
      * Sets the value of the 'country' field.
      * @param value The value of 'country'.
      * @return This builder.
      */
    public google.example.AddressRecord.Builder setCountry(java.lang.CharSequence value) {
      validate(fields()[4], value);
      this.country = value;
      fieldSetFlags()[4] = true;
      return this;
    }

    /**
      * Checks whether the 'country' field has been set.
      * @return True if the 'country' field has been set, false otherwise.
      */
    public boolean hasCountry() {
      return fieldSetFlags()[4];
    }


    /**
      * Clears the value of the 'country' field.
      * @return This builder.
      */
    public google.example.AddressRecord.Builder clearCountry() {
      country = null;
      fieldSetFlags()[4] = false;
      return this;
    }

    /**
      * Gets the value of the 'zip' field.
      * @return The value.
      */
    public java.lang.CharSequence getZip() {
      return zip;
    }


    /**
      * Sets the value of the 'zip' field.
      * @param value The value of 'zip'.
      * @return This builder.
      */
    public google.example.AddressRecord.Builder setZip(java.lang.CharSequence value) {
      validate(fields()[5], value);
      this.zip = value;
      fieldSetFlags()[5] = true;
      return this;
    }

    /**
      * Checks whether the 'zip' field has been set.
      * @return True if the 'zip' field has been set, false otherwise.
      */
    public boolean hasZip() {
      return fieldSetFlags()[5];
    }


    /**
      * Clears the value of the 'zip' field.
      * @return This builder.
      */
    public google.example.AddressRecord.Builder clearZip() {
      zip = null;
      fieldSetFlags()[5] = false;
      return this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public AddressRecord build() {
      try {
        AddressRecord record = new AddressRecord();
        record.street_number = fieldSetFlags()[0] ? this.street_number : (java.lang.Integer) defaultValue(fields()[0]);
        record.street_name = fieldSetFlags()[1] ? this.street_name : (java.lang.CharSequence) defaultValue(fields()[1]);
        record.city = fieldSetFlags()[2] ? this.city : (java.lang.CharSequence) defaultValue(fields()[2]);
        record.state = fieldSetFlags()[3] ? this.state : (java.lang.CharSequence) defaultValue(fields()[3]);
        record.country = fieldSetFlags()[4] ? this.country : (java.lang.CharSequence) defaultValue(fields()[4]);
        record.zip = fieldSetFlags()[5] ? this.zip : (java.lang.CharSequence) defaultValue(fields()[5]);
        return record;
      } catch (org.apache.avro.AvroMissingFieldException e) {
        throw e;
      } catch (java.lang.Exception e) {
        throw new org.apache.avro.AvroRuntimeException(e);
      }
    }
  }

  @SuppressWarnings("unchecked")
  private static final org.apache.avro.io.DatumWriter<AddressRecord>
    WRITER$ = (org.apache.avro.io.DatumWriter<AddressRecord>)MODEL$.createDatumWriter(SCHEMA$);

  @Override public void writeExternal(java.io.ObjectOutput out)
    throws java.io.IOException {
    WRITER$.write(this, SpecificData.getEncoder(out));
  }

  @SuppressWarnings("unchecked")
  private static final org.apache.avro.io.DatumReader<AddressRecord>
    READER$ = (org.apache.avro.io.DatumReader<AddressRecord>)MODEL$.createDatumReader(SCHEMA$);

  @Override public void readExternal(java.io.ObjectInput in)
    throws java.io.IOException {
    READER$.read(this, SpecificData.getDecoder(in));
  }

  @Override protected boolean hasCustomCoders() { return true; }

  @Override public void customEncode(org.apache.avro.io.Encoder out)
    throws java.io.IOException
  {
    out.writeInt(this.street_number);

    out.writeString(this.street_name);

    out.writeString(this.city);

    out.writeString(this.state);

    out.writeString(this.country);

    out.writeString(this.zip);

  }

  @Override public void customDecode(org.apache.avro.io.ResolvingDecoder in)
    throws java.io.IOException
  {
    org.apache.avro.Schema.Field[] fieldOrder = in.readFieldOrderIfDiff();
    if (fieldOrder == null) {
      this.street_number = in.readInt();

      this.street_name = in.readString(this.street_name instanceof Utf8 ? (Utf8)this.street_name : null);

      this.city = in.readString(this.city instanceof Utf8 ? (Utf8)this.city : null);

      this.state = in.readString(this.state instanceof Utf8 ? (Utf8)this.state : null);

      this.country = in.readString(this.country instanceof Utf8 ? (Utf8)this.country : null);

      this.zip = in.readString(this.zip instanceof Utf8 ? (Utf8)this.zip : null);

    } else {
      for (int i = 0; i < 6; i++) {
        switch (fieldOrder[i].pos()) {
        case 0:
          this.street_number = in.readInt();
          break;

        case 1:
          this.street_name = in.readString(this.street_name instanceof Utf8 ? (Utf8)this.street_name : null);
          break;

        case 2:
          this.city = in.readString(this.city instanceof Utf8 ? (Utf8)this.city : null);
          break;

        case 3:
          this.state = in.readString(this.state instanceof Utf8 ? (Utf8)this.state : null);
          break;

        case 4:
          this.country = in.readString(this.country instanceof Utf8 ? (Utf8)this.country : null);
          break;

        case 5:
          this.zip = in.readString(this.zip instanceof Utf8 ? (Utf8)this.zip : null);
          break;

        default:
          throw new java.io.IOException("Corrupt ResolvingDecoder.");
        }
      }
    }
  }
}










