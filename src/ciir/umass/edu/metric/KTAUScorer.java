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
import ciir.umass.edu.utilities.RankLibError;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * @author saladi
 * This class implements KTau (Kendall's Tau) as a metric
 */
public class KTAUScorer extends MetricScorer {
	//This class computes Kendall's Tau from the *WHOLE* ranked list.
    // "K" == 1 indicates we should take the average Kendall's Tau of each
    // RankList to get the performance on the entire dataset

	public HashMap<String, Integer> relDocCount = null;

	public MetricScorer copy()
	{
		return new KTAUScorer();
	}

    /**
     * Compute KTAU of multiple lists by summing the inversions for each list
     * and dividing by the total number of pairs
     */
	public double score(List<RankList> rl)
	{
        if(k == 1)
        {
    		double score = 0.0;
    		for(int i = 0; i < rl.size(); i++)
    			score += 1.0 - 2 * countMisorderedPairs(rl.get(i)) / countTotalPairs(rl.get(i));
    		return score/rl.size();
        }
        else
        {
            int total_pairs = 0;
            double nominator = 0.0;
            double denominator = 0.0;

    		for(int i = 0; i < rl.size(); i++)
            {
                total_pairs = countTotalPairs(rl.get(i));
                nominator += total_pairs - 2 * countMisorderedPairs(rl.get(i));
                denominator += total_pairs;
            }
    		return nominator/denominator;
        }
	}

	/**
	 * Compute Kendall's Tau of a single list
	 * @return KTau of the list.
	 */
	public double score(RankList rl)
	{
        return 1.0 - 2 * countMisorderedPairs(rl) / countTotalPairs(rl);
	}

    /**
     * Calculates the number of misorderd pairs.
     * rl must be in order of the prediction
    */
    public int countMisorderedPairs(RankList rl) {
		int misord = 0;

		for(int k = 0; k < rl.size() - 1; k++)
			for(int l = k + 1; l < rl.size(); l++)
				if(rl.get(k).getLabel() < rl.get(l).getLabel())
					misord++;

		return misord;
    }

    /**
     * Calculates the total number of valid pairs (i.e. distinct values)
    */
    public int countTotalPairs(RankList rl) {
		int totpair = 0;

		for(int k = 0; k < rl.size() - 1; k++)
			for(int l = k + 1; l < rl.size(); l++)
				if(rl.get(k).getLabel() != rl.get(l).getLabel())
					totpair++;

		return totpair;
    }

	public String name()
	{
		return "KTAU";
	}
	public double[][] swapChange(RankList rl)
	{
		//NOTE: Compute swap-change *IGNORING* K (consider the entire ranked list)
		int[] relCount = new int[rl.size()];
		int[] labels = new int[rl.size()];
		int count = 0;
		for(int i=0;i<rl.size();i++)
		{
			if(rl.get(i).getLabel() > 0)//relevant
			{
				labels[i] = 1;
				count++;
			}
			else
				labels[i] = 0;
			relCount[i] = count;
		}

		double[][] changes = new double[rl.size()][];
		for(int i = 0; i < rl.size(); i++)
		{
			changes[i] = new double[rl.size()];
			Arrays.fill(changes[i], 0);
		}

		if(count == 0)
			return changes;//all "0"

		for(int i = 0; i < rl.size() - 1; i++)
		{
			for(int j = i + 1; j < rl.size(); j++)
			{
				double change = 0;
				if(labels[i] != labels[j])
				{
					int diff = labels[j]-labels[i];
					change += ((double)((relCount[i]+diff)*labels[j] - relCount[i]*labels[i])) / (i+1);
					for(int k=i+1;k<=j-1;k++)
						if(labels[k] > 0)
							change += ((double)diff) / (k + 1);
					change += ((double)(-relCount[j] * diff)) / (j + 1);
					//It is equivalent to:  change += ((double)(relCount[j]*labels[i] - relCount[j]*labels[j])) / (j+1);
				}
				changes[j][i] = changes[i][j] = change/count;
			}
		}
		return changes;
	}
}
