# Expert Systems using Drools

We will be using the Drools Java expert system language and libraries in this chapter. Earlier editions of this book used the Jess expert system tool but due to the new more restrictive Jess licensing terms I decided to switch to Drools because it is released under the Apache 2.0 license. The primary web site for Drools is www.jboss.org/drools where you can download the source code and
documentation. Both Jess and Drools are forward chaining inference engines that
use the Rete algorithm and are derived from Charles Forgy's OPS5 language.
One thing to keep in mind whenever you are building a system based on the Rete
algorithm is that Rete scales very well to large numbers of rules but scales at
\begin{math}O(N^2)\end{math} where \begin{math}N\end{math} is the number of 
facts in the system. I have a long history with OPS5, porting it to Xerox Lisp Machines (1982) and the Apple Macintosh
(1984) as well as building custom versions supporting multiple ``worlds'' of data and
rule spaces. One thing that I would like to make clear: Drools is the only
technology that I am covering in this book that I have not used professionally.
That said, I spent some effort getting up to speed on Drools as a replacement
for Jess on future projects.

While there is some interest in using packages like Drools for
business rules to capture business process knowledge, often as embedded components in large systems, expert systems have historically been built to approach human level expertise for very specific tasks like
configuring computer systems and medical diagnosis. The examples in this chapter
are very simple and are intended to show you how to embed Drools in your Java
applications and to show you a few tricks for using forward chaining rule-based
systems. Drools is a Domain Specific Language (DSL) that attempts to provide a syntax
that is easier to use than a general purpose programming language. 

I do not usually recommend Java IDEs but if you already
use Eclipse then I suggest that you use the Drools plugins for Eclipse (the
``Eclipse Drools Workbench'') which help setting up projects and understand the
Drools rule language syntax.

The Eclipse Drools Workbench can automatically generate a small demo which I
will go over in some detail in the next two sections. I then design and
implement two simple example rule systems for solving block world type problems
and for answering help desk questions.

The material in this \index{expert system!forward chaining}
chapter exclusively covers forward chaining production systems (also called ``expert systems''). Forward chaining systems
start with a set of known facts, and apply rules to work towards solving one or
more goals. An alternative approach, often used in Prolog programs, is to use backward chaining. Backward chaining systems start with a final goal and attempt to work backwards towards currently known facts.

TBD: add "expert_system_overview" 
 
The phrase, expert systems, was almost synonymous with artificial
intelligence in the early and mid 1980s. The application of expert system
techniques to real problems, like configuring DEC VAX minicomputers, medical
diagnosis, and evaluating seismic data for planning oil exploration had everyone
very excited. Unfortunately, expert systems were over hyped and there
was an eventual backlash that affected the entire field of AI. Still, the
knowledge of how to write expert systems is a useful skill. This chapter
contains a tutorial for using the Drools system and also shows how
to use machine learning to help generate rules when training data is available.

As seen in the last figure, Drools development is
interactive: you will work in an environment where you can quickly add and
change rules and re-run test cases. This interactive style of development is
similar to using PowerLoom as we saw in Chapter 

## Production Systems

I like to refer to expert systems by a more precise name: production
systems. Productions are rules for transforming state. For example, given
the three production rules:

~~~~~~~~
a => b
b => c
c => d
~~~~~~~~

then if a production system is initialized with the state a, the state d
can be derived by applying these three production rules in order. The form
of these production rules is:

~~~~~~~~
<left-hand side>  => <right-hand side>
~~~~~~~~

or:

~~~~~~~~
LHS => RHS
~~~~~~~~

Like the PowerLoom reasoning system used in Chapter,
much of the power of a rule-based system comes from the ability to use
variables so that the left-hand side (LHS) patterns can match a variety
of known facts (called working memory in Drools). The values of these variables
set in the LHS matching process are substituted for the variables on
the right-hand side (RHS) patterns.

Production rule systems are much less expressive than Description Logic style
reasoners like PowerLoom. The benefits of production rule systems is that they
should have better runtime efficiency and are easier to use -- a smaller
learning curve. Good advice is to use production systems when they are
adequate, and if not, use a more expressive knowledge representation and
reasoning system like PowerLoom.

## The Drools Rules Language

The basic syntax (leaving out optional components) of a Drools rule is:

~~~~~~~~
rule "a name for the rule"
    when
        LHS
    then
        RHS
end
~~~~~~~~

What might
sample LHS and RHS statements look like? Drools rules reference POJOs
(``Plain Old Java Objects'') in both the LHS matching expressions and RHS actions. If you
use the Eclipse Drools Workbench and create a new demo project, the Workbench
will automatically create for you:

- Sample.drl -- a sample rule file.
- com.sample.DroolsTest.java -- defines: a simple Java POJO class $Message$ that is used in the Sample.drl rule file, a utility method for loading rules, and a main method that loads rules and creates an instance of the $Message$ class that ``fires'' the first rule in Sample.drl.

