/*
 * Copyright 2013-2014 Richard M. Hightower
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  		http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * __________                              _____          __   .__
 * \______   \ ____   ____   ____   /\    /     \ _____  |  | _|__| ____    ____
 *  |    |  _//  _ \ /  _ \ /    \  \/   /  \ /  \\__  \ |  |/ /  |/    \  / ___\
 *  |    |   (  <_> |  <_> )   |  \ /\  /    Y    \/ __ \|    <|  |   |  \/ /_/  >
 *  |______  /\____/ \____/|___|  / \/  \____|__  (____  /__|_ \__|___|  /\___  /
 *         \/                   \/              \/     \/     \/       \//_____/
 *      ____.                     ___________   _____    ______________.___.
 *     |    |____ ___  _______    \_   _____/  /  _  \  /   _____/\__  |   |
 *     |    \__  \\  \/ /\__  \    |    __)_  /  /_\  \ \_____  \  /   |   |
 * /\__|    |/ __ \\   /  / __ \_  |        \/    |    \/        \ \____   |
 * \________(____  /\_/  (____  / /_______  /\____|__  /_______  / / ______|
 *               \/           \/          \/         \/        \/  \/
 */

package io.advantageous.boon;

import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

import static io.advantageous.boon.Boon.sputs;

/**
 * Ported this form EasyJava/Facile. Got rid of the FileObject stuff.
 *
 * @author Rick Hightower
 */
public class Runner {


    private final static String shell;
    private final static String shellArgument;

    static {
        // Windows
        if ( System.getProperty( "os.name" ).toLowerCase().indexOf( "win" ) >= 0 ) {
            shell = "cmd.exe";
            shellArgument = "/C";
        }

        // Everyone else
        else {
            shell = "/bin/sh";
            shellArgument = "-c";
        }
    }

    public static List<Path> path() {

        final String[] paths = StringScanner.splitByDelimiters( System.getenv().get( "PATH" ), ":;" );
        return Lists.mapBy( paths, IO.convertToPathFunction );

    }

    public static int exec( String... args ) {
        ProcessRunner runner = new ProcessRunner( null, null, 0, null, false, args );
        return runner.exec();
    }

    public static int exec( int timeout, String... args ) {
        ProcessRunner runner = new ProcessRunner( null, null, timeout, null, false, args );
        return runner.exec();
    }


    public static String run( int timeout, String... args ) {

        return run( timeout, null, args );
    }


    public static String runAt( String cwd, int timeout, String... args ) {

        return runAt( cwd, timeout, null, args );
    }


    public static String run( int timeout, List<Path> path, String... args ) {
        return doRun( timeout, path, false, args );
    }


    public static String runAt( String cwd, int timeout, List<Path> path, String... args ) {
        return doRunAt( cwd, timeout, path, false, args );
    }

    public static ProcessOut runProcess( int timeout, List<Path> path, boolean verbose, String... args ) {
        return runProcessAt( null, timeout, path, verbose, args );
    }

    public static ProcessOut runProcessAt( String cwd, int timeout, List<Path> path, boolean verbose, String... args ) {


        ProcessOut out = new ProcessOut();
        ProcessRunner runner = new ProcessRunner( null, null, timeout, path, verbose, args );
        runner.cwd = cwd;
        out.exit = runner.exec();
        out.stdout = runner.stdOut();
        out.stderr = runner.stdErr();
        out.commandLine = Str.joinCollection( ' ', runner.commandLine );
        return out;
    }


    private static String doRun( int timeout, List<Path> path, boolean verbose, String... args ) {


        ProcessOut out = runProcess( timeout, path, verbose, args );
        if ( out.getExit() != 0 ) {
            throw new ProcessException( Boon.sputs("EXIT CODE", out.getExit(), out.getStderr()) );
        } else {
            return out.getStdout();
        }


    }


    private static String doRunAt( String cwd, int timeout, List<Path> path, boolean verbose, String... args ) {


        ProcessOut out = runProcessAt( cwd, timeout, path, verbose, args );
        if ( out.getExit() != 0 ) {
            throw new ProcessException( Boon.sputs("EXIT CODE", out.getExit(), out.getStderr()) );
        } else {
            return out.getStdout();
        }


    }

    public static ProcessInOut launchProcess( int timeout, List<Path> path, boolean verbose, String... args ) {

        ProcessInOut process = new ProcessInOut();
        process.run( timeout, path, verbose, args );

        return process;

    }

    public static String run( String... args ) {
        return run( 0, args );
    }


    public static String runAt( String cwd, String... args ) {
        return runAt( cwd, 0, args );
    }


    public static String runShell( String... args ) {

        List<String> list = new ArrayList<>( args.length + 2 );
        list.add( shell );
        list.add( shellArgument );
        for ( String arg : args ) {
            list.add( arg );

        }
        return run( 0, list.toArray( new String[ list.size() ] ) );
    }

