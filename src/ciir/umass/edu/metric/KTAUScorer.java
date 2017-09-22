/*===============================================================================
 * Copyright (c) 2010-2012 University of Massachusetts.  All Rights Reserved.
 *
 * Use of the RankLib package is subject to the terms of the software license set
 * forth in the LICENSE file included with this software, and also available at
 * http://people.cs.umass.edu/~vdang/ranklib_license.html
 *
 * This code was adapted from APScorer.java by Shyam Saladi (saladi@caltech.edu)
 *===============================================================================
 */

package ciir.umass.edu.metric;

import ciir.umass.edu.learning.RankList;

import java.util.HashMap;
import java.util.List;
import java.lang.ArithmeticException;

/**
 * @author saladi
 * This class implements KTau (Kendall's Tau) as a metric
 */
public class KTAUScorer extends MetricScorer {
	//This class computes Kendall's Tau from the *WHOLE* ranked list.
    // "K" == 1 indicates we should take the average Kendall's Tau of each
    // RankList to get the performance on the entire dataset

	public HashMap<String, Integer> relDocCount = null;

    public KTAUScorer()
    {

    }
	public KTAUScorer copy()
	{
		return new KTAUScorer();
	}
	public String name()
	{
		return "KTAU";
	}
    /**
     * Compute KTAU of multiple lists by summing the inversions for each list
     * and dividing by the total number of pairs
     */
	public double score(List<RankList> rl)
	{
        if(k == 1)
    		return super.score(rl);
        else
        {
            int[] stats = new int[2];
            int nominator = 0;
            int denominator = 0;

    		for(int i = 0; i < rl.size(); i++)
            {
                stats = countTotalMisorderedPairs(rl.get(i));
                nominator += stats[0] - 2 * stats[1];
                denominator += stats[0];
            }
    		return (double)nominator/denominator;
        }
	}

	/**
	 * Compute Kendall's Tau of a single list
	 * @return KTau of the list.
	 */
	public double score(RankList rl)
	{
        try {
            int[] stats = countTotalMisorderedPairs(rl);
            // System.out.println("total_pairs:" + stats[0] + "; misord:" + stats[1]);
            return 1 - 2.0 * stats[1] / stats[0];
        } catch (ArithmeticException e) {
            // if no valid pairs (i.e. divide by zero), then this grouping
            // is not informative
            return 0;
        }
	}

    /**
     * Calculates the number of valid (i.e. distinct values) and misorderd pairs
     * rl must be in order of the prediction for correct calculation of misordered
     * pairs (smaller index = higher ranking)
    */
    public int[] countTotalMisorderedPairs(RankList rl) {
        // 0: total pairs; 1: misordered pairs
		int[] stats = {0, 0};

		for(int k = 0; k < rl.size() - 1; k++)
			for(int l = k + 1; l < rl.size(); l++)
				if(rl.get(k).getLabel() != rl.get(l).getLabel()) {
					stats[0]++;
    				if(rl.get(k).getLabel() < rl.get(l).getLabel())
    					stats[1]++;
                }
		return stats;
    }

    /**
     * higher label is better, lower index is better
     */
	public double[][] swapChange(RankList rl)
	{
		byte[][] is_ord = new byte[rl.size()][rl.size()];
		double[][] changes = new double[rl.size()][rl.size()];

        // fill with initial ordering
        // init_tau is actually concordant - disconcorant pairs, i.e. Kendall's distance
        double init_kend = 0;
		for(int i = 0; i < rl.size() - 1; i++)
			for(int j = i + 1; j < rl.size(); j++)
				if(rl.get(i).getLabel() > rl.get(j).getLabel())
                    init_kend += is_ord[i][j] = 1;
                else if (rl.get(i).getLabel() < rl.get(j).getLabel())
                    init_kend += is_ord[i][j] = -1;

        // Calculate changes upon swaps
		for(int i = 0; i < rl.size() - 1; i++)
			for(int j = i + 1; j < rl.size(); j++) {
                rl.swap(i, j); // do swap

                // recalculate change in ordering
                int delta = 0;
        		for(int k = i + 1; k < rl.size(); k++) // for row i
                    delta += calcChange(is_ord[i][k], rl.get(i).getLabel(), rl.get(k).getLabel());
        		for(int k = 0; k < i; k++) // for col i
                    delta += calcChange(is_ord[k][i], rl.get(k).getLabel(), rl.get(i).getLabel());
        		for(int k = j + 1; k < rl.size(); k++) // for row j
                    delta += calcChange(is_ord[j][k], rl.get(j).getLabel(), rl.get(k).getLabel());
        		for(int k = 0; k < j; k++) // for col j
                    delta += calcChange(is_ord[k][j], rl.get(k).getLabel(), rl.get(j).getLabel());

                // overcounting of pair i, j
                delta -= calcChange(is_ord[i][j], rl.get(i).getLabel(), rl.get(j).getLabel());

		        changes[j][i] = changes[i][j] = delta/init_kend;

                rl.swap(i, j); // swap back
            }
		return changes;
	}

    public int calcChange(int orig, float higher_score, float lower_score) {
        if (orig == 1 && higher_score < lower_score)
            return -2;
        else if (orig == -1 && higher_score > lower_score)
            return 2;
        return 0;
    }
}
