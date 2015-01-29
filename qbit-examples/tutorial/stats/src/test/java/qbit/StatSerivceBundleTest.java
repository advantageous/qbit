package qbit;

import io.advantageous.qbit.queue.QueueBuilder;
import io.advantageous.qbit.service.ServiceBundle;
import io.advantageous.qbit.service.ServiceBundleBuilder;
import org.boon.core.Sys;
import org.boon.primitive.Arry;
import org.boon.primitive.Int;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import qbit.support.*;

import static org.boon.Boon.puts;
import static org.boon.Exceptions.die;

/**
 * Created by rhightower on 1/28/15.
 */
public class StatSerivceBundleTest {


    boolean ok;
    StatServiceClientInterface statServiceClient;
    StatService statService;
    DebugRecorder recorder;
    DebugReplicator replicator;
    ServiceBundle serviceBundle;

    protected static Object context = Sys.contextToHold();

    @Before
    public void setUp() throws Exception {

        recorder = new DebugRecorder();
        replicator = new DebugReplicator();
        statService = new StatServiceBuilder().setRecorder(recorder).setReplicator(replicator).build();

        QueueBuilder builder = new QueueBuilder().setPollWait(1).setBatchSize(200).setLinkTransferQueue().setCheckEvery(15).setTryTransfer(true);
        //QueueBuilder builder = new QueueBuilder().setPollWait(10).setBatchSize(2000).setArrayBlockingQueue().setSize(100);

        serviceBundle = new ServiceBundleBuilder().setEachServiceInItsOwnThread(true).setQueueBuilder(builder).setInvokeDynamic(false)
                .build();
        serviceBundle.addService(statService);
        serviceBundle.startReturnHandlerProcessor();
        statServiceClient = serviceBundle.createLocalProxy(StatServiceClientInterface.class, "statService");
    }

    @After
    public void tearDown() throws Exception {

        serviceBundle.stop();

    }

    @Test
    public void testRecord() throws Exception {

        statServiceClient.record("mystat", 1);
        serviceBundle.flush();
        Sys.sleep(100);

        ok = replicator.count == 1 || die();

    }

    @Test
    public void testRecordAll() throws Exception {


        String[] names = Arry.array("stat1", "stat2");
        int[] counts = Int.array(1, 2);

        statServiceClient.recordAll(names, counts);
        serviceBundle.flush();
        Sys.sleep(100);

        ok = replicator.count == 3 || die(replicator.count);


    }


    @Test
    public void testRecord1Thousand() throws Exception {
        for (int index=0; index< 1_000; index++) {
            statServiceClient.record("mystat", 1);

        }
        serviceBundle.flush();
        Sys.sleep(400);

        ok = replicator.count == 1000 || die(replicator.count);

    }


    @Test
    public void testRecord4Thousand() throws Exception {
        for (int index=0; index< 4_000; index++) {
            statServiceClient.record("mystat", 1);

        }
        serviceBundle.flush();
        Sys.sleep(100);

        ok = replicator.count == 4000 || die(replicator.count);

    }


    @Test
    public void testRecord100Thousand() throws Exception {
        for (int index=0; index< 100_000; index++) {
            statServiceClient.record("mystat", 1);

        }
        serviceBundle.flush();
        Sys.sleep(100);

        ok = replicator.count == 100_000 || die(replicator.count);

    }


    @Test
    public void testRecord16MillionThreeTimes() throws Exception {

        testRecord16Million();
        Sys.sleep(1000);
        replicator.count=0;
        testRecord16Million();
        Sys.sleep(1000);
        replicator.count=0;
        testRecord16Million();

    }

    @Test
    public void testRecord16Million() throws Exception {

        Sys.sleep(200);

        final long start = System.currentTimeMillis();

        for (int index=0; index< 16_000_000; index++) {
            statServiceClient.record("mystat", 1);

        }
        serviceBundle.flush();

        for (int index = 0; index < 10; index++) {
            Sys.sleep(100);
            puts(replicator.count);

        }
        ok = replicator.count == 16_000_000 || die(replicator.count);


        final long end = System.currentTimeMillis();


        puts(replicator.count, end-start);


    }
}
