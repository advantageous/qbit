package io.advantageous.qbit.example;


import io.advantageous.qbit.annotation.QueueCallback;
import io.advantageous.qbit.annotation.QueueCallbackType;
import io.advantageous.qbit.annotation.Service;
import io.advantageous.qbit.service.Callback;
import org.boon.core.Sys;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Service
public class UserDataService {


    private final List<Runnable> userLoadCallBacks = new ArrayList<>(1_000);

    public void loadUser(final Callback<User> callBack, final String userId) {
        userLoadCallBacks.add(() -> callBack.accept(new User(userId)));
    }

    @QueueCallback(QueueCallbackType.EMPTY)
    private void emptyQueue() {

        pretendToDoIO();
    }


    @QueueCallback(QueueCallbackType.LIMIT)
    private void queueLimit() {

        pretendToDoIO();
    }

    public void pretendToDoIO() {
        Sys.sleep(100);

        userLoadCallBacks.forEach(Runnable::run);

        userLoadCallBacks.clear();

    }


    //private final Map<String, User> map = new HashMap<>(10_000);


}
