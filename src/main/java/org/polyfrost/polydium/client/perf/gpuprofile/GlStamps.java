package org.polyfrost.polydium.client.perf.gpuprofile;

//? if < 26.2 {
/*import org.jetbrains.annotations.Nullable;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL15C;
import org.lwjgl.opengl.GL33C;
import org.lwjgl.opengl.GLCapabilities;

final class GlStamps implements GpuStamps {
    private final int[] queries;

    GlStamps(int size) {
        GLCapabilities caps = GL.getCapabilities();

        if (!caps.OpenGL33 && !caps.GL_ARB_timer_query)
            throw new UnsupportedOperationException("this OpenGL context has no timestamp queries (needs OpenGL 3.3 or ARB_timer_query)");

        queries = new int[size];
        GL15C.glGenQueries(queries);
    }

    @Override
    public void write(int query, @Nullable Object context) {
        GL33C.glQueryCounter(queries[query], GL33C.GL_TIMESTAMP);
    }

    @Override
    public boolean read(int first, int count, long[] nanos) {
        if (GL15C.glGetQueryObjecti(queries[first + count - 1], GL15C.GL_QUERY_RESULT_AVAILABLE) == 0)
            return false;

        for (int i = 0; i < count; i++)
            nanos[i] = GL33C.glGetQueryObjecti64(queries[first + i], GL15C.GL_QUERY_RESULT);

        return true;
    }

    @Override
    public void close() {
        GL15C.glDeleteQueries(queries);
    }
}
*///?}
