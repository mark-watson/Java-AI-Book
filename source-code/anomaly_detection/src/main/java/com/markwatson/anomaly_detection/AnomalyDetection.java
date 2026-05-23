package com.markwatson.anomaly_detection;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * AnomalyDetection is a general purpose class for building anomaly detection
 * models. You should use this type of model when you have mostly negative
 * training examples with relatively few positive examples and you need a model
 * that detects positive (anomaly) inputs.
 *
 * <p>The algorithm computes feature means and variances from training data,
 * then uses a multivariate Gaussian probability density to score new examples.
 * An epsilon threshold (tuned on cross-validation data) separates normal
 * examples from anomalies.</p>
 */
public class AnomalyDetection {

  /**
   * Structured container for evaluation metrics returned by {@link #test}.
   */
  public record TestResult(
      double precision,
      double recall,
      double f1,
      int truePositives,
      int falsePositives,
      int trueNegatives,
      int falseNegatives
  ) {}

  /** Seed for reproducible train/cross-validation/test splits. */
  private static final long DEFAULT_SEED = 42L;

  private static final double SQRT_2_PI = Math.sqrt(2.0 * Math.PI);

  private double bestEpsilon = 0.02;
  private double[] mu;            // [num_features]
  private double[] sigmaSquared;  // [num_features]
  private int numFeatures;

  private double[][] trainingExamples;
  private double[][] crossValidationExamples;
  private double[][] testingExamples;
  private int numTrainingExamples;
  private int numCrossValidationExamples;
  private int numTestingExamples;

  public AnomalyDetection() { }

  /**
   * Build an anomaly detection model from the supplied training data.
   *
   * <p>The data is split into training (60 %), cross-validation, and test
   * sets. Only normal (negative) examples go into the training set, with
   * ~10 % of positive examples intentionally leaked in to make the model
   * robust.</p>
   *
   * @param numFeatures          number of features (including the target column)
   * @param numTrainingExamples  number of rows in {@code trainingExamples}
   * @param trainingExamples     [numTrainingExamples][numFeatures]
   */
  public AnomalyDetection(int numFeatures, int numTrainingExamples, double[][] trainingExamples) {
    var training = new ArrayList<double[]>();
    var crossValidation = new ArrayList<double[]>();
    var testing = new ArrayList<double[]>();
    int outcomeIndex = numFeatures - 1; // index of target outcome
    var rng = new Random(DEFAULT_SEED);

    for (int i = 0; i < numTrainingExamples; i++) {
      if (rng.nextDouble() < 0.6) { // 60% training
        // Only keep normal (negative) examples in training, except
        // remember the reason for using this algorithm is that it
        // works with many more negative examples than positive
        // examples, and that the algorithm works even with some positive
        // examples mixed into the training set. The random test is to
        // allow about 10% positive examples to get into the training set:
        if (trainingExamples[i][outcomeIndex] < 0.5 || rng.nextDouble() < 0.1)
          training.add(trainingExamples[i]);
      } else if (rng.nextDouble() < 0.7) {
          crossValidation.add(trainingExamples[i]);
      } else {
        testing.add(trainingExamples[i]);
      }
    }
    this.numTrainingExamples = training.size();
    this.numCrossValidationExamples = crossValidation.size();
    this.numTestingExamples = testing.size();

    this.trainingExamples = training.toArray(new double[0][]);
    this.crossValidationExamples = crossValidation.toArray(new double[0][]);
    this.testingExamples = testing.toArray(new double[0][]);

    this.mu = new double[numFeatures];
    this.sigmaSquared = new double[numFeatures];
    this.numFeatures = numFeatures;
    for (int nf = 0; nf < this.numFeatures; nf++) {
      double sum = 0;
      for (int nt = 0; nt < this.numTrainingExamples; nt++) sum += this.trainingExamples[nt][nf];
      this.mu[nf] = sum / this.numTrainingExamples;
    }
  }

  public double[] muValues()     { return mu; }
  public double[] sigmaSquared() { return sigmaSquared; }
  public double bestEpsilon()    { return bestEpsilon; }

  /**
   * Train the model using a range of epsilon values. Epsilon is a
   * hyper parameter that we want to find a value that
   * minimizes the error count.
   */
  public void train() {
    double bestErrorCount = 1e10;
    for (int epsilonLoop = 0; epsilonLoop < 200; epsilonLoop++) {
      double epsilon = 0.001 + 0.005 * epsilonLoop;
      double errorCount = trainHelper(epsilon);
      if (errorCount <= bestErrorCount) {
        bestErrorCount = errorCount;
        bestEpsilon = epsilon;
      }
    }
    System.out.println("\n**** Best epsilon value = " + bestEpsilon);

    // retrain for the best epsilon setting the maximum likelihood parameters
    // which are now in epsilon, mu[] and sigmaSquared[]:
    trainHelper(bestEpsilon);

    // finally, we are ready to see how the model performs with test data:
    test(bestEpsilon);
  }

