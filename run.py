#!/usr/bin/python3

import os
from sys import argv

# print("Removing previous .dat.")
# os.system("rm -f `find . -name \"*.dat\"`")

for file in argv[1:]:
	scalaLib = '${SCALA_LIB}/scala-library.jar'
	# profvis = '-agentlib:hprof=file=cpu2.txt,cpu=samples,interval=1000,depth=200'
	profvis = ''
#	command = 'java -Xmx3g -cp "src:libs/peersim-1.0.5.jar:libs/jep-2.3.0.jar:libs/djep-1.0.0.jar:{0}" {1} ' + \
#		'peersim.Simulator {2}'
	command = 'java -Xmx3g -cp "classes:libs/jep-2.3.0.jar:libs/djep-1.0.0.jar:{0}" {1} ' + \
		'peersim.Simulator {2}'
	command = command.format(scalaLib, profvis, file)

	# print('#'*80)
	# print(command + "\n\n----\n")
	os.system(command)
