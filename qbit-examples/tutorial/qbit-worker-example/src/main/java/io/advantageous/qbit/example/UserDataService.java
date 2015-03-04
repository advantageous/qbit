package io.advantageous.qbit.example;


import io.advantageous.qbit.annotation.QueueCallback;
import io.advantageous.qbit.annotation.QueueCallbackType;
import io.advantageous.qbit.annotation.Service;
import io.advantageous.qbit.service.Callback;
import io.advantageous.boon.core.Sys;

import java.util.ArrayList;
import java.util.List;

imio.advantageous.boontic org.boon.Boon.puts;


@Service
public class UserDataService {


    private final List<Runnable> userLoadCallBacks = new ArrayList<>(1_000);

    public void loadUser(final Callback<User> callBack, final String userId) {

        puts("UserDataService :: loadUser called", userId);
        userLoadCallBacks.add(
                new Runnable() {
                    @Override
                    public void run() {
                        callBack.accept(new User(userId));
                    }
                });

    }


    @QueueCallback({QueueCallbackType.EMPTY, QueueCallbackType.LIMIT})
    public void pretendToDoIO() {
        Sys.sleep(100);

        if (userLoadCallBacks.size()==0) {
            return;
        }
        for (Runnable runnable : userLoadCallBacks) {
            runnable.run();
        }
        userLoadCallBacks.clear();

    }




}
