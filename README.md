Polystyrene Simulator
(c) F. Taiani, H. Kervadec 2013

This code implements the Polystyrene protocol on top of the Peersim
simulator. The following is a very brief starting guide to this code
and its accompanying scripts.

Contact:
  François Taïani, francois.taiani@irisa.fr

* Environment

Polystyrene is implemented in Scala, and requires access to the
standard Scala library. You will need to set up the environment
variable SCALA_LIB to a directory containing :

scala-compiler.jar
scala-library.jar
scala-reflect.jar

The code has been tested with Java 1.7.0_13 and Scala 2.9.3. The
included scripts assume python3.

* Compiling Polystyrene

Run ant on the build.xml file in the root directory of the project.

$ ant
  
* Running simulations

** One simulation

The behavior of the Polystyrene protocol is controlled via a PeerSim
configuration file. A sample of this configuration is provided in the
config.txt file. To run the corresponding simulation use:

$ ./run.py config.txt

** Many simulations launched in parallel

The directory 'parallel_launcher' contains python scripts that rely on
the multiprocessing package of python to launch a batch of long
running simulations on multi-core machines.

Look at the several simu_*.py scripts for examples on how to do
this. All simu* scripts supports a set of options that control their
behavior. Use the -h option to get a usage description. For instance:

$ ./simu_test.py -h
usage: simu_test.py [-h] [--debug] [--dry] [--logstdout] [--nocompile]
                    [--nice | --leavecore N]

optional arguments:
  -h, --help     show this help message and exit
  --debug        print debug info
  --dry          dry run (no simulations)
  --logstdout    print log to stdout
  --nocompile    does not compile source prior to running
  --nice         only use half of the CPU cores (excludes --leavecore)
  --leavecore N  number of cores to leave free (excludes --nice)

Results are saved to the corresponding results_* directory.

* Plotting results

The 'parallel_launcher' directory contains a script './compResults.py'
that can be used to plot aggregated results of the simulations when
the number of nodes vary.

For instance

$ ./compResults.py \
   results_corrected_K4_8_200_51200_K2\
   results_corrected_K4_8_200_51200_K4\
   results_corrected_K4_8_200_51200_K8

The graphs produced are saved to the 'graphs' directory.

