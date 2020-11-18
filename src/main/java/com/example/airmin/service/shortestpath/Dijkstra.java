package com.example.airmin.service.shortestpath;

import com.example.airmin.model.Airport;
import com.example.airmin.model.Route;
import lombok.NonNull;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.PriorityQueue;

@Service
public class Dijkstra {

    public Map<Airport, Airport> calculateGraph(final Iterable<Airport> airports, List<Airport> sources, List<Airport> targets) {
        Map<Airport, Airport> path = new LinkedHashMap<>();

        int nmOfReachedTargets = 0;
        airports.forEach(a -> a.setDistance(Double.POSITIVE_INFINITY));
        sources.forEach(s -> s.setDistance(0));

        PriorityQueue<Airport> queue = new PriorityQueue<>(Comparator.comparingDouble(Airport::getDistance));
        queue.addAll(sources);
        while (!queue.isEmpty() || nmOfReachedTargets != targets.size()) {
            final Airport current = queue.poll();

            if (targets.contains(current)) {
                nmOfReachedTargets++;
            }

            if (current == null) {
                break;
            }

            for (final Route e : current.getDepartures()) {
                Airport neighbour = e.getDestination();
                double nDistance = current.getDistance() + e.getPrice();
                if (nDistance < neighbour.getDistance()) {
                    neighbour.setDistance(nDistance);
                    path.put(neighbour, current);

                    queue.remove(neighbour);
                    queue.add(neighbour);
                }
            }
        }

        return path;
    }

    public List<Route> shortestPath(@NonNull List<Airport> destinations, Map<Airport, Airport> predecessors) {
        Optional<Airport> cheapestTarget = predecessors.keySet().stream().filter(destinations::contains)
                .min(Comparator.comparingDouble(Airport::getDistance));


        if (cheapestTarget.isEmpty()) {
            return Collections.emptyList();
        }

        return shortestPath(cheapestTarget.get(), predecessors);
    }

    public List<Route> shortestPath(@NonNull Airport target, Map<Airport, Airport> predecessors) {
        List<Route> routes = new ArrayList<>();
        final Airport dest = predecessors.get(target);
        if (dest == null) {
            return routes;
        }

        Airport destination = target;
        while (predecessors.get(destination) != null) {
            Airport source = predecessors.get(destination);
            for (final Route route : source.getDepartures()) {
                if (route.getDestination().equals(destination) &&
                        compare(destination.getDistance() - source.getDistance(), route.getPrice(), 0.0000001)) {
                    routes.add(route);
                }
            }

            destination = source;
        }

        Collections.reverse(routes);

        return new ArrayList<>(new LinkedHashSet<>(routes));
    }

    /**
     * Because we are not using BigDecimal, compare with tolerance
     */
    private boolean compare(final double a, final double b, double tolerance) {
        return Math.abs(a - b) <= tolerance || Double.valueOf(a).equals(b);
    }
}
