package com.markwatson.neuralnetworks;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Back-propagation neural network with 2 hidden layers
 * <p>
 * <p/>
 * Copyright 1998-2012 by Mark Watson. All rights reserved.
 * <p/>
 * This software is can be used under either of the following licenses:
 * <p/>
 * 1. LGPL v3<br/>
 * 2. Apache 2
 * <p/>
 */
public class Neural_2H implements Serializable {

  private static final long serialVersionUID = -8890368507114690975L;
  protected int numInputs;
  protected int numHidden1;
  protected int numHidden2;
  protected int numOutputs;

  public float[] inputs;
  public float[] hidden1;
  public float[] hidden2;
  public float[] outputs;

  public float[][] W1;
  public float[][] W2;
  public float[][] W3;

  protected float[] output_errors;
  protected float[] hidden1_errors;
  protected float[] hidden2_errors;

  transient protected List<float[]> inputTraining = new ArrayList<>();
  transient protected List<float[]> outputTraining = new ArrayList<>();

  public float TRAINING_RATE = 0.2f;

  public Neural_2H(int num_in, int num_hidden1, int num_hidden2, int num_output) {

    numInputs = num_in;
    numHidden1 = num_hidden1;
    numHidden2 = num_hidden2;
    numOutputs = num_output;
    inputs = new float[numInputs];
    hidden1 = new float[numHidden1];
    hidden2 = new float[numHidden2];
    outputs = new float[numOutputs];
    W1 = new float[numInputs][numHidden1];
    W2 = new float[numHidden1][numHidden2];
    W3 = new float[numHidden2][numOutputs];
    randomizeWeights();

    output_errors = new float[numOutputs];
    hidden1_errors = new float[numHidden1];
    hidden2_errors = new float[numHidden2];
  }

  public void addTrainingExample(float[] inputs, float[] outputs) {
    if (inputs.length != numInputs || outputs.length != numOutputs) {
      throw new IllegalArgumentException(
          "addTrainingExample(): expected " + numInputs + " inputs and " +
          numOutputs + " outputs, but got " + inputs.length + " and " + outputs.length);
    }
    inputTraining.add(inputs);
    outputTraining.add(outputs);
  }

  public void randomizeWeights() {
    var rng = ThreadLocalRandom.current();
    for (int ii = 0; ii < numInputs; ii++)
      for (int hh = 0; hh < numHidden1; hh++)
        W1[ii][hh] = rng.nextFloat() - 0.1f;
    for (int ii = 0; ii < numHidden1; ii++)
      for (int hh = 0; hh < numHidden2; hh++)
        W2[ii][hh] = 2f * rng.nextFloat() - 1f;
    for (int hh = 0; hh < numHidden2; hh++)
      for (int oo = 0; oo < numOutputs; oo++)
        W3[hh][oo] = 2f * rng.nextFloat() - 1f;
  }

  public void slightlyRandomizeWeights() {
    var rng = ThreadLocalRandom.current();
    for (int ii = 0; ii < numInputs; ii++)
      for (int hh = 0; hh < numHidden1; hh++)
        W1[ii][hh] += 0.2f * rng.nextFloat() - 0.1f;
    for (int ii = 0; ii < numHidden1; ii++)
      for (int hh = 0; hh < numHidden2; hh++)
        W2[ii][hh] += 0.2f * rng.nextFloat() - 0.1f;
    for (int hh = 0; hh < numHidden2; hh++)
      for (int oo = 0; oo < numOutputs; oo++)
        W3[hh][oo] += 0.2f * rng.nextFloat() - 0.1f;
  }

  public float[] recall(float[] in) {
    System.arraycopy(in, 0, inputs, 0, numInputs);
    forwardPass();
    return Arrays.copyOf(outputs, numOutputs);
  }

