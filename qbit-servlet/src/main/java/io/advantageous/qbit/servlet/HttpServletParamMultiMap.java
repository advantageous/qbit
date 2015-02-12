package io.advantageous.qbit.servlet;

import io.advantageous.qbit.util.MultiMap;
import org.boon.Lists;
import org.boon.Sets;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

/**
 * Created by rhightower on 2/12/15.
 */
public class HttpServletParamMultiMap implements MultiMap<String, String> {
    private final HttpServletRequest request;

    private final Map<String, String[]> parameterMap;

    public HttpServletParamMultiMap(HttpServletRequest request) {
        this.request = request;
        parameterMap = request.getParameterMap();
    }


    @Override
    public String getFirst(String key) {
        return request.getParameter(key);
    }

    @Override
    public Iterable<String> getAll(String key) {
        return Lists.list(request.getParameterValues(key));
    }

    @Override
    public Iterable<String> keySetMulti() {
        return Lists.list(request.getParameterNames());
    }

    @Override
    public String getSingleObject(String name) {
        return getFirst(name);
    }


    @Override
    public int size() {
        return parameterMap.size();
    }

    @Override
    public boolean isEmpty() {
        return  Lists.list(request.getParameterNames()).size()==0;
    }

    @Override
    public boolean containsKey(Object key) {
        return parameterMap.containsKey(key);
    }

    @Override
    public String get(Object key) {
        return getFirst(key.toString());
    }

    @Override
    public Set<String> keySet() {
        return parameterMap.keySet();
    }

    @Override
    public Iterator<Entry<String, Collection<String>>> iterator() {

        final Iterator<Entry<String, String[]>> iterator = parameterMap.entrySet().iterator();

        return new Iterator<Entry<String, Collection<String>>>() {
            @Override
            public boolean hasNext() {
                return iterator.hasNext();
            }

            @Override
            public Entry<String, Collection<String>> next() {
                final Entry<String, String[]> entry = iterator.next();
                return new Entry<String, Collection<String>>() {
                    @Override
                    public String getKey() {
                        return entry.getKey();
                    }

                    @Override
                    public Collection<String> getValue() {
                        return Arrays.asList(entry.getValue());
                    }

                    @Override
                    public Collection<String> setValue(Collection<String> value) {
                        return null;
                    }
                };
            }
        };
    }

}
