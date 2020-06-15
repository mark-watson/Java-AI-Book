# Deep Learning Using Deeplearning4j

The [Deeplearning4j.org](http://deeplearning4j.org/) Java library supports several neural network algorithms including support for Deep Learning (DL). We will look at an example of DL implementing Deep-Belief networks using the same University of Wisconsin cancer database that we used in the chapters on machine learning with Spark and on anomaly detection. Deep learning refers to neural networks with many layers, possibly with weights connecting neurons in non-adjacent layers which makes it possible to model temporal and spacial patterns in data.

I am going to assume that you have some knowledge of simple backpropagation neural networks. If you are unfamiliar with neural networks you might want to pause and do a web search for "neural networks backpropagation tutorial" or read the neural network chapter in my book [Practical Artificial Intelligence Programming With Java, 4th edition](https://leanpub.com/javaai).

The difficulty in training many layer networks with backpropagation is that the delta errors in the layers near the input layer (and far from the output layer) get very small and training can take a very long time even on modern processors and GPUs. Geoffrey Hinton and his colleagues created a new technique for pretraining weights. In 2012 I took a Coursera course taught by Hinton and some colleagues titled ['Neural Networks for Machine Learning'](https://www.coursera.org/course/neuralnets) and the material may still be available online when you read this book.

For Java developers, Deeplearning4j is a great starting point for experimenting with deep learning. If you also use Python then there are good tutorials and other learning assets at [deeplearning.net](http://deeplearning.net/).

## Deep Belief Networks

Deep Belief Networks (DBN) are a type of deep neural network containing multiple hidden neuron layers where there are no connections between neurons inside any specific hidden layer. Each hidden layer learns features based on training data an the values of weights from the previous hidden layer. By previous layer I refer to the connected layer that is closer to the input layer. A DBN can learn more abstract features, with more abstraction in the hidden layers "further" from the input layer.

DBNs are first trained a layer at a time. Initially a set of training inputs is used to train the weights between the input and the first hidden layer of neurons. Technically, as we preliminarily train each succeeding pair of layers we are training a restricted Boltzmann machine (RBM) to learn a new set of features. It is enough for you to know at this point that RBMs are two layers, input and output, that are completely connected (all neurons in the first layer have a weighted connection to each neuron in the next layer) and there are no inner-layer connections.

As we progressively train a DBN, the output layer for one RBM becomes the input layer for the next neuron layer pair for preliminary training.

Once the hidden layers are all preliminarily trained then backpropagation learning is used to retrain the entire network but now delta errors are calculated by comparing the forward pass outputs with the training outputs and back-propagating errors to update weights in layers proceeding back towards the first hidden layer.

The important thing to understand is that backpropagation tends to not work well for networks with many hidden layers because the back propagated errors get smaller as we process backwards towards the input neurons - this would cause network training to be very slow. By precomputing weights using RBM pairs, we are closer to a set of weights to minimize errors over the training set (and the test set).


## Deep Belief Example

The following screen show shows an IntelliJ project (you can use the free community orprofessional version for the examples in this book) for the example in this chapter:

![IntelliJ project view for the examples in this chapter](images/intellij_dl.png)

The Deeplearning4j library uses user-written Java classes to import training and testing data into a form that the Deeplearning4j library can use. The following listing shows the implementation of the class **WisconsinDataSetIterator** that iterates through the University of Wisconsin cancer data set:

{lang="java",linenos=on}
~~~~~~~~
package com.markwatson.deeplearning;

import org.deeplearning4j.datasets.iterator.BaseDatasetIterator;

import java.io.FileNotFoundException;


public class WisconsinDataSetIterator extends BaseDatasetIterator {
  private static final long serialVersionUID = -2023454995728682368L;

  public WisconsinDataSetIterator(int batch, int numExamples)
         throws FileNotFoundException {
    super(batch, numExamples, new WisconsinDataFetcher());
  }
}
~~~~~~~~

The **WisconsinDataSetIterator** constructor calls its super class with an instance of the class **WisconsinDataFetcher** (defined in the next listing) to read the Comma Separated Values (CSV) spreadsheet data from the file data/cleaned_wisconsin_cancer_data.csv:


{lang="java",linenos=on}
~~~~~~~~
package com.markwatson.deeplearning;

import org.deeplearning4j.datasets.fetchers.CSVDataFetcher;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

/**
 * Created by markw on 10/5/15.
 */
public class WisconsinDataFetcher extends CSVDataFetcher {

  public WisconsinDataFetcher() throws FileNotFoundException {
    super(new FileInputStream("data/cleaned_wisconsin_cancer_data.csv"), 9);
  }
  @Override
  public void fetch(int i) {
    super.fetch(i);
  }

}
~~~~~~~~

In line 14, the last argument 9 defines which column in the input CSV file contains the label data. This value is zero indexed so if you look at the input file data/cleaned_wisconsin_cancer_date.csv this will be the last column. Values of 4 in the last column idicate malignant and values of 2 indicate not malignant.

The following listing shows the definition of the class **DeepBeliefNetworkWisconsinData** that reads the University of Wisconsin cancer data set using the code in the last two listings, randmly selects part of it to use for training and for testing, creates a DBN, and tests it. The value of the variable **numHidden** set in line 51 refers to the number of neurons in each hidden layer. Setting **numberOfLayers** to 3 in line 52 indicates that we will just use a single hidden layer since this value (3) also counts the input and output layers.

The network is configured and constructed in lines 83 through 104. If we increased the number of hidden units (something that you might do for more complex problems) then you would repeat lines 88 through 97 to add a new hidden layer, and you would change the layer indices (first argument) as appropriate in calls to the chained method **.layer()**.


{lang="java",linenos=on}
~~~~~~~~
package com.markwatson.deeplearning;

import org.deeplearning4j.datasets.iterator.DataSetIterator;
import org.deeplearning4j.eval.Evaluation;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
import org.deeplearning4j.nn.conf.layers.RBM;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.dataset.SplitTestAndTrain;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.lossfunctions.LossFunctions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Random;

import org.apache.commons.io.FileUtils;

/**
 * Train a deep belief network on the University of Wisconsin Cancer Data Set.
 */
public class DeepBeliefNetworkWisconsinData {

  private static Logger log =
    LoggerFactory.getLogger(DeepBeliefNetworkWisconsinData.class);

  public static void main(String[] args) throws Exception {

    final int numInput = 9;
    int outputNum = 2;
    /**
     * F1 scores as a function of the number of hidden layer
     * units hyper-parameter:
     *
     *    numHidden   F1
     *    ---------   --
     *            2   0.50734
     *            3   0.87283 (value to use - best result / smallest network)
     *           13   0.87283
     *          100   0.55987 (over fitting)
     *
     *   Other hyper parameters held constant: batchSize = 648
     */
    int numHidden = 3;
    int numberOfLayers = 3; // input, hidden, output
    int numSamples = 648;

    /**
     * F1 scores as a function of the number of batch size:
     *
     *    batchSize   F1
     *    ---------   --
     *           30   0.50000
     *           64   0.78787
     *          323   0.67123
     *          648   0.87283 (best to process all training vectors in one batch)
     *
     *   Other hyper parameters held constant: numHidden = 3
     */
    int batchSize = 648; // equal to number of samples

    int iterations = 100;
    int fractionOfDataForTraining = (int) (batchSize * 0.7);
    int seed = 33117;

    DataSetIterator iter = new WisconsinDataSetIterator(batchSize, numSamples);
    DataSet next = iter.next();
    next.normalizeZeroMeanZeroUnitVariance();

    SplitTestAndTrain splitDataSets =
	  next.splitTestAndTrain(fractionOfDataForTraining, new Random(seed));
    DataSet train = splitDataSets.getTrain();
    DataSet test = splitDataSets.getTest();

    MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder()
        .seed(seed) //use the same random seed
        .iterations(iterations)
        .l1(1e-1).regularization(true).l2(2e-4)
        .list(numberOfLayers - 1) // don't count the input layer
        .layer(0,
            new RBM.Builder(RBM.HiddenUnit.RECTIFIED, RBM.VisibleUnit.GAUSSIAN)
                .nIn(numInput)
                .nOut(numHidden)
                // set variance of random initial weights based on
                // input and output layer size:
                .weightInit(WeightInit.XAVIER)
                .dropOut(0.25)
                .build()
        )
        .layer(1, new OutputLayer.Builder(LossFunctions.LossFunction.MCXENT)
            .nIn(numHidden)
            .nOut(outputNum)
            .activation("softmax")
            .build()
        )
        .build();
    MultiLayerNetwork model = new MultiLayerNetwork(conf);
    model.init();
    model.fit(train);

    log.info("\nEvaluating model:\n");
    Evaluation eval = new Evaluation(outputNum);
    INDArray output = model.output(test.getFeatureMatrix());

    for (int i = 0; i < output.rows(); i++) {
      String target = test.getLabels().getRow(i).toString();
      String predicted = output.getRow(i).toString();
      log.info("target: " + target + " predicted: " + predicted);
    }

    eval.eval(test.getLabels(), output);
    log.info(eval.stats());

    /**
     * Save the model for reuse:
     */
    OutputStream fos = Files.newOutputStream(Paths.get("saved-model.bin"));
    DataOutputStream dos = new DataOutputStream(fos);
    Nd4j.write(model.params(), dos);
    dos.flush();
    dos.close();
    FileUtils.writeStringToFile(
       new File("conf.json"),
                model.getLayerWiseConfigurations().toJson());

    /**
     * Load saved model and test again:
     */
    log.info("\nLoad saved model from disk:\n");
    MultiLayerConfiguration confFromJson =
	  MultiLayerConfiguration.fromJson(
                     FileUtils.readFileToString(new File("conf.json")));
    DataInputStream dis = new DataInputStream(
                                new FileInputStream("saved-model.bin"));
    INDArray newParams = Nd4j.read(dis);
    dis.close();
    MultiLayerNetwork savedModel = new MultiLayerNetwork(confFromJson);
    savedModel.init();
    savedModel.setParameters(newParams);

    log.info("\nEvaluating model loaded from disk:\n");
    Evaluation eval2 = new Evaluation(outputNum);
    INDArray output2 = savedModel.output(test.getFeatureMatrix());

    for (int i = 0; i < output2.rows(); i++) {
      String target = test.getLabels().getRow(i).toString();
      String predicted = output.getRow(i).toString();
      log.info("target: " + target + " predicted: " + predicted);
    }

    eval2.eval(test.getLabels(), output2);
    log.info(eval2.stats());
  }
}
~~~~~~~~

In line 71 we set **fractionOfDataForTraining** to 0.7 which means that we will use 70% of the available data for training and 30% for testing. It is very important to not use training data for testing because performance on recognizing training data should always be good assuming that you have enough memory capacity in a network (i.e., enough hidden units and enough neurons in each hidden layer). In lines 78 through 81 we divide our data into training and testing disjoint sets.

In line 86 we are setting three meta learning parameters: learning rate for the first set of weights between the input and hidden layer to be 1e-1, setting the model to use regularization, and setting the learning rate for the hidden to output weights to be 2e-4.

In line 95 we are setting the dropout factor, here saying that we will randomly not use 25% of the neurons for any given training example. Along with regularization, using dropout helps prevent overfitting. Overfitting occurs when a neural netowrk fails to generalize and learns noise in training data. The goal is to learn important features that affect the utility of a trained model for processing new data. We don't want to learn random noise in the data as important features. Another way to prevent overfitting is to use the smallest possible number of neurons in hidden labels and still perform well on the independent test data.

After training the model in lines 105 through 107, we test the model (lines 110 through 120) on the separate test data. The program output when I ran the model is:

{line-numbers=off}
~~~~~~~~
Evaluating model:

14:59:46.872 [main] INFO  c.m.d.DeepBeliefNetworkWisconsinData -
                                  target: [ 1.00, 0.00] predicted: [ 0.76, 0.24]
14:59:46.873 [main] INFO  c.m.d.DeepBeliefNetworkWisconsinData -
                                  target: [ 1.00, 0.00] predicted: [ 0.65, 0.35]

Actual Class 0 was predicted with Predicted 0 with count 151 times

Actual Class 1 was predicted with Predicted 0 with count 44 times

==========================Scores========================================
 Accuracy:  0.7744
 Precision: 0.7744
 Recall:    1
 F1 Score:  0.8728323699421966
===========================================================================
~~~~~~~~

The F1 score is calculated as twice precision times recall, all divided by precision + recall. We would like F1 to be as close to 1.0 as possible and it is common to spend a fair amount of time experimenting with meta learning paramters to increase F1.

It is also fairly common to try to learn good values of meta learning paramters also. We won't do this here but the process involves splitting the data into three disjoint sets: training, validation, and testing. The meta parameters are varied, training is performed, and using the validation data the best set of meta parameters is selected. Finally, we test the netowrk as defined my meta parameters and learned weights for those meta parameters with the separate test data to see what the effective F1 score is.


## Deep Learning Wrapup

I first used complex neural network topologies in the late 1980s for phoneme (speech) recognition, specifically using time delay neural networks and I gave a talk about it at [IEEE First Annual International Conference on Neural Networks San Diego, California June 21-24, 1987](http://ieeexplore.ieee.org/xpl/articleDetails.jsp?reload=true&arnumber=4307059). Back then, neural networks were not really considered to be a great technology for this application but in the present time Google, Microsoft, and other companies are using deep (many layered) neural networks for speech and image recognition. Exciting work is also being done in the field of natural language processing. I just provided a small example in this chapter that you can experiment with easily. I wanted to introduce you to [Deeplearning4j](http://deeplearning4j.org/) because I think it is probably the easiest way for Java developers to get started working with many layered neural networks and I refer you to [the project documentation](http://deeplearning4j.org/documentation.html).

