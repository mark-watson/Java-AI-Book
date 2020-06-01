# Genetic Algorithms {#ga}

We have seen greedy search algorithms find locally good results that are much worse than the best possible solutions. Genetic Algorithms (GAs) are an efficient means of finding near optimum solutions.

GAs are computer simulations to evolve a population
of chromosomes that contain at least some very fit individuals. Fitness
is specified by a fitness function that rates each individual in the
population.

Setting up a GA simulation is fairly easy: we need to represent (or
encode) the state of a system in a chromosome that is usually
implemented as a set of bits. GA is basically a search operation:
searching for a good solution to a problem where the solution is a very
fit chromosome. The programming technique of using GA is useful for AI
systems that must adapt to changing conditions because “re-programming”
can be as simple as defining a new fitness function and re-running the
simulation. An advantage of GA is that the search process will not often
“get stuck” in local minimum because the genetic crossover process
produces radically different chromosomes in new generations while
occasional mutations (flipping a random bit in a chromosome) cause small
changes. Another aspect of GA is supporting the evolutionary concept of
“survival of the fittest”: by using the fitness function we will
preferentially “breed” chromosomes with higher fitness values.

It is interesting to compare how GAs are trained with how we train
neural networks ([Chapter on Neural Networks](#neural-networks)). We need to
manually “supervise” the training process: for GAs we need to supply a
fitness function and for the two neural network models used in [Chapter on Neural Networks](#neural-networks) we need to supply training data with desired
sample outputs for sample inputs.


## Theory

GAs are typically used to search very large and possibly very high
dimensional search spaces. If we want to find a solution as a single
point in an **N** dimensional space where a fitness function has a near
maximum value, then we have **N** parameters to encode in each chromosome.
In this chapter we will be solving a simple problem that is
one-dimensional so we only need to encode a single number (a floating
point number for this example) in each chromosome. Using a GA toolkit,
like the one developed in Section [section:java-ga-lib], requires two
problem-specific customizations:

-   Characterize the search space by a set of parameters that can be
    encoded in a chromosome (more on this later). GAs work with the
    coding of a parameter set, not the parameters themselves (*Genetic
    Algorithms in Search, Optimization, and Machine Learning*, David
    Goldberg, 1989).

-   Provide a numeric fitness function that allows us to rate the
    fitness of each chromosome in a population. We will use these
    fitness values to determine which chromosomes in the population are
    most likely to survive and reproduce using genetic crossover and
    mutation operations.

The GA toolkit developed in this chapter treats genes as a single bit;
while you can consider a gene to be an arbitrary data structure, the
approach of using single bit genes and specifying the number of genes
(or bits) in a chromosome is very flexible. A population is a set of
chromosomes. A generation is defined as one reproductive cycle of
replacing some elements of the chromosome population with new
chromosomes produced by using a genetic crossover operation followed by
optionally mutating a few chromosomes in the population.

We will describe a simple example problem in this section, write a
general purpose library in [Section on Java Library for Genetic Algorithms](#java-ga-lib), and finish
the chapter in the [Section on Java Genetic Alforihm Example](#java-ga-example)
by solving the problem posed in this section.

{#ga-sample-function}
![Example Function](images/ga_sample_function.png)

For a sample problem, suppose that we want to find the maximum value of
the function **F** with one independent variable **x** in Equation
[math:ga~f~unc1] and as seen in last figure:

**F(x) = sin(x) * sin(0.4 * x) * sin(3 * x)**

The problem that we want to solve is finding a good value of **x** to find
a near to possible maximum value of **F(x)**. To be clear: we encode a
floating point number as a chromosome made up of a specific number of
bits so any chromosome with randomly set bits will represent some random
number in the interval [0, 10]. The fitness function is simply the
function in Equation [math:ga~f~unc1].

{#ga-crossover}
![Crosssover Operation](images/ga_crossover.png)

[Figure showing genetic crossover operation](#ga-crossover) shows an example of a crossover operation hat we will implemet later in te program example. A
random chromosome bit index is chosen, and two chromosomes are “cut” at
this this index and swap cut parts. The two original chromosomes in
**generation_n** are shown on the left of the figure and after the
crossover operation they produce two new chromosomes in
**generation_{n+1}** shown on the right of the figure.

In addition to using crossover operations to create new chromosomes from
existing chromosomes, we will also use genetic mutation: randomly
flipping bits in chromosomes. A fitness function that rates the fitness
value of each chromosome allows us to decide which chromosomes to
discard and which to use for the next generation: we will use the most
fit chromosomes in the population for producing the next generation
using crossover and mutation.

We will implement a general purpose Java GA library in the next section
and then solve the example problem posed in this section at the end of this
chapter in the [GA Example Section](#java-ga-example).


## Java Library for Genetic Algorithms  {#java-ga-lib}


The full implementation of the GA library is in the Java source file
Genetic.java. The following code snippets shows the method signatures
defining the public API for the library; note that there are two
constructors, the first using default values for the fraction of
chromosomes on which to perform crossover and mutation operations and
the second constructor allows setting explicit values for these
parameters:

~~~~~~~~
    abstract public class Genetic {
      public Genetic(int num_genes_per_chromosome,
                     int num_chromosomes)
      public Genetic(int num_genes_per_chromosome,
                     int num_chromosomes,
                     float crossover_fraction,
                     float mutation_fraction)
~~~~~~~~

The method **sort** is used to sort the population of chromosomes in most
fit first order. The methods **getGene** and **setGene** are used to fetch
and change the value of any gene (bit) in any chromosome. These methods
are protected but you will probably not need to override them in derived
classes.

~~~~~~~~
      protected void sort()
      protected boolean getGene(int chromosome,
                                int gene)
      protected void setGene(int chromosome,
                             int gene, int value)
      protected void setGene(int chromosome, 
                             int gene,
                             boolean value)
~~~~~~~~

The methods **evolve**, **doCrossovers**, **doMutations**, and
**doRemoveDuplicates** are utilities for running GA simulations. These
methods are protected but you will probably not need to override them in
derived classes.

~~~~~~~~
      protected void evolve()
      protected void doCrossovers()
      protected void doMutations()
      protected void doRemoveDuplicates()
~~~~~~~~

When you subclass class **Genetic** you must implement the following
abstract method **calcFitness** that will determine the evolution of
chromosomes during the GA simulation.

~~~~~~~~
      // Implement the following method in sub-classes:
      abstract public void calcFitness();
    }
~~~~~~~~

The class **Chromosome** represents a bit set with a specified number of
bits and a floating point fitness value.

~~~~~~~~
    class Chromosome {
      private Chromosome()
      public Chromosome(int num_genes)
      public boolean getBit(int index) 
      public void setBit(int index, boolean value)
      public float getFitness()
      public void setFitness(float value)
      public boolean equals(Chromosome c)
    }
~~~~~~~~

The class **ChromosomeComparator** implements a **Comparator** interface and
is application specific: it is used to sort a population in “best first”
order:

~~~~~~~~
    class ChromosomeComparator
          implements Comparator<Chromosome> {
      public int compare(Chromosome o1,
                         Chromosome o2)
    }
~~~~~~~~

The last class **ChromosomeComparator** is used when using the Java
**Collection** class static **sort** method.

The class **Genetic** is an abstract class: you must subclass it and
implement the method **calcFitness** that uses an application specific
fitness function (that you must supply) to set a fitness value for each
chromosome.

This GA library provides the following behavior:

-   Generates an initial random population with a specified number of
    bits (or genes) per chromosome and a specified number of chromosomes
    in the population

-   Ability to evaluate each chromosome based on a numeric fitness
    function

-   Ability to create new chromosomes from the most fit chromosomes in
    the population using the genetic crossover and mutation operations

There are two class constructors for Genetic set up a new GA experiment
by setting the number of genes (or bits) per chromosome, and the number
of chromosomes in the population.

The **Genetic** class constructors build an array of integers
**rouletteWheel** which is used to weight the most fit chromosomes in the
population for choosing the parents of crossover and mutation
operations. When a chromosome is being chosen, a random integer is
selected to be used as an index into the **rouletteWheel** array; the
values in the array are all integer indices into the chromosome array.
More fit chromosomes are heavily weighted in favor of being chosen as
parents for the crossover operations. The algorithm for the crossover
operation is fairly simple; here is the implementation:

~~~~~~~~
     public void doCrossovers() {
       int num = (int)(numChromosomes * crossoverFraction);
       for (int i = num - 1; i >= 0; i--) {
         // Don't overwrite the "best" chromosome
         // from current generation:
         int c1 = 1 + (int) ((rouletteWheelSize - 1) *
                             Math.random() * 0.9999f);
         int c2 = 1 + (int) ((rouletteWheelSize - 1) *
                             Math.random() * 0.9999f);
         c1 = rouletteWheel[c1];
         c2 = rouletteWheel[c2];
         if (c1 != c2) {
           int locus = 1+(int)((numGenesPerChromosome-2) *
                               Math.random());
           for (int g = 0; g<numGenesPerChromosome; g++) {
             if (g < locus) {
               setGene(i, g, getGene(c1, g));
             } else {
               setGene(i, g, getGene(c2, g));
             }
           }
         }
       }
     }
~~~~~~~~

The method **doMutations** is similar to **doCrossovers**: we randomly
choose chromosomes from the population and for these selected
chromosomes we randomly “flip” the value of one gene (a gene is a bit in
our implementation):

~~~~~~~~
      public void doMutations() {
        int num = (int)(numChromosomes * mutationFraction);
        for (int i = 0; i < num; i++) {
          // Don't overwrite the "best" chromosome
          // from current generation:
          int c = 1 + (int) ((numChromosomes - 1) *
                             Math.random() * 0.99);
          int g = (int) (numGenesPerChromosome *
                         Math.random() * 0.99);
          setGene(c, g, !getGene(c, g));
        }
      }
~~~~~~~~

We developed a general purpose library in this section for simulating
populations of chromosomes that can evolve to a more “fit” population
given a fitness function that ranks individual chromosomes in order of
fitness. In Section [section:java-ga-example] we will develop an
example GA application by defining the size of a population and the
fitness function defined by Equation [math:ga~f~unc1].


## Finding the Maximum Value of a Function  {#java-ga-example}


We will use the Java library in the last section to develop an example
application to find the maximum of the function seen in the [Figure showing a sample function](#ga-sample-function) which shows a plot of our test fucntion that we are using a GA to fit, plotted in the interval [0, 10].

While we could find the maximum value of this function by using Newton’s
method (or even a simple brute force search over the range of the
independent variable **x**), the GA method scales very well to similar problems of higher
dimensionality. The GA also helps us to not find just locally optimum
solutions. In this example we are working in one dimension so we only
need to encode a single variable in a chromosome. As an example of a
higher dimensional system, we might have products of sine waves using 20
independent variables **x1, x2, ..x20**. Still, the one-dimensional case
seen in the [Figure showin the sample function](#ga-sample-function) is a good example for showing you how to set
up a GA simulation.

Our first task is to characterize the search space as one or more
parameters. In general when we write GA applications we might need to
encode several parameters in a single chromosome. For example, if a
fitness function has three arguments we would encode three numbers in a
singe chromosome. In this example problem, we have only one parameter,
the independent variable x. We will encode the parameter x using ten
bits (so we have ten 1-bit genes per chromosome). A good starting place
is writing utility method for converting the 10-bit representation to a
floating-point number in the range [0.0, 10.0]:

~~~~~~~~
    float geneToFloat(int chromosomeIndex) {
      int base = 1;
      float x = 0;
      for (int j=0; j<numGenesPerChromosome; j++)  {
         if (getGene(chromosomeIndex, j)) {
            x += base;
         }
         base *= 2;
      }
~~~~~~~~

After summing up all on bits times their **base_2** value, we need to
normalize what is an integer in the range of [0,1023] to a floating
point number in the approximate range of [0, 10]:

~~~~~~~~
      x /= 102.4f;
      return x;
    }
~~~~~~~~

Note that we do not need the reverse method! We use the GA library from
Section [section:java-ga-lib] to create a population of 10-bit
chromosomes; in order to evaluate the fitness of each chromosome in a
population, we only have to convert the 10-bit representation to a
floating-point number for evaluation using the following fitness
function (Equation [math:ga~f~unc1]):

~~~~~~~~
    private float fitness(float x) {
      return (float)(Math.sin(x) *
                     Math.sin(0.4f * x) *
                     Math.sin(3.0f * x));
    }
~~~~~~~~

Table [tab:chrom~e~ncoding] shows some sample random chromosomes and the
floating point numbers that they encode. The first column shows the gene
indices where the bit is “on,” the second column shows the chromosomes
as an integer number represented in binary notation, and the third
column shows the floating point number that the chromosome encodes. The
center column in the followin table shows the bits in order
where index 0 is the left-most bit, and index 9 if the right-most bit;
this is the reverse of the normal order for encoding integers but the GA
does not care: it works with any encoding we use. Once again, GAs work
with encodings.

~~~~~~~~
  “On bits” in chromosome   As binary   Number encoded
  -----------------------   ---------   --------------
  2, 5, 7, 8, 9             0010010111  9.1015625
  0, 1, 3, 5, 6             1101011000  1.0449219
  0, 3, 5, 6, 7, 8          1001011110  4.7753906
~~~~~~~~


Using methods **geneToFloat** and **fitness** we now implement the abstract
method **calcFitness** from our GA library class **Genetic** so the derived
class **TestGenetic** is not abstract. This method has the responsibility
for calculating and setting the fitness value for every chromosome
stored in an instance of class **Genetic**:

~~~~~~~~
    public void calcFitness() {
      for (int i=0; i<numChromosomes; i++) {
        float x = geneToFloat(i);
        chromosomes.get(i).setFitness(fitness(x));
      }
    }
~~~~~~~~

While it was useful to make this example more clear with a separate
**geneToFloat** method, it would have also been reasonable to simply place
the formula in the method **fitness** in the implementation of the
abstract (in the base class) method **calcFitness**.

In any case we are done with coding this example: you can compile the
two example Java files Genetic.java and TestGenetic.java, and run the
**TestGenetic** class to verify that the example program quickly finds a
near maximum value for this function.

You can try setting different numbers of chromosomes in the population
and try setting non-default crossover rates of 0.85 and a mutation rates
of 0.3. We will look at a run with a small number of chromosomes in the
population created with:

~~~~~~~~
      genetic_experiment =
                     new MyGenetic(10, 20, 0.85f, 0.3f);
      int NUM_CYCLES = 500;
      for (int i=0; i<NUM_CYCLES; i++) {
        genetic_experiment.evolve();
        if ((i%(NUM_CYCLES/5))==0 || i==(NUM_CYCLES-1)) {
          System.out.println("Generation " + i);
          genetic_experiment.print();
        }
      }
~~~~~~~~

In this experiment 85% of chromosomes will be “sliced and diced” with a
crossover operation and 30% will have one of their genes changed. We
specified 10 bits per chromosome and a population size of 20
chromosomes. In this example, I have run 500 evolutionary cycles. After
you determine a fitness function to use, you will probably need to
experiment with the size of the population and the crossover and
mutation rates. Since the simulation uses random numbers (and is thus
non-deterministic), you can get different results by simply rerunning
the simulation. Here is example program output (with much of the output
removed for brevity):

~~~~~~~~
    count of slots in roulette wheel=55
    Generation 0
    Fitness for chromosome 0 is 0.505, occurs at x=7.960
    Fitness for chromosome 1 is 0.461, occurs at x=3.945
    Fitness for chromosome 2 is 0.374, occurs at x=7.211
    Fitness for chromosome 3 is 0.304, occurs at x=3.929
    Fitness for chromosome 4 is 0.231, occurs at x=5.375
    ...
    Fitness for chromosome 18 is -0.282 occurs at x=1.265
    Fitness for chromosome 19 is -0.495, occurs at x=5.281
    Average fitness=0.090 and best fitness for this
    generation:0.505
    ...
    Generation 499
    Fitness for chromosome 0 is 0.561, occurs at x=3.812
    Fitness for chromosome 1 is 0.559, occurs at x=3.703
    ...
~~~~~~~~

This example is simple but is intended to be show you how to encode
parameters for a problem where you want to search for values to maximize
a fitness function that you specify. Using the library developed in this
chapter you should be able to set up and run a GA simulation for your
own applications.