    public static String runShell( int timeout, String... args ) {

        List<String> list = new ArrayList<>( args.length + 2 );
        list.add( shell );
        list.add( shellArgument );
        for ( String arg : args ) {
            list.add( arg );

        }
        return run( timeout, list.toArray( new String[ list.size() ] ) );
    }


    public static int execShell( String... args ) {

        List<String> list = new ArrayList<>( args.length + 2 );
        list.add( shell );
        list.add( shellArgument );
        for ( String arg : args ) {
            list.add( arg );

        }
        return exec( 0, list.toArray( new String[ list.size() ] ) );
    }


    public static int execShell( int timeout, String... args ) {

        List<String> list = new ArrayList<>( args.length + 2 );
        list.add( shell );
        list.add( shellArgument );
        for ( String arg : args ) {
            list.add( arg );

        }
        return exec( timeout, list.toArray( new String[ list.size() ] ) );
    }


    public static class ProcessInOut {

        private ProcessRunner runner;
        private ProcessOut out;

        private AtomicBoolean done = new AtomicBoolean( false );

        private BlockingQueue<String> queueOut;
        private BlockingQueue<String> queueErr;


        private ExecutorService executorService;


        public ProcessInOut() {
            this.queueOut = new ArrayBlockingQueue<>( 100 );
            this.queueErr = new ArrayBlockingQueue<>( 100 );
        }

        public void run( final int timeout, final List<Path> path, final boolean verbose, final String... args ) {
            done.set( false );
            out = new ProcessOut();
            runner = new ProcessRunner( ProcessInOut.this, null, timeout, path, verbose, args );

            executorService = Executors.newSingleThreadExecutor();

            Runnable task = new Runnable() {

                @Override
                public void run() {
                    out.exit = runner.exec();
                    out.stdout = runner.stdOut();
                    out.stderr = runner.stdErr();
                    out.commandLine = Str.joinCollection( ' ', runner.commandLine );
                    done.set( true );
                }
            };


            executorService.submit( task );

        }

        public boolean isDone() {
            return done.get();
        }

        public ProcessOut processOut() {
            return out;
        }

        public BlockingQueue<String> getStdOut() {
            return queueOut;
        }

        public BlockingQueue<String> getStdErr() {
            return queueErr;
        }


        public void kill() {
            runner.process.destroy();
        }

    }


    public static class ProcessOut {
        private int exit;
        private String stdout;
        private String stderr;
        private String commandLine;

        public int getExit() {
            return exit;
        }


        public String getStdout() {
            return stdout;
        }


        public String getStderr() {
            return stderr;
        }

        public String getCommandLine() {
            return commandLine;
        }


        @Override
        public String toString() {
            return "ProcessOut [\nexit=" + exit + ", \nstdout=" + stdout
                    + ", \nstderr=" + stderr + ", \ncommandLine=" + commandLine
                    + "\n]";
        }

    }

    private static void handle( Exception ex ) {
        throw new ProcessException( ex );
    }


    @SuppressWarnings ( "serial" )
    public static class ProcessException extends RuntimeException {

        public ProcessException() {
            super();
        }

        public ProcessException( String m, Throwable t ) {
            super( m, t );
        }

        public ProcessException( String m ) {
            super( m );
        }

        public ProcessException( Throwable t ) {
            super( t );
        }
    }

    public static class ProcessRunner {
        private List<String> commandLine;
        private String password;
        private List<Path> path;

        private ProcessIODrainer fromProcessOutput;
        private ProcessIODrainer fromProcessError;
        private int timeoutInSeconds = 0;
        private boolean verbose;

        private PrintWriter toProcess;


        private ProcessInOut inout;
        private Process process;


        private ExecutorService executorService;

        private ScheduledExecutorService scheduledExecutorService;

        private String cwd;

        public ProcessRunner( ProcessInOut inout, String password, int timeoutInSeconds,
                              List<Path> path, boolean verbose, String... cmdLine ) {


            if ( timeoutInSeconds == 0 ) {
                timeoutInSeconds = 5;
            }
            if ( cmdLine.length == 1 ) {
                cmdLine = Str.split( cmdLine[ 0 ] );
            }


            this.inout = inout;
            this.commandLine = Lists.list( cmdLine );
            this.password = password;
            this.timeoutInSeconds = timeoutInSeconds;
            this.path = path;
            this.verbose = verbose;


            if ( this.path == null ) {
                this.path = Runner.path();
            }

            executorService = Executors.newFixedThreadPool( 2 );

        }