Even if you decide not to use the Eclipse Drools Workbench, I include these
two auto-generated files in the ZIP file for this book and we will use these files
to introduce both the syntax of rues and using rules and Drools in Java
applications in the next section.

Here is the Sample.drl file:

~~~~~~~~
package com.sample
 
import com.sample.DroolsTest.Message;
 
rule "Hello World"
  when
    m : Message(status == Message.HELLO,
                message : message)
  then
    System.out.println(message); 
    m.setMessage("Goodbye cruel world");
    m.setStatus(Message.GOODBYE);
    update(m);
end

rule "GoodBye"
  no-loop true
  when
    m : Message(status == Message.GOODBYE,
                message : message)
  then
    System.out.println(message); 
    m.setMessage(message);    
end
~~~~~~~~

This example rule file defines which Java package it has visibility in; we will
see in the next section that the Java code that defines the POJO $Message$ class
and code that uses these rules will be in the same Java package. This class has
private data (with public accessor methods using Java Bean protocol) for
attributes ``status'' and ``message.''

Another thing that might surprise you in this example is the direct calls to the
static Java method $System.out.println$: this is a hint that Drools will end up
compiling these rules into Java byte code. When Drools sees a reference to the
class $Message$, since there are no Java import statements in this example rule
file, the class $Message$ must be in the package com.sample.

On the LHS
of both rules, any instance of class $Message$ that matches and thus allows
the rule to ``fire'' sets a reference to the matched object to the local
variable $m$ that can then be used on the RHS of the rule. In the first rule,
the attribute $message$ is also stored in a local variable (perhaps
confusingly) also called $message$. Note that the public attribute accessor
methods like $setMessage$ are used to change the state of a matched $Message$
object.

We will see later that the first step in writing a Drools based expert system
is modeling (as Java classes) the data required to represent problem states.
After you have defined these POJO classes you can then proceed with
defining some initial test
cases and start writing rules to handle the test cases. Drools development
of non-trivial projects will involve an iterative process of adding new test
cases, writing new rules or generalizing previously written rules, and making
sure that both the original and newer test cases work.

There is a complete reference description of the Drools rule syntax on the
Drools documentation wiki. The material in this chapter is tutorial in nature:
new features of the rule language and how to use Drools will be introduced as
needed for the examples.

## Using Drools in Java Applications

We looked at the sample rules file Sample.drl in the last section which is
generated automatically when creating a demo project with the Eclipse Drools
Workbench. We will use the other generated file DroolsTest.java as an
illustrative example in this section. The file DroolsTest.java is almost 100
lines long so I will list it in small fragments followed by an explanation of
each code fragment. The first thing to note is that the Java client code is in
the same package as the rules file:

~~~~~~~~
package com.sample;

import java.io.InputStreamReader;
import java.io.Reader;

import org.drools.RuleBase;
import org.drools.RuleBaseFactory;
import org.drools.WorkingMemory;
import org.drools.compiler.PackageBuilder;
import org.drools.rule.Package;
~~~~~~~~

This main function is an example showing how to use a rule package defined in a
rule source file. We will see the definition of the utility method $readRule$
that opens a rule file and returns an instance of class $RuleBase$ shortly.
After creating an instance of $RuleBase$ we create an instance of
the $Message$ class and add it to open memory:

~~~~~~~~
public class DroolsTest {