  /**
   * Calculate probability p(x; mu, sigma_squared) using the
   * Gaussian PDF:  p(x_i) = (1 / (sqrt(2*pi) * sigma))
   *                         * exp(-(x_i - mu)^2 / (2 * sigma^2))
   *
   * @param x - example vector
   * @return average probability across features
   */
  private double p(double[] x) {
    double sum = 0;
    // use (numFeatures - 1) to skip target output:
    for (int nf = 0; nf < numFeatures - 1; nf++) {
      double sigma = Math.sqrt(sigmaSquared[nf]);
      double exponent = -(x[nf] - mu[nf]) * (x[nf] - mu[nf]) / (2.0 * sigmaSquared[nf]);
      sum += (1.0 / (SQRT_2_PI * sigma)) * Math.exp(exponent);
    }
    return sum / numFeatures;
  }

  /**
   * Returns {@code true} if the input vector is classified as an anomaly.
   *
   * @param x example vector
   * @return true when the Gaussian probability falls below the best epsilon threshold
   */
  public boolean isAnomaly(double[] x) {
    double sum = 0;
    // use (numFeatures - 1) to skip target output:
    for (int nf = 0; nf < numFeatures - 1; nf++) {
      double sigma = Math.sqrt(sigmaSquared[nf]);
      double exponent = -(x[nf] - mu[nf]) * (x[nf] - mu[nf]) / (2.0 * sigmaSquared[nf]);
      sum += (1.0 / (SQRT_2_PI * sigma)) * Math.exp(exponent);
    }
    return (sum / numFeatures) < bestEpsilon;
  }

  /**
   * @deprecated Use {@link #isAnomaly(double[])} instead (fixed spelling).
   */
  @Deprecated(since = "1.1", forRemoval = true)
  public boolean isAnamoly(double[] x) {
    return isAnomaly(x);
  }

  private double trainHelper(double epsilon) {
    int outcomeIndex = numFeatures - 1;
    // use (numFeatures - 1) to skip target output:
    for (int nf = 0; nf < numFeatures - 1; nf++) {
      double sum = 0;
      for (int nt = 0; nt < numTrainingExamples; nt++) {
        sum += (trainingExamples[nt][nf] - mu[nf]) * (trainingExamples[nt][nf] - mu[nf]);
      }
      sigmaSquared[nf] = sum / numTrainingExamples;
    }
    double crossValidationErrorCount = 0;
    for (int nt = 0; nt < numCrossValidationExamples; nt++) {
      var x = crossValidationExamples[nt];
      double calculatedValue = p(x);
      if (x[outcomeIndex] > 0.5) { // target training output is ANOMALY
        // if the calculated value is greater than epsilon
        // then this x vector is not an anomaly (e.g., it
        // is a normal sample):
        if (calculatedValue > epsilon) crossValidationErrorCount += 1;
      } else { // target training output is NORMAL
        if (calculatedValue < epsilon) crossValidationErrorCount += 1;
      }
    }
    System.out.println("   cross_validation_error_count = " + crossValidationErrorCount + " for epsilon = " + epsilon);
    return crossValidationErrorCount;
  }

  private TestResult test(double epsilon) {
    int outcomeIndex = numFeatures - 1;
    int numFalsePositives = 0, numFalseNegatives = 0;
    int numTruePositives = 0, numTrueNegatives = 0;
    for (int nt = 0; nt < numTestingExamples; nt++) {
      var x = testingExamples[nt];
      double calculatedValue = p(x);
      if (x[outcomeIndex] > 0.5) { // target training output is ANOMALY
        if (calculatedValue > epsilon) numFalseNegatives++;
        else                           numTruePositives++;
      } else { // target training output is NORMAL
        if (calculatedValue < epsilon) numFalsePositives++;
        else                           numTrueNegatives++;
      }
    }
    double precision = numTruePositives / (double) (numTruePositives + numFalsePositives);
    double recall = numTruePositives / (double) (numTruePositives + numFalseNegatives);
    double f1 = 2.0 * precision * recall / (precision + recall);

    System.out.println("""

         -- best epsilon = %s
         -- number of test examples = %d
         -- number of false positives = %d
         -- number of true positives = %d
         -- number of false negatives = %d
         -- number of true negatives = %d
         -- precision = %.4f
         -- recall = %.4f
         -- F1 = %.4f""".formatted(
        bestEpsilon, numTestingExamples,
        numFalsePositives, numTruePositives,
        numFalseNegatives, numTrueNegatives,
        precision, recall, f1));

    return new TestResult(precision, recall, f1,
        numTruePositives, numFalsePositives, numTrueNegatives, numFalseNegatives);
  }
}
