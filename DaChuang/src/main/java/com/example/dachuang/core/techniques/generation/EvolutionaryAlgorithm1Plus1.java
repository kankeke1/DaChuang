/*
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
package com.example.dachuang.core.techniques.generation;



import com.example.dachuang.core.ModelPLEDGE;
import com.example.dachuang.core.Product;
import com.example.dachuang.core.techniques.prioritization.PrioritizationTechnique;

import java.util.List;
import java.util.Random;

/**
 *
 * @author Christopher Henard
 * 
 * This class represents a search-based approach to generate products.
 */
public class EvolutionaryAlgorithm1Plus1 implements GenerationTechnique {

    public static final String NAME = "(1+1) Evolutionary Algorithm";
    private static final Random random = new Random();

    /**
     * Generate products.
     * @param model the application's modeL
     * @param nbProducts the number of products to generate.
     * @param timeAllowed the time allowed in seconds to generate products.
     * @param prioritizationTechnique the prioritization technique to use.
     * @return a list containing the generated products.
     * @throws Exception if a problem occurs during the generation.
     */
    //传入特征模型，数量时间，优化技术
    @Override
    public List<Product> generateProducts(ModelPLEDGE model, int nbProducts, long timeAllowed, PrioritizationTechnique prioritizationTechnique) throws Exception {
        long startTimeMS = System.currentTimeMillis();
        //?
        Individual indiv = new Individual(model, model.getUnpredictableProducts(nbProducts), prioritizationTechnique);
        indiv.fitnessAndOrdering();
        int nbIter = 0;
        //检查当前时间与开始时间之间是否小于给定的时间限制
        while (System.currentTimeMillis() - startTimeMS < timeAllowed) {
            model.setCurrentAction("Iteration number " + (nbIter + 1));
            Individual newIndiv = new Individual(model, indiv, prioritizationTechnique);
            newIndiv.mutate(Individual.MUTATE_WORST, model);
            newIndiv.fitnessAndOrdering();
            if (newIndiv.getFitness() > indiv.getFitness()) {
                indiv = newIndiv;
            }
            nbIter++;
            model.setProgress((int) ((System.currentTimeMillis() - startTimeMS) / (double) timeAllowed * 100.0));
        }
        //方法返回生成的产品列表，这些产品包含在个体indiv中。
        return indiv.getProducts();
    }

    /**
     * Returns the name of this technique.
     * @return a String representing the name of this technique.
     */
    @Override
    public String getName() {
        return NAME;
    }
}
