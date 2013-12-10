#!/usr/bin/env python3
# -*-  python-indent:2; tab-width:2; indent-tabs-mode:t; -*-

import os
import random
import logging
import math
import socket
import argparse
import time
import re

from multiprocessing import Pool, cpu_count
from sys  import argv, exit

random.seed(0) # set seed for reproducibility

template   = None # no template. Needs to be set at init time.
expe_dir   = None # uninitialized
expe_name  = None
create_config_file = None # callable that return a config file. To be set by user.

def set_expe_name_from_file_name(regex,file_name,postfix=""):
	extract_expe_re = re.compile(regex)
	matches = extract_expe_re.match(os.path.basename(file_name))
	if not matches:
		raise Exception("Could not extract expe name from {}".format(file_name))
	set_expe_name(matches.group(1)+postfix)

def set_up_logging(args):
	format_string = '%(asctime)s {} %(levelname)s:%(message)s'.format(socket.gethostname())
	if args.logstdout:
		file_name = None
	else:
		file_name = 'log_simu_{}.log'.format(socket.gethostname())
	logging.basicConfig(format=format_string,
											filename=file_name,
											level=logging.INFO,
											datefmt='%Y-%m-%d %H:%M:%S')

def set_up_option_parser():
	parser = argparse.ArgumentParser()
	parser.add_argument("--debug"    , help="print debug info"   , action="store_true")
	parser.add_argument("--dry"      , help="dry run (no simulations)"   , action="store_true")
	parser.add_argument("--logstdout", help="print log to stdout", action="store_true")
	parser.add_argument("--nocompile", help="does not compile source prior to running", action="store_true")
	group = parser.add_mutually_exclusive_group()
	group.add_argument("--nice", help="only use half of the CPU cores (excludes --leavecore)",
                    action="store_true")
	group.add_argument("--leavecore", type=int, help="number of cores to leave free (excludes --nice)",
										 action="store",metavar='N')
	return parser

def parse_args():
	parser = set_up_option_parser()
	global args
	args = parser.parse_args()
	set_up_logging(args)
	if args.debug:
		logging.getLogger().setLevel(logging.DEBUG)
	if args.nice:
		logging.info("option nice turned on")
	if args.leavecore:
		logging.info("option leavecore set to {}".format(args.leavecore))
	if args.nocompile:
		logging.info("compilation prior to run disabled")
	logging.info("simulationLauncher initialised")


def set_config_factory(templateFactory):
	global create_config_file
	create_config_file = templateFactory

def ensure_dir(d):
	if not os.path.exists(d):
		os.makedirs(d)

def set_expe_name(expeName):
	global expe_dir
	global expe_name
	expe_name = expeName
	logging.info("Experience name set to '{}'".format(expeName))
	expe_dir  = "results_{}".format(expe_name)
	ensure_dir(expe_dir)

def run_command(command):
	res = os.system(command)
	if res!=0:
		raise Exception("command {} returned result {}".format(command,res))

def get_run_id(args,expe_nb):
	argsToString = "_".join(sorted(["{}{}".format(k,args[k]) for k in args],reverse=True))
	return "{0}_{1}_expe_{2}".format(expe_name,argsToString,expe_nb)

def get_file_with_id(prefix,argsSimu,expe_nb):
	return "{2}/{0}_{1}.txt".format(prefix,get_run_id(argsSimu,expe_nb),expe_dir)

#def launchPeersim(width,length,expe_number,seed):
def launchPeersim(argsSimu,expe_number,seed):
	try:
		start = time.time()
		logging.debug("launchPeersim({},{})".format(argsSimu,expe_number))
		
		# checking that result file does not exist already
		tmpFile = get_file_with_id("expe_results",argsSimu,expe_number)
		logging.debug("tmpFile={}".format(tmpFile))
		if os.path.isfile(tmpFile):
			logging.warning("Result for {} already exists. Skipping.".format(get_run_id(argsSimu,expe_number)))
			logging.debug("Result file {} already exists. Skipping experiment.".format(tmpFile))
			return
		# setting up config file
		#config     = template.format(argsSimu["W"],argsSimu["L"],seed)
		# seed = tmpFile.__hash__() # __hash__ conveniently returns a long int, which we use as seed
		# random.seed(tmpFile.__hash__())
		# seed = random.randint(-2**63,2**63-1) 
		logging.debug("Seed for expe ({},{}) is {}".format(argsSimu,expe_number,seed))
		config = create_config_file(argsSimu,seed)
		configFile = get_file_with_id("config",argsSimu,expe_number)
		with open(configFile, 'w') as f:
			f.write(config)
		f.closed
		# putting command together
		logFile = get_file_with_id("tmp_log",argsSimu,expe_number)
		command = './run.py {0} > {1}.part 2> {2}'.format(configFile,tmpFile,logFile)
		# running command
		logging.info(
			"Launching simulations for {0} expe {1}"
			.format(argsSimu,expe_number)
			)
		logging.debug(
			"Command for {0} expe {1} is {2}"
			.format(argsSimu,expe_number,command)
			)
		if args.dry:
			logging.info("Dry run. No simulation launched.")
		else:
			run_command(command)
			grep_result = os.system("grep Exception {}".format(logFile))
			if (grep_result==0): # Java Exception found in output
				raise Exception("output {} contains an exception.".format(logFile))
			run_command("mv {0}.part {0}".format(tmpFile)) # making result file permanent

		duration = time.time() - start
		logging.info(
			"Simulations for {0} expe {1} completed in {2:.3f}s"
			.format(argsSimu,expe_number,duration)
			)
		return duration

	except Exception as e:
		logging.exception(
			"job launchPeersim({},{}) failed. Skipping job".format(argsSimu,expe_number))

def launch_parallel(simuDescriptions):
	nb_cpu = cpu_count()
	logging.info("{0} CPU cores detected.".format(nb_cpu))
	poolSize  = nb_cpu
	if args.nice:
		poolSize = math.ceil(poolSize/2)
	if args.leavecore:
		poolSize = poolSize-args.leavecore

	logging.info("Using {} cores.".format(poolSize))

	p = Pool(poolSize) 
	logging.info("Process pool with {} threads created.".format(poolSize))
	logging.info("Launching {} peersim simulations in parallel".format(len(simuDescriptions)))

  # launching parallel experiments using pool of processes
	# p.starmap(launchPeersim, simuDescriptions, 1) # only work for python > 3.3

	start = time.time()
	asynch_results = [p.apply_async(launchPeersim, x) for x in simuDescriptions]
	[res.wait() for res in asynch_results]
	duration = time.time() - start

	total_time = sum([res.get() for res in asynch_results if res.get()!=None])

	logging.info("{} peersim simulations on {} cores completed in {:.2f}s. On average {:.2f}s per simulation.".format(
			len(simuDescriptions), poolSize, duration, total_time/len(simuDescriptions)))

parse_args()

if not args.nocompile:
	logging.info("Compiling source code using ant")
	# ensuring source has been compiled
	res = os.system("ant compile")
	if res!=0:
		raise Exception("Compilation failed and returned result {}".format(command,res))