  public static final void main(String[] args) {
    try {
      RuleBase ruleBase = readRule();
      WorkingMemory workingMemory =
             ruleBase.newStatefulSession();
      Message message = new Message();
      message.setMessage(  "Hello World" );
      message.setStatus( Message.HELLO );
      workingMemory.insert( message );
      workingMemory.fireAllRules();     
    } catch (Throwable t) {
      t.printStackTrace();
    }
  }
~~~~~~~~

The main method creates a new rule base and working memory. Working memory is
responsible for maintaining the ``facts'' in the system -- in this case facts
are Plain Old Java Objects (POJOs) that are maintained in a collection.

An instance of class $Message$ is created and its status is set to the constant
value $Message.HELLO$. We saw in the last section how the first example rule has
a condition that allows the rule to ``fire'' if there is any instance of class
$Message$ that has its status attribute set to this value.

The method $fireAllRules$ will keep identifying rules that are eligible to fire,
choosing a rule from this active set using algorithms we will discuss later,
and then repeating this process until no more rules are eligible to fire. There
are other $fireAllRules$ methods that have arguments for a maximum number of
rules to fire and a filter to allow only some eligible rules to execute.


~~~~~~~~
  /**
   * Please note that this is the "low level" rule
   * assembly API.
   */
  private static RuleBase readRule()
                          throws Exception {
    //read in the source
    Reader source =
       new InputStreamReader(
         DroolsTest.class.
                 getResourceAsStream("/Sample.drl"));
    
    // optionally read in the DSL if you are using one:
    // Reader dsl =
    //    new InputStreamReader(
    //     DroolsTest.class.
    //            getResourceAsStream("/mylang.dsl"));
~~~~~~~~
 
 The method $readRule$ is a utility for reading and compiling a rule file
 that was generated automatically by the Eclipse Drools
 Workbench; in general your projects will have one or more rules files that
 you will load as in this example. In method $readRule$ we opened an input
 stream reader on the source code for the example Drools rule file Sample.drl.
 Drools has the ability to modify the rule syntax to create Domain Specific
 Languages (DSLs) that match your application or
 business domain. This can be very useful if you want domain experts to create
 rules since they can ``use their own language.'' We will not cover custom DSLs
 in this chapter but the Drools documentation covers this in detail. Here is
 the rest of the definition of method $readRule$:
 
~~~~~~~~
    // Use package builder to build up a rule package:
    PackageBuilder builder = new PackageBuilder();

    // This will parse and compile in one step:
    builder.addPackageFromDrl(source);

    // Use the following instead of above if you are
    // using a custom DSL:
    //builder.addPackageFromDrl( source, dsl );
    
    // get the compiled package (which is serializable)
    Package pkg = builder.getPackage();
    
    // add the package to a rulebase (deploy the
    // rule package).
    RuleBase ruleBase = RuleBaseFactory.newRuleBase();
    ruleBase.addPackage( pkg );
    return ruleBase;
  }
~~~~~~~~

The $readRule$ utility method can be copied to new rule projects that are not created
using the Eclipse Drools Workbench and modified as appropriate to get you
started with the Java ``boilerplate'' required by Drools. This implementation
uses Drools defaults, the most important being the ``conflict resolution
strategy" that defaults to first checking the most recently modified working
memory POJO objects to see which rules can fire. This produces a depth first
search behavior. We will modify the $readRule$ utility
method later in Section \ref{section:expert_system_blocks} when we will
need to change this default Drools reasoning behavior from
depth first to breadth first search.

We will need a Plain Old Java Object (POJO) class to represent messages in the
example rule set. This demo class was generated by the Eclipse Drools Workbench:
  
~~~~~~~~
public static class Message {
    public static final int HELLO = 0;
    public static final int GOODBYE = 1;
    
    private String message;
    
    private int status;
    
    public String getMessage() {
      return this.message;
    }
    
    public void setMessage(String message) {
      this.message = message;
    }
    
    public int getStatus() {
      return this.status;
    }
    
    public void setStatus( int status ) {
      this.status = status;
    }
  }  
}
~~~~~~~~

You might want to review the example rules using this POJO Message class in
Section \ref{section:expert_system_drools_rules}. Here is the sample output
from running this example code and rule set:

~~~~~~~~
Hello World
Goodbye cruel world
~~~~~~~~

A simple example, but it serves to introduce you the Drools rule syntax and
required Java code. This is also a good example to understand because when you
use the Eclipse Drools Workbench to create a new Drools rule project, it
generates this example automatically as a template for you to modify and re-use.

In the next two sections I will develop two more complicated examples: solving
blocks world problems and supporting a help desk system.

 
## Example Drools Expert System: Blocks World
 
The example in this section solved simple ``blocks world'' problems; see

Figures \ref{fig:expert_system_blocks_1} through
\ref{fig:expert_system_blocks_4} for a very simple example problem.

I like this example because it introduces the topic of ``conflict resolution''
and (unfortunately) shows you that even solving simple problems with rule-based
systems can be difficult. Because of the difficulty of developing and debugging
rule-based systems, they are best for applications that have both high business
value and offer an opportunity to encode business or application knowledge of
experts in rules. So, the example in the next section is a more real-life
example of good application of expert systems, but you will learn valuable
techniques in this example. In the interest of intellectual honesty, I should
say that general blocks world problems like the ``Towers of Hanoi'' problem and
block world problems as the one in this section are usually easily solved using
breadth-first search techniques.

\begin{figure*}
  \centerline{
    \includegraphics[width=0.6in]{images/expert_system_blocks_1.pdf}}
  \caption{Initial state of a blocks world problem with three blocks stacked on
  top of each other. The goal is to move the blocks so that block C is on top of
  block A.}
  \label{fig:expert_system_blocks_1}
\end{figure*}

The Java source code and Drools rule files for this example are in the files
BlockWorld.drl and DroolsBockWorld.java.
 
\begin{figure*}
  \centerline{
    \includegraphics[width=1.3in]{images/expert_system_blocks_2.pdf}}
  \caption{Block C has been removed from block B and placed on the table.}
  \label{fig:expert_system_blocks_2}
\end{figure*}


## POJO Object Models for Blocks World Example

We will use the following three POJO classes (defined in the file
DroolsBockWorld.java as static inner classes). The first POJO class $Block$
represents the state of one block:

