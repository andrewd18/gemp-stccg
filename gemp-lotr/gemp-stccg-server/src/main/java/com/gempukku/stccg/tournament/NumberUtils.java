package com.gempukku.stccg.tournament;

import java.util.ArrayList;
import java.util.List;

public class NumberUtils {
    /**
     * Chooses a combination of count numbers from 0<=i<maxNumber, without repetitions. This method will return different
     * result for every "iteration" value (iteration starts at 0). If there is no more possible combinations, it will
     * return null
     *
     * @param count
     * @param maxNumber
     * @param iteration
     * @return
     */
    public static int[] selectOrderedCombinationOfNumbers(int count, int maxNumber, int iteration) {
        if (count > maxNumber)
            throw new IllegalArgumentException("Count cannot be bigger than maxNumber");

        int left = iteration;
        int[] indexes = new int[count];
        for (int i = 0; i < count; i++) {
            int resultWithOffset = left % (maxNumber - i);
            indexes[i] = resultWithOffset;
            left = left / (maxNumber - i);
        }
        if (left>0)
            return null;
        else {
            List<Integer> values = new ArrayList<>(maxNumber);
            for (int i=0; i<maxNumber; i++)
                values.add(i);

            int[] result = new int[count];
            for (int i=0; i<count; i++)
                result[count-i-1] = values.remove(indexes[i]);

            return result;
        }
    }
}
