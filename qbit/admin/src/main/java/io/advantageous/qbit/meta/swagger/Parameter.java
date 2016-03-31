package io.advantageous.qbit.meta.swagger;

import io.advantageous.boon.json.annotations.SerializedName;

public class Parameter {

    private final String name;
    private final String in;
    private final String description;
    private final boolean required;
    private final Schema schema;
    private final String type;
    private final boolean allowEmptyValue;
    private final Schema items;
    /**
     * Determines the format of the array if type array is used. Possible values are:
     * csv - comma separated values foo,bar.
     * ssv - space separated values foo bar.
     * tsv - tab separated values foo\tbar.
     * pipes - pipe separated values foo|bar.
     * multi - corresponds to multiple parameter instances instead of multiple values for a single instance foo=bar&foo=baz. This is valid only for parameters in "query" or "formData".
     * Default value is csv.
     */
    private final String collectionFormat;


    @SerializedName("default")
    private final String defaultValue;

    /*
    maximum	number	See http://json-schema.org/latest/json-schema-validation.html#anchor17.
exclusiveMaximum	boolean	See http://json-schema.org/latest/json-schema-validation.html#anchor17.
minimum	number	See http://json-schema.org/latest/json-schema-validation.html#anchor21.
exclusiveMinimum	boolean	See http://json-schema.org/latest/json-schema-validation.html#anchor21.
maxLength	integer	See http://json-schema.org/latest/json-schema-validation.html#anchor26.
minLength	integer	See http://json-schema.org/latest/json-schema-validation.html#anchor29.
pattern	string	See http://json-schema.org/latest/json-schema-validation.html#anchor33.
maxItems	integer	See http://json-schema.org/latest/json-schema-validation.html#anchor42.
minItems	integer	See http://json-schema.org/latest/json-schema-validation.html#anchor45.
uniqueItems	boolean	See http://json-schema.org/latest/json-schema-validation.html#anchor49.
enum	[*]	See http://json-schema.org/latest/json-schema-validation.html#anchor76.
multipleOf	number	See http://json-schema.org/latest/json-schema-validation.html#anchor14.
     */

    public Parameter(String name, String in, String description, boolean required, Schema schema, String type,
                     boolean allowEmptyValue, Schema items, String collectionFormat, String defaultValue) {
        this.name = name;
        this.in = in;
        this.description = description;
        this.required = required;
        this.schema = schema;
        this.type = type;
        this.allowEmptyValue = allowEmptyValue;
        this.items = items;
        this.collectionFormat = collectionFormat;
        this.defaultValue = defaultValue;
    }

    public String getName() {
        return name;
    }

    public String getIn() {
        return in;
    }

    public String getDescription() {
        return description;
    }

    public boolean isRequired() {
        return required;
    }

    public Schema getSchema() {
        return schema;
    }

    public String getType() {
        return type;
    }

    public boolean isAllowEmptyValue() {
        return allowEmptyValue;
    }

    public Schema getItems() {
        return items;
    }

    public String getCollectionFormat() {
        return collectionFormat;
    }

    public String getDefaultValue() {
        return defaultValue;
    }
}
