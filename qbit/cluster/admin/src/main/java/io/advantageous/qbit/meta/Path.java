package io.advantageous.qbit.meta;

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
}
