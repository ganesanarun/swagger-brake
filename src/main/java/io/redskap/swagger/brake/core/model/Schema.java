package io.redskap.swagger.brake.core.model;

import static java.lang.String.format;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static org.apache.commons.collections4.CollectionUtils.isEmpty;

import java.util.*;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

@Getter
@EqualsAndHashCode
@ToString
@RequiredArgsConstructor
public class Schema {
    private final String type;
    private final Collection<String> enumValues;
    private final Collection<SchemaAttribute> schemaAttributes;
    private final Schema schema;

    public Optional<Schema> getSchema() {
        return Optional.ofNullable(schema);
    }

    public Collection<String> getEnumValues() {
        if (schema != null) {
            return schema.getEnumValues();
        }
        return enumValues;
    }

    public Map<String, String> getTypes() {
        Collection<SchemaAttribute> schemaAttrs = schemaAttributes;
        if (CollectionUtils.isEmpty(schemaAttrs)) {
            schemaAttrs = Optional.ofNullable(schema).map(Schema::getSchemaAttributes).orElse(Collections.emptyList());
        }
        Map<String, String> types = internalGetTypes(schemaAttrs, "");
        types.put("", type);
        return types;
    }

    private Map<String, String> internalGetTypes(Collection<SchemaAttribute> schemaAttributes, String levelName) {
        Map<String, String> result = schemaAttributes
            .stream()
            .filter(a -> a.getSchema() != null)
            .collect(toMap(a -> generateLeveledName(a.getName(), levelName), a -> a.getSchema().getType()));
        for (SchemaAttribute schemaAttribute : schemaAttributes) {
            Schema childSchema = schemaAttribute.getSchema();
            if (childSchema != null) {
                Collection<SchemaAttribute> childSchemaAttributes = childSchema.getSchemaAttributes();
                if (isEmpty(childSchemaAttributes)) {
                    childSchemaAttributes = childSchema.getSchema().map(Schema::getSchemaAttributes).orElse(Collections.emptyList());
                }
                result.putAll(internalGetTypes(childSchemaAttributes, levelName));
            }
        }
        return result;
    }

    public Collection<String> getAttributeNames() {
        Collection<SchemaAttribute> schemaAttrs = schemaAttributes;
        if (CollectionUtils.isEmpty(schemaAttrs)) {
            schemaAttrs = Optional.ofNullable(schema).map(Schema::getSchemaAttributes).orElse(Collections.emptyList());
        }
        return internalGetAttributeNames(schemaAttrs, "");
    }

    private Collection<String> internalGetAttributeNames(Collection<SchemaAttribute> schemaAttributes, String levelName) {
        List<String> result = schemaAttributes.stream().map(SchemaAttribute::getName).map(name -> generateLeveledName(name, levelName)).collect(toList());
        for (SchemaAttribute schemaAttribute : schemaAttributes) {
            Schema childSchema = schemaAttribute.getSchema();
            if (childSchema != null) {
                Collection<SchemaAttribute> childSchemaAttributes = childSchema.getSchemaAttributes();
                if (isEmpty(childSchemaAttributes)) {
                    childSchemaAttributes = childSchema.getSchema().map(Schema::getSchemaAttributes).orElse(Collections.emptyList());
                }
                result.addAll(internalGetAttributeNames(childSchemaAttributes, generateLeveledName(schemaAttribute.getName(), levelName)));
            }
        }
        return result;
    }

    private String generateLeveledName(String name, String levelName) {
        if (!StringUtils.isBlank(levelName)) {
            return format("%s.%s", levelName, name);
        }
        return name;
    }

    public static class Builder {
        private String type;
        private Collection<String> enumValues;
        private Collection<SchemaAttribute> schemaAttributes;
        private Schema schema;

        public Builder(String type) {
            this.type = type;
        }

        public Builder enumValues(Collection<String> enumValues) {
            this.enumValues = enumValues;
            return this;
        }

        public Builder schemaAttributes(Collection<SchemaAttribute> schemaAttributes) {
            this.schemaAttributes = schemaAttributes;
            return this;
        }

        public Builder schema(Schema schema) {
            this.schema = schema;
            return this;
        }

        public Schema build() {
            Collection<SchemaAttribute> attributes = Collections.emptyList();
            if (schemaAttributes != null) {
                attributes = schemaAttributes;
            }
            Collection<String> enValues = Collections.emptyList();
            if (enumValues != null) {
                enValues = enumValues;
            }
            return new Schema(type, enValues, attributes, schema);
        }
    }
}