        public int exec() throws ProcessException {
            int exit = -666;

            initializePath();


            ProcessBuilder processBuilder = new ProcessBuilder( commandLine );

            if ( cwd != null ) {
                processBuilder.directory( new File( cwd ) );
            }

            String envPath = Str.joinCollection( File.pathSeparatorChar, path );
            processBuilder.environment().put( "PATH", envPath );


            try {
                initializeDrainersScannersAndWriters( processBuilder );


                final Future<?> fromProcessErrorFuture = executorService.submit( fromProcessError );
                final Future<?> fromProcessOutputFuture = executorService.submit( fromProcessOutput );


                if ( timeoutInSeconds == -1 ) {
                    exit = process.waitFor();

                } else {

                    exit = runWithTimeoutTimer( fromProcessErrorFuture, fromProcessOutputFuture );

                }


                fromProcessErrorFuture.get();
                fromProcessOutputFuture.get();


            } catch ( Exception e ) {
                Thread.interrupted();
                handle( e );
            }
            return exit;
        }

        private void initializePath() {
            String cmd = commandLine.get( 0 );
            Path pathCommand = IO.path( cmd );
            if ( !Files.exists( pathCommand ) ) {
                for ( Path dir : path ) {
                    pathCommand = IO.path( dir, cmd );
                    if ( Files.exists( pathCommand ) ) {
                        cmd = pathCommand.toAbsolutePath().toString();
                        break;
                    }
                }
            }
            commandLine.set( 0, cmd );

        }

        private void initializeDrainersScannersAndWriters( ProcessBuilder processBuilder ) throws IOException {
            process = processBuilder.start();

            toProcess = new PrintWriter( new OutputStreamWriter( process.getOutputStream() ) );

            Scanner stdOut = new Scanner( process.getInputStream() );
            Scanner stdErr = new Scanner( process.getErrorStream() );

            if ( inout == null ) {
                fromProcessError = new ProcessIODrainer( stdErr, verbose );
                fromProcessOutput = new ProcessIODrainer( stdOut, toProcess,
                        password, false, verbose );
            } else {
                fromProcessError = new ProcessIODrainer( inout.queueErr, stdErr, verbose );
                fromProcessOutput = new ProcessIODrainer( inout.queueOut, stdOut, toProcess,
                        password, false, verbose );

            }
        }

        private int runWithTimeoutTimer( final Future<?> fromProcessErrorFuture, final Future<?> fromProcessOutputFuture ) throws InterruptedException {
            Runnable command = new Runnable() {

                @Override
                public void run() {
                    process.destroy();
                    fromProcessErrorFuture.cancel( true );
                    fromProcessOutputFuture.cancel( true );
                }
            };


            scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
            final ScheduledFuture<?> scheduledFuture = scheduledExecutorService.scheduleWithFixedDelay( command, timeoutInSeconds, timeoutInSeconds, TimeUnit.SECONDS );
            int exit = process.waitFor();
            scheduledFuture.cancel( true );
            return exit;
        }

        public String stdOut() {
            return fromProcessOutput.getOutput();
        }

        public String stdErr() {
            return fromProcessError.getOutput();
        }

    }

    static class ProcessIODrainer implements Runnable {
        private Scanner fromProcess;
        private String password;
        private PrintWriter toProcess;
        private StringBuilder outputBuffer = new StringBuilder( 1024 );
        private boolean sudo;
        private boolean verbose;
        private BlockingQueue<String> queue;

        ProcessIODrainer( Scanner fromProcess, boolean verbose ) {
            this.fromProcess = fromProcess;
            this.verbose = verbose;
        }

        ProcessIODrainer( BlockingQueue<String> queueOut, Scanner fromProcess, boolean verbose ) {
            this.queue = queueOut;
            this.fromProcess = fromProcess;
            this.verbose = verbose;
        }

        ProcessIODrainer( Scanner fromProcess,
                          PrintWriter toProcess, String password, boolean sudo, boolean verbose ) {
            this.sudo = sudo;
            this.fromProcess = fromProcess;
            this.toProcess = toProcess;
            this.verbose = verbose;
            this.password = password;
        }

        public ProcessIODrainer( BlockingQueue<String> queueOut, Scanner fromProcess,
                                 PrintWriter toProcess, String password, boolean sudo, boolean verbose ) {
            this.queue = queueOut;
            this.sudo = sudo;
            this.fromProcess = fromProcess;
            this.toProcess = toProcess;
            this.verbose = verbose;
            this.password = password;
        }

        public void run() {
            if ( sudo ) {
                try {
                    Thread.sleep( 100 );
                } catch ( InterruptedException e ) {
                    Thread.interrupted();
                }
                toProcess.println( password );
                toProcess.flush();
            }

            try {
                while ( fromProcess.hasNextLine() ) {
                    String line = fromProcess.nextLine();

                    if ( queue != null ) {
                        while ( true ) {
                            try {
                                queue.put( line );
                                break;
                            } catch ( InterruptedException e ) {
                                if ( Thread.currentThread().isInterrupted() ) {
                                    break;
                                } else {
                                    continue;
                                }
                            }
                        }
                    }

                    if ( verbose ) {
                        Boon.puts(line);
                    }
                    outputBuffer.append( line ).append( '\n' );
                }

            } finally {
                fromProcess.close();
            }
        }

        public String getOutput() {
            return outputBuffer.toString();
        }

    }

}


