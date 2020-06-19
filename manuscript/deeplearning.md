# Deep Learning Using Deeplearning4j

The [Deeplearning4j.org](http://deeplearning4j.org/) Java library supports several neural network algorithms including support for Deep Learning (DL). We will look at an example of DL implementing Deep-Belief networks using the same University of Wisconsin cancer database that we used in the chapters on machine learning with Spark and on anomaly detection. Deep learning refers to neural networks with many layers, possibly with weights connecting neurons in non-adjacent layers which makes it possible to model temporal and spacial patterns in data. I will often refer to Deeplearning4j as DL4J.

I am going to assume that you have some knowledge of simple backpropagation neural networks. If you are unfamiliar with neural networks you might want to pause and do a web search for "neural networks backpropagation tutorial" or read the neural network chapter in my book [Practical Artificial Intelligence Programming With Java, 4th edition](https://leanpub.com/javaai).

The difficulty in training many layer networks with backpropagation is that the delta errors in the layers near the input layer (and far from the output layer) get very small and training can take a very long time even on modern processors and GPUs. Geoffrey Hinton and his colleagues created a new technique for pretraining weights. In 2012 I took a Coursera course taught by Hinton and some colleagues titled ['Neural Networks for Machine Learning'](https://www.coursera.org/course/neuralnets) and the material may still be available online when you read this book.

For Java developers, Deeplearning4j is a great starting point for experimenting with deep learning. 

## Feed Forward Classification Networks

Feed forward classification networks are a type of deep neural network that can contain multiple hidden neuron layers. In the example here the adjacent layers are fully connects.

In general, simpler network architectures are better than unnecessarily complicated architectures. For feed forward networks this complexity has two dimensions: the numbers of neurons in hidden layers, and also the number of hidden layers. If you put too many neurons in hidden layers then the training data is effectively memorized and this will hurt performance on data samples not used in training. In practice, I "starve the network" by reducing the number of hidden neurons until the model has reduced accuracy on independent training data. Then I slightly increase the number of neurons in hidden layers.

Using the feed forward classification classes in DL4J, backpropagation learning is used to train the entire network.

In the previous editions of this book I provided examples of implementing back propagation from scratch. I decided to now just show you how to use a library. I recommend taking an online class in deep learning to get the experience of implementing models from scratch. Andrew Ng's online classes are especially good for this.

The important thing to understand is that before deep learning algorithm optimizations, backpropagation did not work well for networks with many hidden layers because the back propagated errors get smaller as we process backwards towards the input neurons and this would cause network training to be very slow. This is referred to as vanishing gradients in the literature. With modern libraries like DL4J, TensorFlow, PyTorch, and mxnet this is not an issue.

## Simple Example

The following screen show shows an IntelliJ project (you can use the free community or professional version for the examples in this book) for the example in this chapter:

![IntelliJ project view for the examples in this chapter](images/intellij_dl.png)

The Deeplearning4j library can use user-written Java classes to import training and testing data into a form that the Deeplearning4j library can use. Some of the examples at [https://github.com/eclipse/deeplearning4j-examples](https://github.com/eclipse/deeplearning4j-examples) use custom data loaders but in this simple example we use built-in utilites for read spreadsheet data.

The following listing shows the definition of the class **ClassifierWisconsinData** that reads the University of Wisconsin cancer data set using the code in the last two listings, randomly selects part of it to use for training and for testing, creates a DBN, and tests it. The value of the variable **numHidden** set in line TBD TBD TBD refers to the number of neurons in each hidden layer.

The network is configured and constructed in lines TBD through TBD. If we increased the number of hidden units (something that you might do for more complex problems) then you would repeat lines TBD through TBD to add a new hidden layer, and you would change the layer indices (first argument) as appropriate in calls to the chained method **.layer()**.


{lang="java",linenos=on}
~~~~~~~~
package com.markwatson.deeplearning;

import org.datavec.api.records.reader.RecordReader;
import org.datavec.api.records.reader.impl.csv.CSVRecordReader;
import org.datavec.api.split.FileSplit;
import org.deeplearning4j.datasets.datavec.RecordReaderDataSetIterator;
import org.deeplearning4j.eval.Evaluation;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.layers.DenseLayer;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.deeplearning4j.optimize.listeners.ScoreIterationListener;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.learning.config.Sgd;
import org.nd4j.linalg.lossfunctions.LossFunctions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;

/**
 * Train a feed forward classifier network on the University of Wisconsin Cancer Data Set.
 */
public class ClassifierWisconsinData {

  private static Logger log = LoggerFactory.getLogger(ClassifierWisconsinData.class);

  public static void main(String[] args) throws Exception {
    int numHidden = 3;
    int numOutputs = 1;
    int batchSize = 64;

    int seed = 33117;

    int numInputs = 9;
    int labelIndex = 9;
    int numClasses = 2;

    RecordReader rr = new CSVRecordReader();
    rr.initialize(new FileSplit(new File("data/","training.csv")));
    DataSetIterator trainIter = new RecordReaderDataSetIterator(rr,batchSize,labelIndex,numClasses);

    RecordReader rrTest = new CSVRecordReader();
    rrTest.initialize(new FileSplit(new File("data/","testing.csv")));
    DataSetIterator testIter = new RecordReaderDataSetIterator(rrTest,batchSize,labelIndex,numClasses);

    MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder()
        .seed(seed) //use the same random seed
        .activation(Activation.TANH)
        .weightInit(WeightInit.XAVIER)
        .updater(new Sgd(0.1))
        .l2(1e-4)
        .list()
        .layer(0,
            new DenseLayer.Builder()
                .nIn(numInputs)
                .nOut(numHidden)
                .build()
        )
        .layer(1, new OutputLayer.Builder(LossFunctions.LossFunction.MCXENT)
            .nIn(numHidden)
            .nOut(numClasses)
            .activation(Activation.SOFTMAX)
            .build()
        )
        .build();
    MultiLayerNetwork model = new MultiLayerNetwork(conf);
    model.init();
    model.setListeners(new ScoreIterationListener(100));
    model.fit( trainIter, 10 );

    Evaluation eval = new Evaluation(numOutputs);
    while (testIter.hasNext()) {
      DataSet ds = testIter.next();
      INDArray features = ds.getFeatures();
      System.out.println("Input features: " + features);
      INDArray labels = ds.getLabels();
      INDArray predicted = model.output(features,false);
      System.out.println("Predicted output: "+ predicted);
      System.out.println("Desired output: "+ labels);
      eval.eval(labels, predicted);
      System.out.println();
    }
    System.out.println(eval.stats());
    System.out.println("Evaluate model....");
    System.out.println(eval.stats());

    System.out.println("\n****************Example finished********************");
  }
}
~~~~~~~~

It is very important to not use training data for testing because performance on recognizing training data should always be good assuming that you have enough memory capacity in a network (i.e., enough hidden units and enough neurons in each hidden layer).

After training the model in lines TBD through TBD, we test the model (lines TBD through TBD) on the separate test data. The program output when I ran the model is (much output removed fr brevity):

{line-numbers=off}
~~~~~~~~
Input features: [[    6.0000,   10.0000,   10.0000,    2.0000,    8.0000,   10.0000,    7.0000,    3.0000,    3.0000], 
  ...
  ]]
Predicted output: [ 
 [    0.1611,    0.8389],
   ...
   ]]
Desired output: [ 
 [         0,    1.0000],
 ...
 ]

========================Evaluation Metrics========================
 # of classes:    2
 Accuracy:        0.8846
 Precision:       0.9000
 Recall:          0.8929
 F1 Score:        0.8800
Precision, recall & F1: macro-averaged (equally weighted avg. of 2 classes)

=========================Confusion Matrix=========================
  0  1
-------
 12  0 | 0 = 0
  3 11 | 1 = 1

Confusion matrix format: Actual (rowClass) predicted as (columnClass) N times
==================================================================

****************Example finished********************
~~~~~~~~

The F1 score is calculated as twice precision times recall, all divided by precision + recall. We would like F1 to be as close to 1.0 as possible and it is common to spend a fair amount of time experimenting with meta learning parameters to increase F1.

It is also fairly common to try to learn good values of meta learning parameters also. We won't do this here but the process involves splitting the data into three disjoint sets: training, validation, and testing. The meta parameters are varied, training is performed, and using the validation data the best set of meta parameters is selected. Finally, we test the network as defined my meta parameters and learned weights for those meta parameters with the separate test data to see what the effective F1 score is.

## Configuring the example using Maven

TBD


## Documentation Other Types of Deep Learning Layers

The [documentation for the built-in layer classes in DL4J](https://deeplearning4j.org/api/latest/org/deeplearning4j/nn/conf/layers/package-tree.html) is probably more than you need for now so let's review the most commonly used (at least by me). In the simple example we used in the last section we used two types of layers:

- [org.deeplearning4j.nn.conf.layers.DenseLayer](https://deeplearning4j.org/api/latest/org/deeplearning4j/nn/conf/layers/DenseLayer.html) - maintains connections to all neurons in the previous and next layer, or it is "fully connected."
- [org.deeplearning4j.nn.conf.layers.OutputLayer](https://deeplearning4j.org/api/latest/org/deeplearning4j/nn/conf/layers/OutputLayer.html) - has built-in behavior for starting the back propagation calculations back through previous layers.

As you build more deep learning enabled applications, depending on what requirements you have, you will likely need to use at least some of the following Dl4J layer classes:

- [org.deeplearning4j.nn.conf.layers.AutoEncoder](https://deeplearning4j.org/api/latest/org/deeplearning4j/nn/conf/layers/AutoEncoder.html) - often used to remove noise from data. Autoencoders work by making the target training output values equal to the input training values while reducing the number of neurons in the AutoEncoding layer. The layer learns a concise representation of data, or "generalizes" data learning what features are important.
- [org.deeplearning4j.nn.conf.layers.CapsuleLayer](https://deeplearning4j.org/api/latest/org/deeplearning4j/nn/conf/layers/CapsuleLayer.html) - Capsule networks are an attempt to be more efficient versions of convolutional models. Convolutional networks discard position information of detected features while capsule models maintain and use this information.
- [org.deeplearning4j.nn.conf.layers.Convolution1D](https://deeplearning4j.org/api/latest/org/deeplearning4j/nn/conf/layers/Convolution1DLayer.html) - one dimensional convolutional layers learn one dimensional feature detectors. Trained layers learn to recognize features but discard the information of where the feature is located. These are often used for data input streams like signal data and word tokens in natural language processing.
- [org.deeplearning4j.nn.conf.layers.Convolution2D](https://deeplearning4j.org/api/latest/org/deeplearning4j/nn/conf/layers/Convolution2D.html) - two dimensional convolutional layers learn two dimensional feature detectors. Trained layers learn to recognize features but discard the information of where the feature is located. These are often used for recognizing if a type of object appears inside a picture. Note that features, for example representing a nose or a mouth, are recognized but their location in an input picture does not matter. So you could cut up an image of someone's face, for example, moving the ears to the picture center, the mouth to the upper left corner, etc., and the picture would still be predicted to contain a face with some probability because using soft max output layers produces class labels that can be interpreted as probabilities since the values over all output classes sum to the value 1.
- [org.deeplearning4j.nn.conf.layers.EmbeddingLayer](https://deeplearning4j.org/api/latest/org/deeplearning4j/nn/conf/layers/EmbeddingLayer.html) - embedding layers are used to transform input data into integer data. My most frequent use of embedding layers is word embedding where each word in training data is, for example, assigned an integer value. This data can be "one hot encoded" and in the case of processing words, if there are 5000 unique words in the training data for a classifier, then the embedding layer would have 5001 neurons, one for each word and one to represent all words not in the training data.
- org.deeplearning4j.nn.conf.layers.FeedForwardLayer;
- org.deeplearning4j.nn.conf.layers.DropoutLayer;
- org.deeplearning4j.nn.conf.layers.LSTM;
- org.deeplearning4j.nn.conf.layers.Pooling1D;
- org.deeplearning4j.nn.conf.layers.Pooling2D;


## Deep Learning Wrapup

I first used complex neural network topologies in the late 1980s for phoneme (speech) recognition, specifically using time delay neural networks and I gave a talk about it at [IEEE First Annual International Conference on Neural Networks San Diego, California June 21-24, 1987](http://ieeexplore.ieee.org/xpl/articleDetails.jsp?reload=true&arnumber=4307059). Back then, neural networks were not really considered to be a great technology for this application but in the present time Google, Microsoft, and other companies are using deep (many layered) neural networks for speech and image recognition. Exciting work is also being done in the field of natural language processing. I just provided a small example in this chapter that you can experiment with easily. I wanted to introduce you to [Deeplearning4j](http://deeplearning4j.org/) because I think it is probably the easiest way for Java developers to get started working with many layered neural networks and I refer you to [the project documentation](http://deeplearning4j.org/documentation.html).

I managed a deep learning team at Capital One 2018-2019 and while there much of my work involved GANs and LSTM deep models (and many of the my 55 US patents were at least partially inspired by this work). I refer you to a good GAN DL4J example on the web [https://github.com/wmeddie/dl4j-gans](https://github.com/wmeddie/dl4j-gans) and a tutorial on LSTM applications from the developers of DL4J [https://deeplearning4j.konduit.ai/getting-started/tutorials/clinical-time-series-lstm](https://deeplearning4j.konduit.ai/getting-started/tutorials/clinical-time-series-lstm).