~~~~~~~~
  public static class Block {  
    protected String name;
    protected String onTopOf;
    protected String supporting;
  
    public Block(String name, String onTopOf,
                 String supporting) {
      this.name = name;
      this.onTopOf = onTopOf;
      this.supporting = supporting;
    }
    public String toString() {
      return "[Block_" + this.hashCode() + " " +
             name + "  on top of: " + onTopOf +
             "  supporting: " + supporting+"]";
    }
    public String getName() {
      return this.name;
    }     
    public void setName(String name) {
      this.name = name;
    }
      
    public String getOnTopOf() {
      return this.onTopOf;
    }     
    public void setOnTopOf(String onTopOf) {
      this.onTopOf = onTopOf;
    }
  
    public String getSupporting() {
      return this.supporting;
    }     
    public void setSupporting(String supporting) {
      this.supporting = supporting;
    }
  }
~~~~~~~~

The next POJO class $OldBlockState$ is used to represent previous states of
blocks as they are being moved as the rules in this example ``fire.'' We will
later see rules that will not put a block into a state that it previously existed in:

~~~~~~~~
  public static class OldBlockState extends Block {
    public OldBlockState(String name,
                         String onTopOf,
                         String supporting) {
      super(name, onTopOf, supporting);
    }
    public String toString() {
      return "[OldBlockState_" + this.hashCode() +
             " " + name + "  on top of: " + onTopOf +
             "  supporting: " + supporting+"]";
    }
  }
~~~~~~~~

The next POJO class $Goal$ is used to represent a goal state for the blocks that
we are trying to reach:

~~~~~~~~
  public static class Goal {
    private String supportingBlock;
    private String supportedBlock;
    public Goal(String supporting, String supported) {
      this.supportingBlock = supporting;
      this.supportedBlock = supported;
    }
    public String toString() {
      return "[Goal_" + this.hashCode() +
             " Goal: supporting block: " +
             supportingBlock +
             "  and supported block: " +
             supportedBlock +"]";
    }
    public void setSupportingBlock(
                      String supportingBlock) {
      this.supportingBlock = supportingBlock;
    }
    public String getSupportingBlock() {
      return supportingBlock;
    }
    public void setSupportedBlock(
                      String supportedBlock) {
      this.supportedBlock = supportedBlock;
    }
    public String getSupportedBlock() {
      return supportedBlock;
    }     
  }
~~~~~~~~
 
 
Each block object has three string attributes: a name, the name of the block
that this block is on top of, and the block that this block supports (is
under). We will also define a block instance with the name ``table.''

\begin{figure*}
  \centerline{
    \includegraphics[width=2.0in]{images/expert_system_blocks_3.pdf}}
  \caption{Block B has been removed from block A and placed on the table.}
  \label{fig:expert_system_blocks_3}
\end{figure*}


We need the POJO class OldBlockState that is a subclass of Block to avoid
cycles in the reasoning process.

\begin{figure*}
  \centerline{
    \includegraphics[width=1.7in]{images/expert_system_blocks_4.pdf}}
  \caption{The goal is solved by placing block C on top of block A.}
  \label{fig:expert_system_blocks_4}
\end{figure*}

\subsection{Drools Rules for Blocks World Example} 

We need four rules for this example and they are listed below with comments as
appropriate:

~~~~~~~~
package com.markwatson.examples.drool
 
import com.markwatson.examples.drool.DroolsBlockWorld
                                    .Goal;
import com.markwatson.examples.drool.DroolsBlockWorld
                                    .Block;
import com.markwatson.examples.drool.DroolsBlockWorld
                                    .OldBlockState;
~~~~~~~~

We place the rules in the same Java package as the Java support code seen in
the next section and the POJO model classes that we saw in the last section.

The first rule has no preconditions so it can always fire. We use the special
rule condition ``no-loop true'' to let the Drools system know that we only want
this rule to fire one time. This rule inserts facts into working memory for the
simple problem seen in Figures \ref{fig:expert_system_blocks_1} through
\ref{fig:expert_system_blocks_4}:

~~~~~~~~
rule "Startup Rule"
  no-loop true
  when
  then
    //insert(new Goal("C", "B"));  // test 1
    insert(new Goal("C", "A"));    // test 2
    // Block(String name, String onTopOf,
    //       String supporting)
    insert(new Block("A", "table", "B"));
    insert(new Block("B", "A", "C"));
    insert(new Block("C", "B", ""));
    insert(new Block("D", "", ""));
    insert(new Block("table", "", "A"));
end
~~~~~~~~

The following rule looks for situations where it is possible to move a block
with a few conditions:

- Find a block $block\_1$ that is on top of another block and is not itself supporting any other blocks
- Find a second block $block\_2$ that is not $block\_1$ and is not itself supporting any other blocks
- Find the block $on\_top\_of\_1$ that is under $block\_2$ and supporting $block\_1$
- Make sure that no previous block with the name in the variable $block\_2$ has already been on top of block $on\_top\_of\_2$ and supporting $block\_1$

If these conditions are met, we can remove the three matching facts and
create facts for the new block positions and a new $OldBlockState$ fact in
working memory. Note that the fourth LHS matching pattern is prefixed with
``not'' so this matches if there are no objects in working memory that match
this pattern:

