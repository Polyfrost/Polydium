package org.polyfrost.polydium.client.perf;

//? if >= 26.2 {
import com.mojang.renderpearl.api.device.GpuSurface;
import com.mojang.renderpearl.backend.vulkan.VulkanQueue;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.Optional;
import java.util.concurrent.locks.LockSupport;

public final class PresentThread {

    private static final int SPINS = 1024;
    private static final ThreadMXBean THREADS = ManagementFactory.getThreadMXBean();

    private static volatile Thread    worker;
    private static volatile boolean   running;
    private static volatile Runnable  job;
    private static volatile Thread    waiting;
    private static volatile Throwable failure;
    private static GpuSurface         armed;
    private static Runnable           captured;
    private static boolean            capturing;
    private static boolean            handedOff;
    public static boolean             vulkanSurface;

    private PresentThread() {
    }

    public static void arm(GpuSurface frameSurface) {
        if (armed != null)
            throw new IllegalStateException("The frame present is already armed");

        armed     = frameSurface;
        handedOff = false;
    }

    public static boolean takeHandedOff() {
        boolean was = handedOff;

        handedOff = false;

        return was;
    }

    public static void disarm() {
        armed = null;
    }

    public static boolean isArmed() {
        return armed != null;
    }

    public static boolean canDeferAcquire(GpuSurface surface) {
        Optional<GpuSurface.Configuration> config = surface.currentConfiguration();

        return vulkanSurface && config.isPresent() && config.get().presentMode() == GpuSurface.PresentMode.IMMEDIATE;
    }

    public static void handOff(VulkanQueue.Submission submission) {
        GpuSurface surface = armed;

        armed     = null;
        capturing = true;

        try {
            surface.present();
        } finally {
            capturing = false;
        }

        Runnable present = captured;

        captured = null;

        if (present == null)
            throw new IllegalStateException("The surface did not hand over its present");

        handedOff = true;

        drain();

        if (!FramePath.frameUsesWorker()) {
            long wallStart = System.nanoTime();
            long cpuStart  = THREADS.getCurrentThreadCpuTime();
            submission.close();
            present.run();
            FramePath.recordGpuWait(Math.max(0L, System.nanoTime() - wallStart - (THREADS.getCurrentThreadCpuTime() - cpuStart)));
            return;
        }

        Thread thread = worker;

        if (thread == null) {
            running = true;
            worker  = thread = Thread.ofPlatform().name("Polydium Present").daemon().priority(Thread.MAX_PRIORITY).start(PresentThread::work);
        }

        job = () -> {
            submission.close();
            present.run();
        };
        LockSupport.unpark(thread);
    }

    public static boolean isCapturing() {
        return capturing;
    }

    public static void capture(Runnable present) {
        if (captured != null)
            throw new IllegalStateException("A present was already captured for this frame");

        captured = present;
    }

    public static void drain() {
        if (Thread.currentThread() == worker)
            return;

        await();

        Throwable thrown = failure;

        if (thrown == null)
            return;

        failure = null;

        if (thrown instanceof RuntimeException runtime)
            throw runtime;

        if (thrown instanceof Error error)
            throw error;

        throw new IllegalStateException("The present thread failed", thrown);
    }

    public static void await() {
        if (Thread.currentThread() == worker)
            return;

        if (job != null) {
            waiting = Thread.currentThread();

            int spins = 0;

            while (job != null) {
                if (spins < SPINS) {
                    spins ++;
                    Thread.onSpinWait();
                } else {
                    LockSupport.parkNanos(PresentThread.class, 1_000_000L);
                }
            }

            waiting = null;
        }
    }

    public static void stop() {
        Thread thread = worker;

        if (thread == null) {
            drain();

            return;
        }

        try {
            drain();
        } finally {
            running = false;
            LockSupport.unpark(thread);

            boolean interrupted = false;

            while (thread.isAlive()) {
                try {
                    thread.join();
                } catch (InterruptedException exception) {
                    interrupted = true;
                }
            }

            worker = null;

            if (interrupted)
                Thread.currentThread().interrupt();
        }
    }

    private static void work() {
        while (running) {
            Runnable next = job;

            if (next == null) {
                LockSupport.park(PresentThread.class);
                continue;
            }

            try {
                next.run();
            } catch (Throwable thrown) {
                failure = thrown;
            }

            job = null;
            LockSupport.unpark(waiting);
        }
    }
}
//?}
