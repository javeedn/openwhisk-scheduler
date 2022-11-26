package it.uniroma2.faas.openwhisk.scheduler.scheduler.policy;

import it.uniroma2.faas.openwhisk.scheduler.util.DeNormalize;
import it.uniroma2.faas.openwhisk.scheduler.scheduler.domain.model.Action;
import it.uniroma2.faas.openwhisk.scheduler.scheduler.domain.model.BlockingCompletion;
import it.uniroma2.faas.openwhisk.scheduler.scheduler.domain.model.IConsumable;
import it.uniroma2.faas.openwhisk.scheduler.scheduler.domain.model.ISchedulable;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.stream.Collectors;
import java.math.*;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toCollection;

/**
 *
 * Weighted Shortest Job First is a calculated as: Cost of Delay divided by Job Duration (or Size).
 * Cost of Delay in the WSJF framework is three components:

    1. Value to the business and/or user

    2. Time criticality

    3. Risk reduction and/or opportunity enablement

    To calculate the Cost of Delay, you will create a scale for each component 
    (for example, 1 to 10) and add them up. 
    The combined number of all three parts will equal your Cost of Delay score.

  * The job duration or size of each activations on list.
    The scale can be different from your Cost of Delay scale (for example, 1 to 20) 
    as long as you are applying it consistently to all activations.
 */
public class WeightedShortestJobFirst implements IPolicy {

    public static final Policy POLICY = Policy.WEIGHTED_SHORTEST_JOB_FIRST;

    private final Map<Action, Long> actionDurationMap = new HashMap<>();
    private final Map<Collection<ISchedulable>, Double> schedulewsjfMap = new HashMap<>();
    private static double maxOfDataRange = 0;
    private static double minOfDataRange = 0;

    @Override
    public @Nonnull Queue<? extends ISchedulable> apply(@Nonnull final Collection<? extends ISchedulable> schedulables) {
        
        final Queue<ISchedulable> invocationQueue = new ArrayDeque<>(schedulables.size());
        if (schedulables.size() == 1) return new ArrayDeque<>(schedulables);
        
        // group received schedulables by action
        final Map<Action, Collection<ISchedulable>> actionGroupedSchedulables = schedulables.stream()
                // see@ https://stackoverflow.com/questions/39172981/java-collectors-groupingby-is-list-ordered
                .collect(groupingBy(ISchedulable::getAction, toCollection(ArrayDeque::new)));
        final boolean isNormalizationEnabled = schedulables.stream().parallel()
                .filter(s -> s.getNormalize() == 1).findAny().isPresent();

        // if there is no mapping for encountered action, add to map and set its duration as +inf
        actionGroupedSchedulables.keySet().forEach(as -> actionDurationMap.putIfAbsent(as, Long.MAX_VALUE));           

        actionDurationMap.entrySet().forEach(a -> {
                  if (actionGroupedSchedulables.containsKey(a.getKey())) {
                    Collection<ISchedulable> listOfSchedulable = actionGroupedSchedulables.get(a.getKey());
                    BigDecimal codValue = BigDecimal.valueOf(computeCod(listOfSchedulable));
                    BigDecimal duration = new BigDecimal((Long) a.getValue());
                    BigDecimal roundedTotalScore = codValue.divide(duration, 2, RoundingMode.HALF_EVEN);
                    double wsjfScore =  roundedTotalScore.doubleValue();
                    schedulewsjfMap.put(listOfSchedulable, wsjfScore);
                  }
                });
        if (isNormalizationEnabled) {
            Collection<Double> wsjfScores = schedulewsjfMap.values();
            double[] wsjfRawScores = wsjfScores.stream().mapToDouble(Double::doubleValue).toArray();
            // Perform Normalization if skewed
            if (DeNormalize.isHighlySkewed(wsjfRawScores, wsjfScores.size())) {
                Arrays.sort(wsjfRawScores);
                minOfDataRange =+ wsjfRawScores[0];
                maxOfDataRange =+ wsjfRawScores[wsjfRawScores.length - 1];
                schedulewsjfMap.keySet().forEach(sec -> {
                    double rawScore = schedulewsjfMap.get(sec);
                    // Update the existing score with new normalized wsjf score
                    schedulewsjfMap.put(sec, DeNormalize.getActualExpCubeForm(rawScore,
                        maxOfDataRange, minOfDataRange));
                });           
            }
        }

        /*
        * One activation on list with a Cost of Delay of 9 and a Job Duration of 1. 
        * That activation WSJF score will be 9
        * Another activation : Cost of Delay = 7 and Job Duration = 4. 
        * That activation WSJF score is only 1.75
        * Activations with higher Cost of Delay scores earn a higher position on your WSJF list. 
        * Activations with lower Job Duration scores also receive a higher WSJF priority.
        */
        
        schedulewsjfMap.entrySet().stream()
            // sort from largest to smallest
           .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
           .forEachOrdered(s -> invocationQueue.addAll(s.getKey()));
        return invocationQueue;
    }

    