~~~~~~~~
rule "Set Block On: move block_1 to block_2"
  when
    fact1 : Block(block_1 : name,
                  on_top_of_1 : onTopOf != "",
                  supporting == "")
    fact2 : Block(block_2 : name != block_1,
                  on_top_of_2 : onTopOf != "",
                  supporting == "")
    fact3 : Block(name == on_top_of_1,
                  on_top_of_3 : onTopOf,
                  supporting == block_1)
    not OldBlockState(name == block_2,
                      onTopOf == on_top_of_2,
                      supporting == block_1)
  then
    System.out.println( fact1 ); 
    System.out.println( fact2 ); 
    System.out.println( fact3 ); 
    retract(fact1);
    retract(fact2);
    retract(fact3);
    insert(new Block(block_1, block_2, ""));
    insert(new Block(block_2, on_top_of_2,
                     block_1));
    insert(new OldBlockState(block_2,
                            on_top_of_2, ""));
    insert(new Block(on_top_of_1,
                     on_top_of_3, ""));
    System.out.println("Moving " + block_1 +
                       " from " + on_top_of_1 + 
                       " to " + block_2);
end
~~~~~~~~

The next rule looks for opportunities to remove $block\_1$ from $block\_2$ if no
other block is sitting on top of $block\_1$ (that is, $block\_1$ is clear):

~~~~~~~~
rule "Clear Block: remove block_1 from block_2"
  when
    fact1 : Block(block_1 : name != "table",
                  on_top_of : onTopOf != "table",
                  supporting == "")
    fact2 : Block(block_2 : name,
                  on_top_of_2 : onTopOf,
                  supporting == block_1)
  then
    System.out.println( fact1 ); 
    System.out.println( fact2 ); 
    retract(fact1);
    retract(fact2);
    insert(new Block(block_1, "table", ""));
    insert(new Block(block_2, on_top_of_2, ""));
    insert(new Block("table", "", block_1));
    System.out.println("Clearing: remove " +
                       block_1 + " from " + 
                       on_top_of + " to table");
end
~~~~~~~~

The next rule checks to see if the current goal is satisfied in which case it
halts the Drools engine:

~~~~~~~~
rule "Halt on goal achieved"
  salience 500
  when
    Goal(b1 : supportingBlock, b2 : supportedBlock)
    Block(name == b1, supporting == b2)
  then
    System.out.println("Done!");
    drools.halt();
end
~~~~~~~~

The Java code in the next section can load and run these example rules.


## Java Code for Blocks World Example

The example in this section introduces something new: modifying the default way
that Drools chooses which rules to ``fire'' (execute) when more than one rule
is eligible to fire. This is referred to as the ``conflict resolution
strategy" and this phrase dates back to the original OPS5 production
system. Drools by default prefers rules that are instantiated by data that is
newer in working memory. This is similar to depth first search.

In the ``blocks world'' example in this section we will need to change the
conflict resolution strategy to process rules in a first-in, first-out order
which is similar to a breadth first search strategy.

First, let us define the problem that we want to solve. Consider labeled blocks
sitting on a table as seen in Figures \ref{fig:expert_system_blocks_1} through
\ref{fig:expert_system_blocks_4}.

The Java code in this section is similar to what we already saw in Section
\ref{section:extert_system_java_drools} so we will just look at the differences
here. To start with, in the utility method $readRule()$ we need to add a few
lines of code to configure Drools to use a breadth-first instead of a
depth-first reasoning strategy:

~~~~~~~~
  private static RuleBase readRule() throws Exception {
    Reader source =
       new InputStreamReader(
        DroolsBlockWorld.class.getResourceAsStream(
                                    "/BlockWorld.drl"));
    PackageBuilder builder = new PackageBuilder();
    builder.addPackageFromDrl( source );
    Package pkg = builder.getPackage();

    // Change the default conflict resolution strategy:      
    RuleBaseConfiguration rbc =
                            new RuleBaseConfiguration();
    rbc.setConflictResolver(new FifoConflictResolver());

    RuleBase ruleBase = RuleBaseFactory.newRuleBase(rbc);
    ruleBase.addPackage(pkg);
    return ruleBase;
  }
~~~~~~~~

The Drools class $FifoConflictResolver$ is not so well named, but a first-in
first-out (FIFO) strategy is like depth first search. The default conflict
resolution strategy favors rules that are eligible to fire from data that has
most recently changed.

Since we have already seen the definition of the Java POJO classes used in the
rules in Section \ref{section:expert_system_blocks_pojos} the only remaining
Java code to look at is in the static main method:

