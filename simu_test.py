#!/usr/bin/env python3
# -*-  python-indent:2; tab-width:2; indent-tabs-mode:t; -*-

# DONE: monotonic behaviour. Does not overwrite existing result files. Just skip experiment.
# DONE: graceful handling of exception (print message, continue further)
# DONE: test with "progressive" length
# DONE: move concatenation of expe to second script
# DONE: factor out multiprocessing to external script

#import os
import random
#import logging
#from multiprocessing import Pool, cpu_count
#from sys import argv, exit

import simulationLauncher as launcher

myTemplate = """# POLYSTYRENE CONFIG FILE
random.seed {2}
simulation.cycles 1000
simulation.experiments 1 # for parallelisation with python mp
#simulation.experiments 25
#simulation.experiments 3 # FT30Oct13 TEST

WIDTH {0}
HEIGHT {1}
VIEW 10
SIZE WIDTH*HEIGHT

network.size SIZE*1.5
#network.size SIZE*1

protocol.link IdleProtocol

#protocol.coord polystyrene.torus.Torus
protocol.coord polystyrene.torus.TorusFrancois
protocol.coord.width WIDTH
protocol.coord.height HEIGHT
protocol.coord.m 20
protocol.coord.psi 5
protocol.coord.K 4
protocol.coord.smart false
protocol.coord.lim 100

#protocol.poly polystyrene.torus.TorusPolystyrene
#protocol.poly polystyrene.torus.TorusSplitBasic
protocol.poly polystyrene.torus.TorusSplitAdvanced
protocol.poly.min_move     {3}
protocol.poly.max_diameter {4}
protocol.poly.m 20
protocol.poly.backup_size 4
protocol.poly.width WIDTH
protocol.poly.height HEIGHT
protocol.poly.smart false


init.0 polystyrene.torus.TorusInitializer
init.0.protocol coord
init.0.width WIDTH
init.0.height HEIGHT
init.0.view VIEW
init.0.size SIZE

init.1 polystyrene.torus.TorusPolyInitializer
init.1.protocol poly
init.1.width WIDTH
init.1.height HEIGHT


control.io polystyrene.core.Observer
control.io.protocol link
control.io.coord_protocol coord
control.io.gnuplot_base graph
control.io.gnuplot true
#control.io.guests true
control.io.guests_base guests

control.metrics polystyrene.core.Metrics
control.metrics.protocol link
control.metrics.coord_protocol coord
control.metrics.size SIZE

control.metrics2 polystyrene.core.MetricsPoly
control.metrics2.protocol link
control.metrics2.coord_protocol poly
control.metrics2.size SIZE

control.failure polystyrene.torus.TorusCatastrophe
control.failure.protocol link
control.failure.coord_protocol coord
control.failure.turn 20
control.failure.size SIZE

control.sanity polystyrene.torus.TorusSanity
control.sanity.protocol link
control.sanity.coord_protocol coord

# control.reinjection polystyrene.core.Reinjection
# control.reinjection.protocol link
# control.reinjection.coord_protocol coord
# control.reinjection.view VIEW
# control.reinjection.size SIZE
# control.reinjection.turn 100

control.stop polystyrene.core.Stop
control.stop.protocol link
control.stop.view VIEW
control.stop.size SIZE
#control.stop.redun 0
control.stop.redun (WIDTH*HEIGHT)/100
control.stop.opti true


include.control metrics metrics2 stop failure"""


# FT30Oct13: parallel version

random.seed(0) # set seed for reproducibility

# listing all experimental run in one large array
nb_expe = 2
a       = 4
b       = 4
step    = 1

def generate_config_file(args,seed):
	return myTemplate.format(args["W"],args["L"],seed,args["MinMo"],args["MaxDi"])

for min_mo  in (0,1):
	for max_di  in (0,1):

		listOfRunParams = [
			({"W":2**s,"L":2**s,"MinMo":min_mo,"MaxDi":max_di},expe_nb,random.randint(-2**63,2**63-1))
			for s       in range(a,b+1,step)
			for expe_nb in range(0,nb_expe)
			]


		launcher.set_expe_name_from_file_name(
			"simu_(.*).py",__file__,"_MinMo{}_MaxDi{}".format(min_mo,max_di)
			)
		launcher.set_config_factory(generate_config_file)
		launcher.launch_parallel(listOfRunParams)
