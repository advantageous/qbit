package io.advantageous.qbit.meta.provider;

import io.advantageous.qbit.meta.RequestMetaData;

public interface MetaDataProvider {


    RequestMetaData get(String path);


}