~~~~~~~~
    RuleBase ruleBase = readRule();
    WorkingMemory workingMemory =
                    ruleBase.newStatefulSession();
    System.out.println("\nInitial Working Memory:\n\n" +
                       workingMemory.toString());
    // Just fire the first setup rule:
    workingMemory.fireAllRules(1);
    Iterator<FactHandle> iter =
                     workingMemory.iterateFactHandles();
    while (iter.hasNext()) {
      System.out.println(iter.next());
    }
    System.out.println("\n\n** Before firing rules...");
    workingMemory.fireAllRules(20);  // limit 20 cycles
    System.out.println("\n\n** After firing rules.");
    System.out.println("\nFinal Working Memory:\n" +
                       workingMemory.toString());
    iter = workingMemory.iterateFactHandles();
    while (iter.hasNext()) {
      System.out.println(iter.next());
    }
~~~~~~~~

For making rule debugging easier I wanted to run the first ``start up'' rule to
define the initial problem facts in working memory, and then print working
memory. That is why I called $workingMemory.fireAllRules(1)$ to ask the Drools
rule engine to just fire one rule. In the last example we called
$workingMemory.fireAllRules()$ with no arguments so the rule engine runs forever
as long as there are rules eligible to fire. After printing the facts in
working memory I call the $fireAllRules(20)$ with a limit of
20 rule firings because blocks world problems can fail to terminate (at least the
simple rules that I have written for this example often failed to terminate
when I was debugging this example). Limiting the number of rule firings is
often a good idea. The output from this example with debug output removed is:

~~~~~~~~
Clearing: remove C from B to table
Moving B from A to C
Clearing: remove B from C to table
Moving A from table to C
Moving C from table to B
Done!
~~~~~~~~

Note that this is not the best solution since it has unnecessary steps. If
you are interested, here is the output with debug printout showing the facts
that enabled each rule to fire:

~~~~~~~~
[Block_11475926 C  on top of: B  supporting: ]
[Block_14268353 B  on top of: A  supporting: C]
Clearing: remove C from B to table
[Block_3739389 B  on top of: A  supporting: ]
[Block_15146334 C  on top of: table  supporting: ]
[Block_2968039 A  on top of: table  supporting: B]
Moving B from A to C
[Block_8039617 B  on top of: C  supporting: ]
[Block_14928573 C  on top of: table  supporting: B]
Clearing: remove B from C to table
[Block_15379373 A  on top of: table  supporting: ]
[OldBlockState_10920899 C  on top of: table  supporting: ]
[Block_4482425 table  on top of:   supporting: A]
Moving A from table to C
[Block_13646336 C  on top of: table  supporting: ]
[Block_11342677 B  on top of: table  supporting: ]
[Block_6615024 table  on top of:   supporting: C]
Moving C from table to B
Done!
~~~~~~~~

This printout does not show the printout of all facts before and after running
this example.

## Example Drools Expert System: Help Desk System
 
Automating help desk functions can improve the quality of customer service and
reduce costs. Help desk software can guide human call operators through
canned explanations that can be thought of as decision trees; for example:
``Customer reports that their refrigerator is not running --\textgreater{} Ask if the power
is on and no circuit breakers are tripped. If customer reports that power source
is OK --\textgreater{} Ask if the light is on inside the refrigerator to determine if
just the compressor motor is out\ldots''.
 We will see in Chapter the chapter on Weka that decision trees can be learned from training data.
One method of implementing a decision tree approach to help desk support would
be to capture customer interactions by skilled support staff, factor operator
responses into standard phrases and customer comments into standard questions,
and use a machine learning package like Weka to learn the best paths through questions and answers.

We will take a different approach in our example for this section: we will
assume that an expert customer service representative has provided us with use
cases of common problems, what customers tend to ask for each problem, and the
responses by customer service representatives. We will develop some sample
Drools rules to encode this knowledge. This approach is likely to be more
difficult to implement than a decision tree system but has the potential
advantage that if individual rules ``make sense'' in general they may end
up being useful in contexts beyond those anticipated by rules developers. With
this greater flexibility comes a potential for less accuracy.

We will start in the next section by developing some POJO object models
required for our example help desk expert system and then in the next section
develop a few example rules.

## Object Models for an Example Help Desk

We will use a single Java POJO class for this example. We want a problem type,
a description of a problem, and a suggestion. A ``real'' help desk system might
use additional classes for intermediate steps in diagnosing problems and
offering advice but for this example, we will chain ``problems'' together. Here
is an example:

~~~~~~~~
Customer: My refrigerator is not running.
Service: I want to know if the power is on. Is the light
on inside the refrigerator?
Customer: No.
Service: Please check your circuit breaker, I will wait.
Customer: All my circuit breakers looked OK and
everything else is running in the kitchen.
Service I will schedule a service call for you.
~~~~~~~~

We will not develop an interactive system; a dialog with a customer is assumed
to be converted into facts in working memory. These facts will be represented
by instances of the class $Problem$. The expert system will apply the rule base
to the facts in working memory and make suggestions. Here is the Java class
$Problem$ that is defined as an inner static class in the file
DroolsHelpDesk.java:

