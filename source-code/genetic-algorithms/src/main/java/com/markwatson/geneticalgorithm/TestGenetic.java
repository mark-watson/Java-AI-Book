package com.markwatson.geneticalgorithm;

/**
 * This is an example of genetic algorithm test class.
 *
 * Copyright 1996-2020 Mark Watson. All Rights Reserved. Apache 2 license.
 *
 * For documentation see my book "Practical Artificial Intelligence Programming
 * With Java", chapter "Genetic Algorithms"
 * at https://leanpub.com/javaai that can be read for free online.
 *
 */

public class TestGenetic {

    static MyGenetic geneticExperiment;

    public static void main(String[] args) {
        // we will use chromosomes with 10 1 bit genes per
        // chromosomes, and a population of 12 chromosomes:
        geneticExperiment = new MyGenetic(10, 20, 0.85, 0.3);
        int geneIndex = 0; //  debug only
        for (Chromosome chromosome : geneticExperiment.chromosomes) {
            System.out.printf("%s : %.6f%n", chromosome, geneticExperiment.geneToDouble(geneIndex++));
        }
        int numCycles = 15000;
        for (int i = 0; i < numCycles; i++) {
            geneticExperiment.evolve();
            if ((i % (numCycles / 100)) == 0 || i == (numCycles - 1)) {
                System.out.println("Generation " + i);
                geneticExperiment.calcFitness(); // suggested by Rick Hall
                geneticExperiment.sort();        // suggested by Rick Hall
                geneticExperiment.print();
            }
        }
    }
}

class MyGenetic extends Genetic {
    MyGenetic(int numGenes, int numChromosomes, double crossoverFraction,
              double mutationFraction) {
        super(numGenes, numChromosomes, crossoverFraction, mutationFraction);
    }

    private double fitness(double x) {
        return Math.sin(x) * Math.sin(0.4 * x) * Math.sin(3.0 * x);
    }

    double geneToDouble(int chromosomeIndex) {
        int base = 1;
        double x = 0;
        for (int j = 0; j < numGenesPerChromosome; j++) {
            if (getGene(chromosomeIndex, j)) {
                x += base;
            }
            base *= 2;
        }
        x /= 102.4;
        return x;
    }

    public void calcFitness() {
        for (int i = 0; i < numChromosomes; i++) {
            double x = geneToDouble(i);
            chromosomes.get(i).setFitness(fitness(x));
        }
    }

    public void print() {
        double sum = 0.0;
        for (int i = 0; i < numChromosomes; i++) {
            double x = geneToDouble(i);
            sum += chromosomes.get(i).getFitness();
            System.out.printf("Fitness for chromosome %d is %.6f, occurs at x=%.6f%n",
                    i, chromosomes.get(i).getFitness(), x);
        }
        sum /= numChromosomes;
        System.out.printf("Average fitness=%.6f and best fitness for this generation:%.6f%n",
                sum, chromosomes.get(0).getFitness());
    }
}
