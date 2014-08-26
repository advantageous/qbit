package org.qbit.service;
import java.util.List;

public interface MethodCall extends Request <List<Object>> {

    String name();

    long timestamp();
}
