package io.advantageous.qbit.example;


import io.advantageous.qbit.annotation.QueueCallback;
import io.advantageous.qbit.annotation.QueueCallbackType;
import io.advantageous.qbit.annotation.Service;
import io.advantageous.qbit.service.Callback;
import io.advantageous.boon.Liio.advantageous.boonort org.boon.cache.SimpleCache;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import static io.advantageous.qbit.service.ServiceProxyUtils.flushServiceProxy;

@Service
public class RecommendationService {


    private final SimpleCache<String, User> users =
            new SimpleCache<>(10_000);


    /* Blocking version. */
//    public List<Recommendation> recommend(final String userName) {
//        System.out.println("recommend called");
//        User user = users.get(userName);
//        if (user == null) {
//            user = loadUser(userName);
//        }
//        return runRulesEngineAgainstUser(user);
//    }

//    private User loadUser(String userName) {
//        return new User("bob");
//    }


    private BlockingQueue<Runnable> callbacks = new ArrayBlockingQueue<Runnable>(10_000);

//    @Autowired

    private UserDataServiceClient userDataService;


    //    @Autowired
    public RecommendationService(UserDataServiceClient userDataService) {
        this.userDataService = userDataService;
    }


    public void recommend(final Callback<List<Recommendation>> recommendationsCallback,
                          final String userName) {


        System.out.println("recommend called");

        User user = users.get(userName);

        if (user == null) {
            userDataService.loadUser(
                    loadedUser -> {

                        System.out.println("GOT CALL BACK FROM SERVICE");
                        handleLoadFromUserDataService(loadedUser, recommendationsCallback);

                    }, userName);

            /* Load user using Callback. */
//            userDataService.loadUser(new Callback<User>() {
//                @Override
//                public void accept(final User loadedUser) {
//                        handleLoadFromUserDataService(loadedUser,
//                                recommendationsCallback);
//                }
//            }, userName);

        } else {
            recommendationsCallback.accept(runRulesEngineAgainstUser(user));
        }

    }

    /**
     * Handle defered recommendations based on user loads.
     */
    private void handleLoadFromUserDataService(final User loadedUser,
                                               final Callback<List<Recommendation>> recommendationsCallback) {

        /** Add a runnable to the callbacks list. */
        callbacks.add(() -> {
            List<Recommendation> recommendations = runRulesEngineAgainstUser(loadedUser);
            recommendationsCallback.accept(recommendations);
        });

//        callbacks.add(new Runnable() {
//            @Override
//            public void run() {
//                List<Recommendation> recommendations = runRulesEngineAgainstUser(loadedUser);
//                recommendationsCallback.accept(recommendations);
//            }
//        });
    }


    @QueueCallback({
            QueueCallbackType.EMPTY,
            QueueCallbackType.START_BATCH,
            QueueCallbackType.LIMIT})
    private void handleCallbacks() {

        flushServiceProxy(userDataService);
        Runnable runnable = callbacks.poll();

        while (runnable != null) {
            runnable.run();
            runnable = callbacks.poll();
        }
    }

    private List<Recommendation> runRulesEngineAgainstUser(final User user) {

        return Lists.list(new Recommendation("Take a walk"), new Recommendation("Read a book"),
                new Recommendation("Love more, complain less"));
    }


}
