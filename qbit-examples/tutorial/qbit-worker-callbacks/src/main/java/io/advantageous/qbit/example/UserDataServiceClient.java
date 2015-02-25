package io.advantageous.qbit.example;

import io.advantageous.qbit.service.Callback;

public interface UserDataServiceClient {

    void loadUser(Callback<User> callBack, String userId);
}
