package io.advantageous.qbit.meta.swagger.builders;

import io.advantageous.qbit.meta.swagger.Parameter;
import io.advantageous.qbit.meta.swagger.Schema;

public class ParameterBuilder {
    private String name;
    private String in = "body";
    private String description;
    private boolean required;
    private Schema schema;


    private String type;
    private boolean allowEmptyValue;
    private Schema items;
    /**
     * Determines the format of the array if type array is used. Possible values are:
     * csv - comma separated values foo,bar.
     * ssv - space separated values foo bar.
     * tsv - tab separated values foo\tbar.
     * pipes - pipe separated values foo|bar.
     * multi - corresponds to multiple parameter instances instead of multiple values for a single instance foo=bar&foo=baz. This is valid only for parameters in "query" or "formData".
     * Default value is csv.
     */
    private String collectionFormat;


    private String defaultValue;

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


    public String getName() {
        return name;
    }

    public ParameterBuilder setName(String name) {
        this.name = name;
        return this;
    }

    public String getIn() {
        return in;
    }

    public ParameterBuilder setIn(String in) {
        this.in = in;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public ParameterBuilder setDescription(String description) {
        this.description = description;
        return this;
    }

    public boolean isRequired() {
        return required;
    }

    public ParameterBuilder setRequired(boolean required) {
        this.required = required;
        return this;
    }

    public Schema getSchema() {
        return schema;
    }

    public ParameterBuilder setSchema(Schema schema) {
        this.schema = schema;
        return this;
    }

    public String getType() {
        return type;
    }

    public ParameterBuilder setType(String type) {
        this.type = type;
        return this;
    }

    public boolean isAllowEmptyValue() {
        return allowEmptyValue;
    }

    public ParameterBuilder setAllowEmptyValue(boolean allowEmptyValue) {
        this.allowEmptyValue = allowEmptyValue;
        return this;
    }

    public Schema getItems() {
        return items;
    }

    public ParameterBuilder setItems(Schema items) {
        this.items = items;
        return this;
    }

    public String getCollectionFormat() {
        return collectionFormat;
    }

    public ParameterBuilder setCollectionFormat(String collectionFormat) {
        this.collectionFormat = collectionFormat;
        return this;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public ParameterBuilder setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
        return this;
    }

    public Parameter build() {
        return new Parameter(getName(), getIn(), getDescription(), isRequired(), getSchema(), getType(),
                isAllowEmptyValue(), getItems(), getCollectionFormat(), getDefaultValue());
    }
}
