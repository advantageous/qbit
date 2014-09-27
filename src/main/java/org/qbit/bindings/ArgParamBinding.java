package org.qbit.bindings;

/**
 * Created by Richard on 7/22/14.
 */
public class ArgParamBinding {

    int methodParamPosition;
    int uriPosition;
    String methodParamName;

    public ArgParamBinding(int methodParamPosition, int uriPosition, String methodParamName) {
        this.methodParamPosition = methodParamPosition;
        this.uriPosition = uriPosition;
        this.methodParamName = methodParamName;
    }

    public String getMethodParamName() {
        return methodParamName;
    }

    public void setMethodParamName(String methodParamName) {
        this.methodParamName = methodParamName;
    }

    public int getUriPosition() {
        return uriPosition;
    }

    public void setUriPosition(int uriPosition) {
        this.uriPosition = uriPosition;
    }

    public int getMethodParamPosition() {
        return methodParamPosition;
    }

    public void setMethodParamPosition(int methodParamPosition) {
        this.methodParamPosition = methodParamPosition;
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
