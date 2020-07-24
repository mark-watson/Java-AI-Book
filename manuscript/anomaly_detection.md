# Anomaly Detection Machine Learning Example

Anomaly detection models are used in one very specific class of use cases: when you have many negative (non-anomaly) examples and relatively few positive (anomaly) examples. We can refer to this as an unbalanced training set. For training we will ignore positive examples, create a model of "how things should be," and hopefully be able to detect anomalies different from the original negative examples.

If you have a large training set of both negative and positive examples then do not use anomaly detection models. If your training examples are balanced then use a classification model as we will see later in the chapter [Deep Learning Using Deeplearning4j](#dl4j).


## Motivation for Anomaly Detection

There are two other examples in this book using the University of Wisconsin cancer data. These other examples are supervised learning. Anomaly detection as we do it in this chapter is, more or less, unsupervised learning.

When should we use anomaly detection? You should use supervised learning algorithms like neural networks and logistic classification when there are roughly equal number of available negative and positive examples in the training data. The University of Wisconsin cancer data set is fairly evenly split between negative and positive examples.

Anomaly detection should be used when you have many negative ("normal") examples and relatively few positive ("anomaly") examples. For the example in this chapter we will simulate scarcity of positive ("anomaly") results by preparing the data using the Wisconsin cancer data as follows:

- We will split the data into training (60%), cross validation (20%) and testing (20%).
- For the training data, we will discard all but two positive ("anomaly") examples. We do this to simulate the real world test case where some positive examples are likely to end up in the training data in spite of the fact that we would prefer the training data to only contain negative ("normal") examples.
- We will use the cross validation data to find a good value for the epsilon meta parameter.
- After we find a good epsilon value, we will calculate the F1 measurement for the model.


## Math Primer for Anomaly Detection

We are trying to model "normal" behavior and we do this by taking each feature and fitting a Gaussian (bell curve) distribution to each feature. The learned parameters for a Gaussian distribution are the mean of the data (where the bell shaped curve is centered) and the variance. You might be more familiar with the term standard deviation, {$$}\sigma{/$$}. Variance is defined as {$$} \sigma ^2{/$$}.

We will need to calculate the probability of a value **x** given the mean and variance of a probability distribution: {$$}P(x : \mu, \sigma ^2){/$$} where {$$}\mu{/$$} is the mean and {$$} \sigma ^2{/$$}
 is the squared variance:

{$$}
P(x : \mu, \sigma ^2) = \frac{1}{{\sigma \sqrt {2\pi } }}e^{{{ - \left( {x - \mu } \right)^2 } \mathord{\left/ {\vphantom {{ - \left( {x - \mu } \right)^2 } {2\sigma ^2 }}} \right. \kern-\nulldelimiterspace} {2\sigma ^2 }}}
{/$$}

where {$$}x_i{/$$} are the samples and we can calculate the squared variance as:

{$$}
\sigma^2 = \frac{\displaystyle\sum_{i=1}^{m}(x_i - \mu)^2} {m}
{/$$}

We calculate the parameters of {$$}\mu{/$$} and {$$} \sigma ^2{/$$} for each feature. A bell shaped distribution in two dimensions is easy to visualize as is an inverted bowl shape in three dimentions. What if we have many features? Well, the math works and don't worry about not being able to picture it in your mind.
 

## AnomalyDetection Utility Class

The class **AnomalyDetection** developed in this section is fairly general purpose. It processes a set of training examples and for each feature calculates {$$}\mu{/$$} and {$$} \sigma ^2{/$$}. We are also training for a third parameter: an epsilon "cutoff" value: if for a given input vector if {$$}P(x : \mu, \sigma ^2){/$$} evaluates to a value greater than epsilon then the input vector is "normal", less than epsilon implies that the input vector is an "anomaly." The math for calulating these three features from training data is fairly easy but the code is not: we need to organize the training data and search for a value of epsilon that minimizes the error for a cross validaton data set.

To be clear: we separate the input examples into three separate sets of training, cross validation, and testing data. We use the training data to set the model parameters, use the cross validation data to learn an epsilon value, and finally use the testing data to get precision, recall, and F1 scores that indicate how well the model detects anomalies in data not used for training and cross validation.

I present the example program as one long listing, with more code explanation after the listing. Please note the long loop over each input training example starting at line 28 and ending on line 74. The code in lines 25 through 44 processes the input training data sample into three disjoint sets of training, cross validation, and testing data. Then the code in lines 45 through 63 copies these three sets of data to Java arrays.

The code in lines 65 through 73 calculates, for a training example, the value of {$$}\mu{/$$} (the varible **mu** in the code).

Please note in the code example that I prepend class variables used in methods with "this." even when it is not required. I do this for legibility and is a personal style.

The following UML class diagram will give you an overview before we dive into the code:

![UML class diagram for the anomaly detection example](images/anomaly-uml.png)


{lang="java",linenos=on}
~~~~~~~~
package com.markwatson.anomaly_detection;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by markw on 10/7/15.
 */
public class AnomalyDetection {

  public AnomalyDetection() { }

  /**
   * AnomalyDetection is a general purpose class for building anomaly detection
   * models. You should use this type of mdel when you have mostly negative
   * training examples with relatively few positive examples and you need a model
   * that detects postive (anomaly) inputs.
   *
   * @param num_features
   * @param num_training_examples
   * @param training_examples [num_training_examples][num_features]
   */
  public AnomalyDetection(int num_features, int num_training_examples,
                          double [][] training_examples) {
    List<double[]> training = new ArrayList<>();
    List<double []> cross_validation = new ArrayList<>();
    List<double []> testing = new ArrayList<>();
    int outcome_index = num_features - 1; // index of target outcome
    for (int i=0; i<num_training_examples; i++) {
      if (Math.random() < 0.6) { // 60% training
        // Only keep normal (negative) examples in training, except
        // remember the reason for using this algorithm is that it
        // works with many more negative examples than positive
        // examples, and that the algorithm works even with some positive
        // examples mixed into the training set. The random test is to
        // allow about 10% positive examples to get into the training set:
        if (training_examples[i][outcome_index] < 0.5 || Math.random() < 0.1)
          training.add(training_examples[i]);
      } else if (Math.random() < 0.7) {
          cross_validation.add(training_examples[i]);
      } else {
        testing.add(training_examples[i]);
      }
    }
    this.num_training_examples = training.size();
    this.num_cross_validation_examples = cross_validation.size();
    this.num_testing_examples = testing.size();

    this.training_examples = new double[this.num_training_examples][];
    for (int k=0; k<this.num_training_examples; k++) {
      this.training_examples[k] = training.get(k);
    }

    this.cross_validation_examples = new double[num_cross_validation_examples][];
    for (int k=0; k<num_cross_validation_examples; k++) {
      this.cross_validation_examples[k] = cross_validation.get(k);
    }

    this.testing_examples = new double[num_testing_examples][];
    for (int k=0; k<num_testing_examples; k++) {
      // dimensions of [num_training_examples][num_features]:
      this.testing_examples[k] = testing.get(k);
    }

    this.mu = new double[num_features];
    this.sigma_squared = new double[num_features];
    this.num_features = num_features;
    for (int nf = 0; nf < this.num_features; nf++) { //
      double sum = 0;
      for (int nt = 0; nt < this.num_training_examples; nt++)
        sum += training_examples[nt][nf];
      this.mu[nf] = sum / this.num_training_examples;
    }
  }

  public double [] muValues()     { return mu; }
  public double [] sigmaSquared() { return sigma_squared; }
  public double bestEpsilon()     { return best_epsilon; }

  /**
   *
   * Train the model using a range of epsilon values. Epsilon is a
   * hyper parameter that we want to find a value that
   * minimizes the error count.
   */
  public void train() {
    double best_error_count = 1e10;
    for (int epsilon_loop=0; epsilon_loop<40; epsilon_loop++) {
      double epsilon = 0.05 + 0.01 * epsilon_loop;
      double error_count = train(epsilon);
      if (error_count < best_error_count) {
        best_error_count = error_count;
        best_epsilon = epsilon;
      }
    }
    System.out.println("\n**** Best epsilon value = " + best_epsilon );

    // retrain for the best epsilon setting the maximum likelyhood parameters
    // which are now in epsilon, mu[] and sigma_squared[]:
    train_helper(best_epsilon);

    // finally, we are ready to see how the model performs with test data:
    test(best_epsilon);
  }

  /**
   * calculate probability p(x; mu, sigma_squared)
   *
   * @param x - example vector
   * @return
   */
  private double p(double [] x) {
    double sum = 0;
    // use (num_features - 1) to skip target output:
    for (int nf=0; nf<num_features - 1; nf++) {
      sum += (1.0 / (SQRT_2_PI * sigma_squared[nf])) *
             Math.exp(- (x[nf] - mu[nf]) * (x[nf] - mu[nf]));
    }
    return sum / num_features;
  }

  /**
   * returns 1 if input vector is an anoomaly
   *
   * @param x - example vector
   * @return
   */
  public boolean isAnamoly(double [] x) {
    double sum = 0;
    // use (num_features - 1) to skip target output:
    for (int nf=0; nf<num_features - 1; nf++) {
      sum += (1.0 / (SQRT_2_PI * sigma_squared[nf])) *
             Math.exp(- (x[nf] - mu[nf]) * (x[nf] - mu[nf]));
    }
    return (sum / num_features) < best_epsilon;
  }

  private double train_helper_(double epsilon) {
    // use (num_features - 1) to skip target output:
    for (int nf = 0; nf < this.num_features - 1; nf++) {
      double sum = 0;
      for (int nt=0; nt<this.num_training_examples; nt++) {
        sum += (this.training_examples[nt][nf] - mu[nf]) *
               (this.training_examples[nt][nf] - mu[nf]);
      }
      sigma_squared[nf] = (1.0 / num_features) * sum;
    }
    double cross_validation_error_count = 0;
    for (int nt=0; nt<this.num_cross_validation_examples; nt++) {
      double[] x = this.cross_validation_examples[nt];
      double calculated_value = p(x);
      if (x[9] > 0.5) { // target training output is ANOMALY
        // if the calculated value is greater than epsilon
        // then this x vector is not an anomaly (e.g., it
        // is a normal sample):
        if (calculated_value > epsilon) cross_validation_error_count += 1;
      } else { // target training output is NORMAL
        if (calculated_value < epsilon) cross_validation_error_count += 1;
      }
    }
    System.out.println("   cross_validation_error_count = " +
                       cross_validation_error_count +
                       " for epsilon = " + epsilon);
    return cross_validation_error_count;
  }

  private double test(double epsilon) {
    double num_false_positives = 0, num_false_negatives = 0;
    double num_true_positives = 0, num_true_negatives = 0;
    for (int nt=0; nt<this.num_testing_examples; nt++) {
      double[] x = this.testing_examples[nt];
      double calculated_value = p(x);
      if (x[9] > 0.5) { // target training output is ANOMALY
        if (calculated_value > epsilon) num_false_negatives++;
        else                            num_true_positives++;
      } else { // target training output is NORMAL
        if (calculated_value < epsilon) num_false_positives++;
        else                            num_true_negatives++;
      }
    }
    double precision = num_true_positives /
                       (num_true_positives + num_false_positives);
    double recall = num_true_positives /
                    (num_true_positives + num_false_negatives);
    double F1 = 2 * precision * recall / (precision + recall);

    System.out.println("\n\n -- best epsilon = " + this.best_epsilon);
    System.out.println(" -- number of test examples = " +
                       this.num_testing_examples);
    System.out.println(" -- number of false positives = " + num_false_positives);
    System.out.println(" -- number of true positives = " + num_true_positives);
    System.out.println(" -- number of false negatives = " + num_false_negatives);
    System.out.println(" -- number of true negatives = " + num_true_negatives);
    System.out.println(" -- precision = " + precision);
    System.out.println(" -- recall = " + recall);
    System.out.println(" -- F1 = " + F1);
    return F1;
  }

  double best_epsilon = 0.02;
  private double [] mu;  // [num_features]
  private double [] sigma_squared; // [num_features]
  private int num_features;
  private static double SQRT_2_PI = 2.50662827463;


  private double[][] training_examples; // [num_features][num_training_examples]
  // [num_features][num_training_examples]:
  private double[][] cross_validation_examples; 
  private double[][] testing_examples; // [num_features][num_training_examples]
  private int num_training_examples;
  private int num_cross_validation_examples;
  private int num_testing_examples;

}
~~~~~~~~

Once the training data and the values of {$$}\mu{/$$} (the varible **mu** in the code) are defined for each feature we can define the method **train** in lines 86 through 104 that calculated the best **epsilon** "cutoff" value for the training data set using the method **train_helper** defined in lines 138 through 165. We use the "best" **epsilon** value by testing with the separate cross validation data set; we do this by calling the method **test** that is defined in lines 167 through 198.


## Example Using the University of Wisconsin Cancer Data

The example in this section loads the University of Wisconsin data and uses the class **AnomalyDetection** developed in the last section to find anomalies, which for this example will be input vectors that represented malignancy in the original data.


{lang="java",linenos=on}
~~~~~~~~
package com.markwatson.anomaly_detection;

import java.io.*;
import org.apache.commons.io.FileUtils;

/**
 * Train a deep belief network on the University of Wisconsin Cancer Data Set.
 */
public class WisconsinAnomalyDetection {

  private static boolean PRINT_HISTOGRAMS = true;

  public static void main(String[] args) throws Exception {

    String [] lines =
      FileUtils.readFileToString(
         new File("data/cleaned_wisconsin_cancer_data.csv")).split("\n");
    double [][] training_data = new double[lines.length][];
    int line_count = 0;
    for (String line : lines) {
      String [] sarr = line.split(",");
      double [] xs = new double[10];
      for (int i=0; i<10; i++) xs[i] = Double.parseDouble(sarr[i]);
      for (int i=0; i<9; i++) xs[i] *= 0.1;
      xs[9] = (xs[9] - 2) * 0.5; // make target output be [0,1] instead of [2,4]
      training_data[line_count++] = xs;
    }

    if (PRINT_HISTOGRAMS) {
      PrintHistogram.historam("Clump Thickness", training_data, 0, 0.0, 1.0, 10);
      PrintHistogram.historam("Uniformity of Cell Size", training_data,
                              1, 0.0, 1.0, 10);
      PrintHistogram.historam("Uniformity of Cell Shape", training_data,
                              2, 0.0, 1.0, 10);
      PrintHistogram.historam("Marginal Adhesion", training_data,
                              3, 0.0, 1.0, 10);
      PrintHistogram.historam("Single Epithelial Cell Size", training_data,
                              4, 0.0, 1.0, 10);
      PrintHistogram.historam("Bare Nuclei", training_data, 5, 0.0, 1.0, 10);
      PrintHistogram.historam("Bland Chromatin", training_data, 6, 0.0, 1.0, 10);
      PrintHistogram.historam("Normal Nucleoli", training_data, 7, 0.0, 1.0, 10);
      PrintHistogram.historam("Mitoses", training_data, 8, 0.0, 1.0, 10);
    }
    
    AnomalyDetection detector = new AnomalyDetection(10, line_count - 1,
                                                     training_data);

    // the train method will print training results like
    // precision, recall, and F1:
    detector.train();

    // get best model parameters:
    double best_epsilon = detector.bestEpsilon();
    double [] mean_values = detector.muValues();
    double [] sigma_squared = detector.sigmaSquared();

    // to use this model, us the method AnomalyDetection.isAnamoly(double []):

    double [] test_malignant = new double[] {0.5,1,1,0.8,0.5,0.5,0.7,1,0.1};
    double [] test_benign = new double[] {0.5,0.4,0.5,0.1,0.8,0.1,0.3,0.6,0.1};
    boolean malignant_result = detector.isAnamoly(test_malignant);
    boolean benign_result = detector.isAnamoly(test_benign);
    System.out.println("\n\nUsing the trained model:");
    System.out.println("malignant result = " + malignant_result +
                       ", benign result = " + benign_result);
  }
}
~~~~~~~~

Data used by an anomaly detecton model should have (roughly) a Gaussian (bell curve shape) distribution. What form does the cancer data have? Unfortunately, each of the data features seems to either have a greater density at the lower range of feature values or large density at the extremes of the data feature ranges. This will cause our model to not perform as well as we would like. Here are the inputs displayed as five-bin histograms:

{line-numbers=off}
~~~~~~~~
Clump Thickness
0	177
1	174
2	154
3	63
4	80

Uniformity of Cell Size
0	393
1	86
2	54
3	43
4	72

Uniformity of Cell Shape
0	380
1	92
2	58
3	54
4	64

Marginal Adhesion
0	427
1	85
2	42
3	37
4	57

Single Epithelial Cell Size
0	394
1	117
2	76
3	28
4	33

Bare Nuclei
0	409
1	44
2	33
3	27
4	135

Bland Chromatin
0	298
1	182
2	41
3	97
4	30

Normal Nucleoli
0	442
1	56
2	39
3	37
4	74

Mitoses
0	567
1	42
2	8
3	17
4	14
~~~~~~~~

I won't do it in this example, but the feature "Bare Nuclei" should be removed because it is not even close to being a bell-shaped distribution. Another thing that you can do (recommended by Andrew Ng in his Coursera Machine Learning class) is to take the log of data and otherwise transform it to something that looks more like a Gaussian distribution. In the class WisconsinAnomalyDetection, you could for example, transform the data using something like:

{lang="java",linenos=on}
~~~~~~~~
  // make the data look more like a Gaussian (bell curve shaped) distribution:
  double min = 1.e6, max = -1.e6;
  for (int i=0; i<9; i++) {
    xs[i] = Math.log(xs[i] + 1.2);
    if (xs[i] < min) min = xs[i];
    if (xs[i] > max) max = xs[i];
  }
  for (int i=0; i<9; i++) xs[i] = (xs[i] - min) / (max - min);
~~~~~~~~

The constant 1.2 in line 4 is a tuning parameter that I got by trial and error by iterating on adjusting the factor and looking at the data histograms.

In a real application you would drop features that you can not transform to something like a Gaussian distribution.

Here are the results of running the code as it is in the github repository for this book:

{line-numbers=off}
~~~~~~~~
 -- best epsilon = 0.28
 -- number of test examples = 63
 -- number of false positives = 0.0
 -- number of true positives = 11.0
 -- number of false negatives = 8.0
 -- number of true negatives = 44.0
 -- precision = 1.0
 -- recall = 0.5789473684210527
 -- F1 = 0.7333333333333334


Using the trained model:
malignant result = true, benign result = false
~~~~~~~~


How do we evaluate these results? The precision value of 1.0 means that there were no false positives. False positives are predictions of a true result when it should have been false. The value 0.578 for recall means that of all the samples that should have been classified as positive, we only predicted about 57.8% of them. The F1 score is calculated as two times the product of precision and recall, divided by the sum of precision plus recall.



