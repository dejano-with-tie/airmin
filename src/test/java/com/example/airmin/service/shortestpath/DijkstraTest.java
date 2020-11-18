package com.example.airmin.service.shortestpath;

import com.example.airmin.model.Airport;
import com.example.airmin.model.Route;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

class DijkstraTest {

    private final Dijkstra dijkstra = new Dijkstra();


    /**
     * @formatter:off
     *      N5 ---- 12 -----  N6
     *     /  \              /  \
     *    1    1            6     1
     *   /       \         /       \
     * N1 - 99 - N2 - 2 - N3 - 4 -  N4
     * @formatter:on
     *
     * Produced shortest path N1 -> N6 should be N1, N5, N2, N3, N4, N6
     */
    @Test
    void findShortest() {
        final Airport n1 = new Airport(1L, "N1", null);
        n1.setId(1L);
        final Airport n2 = new Airport(2L, "N2", null);
        n2.setId(2L);
        final Airport n3 = new Airport(3L, "N3", null);
        n3.setId(3L);
        final Airport n4 = new Airport(4L, "N4", null);
        n4.setId(4L);
        final Airport n5 = new Airport(5L, "N5", null);
        n5.setId(5L);
        final Airport n6 = new Airport(6L, "N6", null);
        n6.setId(6L);
        n1.setDepartures(Arrays.asList(new Route(1d, n1, n5), new Route(99d, n1, n2)));
        n2.setDepartures(Arrays.asList(new Route(2d, n2, n3)));
        n3.setDepartures(Arrays.asList(new Route(6d, n3, n6), new Route(4d, n3, n4)));
        n4.setDepartures(Arrays.asList(new Route(1d, n4, n6)));
        n5.setDepartures(Arrays.asList(new Route(1d, n5, n2), new Route(12d, n5, n6)));
        n6.setDepartures(Arrays.asList());

        final var predecessors = dijkstra.calculateGraph(Arrays.asList(n1, n2, n3, n4, n5, n6),
                Collections.singletonList(n1), Collections.singletonList(n6));
        final List<Route> path = dijkstra.shortestPath(n6, predecessors);
        Assertions.assertEquals(5, path.size());
        Assertions.assertEquals(n1, path.get(0).getSource());
        Assertions.assertEquals(n5, path.get(0).getDestination());
        Assertions.assertEquals(n5, path.get(1).getSource());
        Assertions.assertEquals(n2, path.get(1).getDestination());
        Assertions.assertEquals(n2, path.get(2).getSource());
        Assertions.assertEquals(n3, path.get(2).getDestination());
        Assertions.assertEquals(n3, path.get(3).getSource());
        Assertions.assertEquals(n4, path.get(3).getDestination());
        Assertions.assertEquals(n4, path.get(4).getSource());
        Assertions.assertEquals(n6, path.get(4).getDestination());
        Assertions.assertEquals(9, path.stream().mapToDouble(Route::getPrice).sum());

    }

    /**
     * @formatter:off
     *
     * | ------------- 7 -------|
     * |      N5                N6
     * |     /  \              /  \
     * |    1    1            6     1
     * |   /       \         /       \
     * N0,N1 - 99 - N2 - 2 - N3 - 4 -  N4
     * @formatter:on
     *
     * Produced shortest path N0/N1 -> N6 should be N0, N6
     */
    @Test
    void findShortestWhenMultipleSources() {
        final var n0 = new Airport(0L, "N0", null);
        n0.setId(0L);
        final var n1 = new Airport(1L, "N1", null);
        n1.setId(1L);
        final var n2 = new Airport(2L, "N2", null);
        n2.setId(2L);
        final var n3 = new Airport(3L, "N3", null);
        n3.setId(3L);
        final var n4 = new Airport(4L, "N4", null);
        n4.setId(4L);
        final var n5 = new Airport(5L, "N5", null);
        n5.setId(5L);
        final var n6 = new Airport(6L, "N6", null);
        n6.setId(6L);

        n0.setDepartures(Arrays.asList(new Route(7d, n0, n6)));
        n1.setDepartures(Arrays.asList(new Route(1d, n1, n5), new Route(99d, n1, n2)));
        n2.setDepartures(Arrays.asList(new Route(2d, n2, n3)));
        n3.setDepartures(Arrays.asList(new Route(6d, n3, n6), new Route(4d, n3, n4)));
        n4.setDepartures(Arrays.asList(new Route(1d, n4, n6)));
        n5.setDepartures(Arrays.asList(new Route(1d, n5, n2)));
        n6.setDepartures(Collections.emptyList());

        final var predecessors = dijkstra.calculateGraph(Arrays.asList(n0, n1, n2, n3, n4, n5, n6),
                Arrays.asList(n0, n1), Collections.singletonList(n6));
        final var path = dijkstra.shortestPath(n6, predecessors);
        Assertions.assertEquals(1, path.size());
        Assertions.assertEquals(n0, path.get(0).getSource());
        Assertions.assertEquals(n6, path.get(0).getDestination());
        Assertions.assertEquals(7, path.stream().mapToDouble(Route::getPrice).sum());
    }

}