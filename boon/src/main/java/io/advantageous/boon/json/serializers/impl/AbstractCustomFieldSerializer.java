package io.advantageous.boon.json.serializers.impl;

import io.advantageous.boon.core.reflection.fields.FieldAccess;
import io.advantageous.boon.json.serializers.CustomFieldSerializer;
import io.advantageous.boon.json.serializers.JsonSerializerInternal;
import io.advantageous.boon.primitive.CharBuf;

/**
 * Custom serializer for a specific field of a specific class.
 * The field is identified by its name, not by its alias (specified by the annotation
 */
public abstract class AbstractCustomFieldSerializer implements CustomFieldSerializer {
	private final Class<?> parentClass;
	private final String fieldName;

	/**
	 * Constructor of the serializer.
	 * @param parentClass Class containing the field to serialize.
	 * @param fieldName Name of the field to serialize.
	 */
	public AbstractCustomFieldSerializer(Class<?> parentClass, String fieldName) {
		super();
		this.fieldName = fieldName;
		this.parentClass = parentClass;
	}

	@Override
	public boolean serializeField(JsonSerializerInternal serializer, Object parent, FieldAccess fieldAccess, CharBuf builder) {
		// It only check parent class equality, doesn't work with inheritance
		if (parentClass == parent.getClass() && fieldName.equals(fieldAccess.name())) {
			Object value = fieldAccess.getValue(parent);
			serialize(serializer, parent, fieldAccess, value, builder);
			return true;
		}
		return false;
	}

	/**
	 * This method has to be overloaded to specify how this field is serialized.<br>
	 * For example:
	 * 
	 * <pre>
	 *
	 * if (value != null) {
	 * 	builder.addJsonFieldName(fieldAccess.name());
	 * 	builder.addQuoted(value.toString());
	 * } else {
	 * 	builder.addJsonFieldName(fieldAccess.name());
	 * 	builder.addQuoted("N/A");
	 *
	 * </pre>
	 * @param serializer JsonSerializer to help serializing dates, arrays...
	 * @param parent Class containing the field.
	 * @param fieldAccess Field access.
	 * @param value Value of the field.
	 * @param builder Json builder.
	 */
	protected abstract void serialize(JsonSerializerInternal serializer, Object parent, FieldAccess fieldAccess, Object value,
			CharBuf builder);

}
