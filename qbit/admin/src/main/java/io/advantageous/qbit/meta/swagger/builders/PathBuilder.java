package io.advantageous.qbit.meta.swagger.builders;

import io.advantageous.qbit.meta.swagger.Operation;
import io.advantageous.qbit.meta.swagger.Path;

public class PathBuilder {


    private Operation get;
    private Operation put;
    private Operation post;
    private Operation delete;
    private Operation options;
    private Operation head;
    private Operation patch;

    public Operation getGet() {
        return get;
    }

    public PathBuilder setGet(Operation get) {
        this.get = get;
        return this;
    }

    public Operation getPut() {
        return put;
    }

    public PathBuilder setPut(Operation put) {
        this.put = put;
        return this;
    }

    public Operation getPost() {
        return post;
    }

    public PathBuilder setPost(Operation post) {
        this.post = post;
        return this;
    }

    public Operation getDelete() {
        return delete;
    }

    public PathBuilder setDelete(Operation delete) {
        this.delete = delete;
        return this;
    }

    public Operation getOptions() {
        return options;
    }

    public PathBuilder setOptions(Operation options) {
        this.options = options;
        return this;
    }

    public Operation getHead() {
        return head;
    }

    public PathBuilder setHead(Operation head) {
        this.head = head;
        return this;
    }

    public Operation getPatch() {
        return patch;
    }

    public PathBuilder setPatch(Operation patch) {
        this.patch = patch;
        return this;
    }

    public Path build() {
        return new Path(getGet(), getPut(), getPost(), getDelete(),
                getOptions(), getHead(), getPatch());
    }
}
