package com.opencredo.concourse.domain.events.processing;

import com.opencredo.concourse.domain.events.Event;
import com.opencredo.concourse.domain.events.channels.EventsOutChannel;
import com.opencredo.concourse.domain.events.logging.EventLog;

import java.util.Collection;
import java.util.function.Consumer;

/**
 * Processes the {@link Event}s collected in an {@link com.opencredo.concourse.domain.events.batching.EventBatch}
 * on batch completion.
 */
public interface EventBatchProcessor extends Consumer<Collection<Event>> {

    /**
     * Create an {@link EventBatchProcessor} that forwards the events to the supplied {@link EventsOutChannel}
     * @param outChannel The {@link EventsOutChannel} to forward events to.
     * @return The constructed {@link EventBatchProcessor}.
     */
    static EventBatchProcessor forwardingTo(EventsOutChannel outChannel) {
        return outChannel::accept;
    }

    /**
     * Create an {@link EventBatchProcessor} that logs the events with the supplied {@link EventLog}
     * @param eventLog The {@link EventLog} to log the events with.
     * @return The constructed {@link EventBatchProcessor}.
     */
    static EventBatchProcessor loggingWith(EventLog eventLog) {
        return eventLog::apply;
    }
}