package org.polyfrost.polydium.client.perf;

import java.util.IdentityHashMap;
import java.util.function.Predicate;

public final class OitlessRenderTypes<T> {
    private final Predicate<T> lacksOit;
    private final IdentityHashMap<T, Boolean> answers = new IdentityHashMap<>();
    private int generation = FontGeneration.get();

    public OitlessRenderTypes(Predicate<T> lacksOit) {
        this.lacksOit = lacksOit;
    }

    public boolean lacksOit(T renderType) {
        int current = FontGeneration.get();
        if (current != this.generation) {
            this.generation = current;
            this.answers.clear();
        }
        Boolean answer = this.answers.get(renderType);
        if (answer == null) {
            answer = this.lacksOit.test(renderType);
            this.answers.put(renderType, answer);
        }
        return answer;
    }

    public int size() {
        return this.answers.size();
    }
}
