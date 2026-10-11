package org.polyfrost.polydium.client.perf;

// Ported from Argentum (ParticleMixin) with permission from rdh
public final class ParticleSectionVisibility {
    private ParticleSectionVisibility() {
    }

    public interface Access {
        long polydium$sectionVisibleTick();

        void polydium$sectionVisibleTick(long tick);
    }
}