    @Override
    public void update(@Nonnull final Collection<? extends IConsumable> consumables) {
        // only blocking completions have "annotations" field which contains action's "duration"
        // NOTE: to use ShortestJobFirst policy even with non-blocking action, must be implemented
        //   new Kafka consumer for topic "events" and must be enabled "user_events" in deploy configuration
        //   see@ https://github.com/apache/openwhisk/blob/master/docs/metrics.md#user-specific-metrics
        final Collection<BlockingCompletion> blockingCompletions = consumables.stream()
                .filter(BlockingCompletion.class::isInstance)
                .map(BlockingCompletion.class::cast)
                .filter(bc -> bc.getResponse() != null)
                .collect(Collectors.toUnmodifiableList());

        for (final BlockingCompletion completion : blockingCompletions) {
            final long newObservation = getMetricFrom(completion, false);
            final Action action = getActionFrom(completion);
            final Long currentEstimation = actionDurationMap.getOrDefault(action, Long.MAX_VALUE);
            if (currentEstimation == Long.MAX_VALUE) {
                // set first observation
                actionDurationMap.put(action, newObservation);
            } else {
                // update current estimation
                actionDurationMap.put(action, estimate(currentEstimation, newObservation));
            }
        }
    }

    @Override
    public @Nonnull Policy getPolicy() {
        return POLICY;
    }

    /**
     * New method to retrieve effective duration, since field "duration" includes "initTime".
     *
     * @param blockingCompletion
     * @param includeInitTime
     * @return
     */
    private static long getMetricFrom(@Nonnull final BlockingCompletion blockingCompletion,
                                      final boolean includeInitTime) {
        Long duration = blockingCompletion.getResponse().getDuration();
        if (duration == null) return 0L;

        if (!includeInitTime) {
            final List<Map<String, Object>> annotations = blockingCompletion.getResponse().getAnnotations();
            for (final Map<String, Object> annotation : annotations) {
                if (annotation.get("key").equals("initTime"))
                    duration -= (int) annotation.get("value");
            }

        }

        return duration;
    }

    private static Integer computeCod(Collection<ISchedulable> schedulables) {
      Integer codValueForGivenSchedulables = 0;
      
      for(ISchedulable schedulable : schedulables) {
         if (schedulable != null && schedulable.getPriority() != null 
             && schedulable.getTimePriority() != null 
             && schedulable.getRiskPriority() != null) {
           Integer codValue = getCod(schedulable.getPriority(),
           schedulable.getTimePriority(), schedulable.getRiskPriority());
           codValueForGivenSchedulables = codValueForGivenSchedulables + codValue;
         }
      };
      return codValueForGivenSchedulables;
    }

    private static Integer getCod(Integer valuePriority, Integer timePriority, Integer riskPriority) {
      return valuePriority+timePriority+riskPriority;
    }

    /**
     * new_value = alpha * new_observation + (1 - alpha) * actual_estimation
     * alpha = 1 / 8
     * see@ https://it.wikipedia.org/wiki/Round_Trip_Time
     *
     * @param currentEstimation
     * @param newObservation
     * @return
     */
    private static long estimate(long currentEstimation, long newObservation) {
        final float alpha = 1f / 8f;
        return (long) (alpha * newObservation + (1f - alpha) * currentEstimation);
    }

    private static @Nonnull Action getActionFrom(@Nonnull final BlockingCompletion blockingCompletion) {
        return new Action(
                blockingCompletion.getResponse().getName(),
                blockingCompletion.getResponse().getNamespace(),
                blockingCompletion.getResponse().getVersion()
        );
    }

}
