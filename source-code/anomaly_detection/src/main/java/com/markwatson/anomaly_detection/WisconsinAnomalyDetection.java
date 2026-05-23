package com.markwatson.anomaly_detection;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Train a deep belief network on the University of Wisconsin Cancer Data Set.
 */
public class WisconsinAnomalyDetection {

  private static final boolean PRINT_HISTOGRAMS = true;
  private static final int NUM_HISTOGRAM_BINS = 5;

  public static void main(String[] args) throws IOException {

    var lines = Files.readAllLines(Path.of("data/cleaned_wisconsin_cancer_data.csv"));
    var trainingData = new double[lines.size()][];
    int lineCount = 0;

    for (var line : lines) {
      var sarr = line.strip().split(",");
      var xs = new double[10];
      for (int i = 0; i < 10; i++) xs[i] = Double.parseDouble(sarr[i]);
      for (int i = 0; i < 9; i++) xs[i] *= 0.1;

      // make the data look more like a Gaussian (bell curve shaped) distribution:
      double min = 1.e6, max = -1.e6;
      for (int i = 0; i < 9; i++) {
        xs[i] = Math.log(xs[i] + 1.2);
        if (xs[i] < min) min = xs[i];
        if (xs[i] > max) max = xs[i];
      }
      for (int i = 0; i < 9; i++) xs[i] = (xs[i] - min) / (max - min);

      xs[9] = (xs[9] - 2) * 0.5; // make target output be [0,1] instead of [2,4]
      trainingData[lineCount++] = xs;
    }

    if (PRINT_HISTOGRAMS) {
      PrintHistogram.histogram("Clump Thickness", trainingData, 0, 0.0, 1.0, NUM_HISTOGRAM_BINS);
      PrintHistogram.histogram("Uniformity of Cell Size", trainingData, 1, 0.0, 1.0, NUM_HISTOGRAM_BINS);
      PrintHistogram.histogram("Uniformity of Cell Shape", trainingData, 2, 0.0, 1.0, NUM_HISTOGRAM_BINS);
      PrintHistogram.histogram("Marginal Adhesion", trainingData, 3, 0.0, 1.0, NUM_HISTOGRAM_BINS);
      PrintHistogram.histogram("Single Epithelial Cell Size", trainingData, 4, 0.0, 1.0, NUM_HISTOGRAM_BINS);
      PrintHistogram.histogram("Bare Nuclei", trainingData, 5, 0.0, 1.0, NUM_HISTOGRAM_BINS);
      PrintHistogram.histogram("Bland Chromatin", trainingData, 6, 0.0, 1.0, NUM_HISTOGRAM_BINS);
      PrintHistogram.histogram("Normal Nucleoli", trainingData, 7, 0.0, 1.0, NUM_HISTOGRAM_BINS);
      PrintHistogram.histogram("Mitoses", trainingData, 8, 0.0, 1.0, NUM_HISTOGRAM_BINS);
    }

    var detector = new AnomalyDetection(10, lineCount - 1, trainingData);

    // the train method will print training results like
    // precision, recall, and F1:
    detector.train();

    // get best model parameters:
    double bestEpsilon = detector.bestEpsilon();
    var meanValues = detector.muValues();
    var sigmaSquared = detector.sigmaSquared();
    System.out.println("\nModel parameters:");
    System.out.println("  best epsilon = " + bestEpsilon);
    System.out.println("  num features = " + meanValues.length);
  }
}
