package io.advantageous.qbit.service.impl;

import io.advantageous.qbit.service.ServiceContext;
import io.advantageous.qbit.service.ServiceQueue;
import io.advantageous.qbit.service.health.ServiceHealthManager;
import io.advantageous.reakt.Expected;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static io.advantageous.reakt.Expected.empty;
import static io.advantageous.reakt.Expected.ofNullable;

public class ServiceHealthManagerDefault implements ServiceHealthManager {


    final Logger logger = LoggerFactory.getLogger(ServiceHealthManagerDefault.class);
    private final Expected<Runnable> failCallback;
    private final Expected<Runnable> recoverCallback;
    private Expected<ServiceQueue> serviceQueue = empty();
    private Expected<ServiceContext> serviceContext = empty();

    public ServiceHealthManagerDefault(final Runnable failCallback,
                                       final Runnable recoverCallback) {
        this.failCallback = Expected.ofNullable(failCallback);
        this.recoverCallback = Expected.ofNullable(recoverCallback);
    }


    @Override
    public boolean isFailing() {

        serviceQueue.ifEmpty(this::loadIfEmpty);

        if (serviceQueue.isPresent()) {
            return serviceQueue.get().failing();
        } else {
            logger.warn("Service Queue was not found, but isFailing() was called on ServiceHealthManager");
            return false;
        }

    }

    private void loadIfEmpty() {
        serviceContext.ifEmpty(() -> {
            serviceContext = ofNullable(ServiceContext.serviceContext());
            serviceContext.ifPresent(serviceContext1 -> serviceQueue =
                    ofNullable(serviceContext1.currentService()));

        });
    }

    @Override
    public boolean isOk() {

        serviceQueue.ifEmpty(this::loadIfEmpty);

        if (serviceQueue.isPresent()) {
            return !serviceQueue.get().failing();
        } else {
            logger.warn("Service Queue was not found, but isOk() was called on ServiceHealthManager");
            return true;
        }
    }

    @Override
    public void setFailing() {

        serviceQueue.ifEmpty(this::loadIfEmpty);


        if (serviceQueue.isPresent()) {
            serviceQueue.get().setFailing();
        } else {
            logger.warn("Service Queue was not found, but setFailing() was called on ServiceHealthManager");
        }
        failCallback.ifPresent(Runnable::run);
    }

    @Override
    public void recover() {

        serviceQueue.ifEmpty(this::loadIfEmpty);

        if (serviceQueue.isPresent()) {
            serviceQueue.get().recover();
        } else {
            logger.warn("Service Queue was not found, but recover() was called on ServiceHealthManager");
        }
        recoverCallback.ifPresent(Runnable::run);

    }
}
