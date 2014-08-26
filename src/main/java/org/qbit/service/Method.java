package org.qbit.service;
import java.util.List;

public interface Method extends Request <List<Object>> {

    String name();

}