~~~~~~~~
  public static class Problem {
    // Note: Drools has problems dealing with Java 5
    //       enums as match types so I use static 
    //       integers here. In general, using enums
    //       is much better.
    final public static int NONE = 0;
    // appliance types:
    final public static int REFRIGERATOR = 101;
    final public static int MICROWAVE = 102;
    final public static int TV = 103;
    final public static int DVD = 104;
    // environmentalData possible values:
    final public static int CIRCUIT_BREAKER_OFF = 1002;
    final public static int LIGHTS_OFF_IN_ROOM = 1003;
    // problemType possible values:
    final public static int NOT_RUNNING = 2001;
    final public static int SMOKING = 2002;
    final public static int ON_FIRE = 2003;
    final public static int MAKES_NOISE = 2004;
        
    long serviceId = 0; // unique ID for all problems
                        // dealing with customer problem
    int applianceType = NONE;
    int problemType = NONE;
    int environmentalData = NONE;

    public Problem(long serviceId, int type) {
      this.serviceId = serviceId;
      this.applianceType = type;
    }

    public String toString() {
      return "[Problem: " + enumNames.get(applianceType) +
             " problem type: " + enumNames.get(problemType) +
             " environmental data: " +
             enumNames.get(environmentalData) + "]";
    }
    public long getServiceId() { return serviceId; }
    public int getEnvironmentalData() {
      return environmentalData;
    }
    public int getProblemType() {
      return problemType;
    }
    static Map<Integer, String> enumNames =
                         new HashMap<Integer, String>();
    static {
      enumNames.put(0, "NONE");
      enumNames.put(1002, "CIRCUIT_BREAKER_OFF");
      enumNames.put(1003, "LIGHTS_OFF_IN_ROOM");
      enumNames.put(2001, "NOT_RUNNING");
      enumNames.put(2002, "SMOKING");
      enumNames.put(2003, "ON_FIRE");
      enumNames.put(2004, "MAKES_NOISE");
      enumNames.put(101, "REFRIGERATOR");
      enumNames.put(102, "MICROWAVE");
      enumNames.put(103, "TV");
      enumNames.put(104, "DVD");
    }
  }
~~~~~~~~

It is unfortunate that the current version of Drools does not work well with
Java 5 enums -- the $Problem$ class would have been about half as many lines of
code (no need to map integers to meaningful descriptions for $toString()$) and
the example would also be more type safe.

I used constant values like REFRIGERATOR and RUNNING to represent possible
values for the member class attributes like $applianceType$, $problemType$, and
$environmentalData$. There is obviously a tight binding from the Java POJO
classes like $Problem$ to the rules that use these classes to represent objects
in working memory. We will see a few example help desk rules in the next
section.


## Drools Rules for an Example Help Desk

This demo help desk system is not interactive. The Java code in the next section
loads the rule set that we are about to develop and then programmatically adds
test facts into working memory that simulate two help desk customer
service issues. This is an important example since you will likely want to add
data directly from Java code into Drools working memory.

There are several rules defined in the example file HelpDesk.drl and we will
look at a few of them here. These rules are intended to be a pedantic example
of both how to match attributes in Java POJO classes and to show a few more
techniques for writing Drools rules.

I used to use the Lisp based OPS5 to develop expert systems and I find the
combination of Java and Drools is certainly ``less agile'' to use.
I found myself writing a
rule, then editing the POJO class Problem to add constants for things that I
wanted to use in the rule. With more experience, this less than
interactive process might become more comfortable for me.

As in the blocks world example, we want to place the rules file in the same
package as the Java code using the rules file and import any POJO classes that
we will use in working memory:

~~~~~~~~
package com.markwatson.examples.drool
import com.markwatson.examples.drool.
                               DroolsHelpDesk.Problem;
~~~~~~~~

The first rule sets a higher than default rule
salience so it will fire before any rules with the default rule salience (a value of zero). This rule has a feature that we have not seen before: I have no matching expressions in the
``when'' clause. All Java Problem instances will match the left-hand side of
this rule.

~~~~~~~~
rule "Print all problems"
  salience 100
  when
    p : Problem()
  then
    System.out.println("From rule 'Print all problems': "
                       +  p); 
end
~~~~~~~~

The following rule matches an instance of the class
\begin{math}Problem\end{math} in working memory that has a value of
``Problem.CIRCUIT\_BREAKER\_OFF'' for the vakue of attribute
\begin{math}environmentalData\end{math}. This constant has the integer value of
1002 but is is obviously more clear to use meaningful constant names:

~~~~~~~~
rule "Reset circuit breaker"
  when
    p1 : Problem(environmentalData ==
                          Problem.CIRCUIT_BREAKER_OFF)
  then
    System.out.println("Reset circuit breaker: " + p1);
end
~~~~~~~~

The last rule could perhaps be improved by having it only fire if any
appliance was not currently running; we make this check in the next rule. Notice
that in the next rule we are matching different attributes
(\begin{math}problemType\end{math} and \begin{math}environmentalData\end{math})
and it does not matter if these attributes match in a single working memory
element or two different working memory elements:

