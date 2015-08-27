package io.advantageous.qbit.meta.swagger;

public class Path {
    private final Operation get;
    private final Operation put;
    private final Operation post;
    private final Operation delete;
    private final Operation options;
    private final Operation head;
    private final Operation patch;

    public Path(Operation get, Operation put, Operation post, Operation delete,
                Operation options, Operation head, Operation patch) {
        this.get = get;
        this.put = put;
        this.post = post;
        this.delete = delete;
        this.options = options;
        this.head = head;
        this.patch = patch;
    }

    public Operation getGet() {
        return get;
    }

    public Operation getPut() {
        return put;
    }

    public Operation getPost() {
        return post;
    }

    public Operation getDelete() {
        return delete;
    }

    public Operation getOptions() {
        return options;
    }

    public Operation getHead() {
        return head;
    }

    public Operation getPatch() {
        return patch;
    }
}
