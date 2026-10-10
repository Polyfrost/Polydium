package org.polyfrost.polydium.client.perf;

//? if >= 1.21.11 {
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class BackgroundCleanup {
    private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor(task -> {
        Thread thread = new Thread(task, "Polydium background cleanup");
        thread.setDaemon(true);
        return thread;
    });

    private BackgroundCleanup() {
    }

    public static void run(Runnable task) {
        EXECUTOR.execute(task);
    }
}
//?}
