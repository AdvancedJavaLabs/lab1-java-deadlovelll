package org.graph;

import org.openjdk.jcstress.annotations.*;
import org.openjdk.jcstress.infra.results.I_Result;

import java.util.concurrent.ExecutionException;

@JCStressTest
@State
@Description("Checks for data race in parallelBFS when visited.set() is not atomic")
@Outcome(id = "15", expect = Expect.ACCEPTABLE, desc = "All 4 vertices visited (1111b)")
@Outcome(expect = Expect.FORBIDDEN, desc = "Race caused some vertices lost or duplicated")
public class ConcurrentStartsTest {

    final Graph g;

    public ConcurrentStartsTest() {
        g = new Graph(4);
        g.addEdge(0,1);
        g.addEdge(1,2);
        g.addEdge(2,3);
    }

    @Actor
    public void actor1() {
        try {
            g.parallelBFS(0);
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    @Actor
    public void actor2() {
        try {
            g.parallelBFS(1);
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    @Arbiter
    public void arbiter(I_Result r) {
        int mask = 0;
        for (int i = 0; i < 4; i++) {
            if (g.getVisited(i) != 0) mask |= (1 << i);
        }
        r.r1 = mask;
    }
}
