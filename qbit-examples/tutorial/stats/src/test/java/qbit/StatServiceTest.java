package qbit;

import io.advantageous.qbit.util.Timer;
import org.boon.primitive.Arry;
import org.boon.primitive.Int;
import org.boon.primitive.Lng;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import qbit.support.*;

import static org.boon.Exceptions.die;


public class StatServiceTest {


    boolean ok;
    StatService statService;
    DebugRecorder recorder;
    DebugReplicator replicator;

    @Before
    public void setUp() throws Exception {

        recorder = new DebugRecorder();
        replicator = new DebugReplicator();
        statService = new StatServiceBuilder().setRecorder(recorder).setReplicator(replicator).build();

    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void testRecord() throws Exception {

        statService.record("mystat", 1);

        ok = replicator.count == 1 || die();
        ok = recorder.count == 0 || die();

        statService.process();
        statService.queueLimit();
        statService.queueEmpty();

        ok = recorder.count == 0 || die();


        //Time dilation
        statService.time(System.currentTimeMillis() + 61 * 1000 );
        statService.process();

        ok = recorder.count == 1 || die();

    }

    @Test
    public void testRecordAll() throws Exception {


        String[] names = Arry.array("stat1", "stat2");
        int[] counts = Int.array(1, 2);
        //long[] times = Lng.array(Timer.timer().now(), Timer.timer().now() + 2000);

        statService.recordAll(names, counts);

        ok = replicator.count == 3 || die(replicator.count);
        ok = recorder.count == 0 || die(recorder.count);

        statService.process();
        statService.queueLimit();
        statService.queueEmpty();

        ok = recorder.count == 0 || die(recorder.count);


        //Time dilation
        statService.time(System.currentTimeMillis() + 61 * 1000 );
        statService.process();

        ok = recorder.count == 2 || die(recorder.count);

    }


    @Test
    public void testReplicators() throws Exception {


        statService = new StatServiceBuilder().setRecorder(recorder)
                .addReplicator(replicator)
                .addReplicator(replicator)
                .addReplicator(new NoOpReplicator())
                .build();


        String[] names = Arry.array("stat1", "stat2");
        int[] counts = Int.array(1, 2);
        long[] times = Lng.array(Timer.timer().now(), Timer.timer().now() + 2000);

        statService.recordAllWithTimes(names, counts, times);

        ok = replicator.count == 6 || die(replicator.count);
        ok = recorder.count == 0 || die(recorder.count);

        statService.process();
        statService.queueLimit();
        statService.queueEmpty();

        ok = recorder.count == 0 || die(recorder.count);


        //Time dilation
        statService.time(System.currentTimeMillis() + 61 * 1000 );
        statService.process();

        ok = recorder.count == 2 || die(recorder.count);

        statService = new StatServiceBuilder().setRecorder(new NoOpRecorder())
                .addReplicator(replicator)
                .addReplicator(replicator)
                .addReplicator(new NoOpReplicator())
                .build();


        //Time dilation
        statService.time(System.currentTimeMillis() + 61 * 1000 );
        statService.process();

        ok = recorder.count == 2 || die(recorder.count);


    }
}