  public void forwardPass() {
    int i, h, o;
    Arrays.fill(hidden1, 0.0f);
    Arrays.fill(hidden2, 0.0f);
    for (i = 0; i < numInputs; i++) {
      for (h = 0; h < numHidden1; h++) {
        hidden1[h] += inputs[i] * W1[i][h];
      }
    }
    for (i = 0; i < numHidden1; i++) {
      for (h = 0; h < numHidden2; h++) {
        hidden2[h] += hidden1[i] * W2[i][h];
      }
    }
    Arrays.fill(outputs, 0.0f);
    for (h = 0; h < numHidden2; h++) {
      for (o = 0; o < numOutputs; o++) {
        outputs[o] += sigmoid(hidden2[h]) * W3[h][o];
      }
    }
  }

  public float train() {
    return train(inputTraining, outputTraining);
  }

  private int current_example = 0;
  private int count_control_randomize_weights = 0;

  public float train(List<float[]> ins, List<float[]> v_outs) {
    int i, h, o;
    float error = 0.0f;
    int num_cases = ins.size();
    // zero out error arrays:
    Arrays.fill(hidden1_errors, 0.0f);
    Arrays.fill(hidden2_errors, 0.0f);
    Arrays.fill(output_errors, 0.0f);
    // copy the input values:
    System.arraycopy(ins.get(current_example), 0, inputs, 0, numInputs);
    // copy the output values:
    float[] outs = v_outs.get(current_example);

    // perform a forward pass through the network:

    forwardPass();

    for (o = 0; o < numOutputs; o++) {
      output_errors[o] =
          (outs[o] -
              outputs[o])
              * sigmoidP(outputs[o]);
    }
    for (h = 0; h < numHidden2; h++) {
      hidden2_errors[h] = 0.0f;
      for (o = 0; o < numOutputs; o++) {
        hidden2_errors[h] +=
            output_errors[o] * W3[h][o];
      }
    }
    for (h = 0; h < numHidden1; h++) {
      hidden1_errors[h] = 0.0f;
      for (o = 0; o < numHidden2; o++) {
        hidden1_errors[h] +=
            hidden2_errors[o] * W2[h][o];
      }
    }
    for (h = 0; h < numHidden2; h++) {
      hidden2_errors[h] =
          hidden2_errors[h] * sigmoidP(hidden2[h]);
    }
    for (h = 0; h < numHidden1; h++) {
      hidden1_errors[h] =
          hidden1_errors[h] * sigmoidP(hidden1[h]);
    }
    // update the hidden2 to output weights:
    for (o = 0; o < numOutputs; o++) {
      for (h = 0; h < numHidden2; h++) {
        W3[h][o] +=
            TRAINING_RATE * output_errors[o] * hidden2[h];
        W3[h][o] = clampWeight(W3[h][o]);
      }
    }
    // update the hidden1 to hidden2 weights:
    for (o = 0; o < numHidden2; o++) {
      for (h = 0; h < numHidden1; h++) {
        W2[h][o] +=
            TRAINING_RATE * hidden2_errors[o] * hidden1[h];
        W2[h][o] = clampWeight(W2[h][o]);
      }
    }
    // update the input to hidden1 weights:
    for (h = 0; h < numHidden1; h++) {
      for (i = 0; i < numInputs; i++) {
        W1[i][h] +=
            TRAINING_RATE * hidden1_errors[h] * inputs[i];
        W1[i][h] = clampWeight(W1[i][h]);
      }
    }
    for (o = 0; o < numOutputs; o++) {
      error += Math.abs(outs[o] - outputs[o]);
    }
    current_example++;
    if (current_example >= num_cases) current_example = 0;
    if ((count_control_randomize_weights > 500) && (error > 1)) {
      count_control_randomize_weights = 0;
      randomizeWeights();
      System.out.println("Weights randomized (training was not converging).");
    }
    return error;
  }

  public float clampWeight(float weight) {
    return Math.clamp(weight, -10f, 10f);
  }

  protected float sigmoid(float x) {
    return
        (float) (1.0f / (1.0f + Math.exp((double) (-x))));
  }

  protected float sigmoidP(float x) {
    double z = sigmoid(x);
    return (float) (z * (1.0f - z));
  }

}
