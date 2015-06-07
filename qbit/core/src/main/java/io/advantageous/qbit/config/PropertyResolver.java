package io.advantageous.qbit.config;

import io.advantageous.boon.core.Conversions;

import java.util.Properties;

/**
 * Used for builders that need to be set from system property overrides and such.
 * It is essentially a way to get at properties stored in other property systems.
 */
public interface PropertyResolver {

    static PropertyResolver createPropertiesPropertyResolver(final String prefix,
                                                             final Properties properties) {
        return propertyName -> properties.getProperty(prefix + propertyName);
    }

    static PropertyResolver createSystemPropertyResolver(final String prefix) {
        return createPropertiesPropertyResolver(prefix, System.getProperties());
    }

    Object getProperty(final String propertyName);

    default Integer getIntegerProperty(final String propertyName) {
        final Object property = getProperty(propertyName);

        if (property instanceof Number) {
            return ((Number) property).intValue();
        } else if (property instanceof Enum) {
            return ((Enum) property).ordinal();
        } else if (property instanceof CharSequence) {
            return Integer.valueOf(property.toString());
        } else if (property == null) {
            return null;
        } else {
            throw new IllegalStateException("Unexpected type " + property.getClass().getName());
        }
    }

    default Integer getIntegerProperty(final String propertyName, final int defaultValue) {
        final Integer value = getIntegerProperty(propertyName);
        return value == null ? defaultValue : value;
    }

    default Boolean getBooleanProperty(final String propertyName) {
        final Object property = getProperty(propertyName);

        if (property instanceof Boolean) {
            return ((Boolean) property);
        } else if (property instanceof CharSequence) {
            return Boolean.valueOf(property.toString());
        } else if (property == null) {
            return null;
        } else {
            throw new IllegalStateException("Unexpected type " + property.getClass().getName());
        }
    }

    default Boolean getBooleanProperty(final String propertyName, final boolean defaultValue) {
        final Boolean value = getBooleanProperty(propertyName);
        return value == null ? defaultValue : value;
    }


    default Long getLongProperty(final String propertyName) {
        final Object property = getProperty(propertyName);

        if (property instanceof Number) {
            return ((Number) property).longValue();
        } else if (property instanceof Enum) {
            return (long) ((Enum) property).ordinal();
        } else if (property instanceof CharSequence) {
            return Long.valueOf(property.toString());
        } else if (property == null) {
            return null;
        } else {
            throw new IllegalStateException("Unexpected type " + property.getClass().getName());
        }
    }

    default Long getLongProperty(final String propertyName, final long defaultValue) {
        final Long value = getLongProperty(propertyName);
        return value == null ? defaultValue : value;
    }


    default Double getDoubleProperty(final String propertyName) {
        final Object property = getProperty(propertyName);

        if (property instanceof Number) {
            return ((Number) property).doubleValue();
        } else if (property instanceof CharSequence) {
            return Double.valueOf(property.toString());
        } else if (property == null) {
            return null;
        } else {
            throw new IllegalStateException("Unexpected type " + property.getClass().getName());
        }
    }


    default Double getDoubleProperty(final String propertyName, final double defaultValue) {
        final Double value = getDoubleProperty(propertyName);
        return value == null ? defaultValue : value;
    }


    default Float getFloatProperty(final String propertyName) {
        final Object property = getProperty(propertyName);

        if (property instanceof Number) {
            return ((Number) property).floatValue();
        } else if (property instanceof CharSequence) {
            return Float.valueOf(property.toString());
        } else if (property == null) {
            return null;
        } else {
            throw new IllegalStateException("Unexpected type " + property.getClass().getName());
        }
    }


    default Float getFloatProperty(final String propertyName, final float defaultValue) {
        final Float value = getFloatProperty(propertyName);
        return value == null ? defaultValue : value;
    }


    default String getStringProperty(final String propertyName) {
        final Object property = getProperty(propertyName);

        if (property != null) {
            return property.toString();
        } else {
            return null;
        }
    }


    default String getStringProperty(final String propertyName, final String defaultValue) {
        final String value = getStringProperty(propertyName);
        return value == null ? defaultValue : value;
    }


    default <T> T getGenericProperty(final String propertyName, Class<T> type) {
        final Object value = getProperty(propertyName);
        if (value != null) {
            return Conversions.coerce(type, value);
        } else {
            return null;
        }
    }


    default <T> T getGenericPropertyWithDefault(final String propertyName, final T defaultValue) {
        @SuppressWarnings("unchecked") final T value = getGenericProperty(propertyName, (Class<T>) defaultValue.getClass());
        return value == null ? defaultValue : value;
    }
}
