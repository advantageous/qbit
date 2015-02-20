package io.advantageous.qbit.example;


import io.advantageous.qbit.annotation.QueueCallback;
import io.advantageous.qbit.annotation.QueueCallbackType;
import io.advantageous.qbit.annotation.Service;
import io.advantageous.qbit.service.Callback;
import io.advantageous.qbit.service.ServiceProxyUtils;
import org.boon.Lists;
import org.boon.cache.SimpleCache;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

@Service
public class RecommendationService {


    private final SimpleCache<String, User> users =
            new SimpleCache<>(10_000);

    private List<Runnable> callbacks = new ArrayList<>(10_000);

    @Autowired
    private UserDataServiceClient userDataService;



    @Autowired
    public RecommendationService(UserDataServiceClient userDataService) {
        this.userDataService = userDataService;
    }


    public void recommend(final Callback<List<Recommendation>> recommendationsCallback,
                          final String userName) {


        System.out.println("recommend called");
        handleCallbacks();

        User user = users.get(userName);

        if (user == null) {

            // Shortcut for testing... callbacks.add(() -> recommendationsCallback.accept(runRulesEngineAgainstUser(new User("Bobby"))));

            userDataService.loadUser(
                    loadedUser -> {

                        System.out.println("GOT CALL BACK FROM SERVICE");
                        handleLoadFromUserDataService(loadedUser, recommendationsCallback);

                    }, userName);
        } else {
            recommendationsCallback.accept(runRulesEngineAgainstUser(user));
        }

    }

    /** Handle defered recommendations based on user loads. */
    private void handleLoadFromUserDataService(final User loadedUser,
                                               final Callback<List<Recommendation>> recommendationsCallback) {

        /** Add a runnable to the callbacks list. */
        callbacks.add(() -> {
            List<Recommendation> recommendations = runRulesEngineAgainstUser(loadedUser);
            recommendationsCallback.accept(recommendations);
        });
    }


    @QueueCallback(QueueCallbackType.EMPTY)
    private void emptyQueue() {

        handleCallbacks();
    }

    private void handleCallbacks() {

        ServiceProxyUtils.flushServiceProxy(userDataService);
        if (callbacks.size() > 0) {
            callbacks.forEach(Runnable::run);
            callbacks.clear();
        }
    }


    @QueueCallback(QueueCallbackType.LIMIT)
    private void queueLimit() {


        handleCallbacks();
    }


    private List<Recommendation> runRulesEngineAgainstUser(final User user) {

        return Lists.list(new Recommendation("Take a walk"), new Recommendation("Read a book"),
                new Recommendation("Love more, complain less"));
    }


}
