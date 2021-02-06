package it.uniroma2.faas.openwhisk.scheduler.scheduler.advanced;

import it.uniroma2.faas.openwhisk.scheduler.data.source.domain.model.Activation;
import it.uniroma2.faas.openwhisk.scheduler.data.source.domain.model.ITraceable;
import it.uniroma2.faas.openwhisk.scheduler.scheduler.Scheduler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static it.uniroma2.faas.openwhisk.scheduler.data.source.remote.consumer.kafka.ActivationKafkaConsumer.ACTIVATION_STREAM;

/**
 * This class add functionality to base scheduler, tracing all actions generated
 * (from Apache OpenWhisk Controller component) and assigning to them priority respectively.
 * To all subsequent actions will be assigned priority based on first action in composition.
 */
public class TracerScheduler extends AdvancedScheduler {

    private final static Logger LOG = LogManager.getLogger(TracerScheduler.class.getCanonicalName());

    public static final long DEFAULT_TIME_LIMIT_MS = TimeUnit.MINUTES.toMillis(5);

    private long timeLimitMs = DEFAULT_TIME_LIMIT_MS;
    // see@ https://stackoverflow.com/questions/14148331/how-to-get-a-hashmap-value-with-three-values
    // represent the state of currently active compositions - <PrimaryActivationID, <Priority, Timestamp>>
    private final Map<String, Map.Entry<Integer, Long>> compositionsMap = new HashMap<>();

    public TracerScheduler(@Nonnull Scheduler scheduler) {
        super(scheduler);
    }

    /**
     * Action belonging to a composition are traced based on {@link Activation#getCause()} field
     * in order to track priority. This is necessary because subsequent actions in a composition are generated
     * by OpenWhisk Controller component directly and could not have {@link Activation#K_SCHEDULER} field
     * associated with them.
     *
     * In this first implementation, actions are associated with a temporal mark and are removed from the map
     * once are considered too old, based on {@link #timeLimitMs}.
     *
     * In newer implementation could be considered to use Kafka topic "completedN" to remove completed compositions.
     * @param stream stream indicator
     * @param data consumable object to be processed
     */
    @Override
    public <T> void newEvent(@Nonnull UUID stream, @Nonnull final Collection<T> data) {
        checkNotNull(stream, "Stream can not be null.");
        checkNotNull(data, "Data can not be null.");

        if (!stream.equals(ACTIVATION_STREAM)) {
            LOG.warn("Unable to manage data type from stream {}.", stream.toString());
            return;
        }

        final List<ITraceable> traceables = data.stream()
                .filter(ITraceable.class::isInstance)
                .map(ITraceable.class::cast)
                .collect(Collectors.toList());
        final Collection<T> nonTraceables = data.stream()
                .filter(e -> !traceables.contains(e))
                .collect(Collectors.toUnmodifiableList());
        if (nonTraceables.size() > 0) {
            LOG.warn("Non traceable objects ({}) found in stream {} (over {} received).",
                    nonTraceables.size(), stream.toString(), data.size());
        }

        traceCompositions(traceables);
        updateCompositionsMap();
        LOG.trace("Processing {} traceables objects (over {} received).", traceables.size(), data.size());

        super.newEvent(stream, traceables);
    }

    /**
     * In place substitution of {@link ITraceable} objects with missing priority level.
     *
     * @param traceables list to check.
     */
    private void traceCompositions(@Nonnull List<ITraceable> traceables) {
        checkNotNull(traceables, "Traceable list can not be null.");

        ListIterator<ITraceable> traceableListIterator = traceables.listIterator();
        while (traceableListIterator.hasNext()) {
            ITraceable traceable = traceableListIterator.next();
            // if cause is not null current activation belongs to a composition
            if (traceable.getCause() != null) {
                int priority = traceable.getPriority() == null
                        ? 0
                        : traceable.getPriority();
                Map.Entry<Integer, Long> entry = compositionsMap.putIfAbsent(
                        traceable.getActivationId(),
                        new AbstractMap.SimpleImmutableEntry<>(priority, Instant.now().toEpochMilli())
                );
                // if there is already an entry for PrimaryActivationID, use priority associated to process activation
                if (entry != null) {
                    // if traced priority differs, create new traceable with correct priority
                    if (entry.getKey() != priority) {
                        traceableListIterator.set(traceable.with(entry.getKey()));
                        LOG.trace("Adding priority level {} associated with cause {} for activation with id {}",
                                entry.getKey(), traceable.getCause(), traceable.getActivationId());
                    }
                } else {
                    LOG.trace("Created new entry for cause id {} with priority {} - generated by activation with id {}.",
                            traceable.getCause(), traceable.getPriority(), traceable.getActivationId());
                }
            }
            // otherwise probably the activation received does not belongs to a composition
        }
    }

    /**
     * Remove entries older than {@link #timeLimitMs}.
     */
    private void updateCompositionsMap() {
        long now = Instant.now().toEpochMilli();
        int sizeBeforeUpdate = compositionsMap.size();
        compositionsMap.values().removeIf(entry -> now - entry.getValue() > timeLimitMs);
        int sizeAfterUpdate = compositionsMap.size();
        if (sizeBeforeUpdate != sizeAfterUpdate) {
            LOG.trace("Removed {} elements from compositions map (retention {} ms).",
                    sizeBeforeUpdate - sizeAfterUpdate, timeLimitMs);
        }
    }

    public void setTimeLimitMs(long timeLimitMs) {
        checkArgument(timeLimitMs >= 0, "Time limit must be >= 0.");
        this.timeLimitMs = timeLimitMs;
    }

}