package io.advantageous.qbit.meta.params;

public class URINamedParam extends NamedParam{


    private final int indexIntoURI;

    public URINamedParam(final boolean required, String name,
                         final Object defaultValue, int indexIntoURI){
        super(required, name, defaultValue, ParamType.PATH_BY_NAME);
        this.indexIntoURI = indexIntoURI;
    }

    public int getIndexIntoURI() {
        return indexIntoURI;
    }

}
