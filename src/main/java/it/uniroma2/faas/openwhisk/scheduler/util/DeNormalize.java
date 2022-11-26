package it.uniroma2.faas.openwhisk.scheduler.util;

import javax.annotation.Nonnull;
import java.util.*;
import java.math.*;
import java.lang.Math;

/**
 * This option is invoked by user for enhancing the WSJF technique
 * By Default this feature stays disbaled, when specified, it contains 
 * activities which prevents WSJF priority queue to be in healthy state
 * 
 * 1) Avoid starvation, by constantly rebasing the skewness with incoming request
 * 2) Prevents performance degradation to PriorityFirst / ShortestJobFirst
 * 
 */
public class DeNormalize {

    /**
     * Normalise skewed data with base formula
     * a = a^(a^1/3)
     * 
     */
    public static @Nonnull double getActualExpCubeForm(@Nonnull final double item,
            double maxOfDataRange, double minOfDataRange) {
        
        BigDecimal min = new BigDecimal(minOfDataRange);
        BigDecimal max = new BigDecimal(maxOfDataRange);
        if (max.compareTo(min) != 0) {
            double result = Math.pow(item, 1 / 3);
            result = Math.pow(item, result); 
            BigDecimal bigDecimalResult = new BigDecimal(result);
            MathContext mode = new MathContext(2);
            return bigDecimalResult.round(mode).doubleValue();
        }
        return item;
    }

    /**
     * Computing Skewness Of Given Array and justifying whether it is skewed or not
     * 
     * If this value is between:
     *  * -0.5 and 0.5, the distribution of the value is almost symmetrical
     * 
     *  * -1 and -0.5, the data is negatively skewed, and if it is between 0.5 to 1, the data is positively skewed.
     *    The skewness is moderate.
     * 
     *  * If the skewness is lower than -1 (negatively skewed) or greater than 1 (positively skewed),
     *    the data is highly skewed.
     * 
     */
    public static @Nonnull boolean isHighlySkewed(@Nonnull final double arr[], @Nonnull final int n) {

        double actualValue = getSkewness(arr, n);
        double upperRange = Double.valueOf(1);
        double lowerRange = Double.valueOf(-1);
        if ( Double.compare(actualValue, upperRange) < 0  || Double.compare(actualValue, lowerRange) > 0) {
            return true;
        }
        return false;
    }

    /**
     * Function to calculate Mean.
     * 
     */
    private static double getMean(double arr[], int n)
    {
        double sum = 0;
        for (int i = 0; i < n; i++)
            sum += arr[i];
 
        return sum / n;
    }
 
    /**
     * Function to calculate Median.
     * 
     */
    private static double getMedian(double arr[], int n)
    {
        Arrays.sort(arr);
 
        if (n % 2 != 0)
            return (double)arr[n / 2];
 
        return (double)(arr[(n - 1) / 2] + arr[n / 2]) / 2.0;
    }
 
    /**
     * Function to calculate standard deviation of data.
     * 
     */
    private static double getStandardDeviation(double arr[], int n)
    {
         
        double sum = 0 ;
        
        for (int i = 0; i < n; i++)
            sum = (arr[i] - getMean(arr, n)) *
                        (arr[i] - getMean(arr, n));
                 
        return Math.sqrt(sum / n);
    }

    /**
     * Function to calculate skewness.
     * 
     */
    private static double getSkewness(double arr[], int n)
    {
        
        double pearsonCoefficientOfSkewness = (3 * (getMean(arr, n) - getMedian(arr, n))) 
                                                        / getStandardDeviation(arr, n);
        return pearsonCoefficientOfSkewness;
    }

}
