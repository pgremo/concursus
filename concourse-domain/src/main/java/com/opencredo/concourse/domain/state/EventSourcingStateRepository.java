package com.opencredo.concourse.domain.state;

import com.opencredo.concourse.domain.events.Event;
import com.opencredo.concourse.domain.events.binding.EventTypeBinding;
import com.opencredo.concourse.domain.events.sourcing.CachedEventSource;
import com.opencredo.concourse.domain.events.sourcing.EventReplayer;
import com.opencredo.concourse.domain.events.sourcing.EventSource;
import com.opencredo.concourse.domain.time.TimeRange;

import java.time.Instant;
import java.util.AbstractMap.SimpleEntry;
import java.util.*;
import java.util.Map.Entry;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class EventSourcingStateRepository<T> implements StateRepository<T> {

    public static <T> EventSourcingStateRepository<T> using(Supplier<StateBuilder<T>> stateSupplier, EventSource eventSource, EventTypeBinding typeBinding, Comparator<Event> causalOrdering) {
        return new EventSourcingStateRepository<T>(stateSupplier, eventSource, typeBinding, causalOrdering);
    }

    private final Supplier<StateBuilder<T>> stateSupplier;
    private final EventSource eventSource;
    private final EventTypeBinding typeBinding;
    private final Comparator<Event> causalOrdering;

    private EventSourcingStateRepository(Supplier<StateBuilder<T>> stateSupplier, EventSource eventSource, EventTypeBinding typeBinding, Comparator<Event> causalOrdering) {
        this.stateSupplier = stateSupplier;
        this.eventSource = eventSource;
        this.typeBinding = typeBinding;
        this.causalOrdering = causalOrdering;
    }

    @Override
    public Optional<T> getState(UUID aggregateId, Instant upTo) {
        return getState(typeBinding.replaying(eventSource, aggregateId, TimeRange.fromUnbounded().toExclusive(upTo)));
    }

    private Optional<T> getState(EventReplayer eventReplayer) {
        StateBuilder<T> state = stateSupplier.get();
        eventReplayer.inAscendingOrder(causalOrdering).replayAll(state);
        return state.get();
    }

    @Override
    public Map<UUID, T> getStates(Collection<UUID> aggregateIds, Instant upTo) {
        final CachedEventSource preloaded = typeBinding.preload(eventSource, aggregateIds, TimeRange.fromUnbounded().toExclusive(upTo));
        return aggregateIds.stream().flatMap(id ->
                getState(typeBinding.replaying(preloaded, id, TimeRange.unbounded()))
                        .map(s -> Stream.of(new SimpleEntry<>(id, s))).orElseGet(Stream::empty))
            .collect(Collectors.toMap(Entry::getKey, Entry::getValue));
    }
}