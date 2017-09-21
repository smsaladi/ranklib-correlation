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

import org.apache.commons.math3.stat.correlation.SpearmansCorrelation;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * @author saladi
 * This class implements Spearmans's rho as a metric
 */
public class SPEARScorer extends CORRScorer {
    public SpearmansCorrelation pear = new SpearmansCorrelation();

	public SPEARScorer()
	{

	}
	public SPEARScorer copy()
	{
		return new SPEARScorer();
	}
	public String name()
	{
		return "SPEAR";
	}

    public double scorer(double[] known, double[] pred)
    {
        return pear.correlation(known, pred);
    }
}
