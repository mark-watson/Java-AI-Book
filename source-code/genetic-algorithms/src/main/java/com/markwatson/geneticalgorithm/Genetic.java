package com.markwatson.geneticalgorithm;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

/**
 * This is an example genetic algorithm library that uses no external libraries.
 *
 * Copyright 1996-2020 Mark Watson. All Rights Reserved. Apache 2 license.
 *
 * For documentation see my book "Practical Artificial Intelligence Programming
 * With Java", chapter "Genetic Algorithms"
 * at https://leanpub.com/javaai that can be read for free online.
 *
 */

abstract public class Genetic {

	protected final int numGenesPerChromosome; // number of genes per chromosome
	protected final int numChromosomes; // number of chromosomes
	List<Chromosome> chromosomes;
	private final double crossoverFraction;
	private final double mutationFraction;
	private final int[] rouletteWheel;
	private final int rouletteWheelSize;

	public Genetic(int numGenesPerChromosome, int numChromosomes) {
		this(numGenesPerChromosome, numChromosomes, 0.8, 0.01);
	}

	public Genetic(int numGenesPerChromosome, int numChromosomes,
			double crossoverFraction, double mutationFraction) {
		this.numGenesPerChromosome = numGenesPerChromosome;
		this.numChromosomes = numChromosomes;
		this.crossoverFraction = crossoverFraction;
		this.mutationFraction = mutationFraction;
		chromosomes = new ArrayList<>(numChromosomes);
		var rng = ThreadLocalRandom.current();
		for (int i = 0; i < numChromosomes; i++) {
			chromosomes.add(new Chromosome(numGenesPerChromosome));
			for (int j = 0; j < numGenesPerChromosome; j++) {
				chromosomes.get(i).setBit(j, rng.nextBoolean());
			}
		}
		sort();
		// Build the roulette wheel for fitness-proportionate selection.
		// Higher-ranked chromosomes (lower index after sorting by descending fitness)
		// get more slots, giving them a higher probability of being selected.
		rouletteWheelSize = buildRouletteWheelSize(numChromosomes);
		System.out.println("count of slots in roulette wheel=" + rouletteWheelSize);
		rouletteWheel = buildRouletteWheel(numChromosomes, rouletteWheelSize);
	}

	private static int buildRouletteWheelSize(int numChromosomes) {
		int size = 0;
		for (int i = 0; i < numChromosomes; i++) {
			size += i + 1;
		}
		return size;
	}

	private static int[] buildRouletteWheel(int numChromosomes, int wheelSize) {
		var wheel = new int[wheelSize];
		int numTrials = numChromosomes;
		int index = 0;
		for (int i = 0; i < numChromosomes; i++) {
			for (int j = 0; j < numTrials; j++) {
				wheel[index++] = i;
			}
			numTrials--;
		}
		return wheel;
	}
	
	public void sort() {
		chromosomes.sort(Comparator.comparingDouble(Chromosome::getFitness).reversed());
	}

	public boolean getGene(int chromosome, int gene) {
		return chromosomes.get(chromosome).getBit(gene);
	}

	public void setGene(int chromosome, int gene, int value) {
		chromosomes.get(chromosome).setBit(gene, value != 0);
	}

	public void setGene(int chromosome, int gene, boolean value) {
		chromosomes.get(chromosome).setBit(gene, value);
	}

	public void evolve() {
		calcFitness();
		sort();
		doCrossovers();
		doMutations();
		doRemoveDuplicates();
	}

	public void doCrossovers() {
		var rng = ThreadLocalRandom.current();
		int num = (int) (numChromosomes * crossoverFraction);
		for (int i = num - 1; i >= 0; i--) {
			// don't overwrite the "best" chromosome from current generation:
			int c1 = rouletteWheel[rng.nextInt(1, rouletteWheelSize)];
			int c2 = rouletteWheel[rng.nextInt(1, rouletteWheelSize)];
			if (c1 != c2) {
				int locus = rng.nextInt(1, numGenesPerChromosome - 1);
				for (int g = 0; g < numGenesPerChromosome; g++) {
					if (g < locus) {
						setGene(i, g, getGene(c1, g));
					} else {
						setGene(i, g, getGene(c2, g));
					}
				}
			}
		}
	}

	public void doMutations() {
		var rng = ThreadLocalRandom.current();
		int num = (int) (numChromosomes * mutationFraction);
		for (int i = 0; i < num; i++) {
			// don't overwrite the "best" chromosome from current generation:
			int c = rng.nextInt(1, numChromosomes);
			int g = rng.nextInt(numGenesPerChromosome);
			setGene(c, g, !getGene(c, g));
		}
	}

	public void doRemoveDuplicates() {
		var rng = ThreadLocalRandom.current();
		for (int i = numChromosomes - 1; i > 3; i--) {
			for (int j = 0; j < i; j++) {
				if (chromosomes.get(i).equals(chromosomes.get(j))) {
					int g = rng.nextInt(numGenesPerChromosome);
					setGene(i, g, !getGene(i, g));
					break;
				}
			}
		}
	}

	// Override the following function in sub-classes:
	abstract public void calcFitness();
}

class Chromosome {
	private final BitSet bits;
	private double fitness = -999;

	public Chromosome(int numGenes) { bits = new BitSet(numGenes); }

	public boolean getBit(int index) {
		return bits.get(index);
	}

	@Override
	public String toString() {
		return "Chromosome[fitness=%.6f, bits=%s]".formatted(fitness, bits);
	}

	public void setBit(int index, boolean value) {
		bits.set(index, value);
	}

	public double getFitness() {
		return fitness;
	}

	public void setFitness(double value) {
		fitness = value;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof Chromosome c)) return false;
		return bits.equals(c.bits);
	}

	@Override
	public int hashCode() {
		return bits.hashCode();
	}
}
