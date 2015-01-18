package io.advantageous.qbit.bindings;


/**
 *
 *
 * @author Rick Hightower
 *
 * Created by Richard on 7/22/14.
 */
public class ArgParamBinding {

    final int methodParamPosition;
    final int uriPosition;
    final String methodParamName;

    public ArgParamBinding(int methodParamPosition, int uriPosition, String methodParamName) {
        this.methodParamPosition = methodParamPosition;
        this.uriPosition = uriPosition;
        this.methodParamName = methodParamName;
    }

    public String getMethodParamName() {
        return methodParamName;
    }

    public int getUriPosition() {
        return uriPosition;
    }

    public int getMethodParamPosition() {
        return methodParamPosition;
    }

    @Override
    public String toString() {
        return "ArgParamBinding{" +
                "methodParamPosition=" + methodParamPosition +
                ", uriPosition=" + uriPosition +
                ", methodParamName='" + methodParamName + '\'' +
                '}';
    }
}
