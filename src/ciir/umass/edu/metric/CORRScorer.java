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

/**
 * @author saladi
 * This class implements a generic correlation coefficient as a metric
 */
public abstract class CORRScorer extends MetricScorer {
	//This class computes correlation from the *WHOLE* ranked list.

	public HashMap<String, Integer> relDocCount = null;

	public CORRScorer()
	{
		this.k = 0;
	}

    /**
     * The scoring calculation to use
     */
    public abstract double scorer(double[] known, double[] pred);

	/**
	 * Compute correlation of a single list
	 */
	public double score(RankList rl)
	{
        double[] known = new double[rl.size()];
        double[] pred = new double[rl.size()];

        double first_label = rl.get(0).getLabel();
        boolean all_same = true;

		for(int i = 0; i < rl.size(); i++)
		{
            known[i] = rl.get(i).getLabel();
            if (first_label != known[i])
                all_same = false;

            pred[i] = rl.get(i).getCached();
		}

        if (all_same)
            return 0;

        return scorer(known, pred);
	}

	public double[][] swapChange(RankList rl)
	{
		double[][] changes = new double[rl.size()][rl.size()];

        // check if the list has all of the same labels
        double first_label = rl.get(0).getLabel();
        boolean all_same = true;
		for(int i = 1; i < rl.size(); i++)
            if (first_label != rl.get(i).getLabel())
                all_same = false;
        if (all_same)
            return changes;

        double init_corr = score(rl);
        // Calculate changes upon swaps
		for(int i = 0; i < rl.size() - 1; i++)
			for(int j = i + 1; j < rl.size(); j++) {
                rl.swap(i, j); // do swap
		        changes[j][i] = changes[i][j] = score(rl)/init_corr;
                rl.swap(i, j); // swap back
            }

		return changes;
    }
}