~~~~~~~~
rule "Check for reset circuit breaker"
  when
    p1 : Problem(problemType == Problem.NOT_RUNNING)
    Problem(environmentalData ==
            Problem.CIRCUIT_BREAKER_OFF)
  then
    System.out.println("Check for power source: " + p1 +
             ". The unit is not is not on and " +
             "the circuit breaker is tripped - check " +
             "the circuit breaker for this room.");
end
~~~~~~~~

We will look at the Java code to use these example rules in the next section.


## Java Code for an Example Help Desk

We will see another trick for using Drools in this example: creating working
memory elements (i.e., instances of the Problem POJO class) in Java code instead
of in a ``startup rule'' as we did for the blocks world example. The code in
this section is also in the DroolsHelpDesk.java source file (as is the POJO
class definition seen in Section
\ref{section:expert_system_object_model_help_desk}).

The static main method in the $DroolsHelpDesk$ class is very similar to the main
method in the blocks world example except that here we also call a new method
$createTestFacts$:

~~~~~~~~
  public static final void main(String[] args)
                             throws Exception {
    //load up the rulebase
    RuleBase ruleBase = readRule();
    WorkingMemory workingMemory =
       ruleBase.newStatefulSession();
    createTestFacts(workingMemory);

    .. same as the blocks world example ..
  }
~~~~~~~~

We already looked at the utility method $readRule$ in Section
\ref{section:expert_system_java_blocks_world} so we will just look at the new
method $createTestFacts$ that creates two instance of the POJO class
$Problem$ in working memory:

~~~~~~~~
  private static void
       createTestFacts(WorkingMemory workingMemory)
          throws Exception {
    Problem p1 = new Problem(101, Problem.REFRIGERATOR);
    p1.problemType = Problem.NOT_RUNNING;
    p1.environmentalData = Problem.CIRCUIT_BREAKER_OFF;
    workingMemory.insert(p1);

    Problem p2 = new Problem(101, Problem.TV);
    p2.problemType = Problem.SMOKING;
    workingMemory.insert(p2);
  }
~~~~~~~~

In this code we created new instances of the class $Problem$ and set desired
attributes. We then use the $WorkingMemory$ method $insert$ to add the ojects
to the working memory collection that Drools maintains. The output when running
this example is (reformatted to fit the page width):

~~~~~~~~
From rule 'Print all problems':
   [Problem: TV
               problem type: SMOKING
               environmental data: NONE]

From rule 'Print all problems':
   [Problem: REFRIGERATOR
               problem type: NOT_RUNNING
               environmental data: CIRCUIT_BREAKER_OFF]

Unplug appliance to prevent fire danger:
   [Problem: TV problem type: SMOKING
                          environmental data: NONE]

Check for power source:
   [Problem: REFRIGERATOR
               problem type: NOT_RUNNING
               environmental data: CIRCUIT_BREAKER_OFF]
   The unit is not is not on and the circuit breaker
   is tripped - check the circuit breaker for this room.
~~~~~~~~
 
## Notes on the Craft of Building Expert Systems
 
 It may seem like rule-based expert systems have a lot of programming overhead;
 that is, it will seem excessively difficult to solve simple problems using production systems.
 However, for encoding large ill-structured problems, production systems provide
 a convenient notation for collecting together what would otherwise be too large
 a collection of unstructured data and heuristic rules (\textit{Programming
 Expert Systems in Ops5: An Introduction to Rule-Based Programming}, Brownston
 et al. 1985). As a programming technique, writing rule-based expert systems is not for
 everyone. Some programmers find rule-based programming to be cumbersome, while others find
 it a good fit for solving some types of problems. I encourage the reader to
 have some fun experimenting with Drools, both with the examples in this
 chapter, and the many examples in the Drools distribution package and
 documentation.

Before starting a moderate or large expert system project, there are several steps
that I recommend:

- Write a detailed description of the problem to be solved.
- Decide what structured data elements best describe the problem space.
- Try to break down the problem into separate modules of rules; if possible, try to develop and test these smaller modules independently, preferably one source file per module.
- Plan on writing specific rules that test parts of the system by initializing working memory for specific tests for the various modules; these tests will be very important when testing all of the modules together because tests that work correctly for a single module may fail when all modules are loaded due to unexpected rule interactions.

Production systems model fairly accurately the stimulus-response behavior in
people. The left-hand side (LHS) terms represent environmental data that triggers a
response or action represented by the right-hand side (RHS) terms in production
rules. Simple stimulus-response types of production rules might be adequate for
modeling simple behaviors, but our goal in writing expert systems is to encode
deep knowledge and the ability to make complex decisions in a very narrow (or
limited) problem domain. In order to model complex decision-making abilities, we
also often need to add higher-level control functionality to expert systems.
This higher level, or meta control, can be the control of which rule modules
are active. We did not look at the Drools APIs for managing modules in this
chapter but these APIs are covered in the Drools documentation. Hopefully, this
chapter both gave you a quick-start for experimenting with Drools and enough experience to
know if a rule-based system might be a good fit for your own development.



