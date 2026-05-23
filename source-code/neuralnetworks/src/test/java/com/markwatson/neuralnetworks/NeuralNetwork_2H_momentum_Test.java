package com.markwatson.neuralnetworks;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NeuralNetwork_2H_momentum_Test {

    static float[] in1 = {0.1f,  0.1f, 0.9f};
    static float[] in2 = {0.1f,  0.9f, 0.1f};
    static float[] in3 = {0.9f,  0.1f, 0.1f};

    static float[] out1 = {0.9f, 0.1f, 0.1f};
    static float[] out2 = {0.1f, 0.1f, 0.9f};
    static float[] out3 = {0.1f, 0.9f, 0.1f};

    static float[] test1 = {0.1f, 0.1f, 0.9f};
    static float[] test2 = {0.1f, 0.9f, 0.1f};
    static float[] test3 = {0.9f, 0.1f, 0.1f};

    void test_recall(Neural_2H_momentum nn, float[] inputs) {
        float[] results = nn.recall(inputs);
        System.out.print("Test case: ");
        for (float input : inputs) System.out.print(pp(input) + " ");
        System.out.print(" results: ");
        for (float result : results) System.out.print(pp(result) + " ");
        System.out.println();
    }

    /**
     * Train network and verify it learns the rotation pattern.
     * Uses a retry loop because stochastic weight initialization
     * may occasionally produce non-convergent starting points.
     */
    @Test
    void testTraining() {
        for (int attempt = 0; attempt < 5; attempt++) {
            Neural_2H_momentum nn = new Neural_2H_momentum(3, 3, 3, 3, 0.75f);
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
                    System.out.println("attempt " + attempt + " cycle " + i + " error is " + error);
                    error = 0;
                }
            }
            System.out.println("Test results should rotate inputs:");
            test_recall(nn, test1);
            test_recall(nn, test2);
            test_recall(nn, test3);

            // Verify the network learned: each recalled output's max index should match expected
            float[] r1 = nn.recall(test1);
            float[] r2 = nn.recall(test2);
            float[] r3 = nn.recall(test3);
            boolean converged = (r1[0] > r1[1] && r1[0] > r1[2])
                             && (r2[2] > r2[0] && r2[2] > r2[1])
                             && (r3[1] > r3[0] && r3[1] > r3[2]);
            if (converged) {
                System.out.println("Converged on attempt " + attempt);
                return; // success
            }
            System.out.println("Attempt " + attempt + " did not converge, retrying...");
        }
        // If we get here, all attempts failed
        assertTrue(false, "Network did not converge after 5 attempts");
    }

    static String pp(float x) {
        return String.format("% .2f", x);
    }
}