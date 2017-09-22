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

import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;

/**
 * @author saladi
 * This class implements Pearson's r as a metric
 */
public class PEARScorer extends CORRScorer {
    public PearsonsCorrelation pear = new PearsonsCorrelation();

	public PEARScorer()
	{

	}
	public PEARScorer copy()
	{
		return new PEARScorer();
	}
	public String name()
	{
		return "PEAR";
	}

    public double scorer(double[] known, double[] pred)
    {
        return pear.correlation(known, pred);
    }
}
