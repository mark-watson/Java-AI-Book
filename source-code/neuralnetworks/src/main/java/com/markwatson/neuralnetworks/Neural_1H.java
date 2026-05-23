package com.markwatson.neuralnetworks;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Back-propagation neural network with 1 hidden layer
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
public class Neural_1H implements Serializable {

  private static final long serialVersionUID = 1L;

  protected int numInputs;
  protected int numHidden;
  protected int numOutputs;

  public float[] inputs;
  public float[] hidden;
  public float[] outputs;

  public float[][] W1;
  public float[][] W2;

  protected float[] output_errors;
  protected float[] hidden_errors;

  public float learningRate = 0.2f;

  transient protected List<float[]> inputTraining = new ArrayList<>();
  transient protected List<float[]> outputTraining = new ArrayList<>();


  public Neural_1H(int num_in, int num_hidden, int num_output) {
    numInputs = num_in;
    numHidden = num_hidden;
    numOutputs = num_output;
    inputs = new float[numInputs];
    hidden = new float[numHidden];
    outputs = new float[numOutputs];
    W1 = new float[numInputs][numHidden];
    W2 = new float[numHidden][numOutputs];
    randomizeWeights();

    output_errors = new float[numOutputs];
    hidden_errors = new float[numHidden];
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

  public void setLearningRate(float f) {
    learningRate = f;
  }

  public void randomizeWeights() {
    var rng = ThreadLocalRandom.current();
    for (int ii = 0; ii < numInputs; ii++)
      for (int hh = 0; hh < numHidden; hh++)
        W1[ii][hh] = rng.nextFloat() - 0.5f;
    for (int hh = 0; hh < numHidden; hh++)
      for (int oo = 0; oo < numOutputs; oo++)
        W2[hh][oo] = rng.nextFloat() - 0.5f;
  }

  public void slightlyRandomizeWeights() {
    var rng = ThreadLocalRandom.current();
    for (int ii = 0; ii < numInputs; ii++)
      for (int hh = 0; hh < numHidden; hh++)
        W1[ii][hh] += 0.2f * rng.nextFloat() - 0.1f;
    for (int ii = 0; ii < numHidden; ii++)
      for (int hh = 0; hh < numOutputs; hh++)
        W2[ii][hh] += 0.2f * rng.nextFloat() - 0.1f;
  }

  public void jiggleWeights(float factor) {
    var rng = ThreadLocalRandom.current();
    for (int ii = 0; ii < numInputs; ii++)
      for (int hh = 0; hh < numHidden; hh++)
        W1[ii][hh] += factor * rng.nextFloat() - (factor / 2);
    for (int hh = 0; hh < numHidden; hh++)
      for (int oo = 0; oo < numOutputs; oo++)
        W2[hh][oo] += factor * rng.nextFloat() - (factor / 2);
  }

  public float[] recall(float[] in) {
    System.arraycopy(in, 0, inputs, 0, numInputs);
    forwardPass();
    return Arrays.copyOf(outputs, numOutputs);
  }

  public void forwardPass() {
    int i, h, o;
    Arrays.fill(hidden, 0.0f);
    for (i = 0; i < numInputs; i++) {
      for (h = 0; h < numHidden; h++) {
        hidden[h] += inputs[i] * W1[i][h];
      }
    }
    Arrays.fill(outputs, 0.0f);
    for (h = 0; h < numHidden; h++) {
      for (o = 0; o < numOutputs; o++) {
        outputs[o] += sigmoid(hidden[h]) * W2[h][o];
      }
    }
    for (o = 0; o < numOutputs; o++)
      outputs[o] = sigmoid(outputs[o]);
  }

  public float train() {
    return train(inputTraining, outputTraining);
  }

  // for debug graphics: train only one example at a time:
  private int current_example = 0;
  private int count_control_randomize_weights = 0;

  public float train(List<float[]> v_ins, List<float[]> v_outs) {
    int i, h, o;
    float error = 0.0f;
    int num_cases = v_ins.size();
    count_control_randomize_weights += 1;
    // zero out error arrays:
    Arrays.fill(hidden_errors, 0.0f);
    Arrays.fill(output_errors, 0.0f);
    // copy the input values:
    System.arraycopy(v_ins.get(current_example), 0, inputs, 0, numInputs);
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
    for (h = 0; h < numHidden; h++) {
      hidden_errors[h] = 0.0f;
      for (o = 0; o < numOutputs; o++) {
        hidden_errors[h] +=
            output_errors[o] * W2[h][o];
      }
    }
    for (h = 0; h < numHidden; h++) {
      hidden_errors[h] =
          hidden_errors[h] * sigmoidP(hidden[h]);
    }
    // update the hidden to output weights:
    for (o = 0; o < numOutputs; o++) {
      for (h = 0; h < numHidden; h++) {
        W2[h][o] +=
            learningRate * output_errors[o] * hidden[h];
      }
    }
    // update the input to hidden weights:
    for (h = 0; h < numHidden; h++) {
      for (i = 0; i < numInputs; i++) {
        W1[i][h] +=
            learningRate * hidden_errors[h] * inputs[i];
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


  protected float sigmoid(float x) {
    return
        (float) (1.0f / (1.0f + Math.exp((double) (-x))));
  }

  protected float sigmoidP(float x) {
    double z = sigmoid(x);
    return (float) (z * (1.0f - z));
  }

}
