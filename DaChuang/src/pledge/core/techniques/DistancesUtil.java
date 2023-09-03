/*
 * 
 * Author : Christopher Henard (christopher.henard@uni.lu)
 * Date : 01/11/2012
 * Copyright 2012 University of Luxembourg – Interdisciplinary Centre for Security Reliability and Trust (SnT)
 * All rights reserved
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package pledge.core.techniques;

import java.util.HashSet;
import java.util.Set;
import pledge.core.Product;

/**
 * This class defines some common normalized distances to measure the 
 * distance between two products.
 * 
 * @author Christopher Henard
 */
public class DistancesUtil {

    private static double getSetBasedDistance(Product p1, Product p2, double weight) {
        Set<Integer> intersection = new HashSet<Integer>(p1);
        Set<Integer> union = new HashSet<Integer>(p1);
        intersection.retainAll(p2);
        union.addAll(p2);
        double intersectionSize = intersection.size();
        double unionSize = union.size();

        return 1.0 - (intersectionSize / (intersectionSize + weight * (unionSize - intersectionSize)));
    }

    /**
     * Return the jaccard distance between two products.
     * @param p1 the first product to consider.
     * @param p2 the second product to consider.
     * @return the resulting jaccard distance between p1 and p2
     */
    public static double getJaccardDistance(Product p1, Product p2) {
        return getSetBasedDistance(p1, p2, 1.0);
    }

    /**
     * Return the dice distance between two products.
     * @param p1 the first product to consider.
     * @param p2 the second product to consider.
     * @return the resulting dice distance between p1 and p2
     */
    public static double getDiceDistance(Product p1, Product p2) {
        return getSetBasedDistance(p1, p2, 0.5);
    }

    /**
     * Return the anti dice distance between two products.
     * @param p1 the first product to consider.
     * @param p2 the second product to consider.
     * @return the resulting anti dice distance between p1 and p2
     */
    public static double getAntiDiceDistance(Product p1, Product p2) {
        return getSetBasedDistance(p1, p2, 2.0);
    }
}
