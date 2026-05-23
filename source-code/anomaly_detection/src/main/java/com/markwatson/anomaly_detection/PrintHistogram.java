package com.markwatson.anomaly_detection;

/**
 * Text-based histogram printer for visualizing data distributions.
 */
public class PrintHistogram {

  /**
   * Print a text histogram for a single feature column.
   *
   * @param title          display name of the feature
   * @param values         the full data matrix
   * @param indexToDisplay column index to plot
   * @param min            expected minimum value
   * @param max            expected maximum value
   * @param numBins        number of histogram bins
   */
  public static void histogram(String title, double[][] values, int indexToDisplay,
                                double min, double max, int numBins) {
    var bins = new int[numBins];
    for (var row : values) {
      if (row == null) continue;
      double x = row[indexToDisplay];
      int ind = (int) (0.99 * (x - min) * numBins / max);
      // Guard against out-of-range indices
      ind = Math.max(0, Math.min(ind, numBins - 1));
      bins[ind] += 1;
    }
    System.out.println("\n" + title);
    for (int i = 0; i < numBins; i++) {
      System.out.println(i + "\t" + bins[i] + "\t" + "█".repeat(bins[i] / 5));
    }
  }

  /**
   * @deprecated Use {@link #histogram(String, double[][], int, double, double, int)} instead (fixed spelling).
   */
  @Deprecated(since = "1.1", forRemoval = true)
  public static void historam(String title, double[][] values, int indexToDisplay,
                               double min, double max, int numBins) {
    histogram(title, values, indexToDisplay, min, max, numBins);
  }
}
