package io.advantageous.qbit.meta.swagger.builders;

import io.advantageous.qbit.meta.swagger.ExternalDocumentation;
import io.advantageous.qbit.meta.swagger.Operation;
import io.advantageous.qbit.meta.swagger.Parameter;
import io.advantageous.qbit.meta.swagger.Response;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class OperationBuilder {

    private String operationId;
    private String summary;
    private String description;
    private List<String> tags;
    private ExternalDocumentation externalDocs;
    private List<String> consumes;
    private List<String> produces;
    private Map<Integer, Response> responses;
    private List<Parameter> parameters;

    public List<Parameter> getParameters() {
        if (parameters == null) {
            parameters = new ArrayList<>();
        }
        return parameters;
    }

    public OperationBuilder setParameters(List<Parameter> parameters) {
        this.parameters = parameters;
        return this;
    }

    public OperationBuilder addParameter(final Parameter parameter) {
        this.getParameters().add(parameter);
        return this;
    }

    public String getOperationId() {
        return operationId;
    }

    public OperationBuilder setOperationId(String operationId) {
        this.operationId = operationId;
        return this;
    }

    public String getSummary() {
        return summary;
    }

    public OperationBuilder setSummary(String summary) {
        this.summary = summary;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public OperationBuilder setDescription(String description) {
        this.description = description;
        return this;
    }

    public List<String> getTags() {
        return tags;
    }

    public OperationBuilder setTags(List<String> tags) {
        this.tags = tags;
        return this;
    }

    public ExternalDocumentation getExternalDocs() {
        return externalDocs;
    }

    public OperationBuilder setExternalDocs(ExternalDocumentation externalDocs) {
        this.externalDocs = externalDocs;
        return this;
    }

    public List<String> getConsumes() {
        if (consumes == null) {
            consumes = new ArrayList<>();
        }
        return consumes;
    }

    public OperationBuilder setConsumes(List<String> consumes) {
        this.consumes = consumes;
        return this;
    }

    public List<String> getProduces() {
        if (produces == null) {
            produces = new ArrayList<>();
        }
        return produces;
    }

    public OperationBuilder setProduces(List<String> produces) {
        this.produces = produces;
        return this;
    }

    public Map<Integer, Response> getResponses() {
        if (responses == null) {
            responses = new LinkedHashMap<>();
        }
        return responses;
    }

    public OperationBuilder setResponses(Map<Integer, Response> responses) {
        this.responses = responses;
        return this;
    }

    public Operation build() {
        return new Operation(getOperationId(), getSummary(),
                getDescription(), getTags(), getExternalDocs(), getConsumes(),
                getProduces(), getResponses(), getParameters());
    }
}
