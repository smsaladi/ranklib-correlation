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
import ciir.umass.edu.utilities.KendallsCorrelation;

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

	public KTAUScorer()
	{
		this.k = 0;
	}
	public MetricScorer copy()
	{
		return new KTAUScorer();
	}

    /**
     * Compute KTAU of multiple lists by summing the inversions for each list
     * and dividing by the total number of pairs
     * Don't use `public double score(RankList rl)` since the calculation will
     * be less precise by going through the KTAU of each list
     */
	public double score(List<RankList> rl)
	{
        if(k == 1)
            return super.score(rl);
        else
        {
            double nominator = 0.0;
            double total_pairs = 0.0;

    		for(int i = 0; i < rl.size(); i++) {
                RankList this_rl = rl.get(i);

                double[] order = new double[this_rl.size()];
                double[] values = new double[this_rl.size()];

        		for(int j = 0; j < this_rl.size(); j++)
        		{
                    order[j] = -1 * j;
                    values[j] = this_rl.get(j).getLabel();
        		}

                double[] ktau = KendallsCorrelation.correlation(order, values);
                nominator += ktau[0];
                total_pairs += ktau[1];
            }
    		return nominator/total_pairs;
        }
	}

	/**
	 * Compute Kendall's Tau of a single list
	 * @return KTau of the list.
	 */
	public double score(RankList rl)
	{
        double[] order = new double[rl.size()];
        double[] values = new double[rl.size()];

		for(int i = 0; i < rl.size(); i++)
		{
            order[i] = -1 * i;
            values[i] = rl.get(i).getLabel();
		}

        double[] ktau = KendallsCorrelation.correlation(order, values);
		return ktau[0] / ktau[1];
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
		int rdCount = 0;//total number of relevant documents
		if(relDocCount != null)//if an external qrels file is specified
		{
			Integer it = relDocCount.get(rl.getID());
			if(it != null)
				rdCount = it;
		}
		else
			rdCount = count;

		double[][] changes = new double[rl.size()][];
		for(int i=0;i<rl.size();i++)
		{
			changes[i] = new double[rl.size()];
			Arrays.fill(changes[i], 0);
		}

		if(rdCount == 0 || count == 0)
			return changes;//all "0"

		for(int i=0;i<rl.size()-1;i++)
		{
			for(int j=i+1;j<rl.size();j++)
			{
				double change = 0;
				if(labels[i] != labels[j])
				{
					int diff = labels[j]-labels[i];
					change += ((double)((relCount[i]+diff)*labels[j] - relCount[i]*labels[i])) / (i+1);
					for(int k=i+1;k<=j-1;k++)
						if(labels[k] > 0)
							change += ((double)diff) / (k+1);
					change += ((double)(-relCount[j]*diff)) / (j+1);
					//It is equivalent to:  change += ((double)(relCount[j]*labels[i] - relCount[j]*labels[j])) / (j+1);
				}
				changes[j][i] = changes[i][j] = change/rdCount;
			}
		}
		return changes;
	}
}
