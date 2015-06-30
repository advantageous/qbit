package io.advantageous.qbit.meta;

import java.util.List;
import java.util.Map;

public class Operation {



    private final String operationId;
    private final String summary;
    private final String description;
    private final List<String> tags;
    private final ExternalDocumentation externalDocs;
    private final List<String> consumes;
    private final List<String> produces;
    private final Map<Integer, Response> responses;

    public Operation(String operationId, String summary, String description, List<String> tags, ExternalDocumentation externalDocs, List<String> consumes, List<String> produces, Map<Integer, Response> responses) {
        this.operationId = operationId;
        this.summary = summary;
        this.description = description;
        this.tags = tags;
        this.externalDocs = externalDocs;
        this.consumes = consumes;
        this.produces = produces;
        this.responses = responses;
    }
}
