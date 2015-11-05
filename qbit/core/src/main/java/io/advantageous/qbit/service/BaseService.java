package io.advantageous.qbit.service;

import io.advantageous.qbit.queue.QueueCallBackHandler;
import io.advantageous.qbit.reactive.Reactor;
import io.advantageous.qbit.service.stats.StatsCollector;
import io.advantageous.qbit.util.Timer;

public abstract class BaseService implements QueueCallBackHandler{


    protected final StatsCollector statsCollector;
    protected final Reactor reactor;
    protected final Timer timer;
    protected long time;

    public BaseService(Reactor reactor, Timer timer, final StatsCollector statsCollector) {
        this.statsCollector = statsCollector;
        this.reactor = reactor;
        this.timer = timer;
    }

    @Override
    public void queueLimit() {
        doProcess();
    }

    @Override
    public void queueEmpty() {
        doProcess();
    }

    @Override
    public void queueIdle() {
        doProcess();
    }


    private void doProcess() {
        time = timer.time();
        reactor.process();
        process();
    }

    protected  void process() {

    }

}
