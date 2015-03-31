package io.advantageous.qbit.meta.params;

public class URIPositionalParam extends PositionalParam {

    private int indexIntoURI;

    public URIPositionalParam(final boolean required, final int position,
                              final Object defaultValue, int indexIntoURI){
        super(required, position, defaultValue, ParamType.PATH_BY_POSITION);
        this.indexIntoURI = indexIntoURI;
    }

    public int getIndexIntoURI() {
        return indexIntoURI;
    }
}
