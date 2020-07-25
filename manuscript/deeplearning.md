# Deep Learning Using Deeplearning4j {#dl4j}

One limitation of back propagation neural networks seen in the last chapter is that they are limited to the number of neuron layers that can be efficiently trained. If you experimented with the sample back propagation code then you may have noticed that it took longer to train a network with two hidden layers compared to the training time for a network with only one hidden layer. There are also problems like vanishing gradients (the backpropagated errors that are used to update connection weights) that occur in architectures with many layers. Deep learning uses computational improvements to mitigate the vanishing gradient problem like using ReLu activation functions rather than the more traditional Sigmoid function, and networks called "skip connections" networks where some layers are initially turned off with connections skipping to the next active layer. After some initial training the skipped layers are activated and become part of the model (as in ResNet50, mentioned in the section [Roadmap for the DL4J Model Zoo](#zoo) at the end of this chapter).

Digging deeper into the problem of vanishing gradients, the problem with back propagation networks is that as error gradients are back propagated through the network toward the input layer, the gradients get smaller and smaller. The effect is that it can take a lot of time to train back propagation networks with many hidden layers. Even worse, the small backpropagated errors get so small that they cause numerical underflows.

I became interested in deep learning neural networks when I took Geoffrey Hinton's Neural Network class (a Coursera class, taken summer of 2012) and then for the next seven years most of my professional work involved deep learning. I have used GAN (generative adversarial networks) models for synthesizing numeric spreadsheet data, LSTM (long short term memory) models to synthesize highly structured text data like nested JSON, and for NLP (natural language processing). Several of my 55 US patents use neural network and Deep Learning technology.

The [Deeplearning4j.org](http://deeplearning4j.org/) Java library supports many neural network algorithms including support for Deep Learning (DL).  Note that I will often refer to Deeplearning4j as DL4J. 

We will first look at a simple example of a feed forward network using the same University of Wisconsin cancer database that we used earlier. Deep learning refers to neural networks with many layers, possibly with weights connecting neurons in non-adjacent layers which makes it possible to model temporal and spacial patterns in data.

There is a separate [repository of DL4J examples](https://github.com/eclipse/deeplearning4j-examples) that you should clone because the last half of this chapter is a general discussion of running the DL4J examples and modifying them for your needs with one additional example using LSTM models.

After the first simple example we then look at how to set up DL4J projects using Maven, and then discuss other types of layer classes that you will likely use in your projects. After learning how to set up and use DL4J and having a roadmap of commonly used layer classes, then you will then be set to work on your own projects.

## Feed Forward Classification Networks

Feed forward classification networks are a type of deep neural network that can contain multiple hidden neuron layers. In the example here the adjacent layers are fully connected (all neurons in adjacent layers are connected), as in the examples from the last chapter. The difference here is the use of the DL4J library that is written to scale to large problems and to use GPUs if you have them available.

In general, simpler network architectures are better than unnecessarily complicated architectures. You can start with simple architectures and add layers, different layer types, and parallel models as-needed. For feed forward networks model complexity has two dimensions: the numbers of neurons in hidden layers, and also the number of hidden layers. If you put too many neurons in hidden layers then the training data is effectively memorized and this will hurt performance on data samples not used in training. In practice, I "starve the network" by reducing the number of hidden neurons until the model has reduced accuracy on independent test data. Then I slightly increase the number of neurons in hidden layers. This technique helps avoid models simply memorizing training data.

## Feed Forward Example

The following screen shot shows an IntelliJ project (you can use the free community or professional version for the examples in this book) for the example in this chapter:

![IntelliJ project view for the examples in this chapter](images/intellij_dl.png)

The Deeplearning4j library can use user-written Java classes to import training and testing data into a form that the Deeplearning4j library can use. Some of the examples at [https://github.com/eclipse/deeplearning4j-examples](https://github.com/eclipse/deeplearning4j-examples) use custom data loaders but in this simple example we use built-in utilities for reading spreadsheet data (see lines 46-56 in the following listing).

The class **ClassifierWisconsinData** reads the University of Wisconsin cancer training and testing data sets, creates a model (lines 59-81), trains it (line 82) and tests it (lines 84-97). The value of the variable **numHidden** set in line 3 refers to the number of neurons in each hidden layer.

You can increase the number of hidden units in line 36 (something that you might do for more complex problems). To add a hidden layer you can repeat lines 66-71, and you would change the layer indices (first argument) as appropriate in calls to the chained method **.layer()** so the layer indices are all different and increasing in value.


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

  private static final Logger log = 
     LoggerFactory.getLogger(ClassifierWisconsinData.class);

  public static void main(String[] args) throws Exception {
    int numHidden = 3;
    int numOutputs = 1;
    int batchSize = 64;

    int seed = 33117;

    int numInputs = 9;
    int labelIndex = 9;
    int numClasses = 2;

    RecordReader recordReader = new CSVRecordReader();
    recordReader.initialize(new FileSplit(new File("data/","training.csv")));
    
    DataSetIterator trainIter =
      new RecordReaderDataSetIterator(recordReader,batchSize,labelIndex,numClasses);

    RecordReader recordReaderTest = new CSVRecordReader();
    recordReaderTest.initialize(
        new FileSplit(new File("data/","testing.csv")));
    DataSetIterator testIter =
        new RecordReaderDataSetIterator(recordReaderTest,batchSize,
                                    labelIndex,numClasses);

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
    System.out.println("Evaluate model....");
    System.out.println(eval.stats());
  }
}
~~~~~~~~

It is very important to not use training data for testing because performance on recognizing training data should always be good assuming that you have enough memory capacity in a network (i.e., enough hidden units and enough neurons in each hidden layer).

The program output is (much output removed for brevity):

{line-numbers=off}
~~~~~~~~
Input features: [[6.0000, 10.0000, 10.0000, 2.0000,    8.0000, 10.0000, 7.0000, 3.0000, 3.0000], 
  ...
  ]]
Predicted output: [ 
 [0.1611, 0.8389],
   ...
   ]]
Desired output: [ 
 [0, 1.0000],
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

~~~~~~~~

The F1 score is calculated as twice precision times recall, divided by precision + recall. We would like F1 to be as close to 1.0 as possible and it is common to spend a fair amount of time experimenting with meta learning parameters to increase F1.

It is also fairly common to try to learn good values of meta learning parameters also. We won't do this here but the process involves splitting the data into three disjoint sets: training, validation, and testing. The meta parameters are varied, training is performed, and using the validation data the best set of meta parameters is selected. Finally, we test the network as defined by meta parameters and learned weights for those meta parameters with the separate test data to see what the effective F1 score is.

## Configuring the Example Using Maven

There is a Maven pom.xml configuration file for this example that is configured for a recent version of DL4J (as I write this in July 2020). DL4J is fairly good at detecting if the Open BLAS library is available, if CUDA software support for any GPUs on your system are available, etc. If you try running the *Makefile* and get any errors, then check the [DL4J Quickstart and setup guide](https://deeplearning4j.konduit.ai/getting-started/quickstart) to see if there are any dependencies that you need on your system. The *Makefile* has a single target:

{line-numbers=off}
~~~~~~~~
deep_wisconsin:
  mvn install
  mvn exec:java \
    -Dexec.mainClass="com.markwatson.deeplearning.ClassifierWisconsinData"
~~~~~~~~

## Documentation for Other Types of Deep Learning Layers

The [documentation for the built-in layer classes in DL4J](https://deeplearning4j.org/api/latest/org/deeplearning4j/nn/conf/layers/package-tree.html) is probably more than you need for now so let's review the most other types of layers that I sometimes use. In the simple example we used in the last section we used two types of layers:

- [org.deeplearning4j.nn.conf.layers.DenseLayer](https://deeplearning4j.org/api/latest/org/deeplearning4j/nn/conf/layers/DenseLayer.html) - maintains connections to all neurons in the previous and next layer, or it is "fully connected."
- [org.deeplearning4j.nn.conf.layers.OutputLayer](https://deeplearning4j.org/api/latest/org/deeplearning4j/nn/conf/layers/OutputLayer.html) - has built-in behavior for starting the back propagation calculations back through previous layers.

As you build more deep learning enabled applications, depending on what requirements you have, you will likely need to use at least some of the following Dl4J layer classes:

- [org.deeplearning4j.nn.conf.layers.AutoEncoder](https://deeplearning4j.org/api/latest/org/deeplearning4j/nn/conf/layers/AutoEncoder.html) - often used to remove noise from data. Autoencoders work by making the target training output values equal to the input training values while reducing the number of neurons in the AutoEncoding layer. The layer learns a concise representation of data, or "generalizes" data by learning in which features are important.
- [org.deeplearning4j.nn.conf.layers.CapsuleLayer](https://deeplearning4j.org/api/latest/org/deeplearning4j/nn/conf/layers/CapsuleLayer.html) - Capsule networks are an attempt to be more efficient versions of convolutional models. Convolutional networks discard position information of detected features while capsule models maintain and use this information.
- [org.deeplearning4j.nn.conf.layers.Convolution1D](https://deeplearning4j.org/api/latest/org/deeplearning4j/nn/conf/layers/Convolution1DLayer.html) - one dimensional convolutional layers learn one dimensional feature detectors. Trained layers learn to recognize features but discard the information of where the feature is located. These are often used for data input streams like signal data and word tokens in natural language processing.
- [org.deeplearning4j.nn.conf.layers.Convolution2D](https://deeplearning4j.org/api/latest/org/deeplearning4j/nn/conf/layers/Convolution2D.html) - two dimensional convolutional layers learn two dimensional feature detectors. Trained layers learn to recognize features but discard the information of where the feature is located. These are often used for recognizing if a type of object appears inside a picture. Note that features, for example representing a nose or a mouth, are recognized but their location in an input picture does not matter. For example, you could cut up an image of someone's face, moving the ears to the picture center, the mouth to the upper left corner, etc., and the picture would still be predicted to contain a face with some probability because using soft max output layers produces class labels that can be interpreted as probabilities since the values over all output classes sum to the value 1.
- [org.deeplearning4j.nn.conf.layers.EmbeddingLayer](https://deeplearning4j.org/api/latest/org/deeplearning4j/nn/conf/layers/EmbeddingLayer.html) - embedding layers are used to transform input data into integer data. My most frequent use of embedding layers is word embedding where each word in training data is assigned an integer value. This data can be "one hot encoded" and in the case of processing words, if there are 5000 unique words in the training data for a classifier, then the embedding layer would have 5001 neurons, one for each word and one to represent all words not in the training data. If the word index (indexing is zero-based) is, for example 117, then the activation value for neuron at index 117 is set to one and all others in the layer are set to zero.
- [org.deeplearning4j.nn.conf.layers.FeedForwardLayer](https://deeplearning4j.org/api/latest/org/deeplearning4j/nn/conf/layers/FeedForwardLayer.html) - this is a super class for most specialized types of feed forward layers so reading through the class reference is recommended.
- [org.deeplearning4j.nn.conf.layers.DropoutLayer](https://deeplearning4j.org/api/latest/org/deeplearning4j/nn/conf/layers/DropoutLayer.html) - dropout layers are very useful for preventing learning new input patterns from making the network forget previously learned patterns. For each training batch, some fraction of neurons in a dropout layer are turned off and don't update their weights during a training batch cycle. The development of using dropout was key historically for getting deep learning networks to work with many layers and large amounts of training data.
- [org.deeplearning4j.nn.conf.layers.LSTM](https://deeplearning4j.org/api/latest/org/deeplearning4j/nn/conf/layers/LSTM.html) - LSTM layers are used to extend the temporal memory of what a layer can remember. LSTM are a refinement of RNN models that use an input window to pass through a data stream and the RNN model can only use what is inside this temporal sampling window.
- [org.deeplearning4j.nn.conf.layers.Pooling1D](https://deeplearning4j.org/api/latest/org/deeplearning4j/nn/conf/layers/Pooling1D.html) - a one dimensional pooling layer transforms a longer input to a shorter output by downsampling, i.e., there are fewer output connections than input connections.
- [org.deeplearning4j.nn.conf.layers.Pooling2D](https://deeplearning4j.org/api/latest/org/deeplearning4j/nn/conf/layers/Pooling2D.html) - a two dimensional pooling layer transforms a larger two dimensional array of data input to a smaller output two dimensional array by downsampling.

## Running the DL4J Example Programs and Modifying Them For Your Use

To get started clone the DL4J examples repository written by the authors of DL4J (if you have not already done so) and fetch all of the required libraries:

{lang="bash",linenos=off}
~~~~~~~~
git clone https://github.com/eclipse/deeplearning4j-examples
cd deeplearning4j-examples/dl4j-examples
mvn install
~~~~~~~~

In the next section we will modify [Alex Black's](https://github.com/AlexDBlack) character generating LSTM example for a different application (modeling and generating CSV data). Before moving on to the example in the next section you may want to run his example using:

{lang="bash",linenos=off}
~~~~~~~~
cd deeplearning4j-examples/dl4j-examples
mvn exec:java -Dexec.mainClass="org.deeplearning4j.examples.advanced.modelling.charmodelling.generatetext.GenerateTxtModel"
~~~~~~~~

His example downloads the complete works of Shakespeare from the web and trains a recurrent network LSTM model by passing an input window through the complete text. Each input character is one-hot encoded and the target output is the same one-hot encoded text data in the input window except the sample is shifted one character further in the input text. The model learns to predict the next character in a sequence given a sample of input seed text.

Let's review one-hot encoding. We need to convert each character in the training data to a one-hot encoding which is a vector of all 0.0 values except for a single value of 1.0. If there are, for example,  256 unique characters in the training data the vector will have 256+1 elements because we add an "unknown character" that represents characters the model may see in the future that are not in the training data. For example if the index of character "m" is 77, then we set element at index 77 to one, all other elements being zero. We won't look at the implementation of one-hot encoding here because DL4J provides APIs for this. I cover one-hot-encoding implementation in another book in the chapter on [Deep Learning for the Hy Language](https://leanpub.com/hy-lisp-python/read#leanpub-auto-deep-learning) that you can read online.

Here we will be using one-hot encoding for characters but it also works well for encoding words in a vocabulary. Training data may typically have about 10,000 unique words so the one-hot encoding vector would have 10,001 elements. We add a special word position for "unknown word." For some applications we also use word-embeddings using a smaller vector, 500 elements being a reasonable size. We won't be using word-embeddings here, or one-hot word embedding but I want you to know what these terms mean.

By processing sufficient sample text, an LSTM network can learn to model the "language" of the input text. If you train on samples from Shakespeare then the model generates text that looks like Shakespeare wrote it. This also works for any author with a specific writing style. LSTM networks are used to model programming languages and many types of structured information.

For a customer, I used an LSTM model trained on JSON log data from AWS. They wanted to have a large amount of test data that did not contain any sensitive information so we generated "fake data" in the correct schema. The LSTM model worked fairly well, the major restriction being that I checked each generated JSON datum for having valid syntax and be valid to the Schema of the original data, discarding samples that failed these tests.

The examples provided with DL4J cover most of the deep learning use cases you may require for your work so I want you to be comfortable running all of the examples and knowing where to start if you want to modify them for a different purposes. If you load the entire DL4J examples repository in a Java IDE and use global search then you should be able to find appropriate CNN, Classification, Regression, etc., examples similar to your current use case that you can modify.

As an experiment, I wanted to try using an LSTM character generation model to generate CSV style spreadsheet data and you can see the implementation of this idea in next section where I modified Alex Black's character generating LSTM example.

## Modifying the Character Generating LSTM Example to Model and Generate CSV Spreadsheet Data

Here are the major changes the I made to the **GenerateTxtModel** example written by Alex Black. I start by preparing the text training data where I substitute the original method that uses the complete works of William Shakespeare in the class **getShakespeareIterator** with a new class **getWisconsinDataIterator** that processes the CSV file in **data/training.csv** in class **LstmCharGenerator**:

{lang="java",linenos=off}
~~~~~~~~
  static CharacterIterator getWisconsinDataIterator(int miniBatchSize,
                           int sequenceLength)
                 throws IOException {
    String fileLocation = "data/training.csv";
    char[] validCharacters = {'0', '1', '2', '3', '4', '5', '6', '7', '8',
                              '9', ',', '\n'};
    System.out.println("++ valid characters in training data: ");
    for (char ch : validCharacters) System.out.print(" " + ch);
    System.out.println();
    return new CharacterIterator(fileLocation, StandardCharsets.UTF_8,
        miniBatchSize, sequenceLength, validCharacters, new Random(12345));
  }
~~~~~~~~

Changes for configuring the model:

{lang="java",linenos=off}
~~~~~~~~
    int lstmLayerSize = 400; //Number of units in each LSTM layer
    int miniBatchSize = 16; //Size of mini batch to use when  training
    int exampleLength = 250;; //Length of each training example sequence to use.
    int tbpttLength = 40; //Length for truncated backpropagation through time
    int numEpochs = 100; //Total number of training epochs
    int generateSamplesEveryNMinibatches = 5; //How frequently to generate samples
    int nSamplesToGenerate = 20; //Number of samples to generate each training epoch
    int nCharactersToSample = 300; //Length of each sample to generate
~~~~~~~~

I made a third change to Alex Black's example: I discarded generated samples that didn't match the schema of the original data in the method **sampleCharactersFromNetwork**:

{lang="java",linenos=off}
~~~~~~~~
          System.out.println("----- Samples -----");
          for( int j=0; j<samples.length; j++ ) {
            // discard samples that don't contain 10 numbers per line
            String [] lines = samples[j].split("\n");
            for (int k=0; k<lines.length; k++) {
              if (StringUtils.countMatches(lines[k], ",") == 9 &&
                  lines[k].split(",").length == 10) {
                System.out.println(lines[k]);
              }
            }
          }
~~~~~~~~

We will look at three samples taken about two minutes into the training process, after ten minutes, and finally after twenty minutes. You might want to look at the training file **data/training.csv** to understand what we are trying to model and then generate similar data.

Here is sample output after training the model for about two minutes (most lines are not properly formatted or valid):

{linenos=off}
~~~~~~~~
0,4,000,2,5,1,1,5,8,0
,,,,61,,,,,8
,,,6014,0,1,1,1,1,0
1,4,,3,1,2,2,5,3,0
1,,7,,,,60,1,0,7
1,2,6,1,2,1,3,1,1,6
0,5,1,2,3,2,1,,000000,8
8,1,5,1,,8,1,0,1,0
2,1,2,6,1,1,2,8,0,2
~~~~~~~~

The next sample shows output after training the model for a ten minutes. There are errors in lines 1 and 4. Line 1 has a value of "16" in the row that is outside the allowed data range. Line 4 has the value "13" that is also outside the allowed data range.

{linenos=on}
~~~~~~~~
1,1,1,1,2,16,7,1,1,1
6,1,1,1,2,1,3,1,1,0
6,1,1,1,2,1,1,1,1,0
13,10,4,10,3,10,10,7,1,1
1,1,1,1,2,1,1,1,1,0
2,1,1,1,2,1,1,1,1,0
~~~~~~~~


Final model after training for twenty minutes:

{linenos=off}
~~~~~~~~
1,1,1,1,1,1,1,1,1,0
5,1,1,1,2,1,2,1,1,0
8,8,6,10,2,10,7,10,1,1
5,1,1,1,2,1,1,1,1,0
2,1,4,3,2,1,1,1,1,0
1,1,1,1,1,1,1,1,1,0
1,1,1,1,1,1,3,1,1,0
4,1,1,1,3,1,1,10,2,1
~~~~~~~~

This example should convince you, dear reader, that the language modeling capabilities of LSTM models is surprising and effective. 

You can modify this example to try modeling other types of test. You might try modeling a large sample of Python or Java source code, or text in any language you might know like German, Farsi, Hebrew, or Spanish. For languages like Farsi and Hebrew that read from right to left you would need to either use a Bidirectional LSTM or change the program to stream input "backwards" for each line so a standard LSTM could correctly predict characters in the order that a human reader processes.

There are interesting papers on using LSTM models to analyze design specifications that feeds into convolutional layers that generate images for design documents. Is this process perfect? No, but still impressive and provides some intuition into what may be possible in the future.

## Roadmap for the DL4J Model Zoo {#zoo}

DL4J supports a Model Zoo containing the following pre-trained models your own projects ([see documentation](https://deeplearning4j.konduit.ai/model-zoo/zoo-models)):

- AlexNet - was a breakthrough for image recognition, AlexNet is a convolutional neural network designed by Alex Krizhevsky (with Ilya Sutskever and Geoffrey Hinton). AlexNet used Relu instead of arc-tangent or Sigmoid activation.
- Darknet19 - is a type of realtime YOLO model.
- FaceNetNN4Small2 - is a small version of the FaceNet embeddings for face recognition.
- InceptionResNetV1 - Inception models use many convolutional layers. 
- LeNet - of historical interest, a convolutional model design by Yann LeCun and his colleagues (1998).
- NASNet - like Inception and Xception models, with claimed superior results.
- ResNet50 - residual neural network that uses "skip connections" that connect neurons in non-adjacent layers which helps reduce the vanishing gradient problem for models with many layers. Skipped layers are connected later in the training process (example class **AlphaGoZeroTrainer**).
- SimpleCNN - simple architecture using alternating pooling and convolutional layers.
- SqueezeNet - architecture for computer vision that experiments with smaller neural networks with fewer parameters.
- TextGenerationLSTM - a general model architecture for using LSTM layers for building language models from input text and then generating new similar text.
- TinyYOLO - a small YOLO model (example class *TinyYoloHouseNumberDetection** demonstrates Transfer Learning).
- UNet - convolutional model designed to perform medical image segmentation.
- VGG16 - convolutional model for classification and detection using convolutional (with ReLu), max pooling, and fully connected layers (example class **FitFromFeaturized** demonstrates Transfer Learning).
- VGG19 - a larger variant of VGG16 that uses (16 convolution layers, 3 fully connected layers, 5 max pooling layers and 1 SoftMax layer.
- Xception - a newer version of Inception V3 with slightly better performance but long training times.
- YOLO2 - "you only look once" real time object detection.

If you are interested in fine tuning existing models (Transfer Learning) then I suggest that you start with the example class **FitFromFeaturized**. You can look at the code using your favorite editor and run this example using:

{lang="bash",linenos=off}
~~~~~~~~
$ cd deeplearning4j-examples/dl4j-examples

$ emacs src/main/java/org/deeplearning4j/examples/advanced/features/transferlearning/editlastlayer/presave/FitFromFeaturized.java

$ mvn exec:java -Dexec.mainClass="org.deeplearning4j.examples.advanced.features.transferlearning.editlastlayer.presave.FitFromFeaturized"
~~~~~~~~

This is a good example to get started with because it is short (about 90 lines of code) and shows clearly how to take a saved model and build a configuration to add your own output layer and then perform additional training using your own data.

## Deep Learning Wrapup

I first used complex neural network topologies in the late 1980s for phoneme (speech) recognition, specifically using time delay neural networks and I gave a talk about it at [IEEE First Annual International Conference on Neural Networks San Diego, California June 21-24, 1987](http://ieeexplore.ieee.org/xpl/articleDetails.jsp?reload=true&arnumber=4307059). In the following year I wrote the Backpropagation neural network code that my company used in a bomb detector that we built for the FAA. Back then, neural networks were not widely accepted but in the present time Google, Microsoft, and many other companies are using deep learning for a wide range of practical problems. Exciting work is also being done in the field of natural language processing. The examples in this chapter are simple so you can experiment with them easily. I wanted to introduce you to [Deeplearning4j](http://deeplearning4j.org/) because I think it is probably the easiest way for Java developers to get started working with many layered neural networks and I refer you to [the project documentation](http://deeplearning4j.org/documentation.html).

I managed a deep learning team at Capital One (2018-2019) and while there much of my work involved GANs and LSTM deep models (and several of my 55 US patents were at least partially inspired by this work). I refer you to a good GAN DL4J example on the web [https://github.com/wmeddie/dl4j-gans](https://github.com/wmeddie/dl4j-gans) and a tutorial on LSTM applications from the developers of DL4J [https://deeplearning4j.konduit.ai/getting-started/tutorials/clinical-time-series-lstm](https://deeplearning4j.konduit.ai/getting-started/tutorials/clinical-time-series-lstm).

Deep Learning has become a standard tool for modeling data and making predictions or classifying data. Most of the online classes on Deep Learning use Python. DL4J can import Keras/TensorFlow models so one strategy is for you to build models using Python and import trained models into DL4J.
