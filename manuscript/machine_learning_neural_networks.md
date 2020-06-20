# Neural Networks {#neural-networks}

Neural networks can be used to efficiently solve many problems that are
intractable or difficult using other AI programming techniques. I spent
almost two years on a DARPA neural network tools advisory panel, wrote
the first version of the ANSim neural network product, and have used
neural networks for a wide range of application problems (radar
interpretation, bomb detection, and as controllers in computer games).
Mastering the use of simulated neural networks will allow you to solve
many types of problems that are very difficult to solve using other
methods.

I will cover Hopfield and Backpropagation neural networks in this chapter.
As I am writing this, the fourth edition of this book, there is an exciting
new technology that often goes by the name "Deep Learning" that uses a
computational trick to train Backpropagation networks with many layers.
Google has used Deep Learning networks for automated image identification.
I am not covering Deep Learning in this book, but after experimenting with the
Java Backpropagation in this chapter you might then look at these
[Examples of Deep Learning]{#http://deeplearning.net/tutorial/}.

Although most of this book is intended to provide practical advice (with
some theoretical background) on using AI programming techniques, I
cannot imagine being interested in practical AI programming without also
wanting to think about the philosophy and mechanics of how the human
mind works. I hope that my readers share this interest.

In this book, we have examined techniques for focused problem solving,
concentrating on performing one task at a time. However, the physical
structure and dynamics of the human brain is inherently parallel and
distributed [*Parallel Distributed Processing: Explorations in the
Microstructure of Cognition*, Rumelhart, McClelland, etc. 1986]. We are
experts at doing many things at once. For example, I simultaneously can
walk, talk with my wife, keep our puppy out of cactus, and enjoy the
scenery behind our house in Sedona, Arizona. AI software systems
struggle to perform even narrowly defined tasks well, so how is it that
we are able to simultaneously perform several complex tasks? There is no
clear or certain answer to this question at this time, but certainly the
distributed neural architecture of our brains is a requirement for our
abilities. Unfortunately, artificial neural network simulations do not
currently address “multi-tasking” (other techniques that do address this
issue are multi-agent systems with some form or mediation between
agents).

Also interesting is the distinction between instinctual behavior and
learned behavior. Our knowledge of GAs from [Chapter on Genetic Algorithms](#ga)
provides a clue to how the brains of especially lower order animals can
be hardwired to provide efficient instinctual behavior under the
pressures of evolutionary forces (i.e., likely survival of more fit
individuals). This works by using genetic algorithms to design specific
neural wiring. I have used genetic algorithms to evolve recurrent neural
networks for control applications. This work only had partial success
but did convince me that biological genetic pressure is probably
adequate to “pre-wire” some forms of behavior in natural (biological)
neural networks.

While we will study supervised learning techniques in this chapter, it
is possible to evolve both structure and attributes of neural networks
using other types of neural network models like Adaptive Resonance
Theory (ART) to autonomously learn to classify learning examples without
intervention.

We will start this chapter by discussing human neuron cells and which
features of real neurons that we will model. Unfortunately, we do not
yet understand all of the biochemical processes that occur in neurons,
but there are fairly accurate models available (web search “neuron
biochemical”). Neurons are surrounded by thin hair-like structures
called dendrites which serve to accept activation from other neurons.
Neurons sum up activation from their dendrites and each neuron has a
threshold value; if the activation summed over all incoming dendrites
exceeds this threshold, then the neuron fires, spreading its activation
to other neurons. Dendrites are very localized around a neuron. Output
from a neuron is carried by an axon, which is thicker than dendrites and
potentially much longer than dendrites in order to affect remote
neurons. The following figure shows the physical structure of a
neuron; in general, the neuron’s axon would be much longer than is seen
in this figure. The axon terminal buttons transfer
activation to the dendrites of neurons that are close to the individual
button. An individual neuron is connected to up to ten thousand other
neurons in this way.

{#nn-neuron}
![Neuron](images/nn_neuron.png)


The activation absorbed through dendrites is summed together, but the
firing of a neuron only occurs when a threshold is passed. In neural network simulations there are several common ways to model neurons and connections between neurons that we will see n both this and the next chapter.

## Hopfield Neural Networks  {#hopfield}

Hopfield neural networks implement associative (or content addressable)
memory. A Hopfield network is trained using a set of patterns. After
training, the network can be shown a pattern similar to one of the
training inputs and it will hopefully associate the “noisy” pattern with
the correct input pattern. Hopfield networks are very different than
back propagation networks (covered later in the [Section of Backpropagation](#backprop))
because the training data only contains input examples unlike back
propagation networks that are trained to associate desired output
patterns with input patterns. Internally, the operation of Hopfield
neural networks is very different than back propagation networks. We use
Hopfield neural networks to introduce the subject of neural nets because
they are very easy to simulate with a program, and they can also be very
useful in practical applications.

The inputs to Hopfield networks can be any dimensionality. Hopfield
networks are often shown as having a two-dimensional input field and are
demonstrated recognizing characters, pictures of faces, etc. However, we
will lose no generality by implementing a Hopfield neural network
toolkit with one-dimensional inputs because a two-dimensional image can
be represented by an equivalent one-dimensional array.

How do Hopfield networks work? A simple analogy will help. The trained
connection weights in a neural network represent a high dimensional
space. This space is folded and convoluted with local minima
representing areas around training input patterns. For a moment,
visualize this very high dimensional space as just being the three
dimensional space inside a room. The floor of this room is a convoluted
and curved surface. If you pick up a basketball and bounce it around the
room, it will settle at a low point in this curved and convoluted floor.
Now, consider that the space of input values is a two-dimensional grid a
foot above the floor. For any new input, that is equivalent to a point
defined in horizontal coordinates; if we drop our basketball from a
position above an input grid point, the basketball will tend to roll
down hill into local gravitational minima. The shape of the curved and
convoluted floor is a calculated function of a set of training input
vectors. After the “floor has been trained” with a set of input vectors,
then the operation of dropping the basketball from an input grid point
is equivalent to mapping a new input into the training example that is
closest to this new input using a neural network.

A common technique in training and using neural networks is to add noise
to training data and weights. In the basketball analogy, this is
equivalent to “shaking the room” so that the basketball finds a good
minima to settle into, and not a non-optimal local minima. We use this
technique later when implementing back propagation networks. The weights
of back propagation networks are also best visualized as defining a very
high dimensional space with a manifold that is very convoluted near
areas of local minima. These local minima are centered near the
coordinates defined by each input vector.

## Java Classes for Hopfield Neural Networks

The Hopfield neural network model is defined in the file Hopfield.java.
Since this file only contains about 65 lines of code, we will look at
the code and discuss the algorithms for storing and recall of patterns
at the same time. In a Hopfield neural network simulation, every neuron
is connected to every other neuron.

Consider a pair of neurons indexed by **i** and **j**. There is a weight
**W_{i,j}** between these neurons that corresponds in the code to the
array element **weight[i,j]**. We can define energy between the
associations of these two neurons as:

~~~~~~~~
energy[i,j] = -weight[i,j] * activation[i] * activation[j]
~~~~~~~~

In the Hopfield neural network simulator, we store activations (i.e.,
the input values) as floating point numbers that get clamped in value to
-1 (for off) or +1 (for on). In the energy equation, we consider an
activation that is not clamped to a value of one to be zero. This energy
is like “gravitational energy potential” using a basketball court
analogy: think of a basketball court with an overlaid 2D grid, different
grid cells on the floor are at different heights (representing energy
levels) and as you throw a basketball on the court, the ball naturally
bounces around and finally stops in a location near to the place you
threw the ball, in a low grid cell in the floor – that is, it settles in
a locally low energy level. Hopfield networks function in much the same
way: when shown a pattern, the network attempts to settle in a local
minimum energy point as defined by a previously seen training example.

When training a network with a new input, we are looking for a low
energy point near the new input vector. The total energy is a sum of the
above equation over all (i,j).

The class constructor allocates storage for input values, temporary
storage, and a two-dimensional array to store weights:

~~~~~~~~
     public Hopfield(int numInputs) {
       this.numInputs = numInputs;
       weights = new float[numInputs][numInputs];
       inputCells = new float[numInputs];
       tempStorage = new float[numInputs];
     } 
~~~~~~~~

Remember that this model is general purpose: multi-dimensional inputs
can be converted to an equivalent one-dimensional array. The method
**addTrainingData** is used to store an input data array for later
training. All input values get clamped to an “off” or “on” value by the
utility method **adjustInput**. The utility method **truncate** truncates
floating-point values to an integer value. The utility method
**deltaEnergy** has one argument: an index into the input vector. The
class variable **tempStorage** is set during training to be the sum of a
row of trained weights. So, the method **deltaEnergy** returns a measure
of the energy difference between the input vector in the current input
cells and the training input examples:

~~~~~~~~
     private float deltaEnergy(int index) {
       float temp = 0.0f;
       for (int j=0; j<numInputs; j++) {
         temp += weights[index][j] * inputCells[j];
       }
       return 2.0f * temp - tempStorage[index];
     } 
~~~~~~~~

The method **train** is used to set the two-dimensional weight array and
the one-dimensional **tempStorage** array in which each element is the sum
of the corresponding row in the two-dimensional weight array:

~~~~~~~~
     public void train() {
       for (int j=1; j<numInputs; j++) {
         for (int i=0; i<j; i++) {
           for (int n=0; n<trainingData.size(); n++) {
             float [] data = (float [])trainingData.elementAt(n);
             float temp1 = adjustInput(data[i]) * adjustInput(data[j]);
             float temp = truncate(temp1 + weights[j][i]);
             weights[i][j] = weights[j][i] = temp;
           }
         }
       }
       for (int i=0; i<numInputs; i++) {
         tempStorage[i] = 0.0f;
         for (int j=0; j<i; j++) {
           tempStorage[i] += weights[i][j];
         }
       }
     } 
~~~~~~~~

Once the arrays **weight** and **tempStorage** are defined, it is simple to
recall an original input pattern from a similar test pattern:

~~~~~~~~
     public float [] recall(float [] pattern, int numIterations) {
       for (int i=0; i<numInputs; i++) {
         inputCells[i] = pattern[i];
       }
       for (int ii = 0; ii<numIterations; ii++) {
         for (int i=0; i<numInputs; i++) {
           if (deltaEnergy(i) > 0.0f) {
             inputCells[i] = 1.0f;
           } else {
             inputCells[i] = 0.0f;
           }
         }
       }
       return inputCells;
     } 
~~~~~~~~

## Testing the Hopfield Neural Network Class

The test program for the Hopfield neural network class is
**Test\_Hopfield**. This test program defined three test input patterns,
each with ten values:

~~~~~~~~
     static float [] data [] = {
       { 1, 1, 1, -1, -1, -1, -1, -1, -1, -1},
       {-1, -1, -1, 1, 1, 1, -1, -1, -1, -1},
       {-1, -1, -1, -1, -1, -1, -1, 1, 1, 1} }; 
~~~~~~~~

The following code fragment shows how to create a new instance of the
**Hopfield** class and train it to recognize these three test input
patterns:

~~~~~~~~
     test = new Hopfield(10);
     test.addTrainingData(data[0]);
     test.addTrainingData(data[1]);
     test.addTrainingData(data[2]);
     test.train(); 
~~~~~~~~

The static method **helper** is used to slightly scramble an input
pattern, then test the training Hopfield neural network to see if the
original pattern is re-created:

~~~~~~~~
     helper(test, "pattern 0", data[0]);
     helper(test, "pattern 1", data[1]);
     helper(test, "pattern 2", data[2]); 
~~~~~~~~

The following listing shows an implementation of the method **helper**
(the called method **pp** simply formats a floating point number for
printing by clamping it to zero or one). This version of the code
randomly flips one test bit and we will see that the trained Hopfield
network almost always correctly recognizes the original pattern. The
version of method **helper** included in the ZIP file for this book is
slightly different in that two bits are randomly flipped (we will later
look at sample output with both one and two bits randomly flipped).

~~~~~~~~
     private static void helper(Hopfield test,
                                String s,
                                float [] test_data) {
       float [] dd = new float[10];
       for (int i=0; i<10; i++) {
         dd[i] = test_data[i];
       }
       int index = (int)(9.0f * (float)Math.random());
       if (dd[index] < 0.0f) dd[index] = 1.0f; else dd[index] = -1.0f;
       float [] rr = test.recall(dd, 5);
       System.out.print(s+"\nOriginal data: ");
       for (int i = 0; i < 10; i++)
         System.out.print(pp(test_data[i]) + " ");
       System.out.print("\nRandomized data: ");
       for (int i = 0; i < 10; i++)
         System.out.print(pp(dd[i]) + " ");
       System.out.print("\nRecognized pattern: ");
       for (int i = 0; i < 10; i++)
         System.out.print(pp(rr[i]) + " ");
       System.out.println();
     } 
~~~~~~~~

The following listing shows how to run the program, and lists the
example output:

~~~~~~~~
     java Test_Hopfield
     pattern 0
     Original data: 1 1 1 0 0 0 0 0 0 0
     Randomized data: 1 1 1 0 0 0 1 0 0 0
     Recognized pattern: 1 1 1 0 0 0 0 0 0 0
     pattern 1
     Original data: 0 0 0 1 1 1 0 0 0 0
     Randomized data: 1 0 0 1 1 1 0 0 0 0
     Recognized pattern: 0 0 0 1 1 1 0 0 0 0
     pattern 2
     Original data: 0 0 0 0 0 0 0 1 1 1
     Randomized data: 0 0 0 1 0 0 0 1 1 1
     Recognized pattern: 0 0 0 0 0 0 0 1 1 1 
~~~~~~~~

In this listing we see that the three sample training patterns in
**Test\_Hopfield.java** are re-created after scrambling the data by
changing one randomly chosen value to its opposite value. When you run
the test program several times you will see occasional errors when one
random bit is flipped and you will see errors occur more often with two
bits flipped. Here is an example with two bits flipped per test: the
first pattern is incorrectly reconstructed and the second and third
patterns are reconstructed correctly:

~~~~~~~~
     pattern 0
     Original data: 1 1 1 0 0 0 0 0 0 0
     Randomized data: 0 1 1 0 1 0 0 0 0 0
     Recognized pattern: 1 1 1 1 1 1 1 0 0 0
     pattern 1
     Original data: 0 0 0 1 1 1 0 0 0 0
     Randomized data: 0 0 0 1 1 1 1 0 1 0
     Recognized pattern: 0 0 0 1 1 1 0 0 0 0
     pattern 2
     Original data: 0 0 0 0 0 0 0 1 1 1
     Randomized data: 0 0 0 0 0 0 1 1 0 1
     Recognized pattern: 0 0 0 0 0 0 0 1 1 1 
~~~~~~~~



## Back Propagation Neural Networks  {#backprop}


The next neural network model that we will use is called back
propagation, also known as back-prop or delta rule learning. In this
model, neurons are organized into data structures that we call layers.
The [Figure showing Backpropagation network with No Hidden Layer](#nn-backprop-no-hidden) shows a simple neural network with two
layers; this network is shown in two different views: just the neurons
organized as two one-dimensional arrays, and as two one-dimensional
arrays with the connections between the neurons. In our model, there is
a connection between two neurons that is characterized by a single
floating-point number that we will call the connection’s weight. A
weight **W_{i,j}** connects input neuron **i** to output neuron **j**. In the
back propagation model, we always assume that a neuron is connected to
every neuron in the previous layer.

A key feature of back-prop neural networks is that they can be efficiently trained.
Training is performed by calculating sets of weights for connecting each
layer. As we will see, we will train networks by applying input values
to the input layer, allowing these values to propagate through the
network using the current weight values, and calculating the errors
between desired output values and the output values from propagation of
input values through the network.

The errors at the output layer are used to calculate gradients (or corrections) to the weights feeding into the output layer. Gradients are back propagated through the network allowing all weights in the network to be updated to reduce errors visible at the output layer.

One limitation of back propagation neural networks is that they are limited to the number of neuron layers that can be efficiently trained. The problem is that as error gradients are back propagated through the netowrk toward the input layer, the gradients get smalller and smaller. The effect is that it can take a lot of time to train back propagation networks with many hidden layers. We will see in the next chaper on Deep Learning how this problem can be solved and deep networks (i.e., networks with many hidden layers) can be trained.

Initially, weights are set to small
random values. You will get a general idea for how this is done in this
section and then we will look at Java implementation code in the [Section for a Java Class Library for Back Propagation](#nn-bp-lib).

In the [Figure showing Backpropagation network with No Hidden Layer](#nn-backprop-no-hidden), we only have two neuron layers, one for
the input neurons and one for the output neurons. Networks with no
hidden layers are not usually useful – I am using the network in 
the [Figure showing Backpropagation network with No Hidden Layer](#nn-backprop-no-hidden)
just to demonstrate layer to layer connections through a weights array.

{#nn-backprop-no-hidden}
![Example Backpropagation network with No Hidden Layer](images/nn_backprop2d.png)


To calculate the activation of the first output neuron **O1**, we evaluate
the sum of the products of the input neurons times the appropriate
weight values; this sum is input to a **Sigmoid** activation function (see
the [Figure showing the Sigmoid Function](#nn-sigmoid)) and the result is the new activation value for
**O1**. Here is the formula for the simple network in the [Figure showing Backpropagation network with No Hidden Layer](#nn-backprop-no-hidden):

~~~~~~~~
O1 = Sigmoid (I1 \* W[1,1] + I2 \* W[2,1])
O2 = Sigmoid (I2 \* W[1,2] + I2 \* W[2,2])
~~~~~~~~

The [Figure showing the Sigmoid Function](#nn-sigmoid) shows a plot of the **Sigmoid** function and the
derivative of the sigmoid function (**SigmoidP**). We will use the
derivative of the **Sigmoid** function when training a neural network
(with at least one hidden neuron layer) with classified data examples.

{#nn-sigmoid}
![Sigmoid Function and Derivative of Sigmoid Function (SigmoidP)](images/nn_sigmoid.png)


A neural network like the one seen in the [Figure showing Backpropagation network with No Hidden Layer](#nn-backprop-no-hidden) is
trained by using a set of training data. For back propagation networks,
training data consists of matched sets of input with matching desired
output values. We want to train a network to not only produce the same
outputs for training data inputs as appear in the training data, but
also to generalize its pattern matching ability based on the training
data to be able to match test patterns that are similar to training
input patterns. A key here is to balance the size of the network against
how much information it must hold. A common mistake when using back-prop
networks is to use too large a network: a network that contains too many
neurons and connections will simply memorize the training examples,
including any noise in the training data. However, if we use a smaller
number of neurons with a very large number of training data examples,
then we force the network to generalize, ignoring noise in the training
data and learning to recognize important traits in input data while
ignoring statistical noise.

How do we train a back propagation neural network given that we have a
good training data set? The algorithm is quite easy; we will now walk
through the simple case of a two-layer network like the one in the [Figure showing Backpropagation network with No Hidden Layer](#nn-backprop-no-hidden), and later in the [Section for a Java Class Library for Back Propagation](#nn-bp-lib) we will
review the algorithm in more detail when we have either one or two
hidden neuron layers between the input and output layers.

In order to train the network in the [Figure showing Backpropagation network with No Hidden Layer](#nn-backprop-no-hidden), we repeat
the following learning cycle several times:

1.  Zero out temporary arrays for holding the error at each neuron. The
    error, starting at the output layer, is the difference between the
    output value for a specific output layer neuron and the calculated
    value from setting the input layer neuron’s activation values to the
    input values in the current training example, and letting activation
    spread through the network.

2.  Update the weight **W_{i,j}** (where **i** is the index of an input
    neuron, and **j** is the index of an output neuron) using the formula
    **W_{i,j} += learning\_rate * output\_error_j*I_i** (**learning\_rate**
    is a tunable parameter) and **output\_error_j** was calculated in step
    1, and **I_i** is the activation of input neuron at index **i**.

This process is continued to either a maximum number of learning cycles
or until the calculated output errors get very small. We will see later
that the algorithm is similar but slightly more complicated, when we
have hidden neuron layers; the difference is that we will “back
propagate” output errors to the hidden layers in order to estimate
errors for hidden neurons. We will cover more on this later. This type
of neural network is too simple to solve very many interesting problems,
and in practical applications we almost always use either one additional
hidden neuron layer or two additional hidden neuron layers. The [Figure
showing mappings supported by zero
hidden layer, one hidden layer, and two hidden hidden layer networks](#nn-maping) shows the types of problems that can be solved networks with different numbers of hidden layers.

{#nn-maping}
![Mappings supported by 0, 1, and 2 hidden layer neural networks](images/nn_maping.png)


## A Java Class Library for Back Propagation {#nn-bp-lib}


The back propagation neural network library used in this chapter was
written to be easily understood and is useful for many problems.
However, one thing that is not in the implementation in this section (it
is added in the [Section on using Momentum to speed up training](#nn-bprop-momentum)) is something usually
called “momentum” to speed up the training process at a cost of doubling
the storage requirements for weights. Adding a “momentum” term not only
makes learning faster but also increases the chances of successfully
learning more difficult problems.

We will concentrate in this section on implementing a back-prop learning
algorithm that works for both one and two hidden layer networks. As we
saw in the [Figure
showing mappings supported by zero
hidden layer, one hidden layer, and two hidden hidden layer networks](#nn-maping) a network with two hidden layers is
capable of arbitrary mappings of input to output values so it used to be common opinion that there was no
theoretical reason for using networks with three hidden
layers. With recent projects using Deep Learning, as I mentioned at the beginning of this chapter, neural networks with many hidden layers are now common practice.

{#example-1-hidden-layer-1}
![Example showing 1 hidden layer](images/nn_1d_example_1.png)

{#example-2-hidden-layer1-1}
![Example showing 2 hidden layers](images/nn_2d_example_1.png)


The source directory src-neural-networks contains example programs for
both back propagation neural networks and Hopfield neural networks which
we saw at the beginning of this chapter. The relevant files for the back
propagation examples are:

-   Neural\_1H.java – contains a class for simulating a neural network with one hidden neuron layer
-   Test\_1H.java – a text-based test program for the class Neural\_1H
-   GUITest\_1H.java – a GUI-based test program for the class Neural\_1H
-   Neural\_2H.java – contains a class for simulating a neural network with two hidden neuron layers
-   Neural\_2H\_momentum.java – contains a class for simulating a neural network with two hidden neuron layers and implements momentum learning (implemented in the [Section on using Momentum to speed up training](#nn-bprop-momentum)
-   Test\_2H.java – a text-based test program for the class Neural\_2H
-   GUITest\_2H.java – a GUI-based test program for the class Neural\_2H
-   GUITest\_2H\_momentum.java – a GUI-based test program for the class Neural\_2H\_momentum that uses momentum learning (implemented in the [Section on using Momentum to speed up training](#nn-bprop-momentum)
-   Plot1DPanel – a Java JFC graphics panel for the values of a one-dimensional array of floating point values
-   Plot2DPanel – a Java JFC graphics panel for the values of a two-dimensional array of floating point values

The GUI files are for demonstration purposes only, and we will not
discuss the code for these classes; if you are interested in the demo
graphics code and do not know JFC Java programming, there are a few good
JFC tutorials at the web site java.sun.com.

It is common to implement back-prop libraries to handle either zero,
one, or two hidden layers in the same code base. At the risk of having
to repeat similar code in two different classes, I decided to make the
**Neural\_1H** and **Neural\_2H** classes distinct. I think that this makes
the code a little easier for you to understand. As a practical point,
you will almost always start solving a neural network problem using only
one hidden layer and only progress to trying two hidden layers if you
cannot train a one hidden layer network to solve the problem at-hand
with sufficiently small error when tested with data that is different
than the original training data. One hidden layer networks require less
storage space and run faster in simulation than two hidden layer
networks.

In this section we will only look at the implementation of the class
**Neural\_2H** (class **Neural\_1H** is simpler and when you understand how
**Neural\_2H** works, the simpler class is easy to understand also). This
class implements the **Serializable** interface and contains a utility
method **save** to write a trained network to a disk file:

~~~~~~~~
     class Neural_2H implements Serializable { 
~~~~~~~~

There is a static factory method that reads a saved network file from
disk and builds an instance of **Neural\_2H** and there is a class
constructor that builds a new untrained network in memory, given the
number of neurons in each layer:

~~~~~~~~
     public static Neural_2H Factory(String serialized_file_name)
     public Neural_2H(int num_in,
                      int num_hidden1,
                      int num_hidden2, int num_output) 
~~~~~~~~

An instance of **Neural\_2H** contains training data as transient data
that is not saved by method **save**.

~~~~~~~~
     transient protected ArrayList inputTraining = new Vector();
     transient protected ArrayList outputTraining = new Vector(); 
~~~~~~~~

I want the training examples to be native float arrays so I used generic
**ArrayList** containers. You will usually need to experiment with
training parameters in order to solve difficult problems. The learning
rate not only controls how large the weight corrections we make each
learning cycle but this parameter also affects whether we can break out
of local minimum. Other parameters that affect learning are the ranges
of initial random weight values that are hardwired in the method
**randomizeWeights()** and the small random values that we add to weights
during the training cycles; these values are set in in
**slightlyRandomizeWeights()**. I usually only need to adjust the learning
rate when training back-prop networks:

~~~~~~~~
     public float TRAINING_RATE = 0.5f; 
~~~~~~~~

I often decrease the learning rate during training – that is, I start
with a large learning rate and gradually reduce it during training. The
calculation for output neuron values given a set of inputs and the
current weight values is simple. I placed the code for calculating a
forward pass through the network in a separate method **forwardPass()**
because it is also used later in the method **training**:

~~~~~~~~
     public float[] recall(float[] in) {
       for (int i = 0; i < numInputs; i++) inputs[i] = in[i];
       forwardPass();
       float[] ret = new float[numOutputs];
       for (int i = 0; i < numOutputs; i++) ret[i] = outputs[i];
       return ret;
     }

     public void forwardPass() {
       for (int h = 0; h < numHidden1; h++) {
         hidden1[h] = 0.0f;
       }
       for (int h = 0; h < numHidden2; h++) {
         hidden2[h] = 0.0f;
       }
       for (int i = 0; i < numInputs; i++) {
         for (int h = 0; h < numHidden1; h++) {
           hidden1[h] += inputs[i] * W1[i][h];
         }
       }
       for (int i = 0; i < numHidden1; i++) {
         for (int h = 0; h < numHidden2; h++) {
           hidden2[h] += hidden1[i] * W2[i][h];
         }
       }
       for (int o = 0; o < numOutputs; o++) outputs[o] = 0.0f;
       for (int h = 0; h < numHidden2; h++) {
         for (int o = 0; o < numOutputs; o++) {
           outputs[o] += sigmoid(hidden2[h]) * W3[h][o];
         }
       }
     } 
~~~~~~~~

While the code for **recall** and **forwardPass** is almost trivial, the
training code in method **train** is more complex and we will go through
it in some detail. Before we get to the code, I want to mention that
there are two primary techniques for training back-prop networks. The
technique that I use is to update the weight arrays after each
individual training example. The other technique is to sum all output
errors over the entire training set (or part of the training set) and
then calculate weight updates. In the following discussion, I am going
to weave my comments on the code into the listing. The private member
variable **current\_example** is used to cycle through the training
examples: one training example is processed each time that the **train**
method is called:

~~~~~~~~
     private int current_example = 0;

     public float train(ArrayList ins, ArrayList v_outs) {
~~~~~~~~

Before starting a training cycle for one example, we zero out the arrays
used to hold the output layer errors and the errors that are back
propagated to the hidden layers. We also need to copy the training
example input values and output values:

~~~~~~~~
     int i, h, o; float error = 0.0f;
     int num_cases = ins.size(); 
     for (int example=0; example<num_cases; example++) {
       // zero out error arrays:
       for (h = 0; h < numHidden1; h++) hidden1_errors[h] = 0.0f;
       for (h = 0; h < numHidden2; h++) hidden2_errors[h] = 0.0f;
       for (o = 0; o < numOutputs; o++) output_errors[o] = 0.0f;
       // copy the input values:
       for (i = 0; i < numInputs; i++) {
         inputs[i] = ((float[]) ins.get(current_example))[i];
       }
       // copy the output values:
       float[] outs = (float[]) v_outs.get(current_example);
~~~~~~~~

We need to propagate the training example input values through the
hidden layers to the output layers. We use the current values of the
weights:

~~~~~~~~
     forwardPass(); 
~~~~~~~~

After propagating the input values to the output layer, we need to
calculate the output error for each output neuron. This error is the
difference between the desired output and the calculated output; this
difference is multiplied by the value of the calculated output neuron
value that is first modified by the **Sigmoid** function that we saw in
the [Figure showing the Sigmoid Function](#nn-sigmoid).
The **Sigmoid** function is to clamp the
calculated output value to a reasonable range.

~~~~~~~~
     for (o = 0; o < numOutputs; o++) {
       output_errors[o] = (outs[o] - outputs[o]) * sigmoidP(outputs[o]);
     } 
~~~~~~~~

The errors for the neuron activation values in the second hidden layer
(the hidden layer connected to the output layer) are estimated by
summing for each hidden neuron its contribution to the errors of the
output layer neurons. The thing to notice is that if the connection
weight value between hidden neuron **h** and output neuron **o** is large,
then hidden neuron **h** is contributing more to the error of output
neuron **o** than other neurons with smaller connecting weight values:

~~~~~~~~
     for (h = 0; h < numHidden2; h++) {
       hidden2_errors[h] = 0.0f; for (o = 0; o < numOutputs; o++) {
         hidden2_errors[h] += output_errors[o] * W3[h][o];
       }
     } 
~~~~~~~~

We estimate the errors in activation energy for the first hidden layer
neurons by using the estimated errors for the second hidden layers that
we calculated in the last code snippet:

~~~~~~~~
     for (h = 0; h < numHidden1; h++) {
       hidden1_errors[h] = 0.0f; for (o = 0; o < numHidden2; o++) {
         hidden1_errors[h] += hidden2_errors[o] * W2[h][o];
       }
     }
~~~~~~~~

After we have scaled estimates for the activation energy errors for both
hidden layers we then want to scale the error estimates using the
derivative of the sigmoid function’s value of each hidden neuron’s
activation energy:

~~~~~~~~
     for (h = 0; h < numHidden2; h++) {
       hidden2_errors[h] = hidden2_errors[h] * sigmoidP(hidden2[h]);
     }
     for (h = 0; h < numHidden1; h++) {
       hidden1_errors[h] = hidden1_errors[h] * sigmoidP(hidden1[h]);
     } 
~~~~~~~~

Now that we have estimates for the hidden layer neuron errors, we update
the weights connecting to the output layer and each hidden layer by
adding the product of the current learning rate, the estimated error of
each weight’s target neuron, and the value of the weight’s source
neuron:

~~~~~~~~
     // update the hidden2 to output weights:
     for (o = 0; o < numOutputs; o++) {
       for (h = 0; h < numHidden2; h++) {
         W3[h][o] += TRAINING_RATE * output_errors[o] * hidden2[h];
         W3[h][o] = clampWeight(W3[h][o]);
       }
     }
     // update the hidden1 to hidden2 weights:
     for (o = 0; o < numHidden2; o++) {
       for (h = 0; h < numHidden1; h++) {
         W2[h][o] += TRAINING_RATE * hidden2_errors[o] * hidden1[h];
         W2[h][o] = clampWeight(W2[h][o]);
       }
     }
     // update the input to hidden1 weights:
     for (h = 0; h < numHidden1; h++) {
       for (i = 0; i < numInputs; i++) {
         W1[i][h] += TRAINING_RATE * hidden1_errors[h] * inputs[i];
         W1[i][h] = clampWeight(W1[i][h]);
       }
     }
     for (o = 0; o < numOutputs; o++) {
       error += Math.abs(outs[o] - outputs[o]);
     } 
~~~~~~~~

The last step in this code snippet was to calculate an average error
over all output neurons for this training example. This is important so
that we can track the training status in real time. For very long
running back-prop training experiments I like to be able to see this
error graphed in real time to help decide when to stop a training run.
This allows me to experiment with the learning rate initial value and
see how fast it decays. The last thing that method **train** needs to do
is to update the training example counter so that the next example is
used the next time that **train** is called:

~~~~~~~~
     current_example++;
     if (current_example >= num_cases)
       current_example = 0;
     return error;
   } 
~~~~~~~~

You can look at the implementation of the Swing GUI test class
**GUTest\_2H** to see how I decrease the training rate during training. I
also monitor the summed error rate over all output neurons and
occasionally randomize the weights if the network is not converging to a
solution to the current problem.

## Adding Momentum to Speed Up Back-Prop Training  {#nn-bprop-momentum}

We did not use a momentum term in the Java code in the [Section for a Java Class Library for Back Propagation](#nn-bp-lib). For difficult to train problems, adding a momentum
term can drastically reduce the training time at a cost of doubling the
weight storage requirements. To implement momentum, we remember how much
each weight was changed in the previous learning cycle and make the
weight change larger if the current change in “direction” is the same as
the last learning cycle. For example, if the change to weight **W_{i,j}**
had a large positive value in the last learning cycle and the calculated
weight change for **W_{i,j}** is also a large positive value in the
current learning cycle, then make the current weight change even larger.
Adding a “momentum” term not only makes learning faster but also
increases the chances of successfully learning more difficult problems.

I modified two of the classes from the [Section for a Java Class Library for Back Propagation](#nn-bp-lib) to use
momentum:

-   Neural\_2H\_momentum.java – training and recall for two hidden layer
    back-prop networks. The constructor has an extra argument “alpha”
    that is a scaling factor for how much of the previous cycle’s weight
    change to add to the new calculated delta weight values.

-   GUITest\_2H\_momentum.java – a GUI test application that tests the
    new class **Neural\_2H\_momentum**.

The code for class **Neural\_2H\_momentum** is similar to the code for
**Neural\_2H** that we saw in the last section so here we will just look
at the differences. The class constructor now takes another parameter
**alpha** that determines how strong the momentum correction is when we
modify weight values:

~~~~~~~~
     // momentum scaling term that is applied
     // to last delta weight: private float alpha = 0f; 
~~~~~~~~

While this **alpha** term is used three times in the training code, it
suffices to just look at one of these uses in detail. When we allocated
the three weight arrays **W1**, **W2**, and **W3** we also now allocate three
additional arrays of corresponding same size: **W1\_last\_delta**,
**W2\_last\_delta**, and **W3\_last\_delta**. These three new arrays are
used to store the weight changes for use in the next training cycle.
Here is the original code to update **W3** from the last section:

~~~~~~~~
     W3[h][o] += TRAINING_RATE * output_errors[o] * hidden2[h]; 
~~~~~~~~

The following code snippet shows the additions required to use momentum:

~~~~~~~~
     W3[h][o] += TRAINING_RATE * output_errors[o] * hidden2[h] +
       // apply the momentum term:
       alpha * W3_last_delta[h][o]; 
     W3_last_delta[h][o] = TRAINING_RATE * output_errors[o] * hidden2[h]; 
~~~~~~~~

I mentioned in the last section that there are two techniques for
training back-prop networks: updating the weights after processing each
training example or waiting to update weights until all training
examples are processed. I always use the first method when I don’t use
momentum. In many cases it is best to use the second method when using
momentum.
