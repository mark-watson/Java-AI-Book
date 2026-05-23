package com.markwatson.neuralnetworks;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NeuralNetwork_2H_Test {

    static float[] in1 = {0.1f,  0.1f, 0.9f};
    static float[] in2 = {0.1f,  0.9f, 0.1f};
    static float[] in3 = {0.9f,  0.1f, 0.1f};

    static float[] out1 = {0.9f, 0.1f, 0.1f};
    static float[] out2 = {0.1f, 0.1f, 0.9f};
    static float[] out3 = {0.1f, 0.9f, 0.1f};

    static float[] test1 = {0.1f, 0.1f, 0.9f};
    static float[] test2 = {0.1f, 0.9f, 0.1f};
    static float[] test3 = {0.9f, 0.1f, 0.1f};

    void test_recall(Neural_2H nn, float[] inputs) {
        float[] results = nn.recall(inputs);
        System.out.print("Test case: ");
        for (float input : inputs) System.out.print(pp(input) + " ");
        System.out.print(" results: ");
        for (float result : results) System.out.print(pp(result) + " ");
        System.out.println();
    }

    /**
     * Train network and verify it learns the rotation pattern.
     */
    @Test
    void testTraining() {
        Neural_2H nn = new Neural_2H(3, 3, 3, 3);
        nn.addTrainingExample(in1, out1);
        nn.addTrainingExample(in2, out2);
        nn.addTrainingExample(in3, out3);
        double error = 0;
        for (int i = 0; i < 50000; i++) {
            if (i == 5000 || i == 8000 || i == 10000 || i == 12000) nn.TRAINING_RATE *= 0.75f;
            error += nn.train();
            if (i > 0 && (i % 1000 == 0)) {
                error /= 1000;
                if (error > 0.75) {
                    nn.randomizeWeights();
                    nn.TRAINING_RATE = 0.75f;
                } else if (error > 0.3) {
                    nn.slightlyRandomizeWeights();
                }
                System.out.println("cycle " + i + " error is " + error);
                error = 0;
            }
        }
        System.out.println("Test results should rotate inputs:");
        test_recall(nn, test1);
        test_recall(nn, test2);
        test_recall(nn, test3);

        // Verify the network learned: each recalled output's max index should match expected
        float[] r1 = nn.recall(test1);
        assertTrue(r1[0] > r1[1] && r1[0] > r1[2], "test1 should map to output[0] dominant");
        float[] r2 = nn.recall(test2);
        assertTrue(r2[2] > r2[0] && r2[2] > r2[1], "test2 should map to output[2] dominant");
        float[] r3 = nn.recall(test3);
        assertTrue(r3[1] > r3[0] && r3[1] > r3[2], "test3 should map to output[1] dominant");
    }

    static String pp(float x) {
        return String.format("% .2f", x);
    }
}