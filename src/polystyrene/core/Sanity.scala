package polystyrene.core

import peersim.config.Configuration
import peersim.core.Network
import peersim.core.Node
import peersim.core.Control

abstract class Sanity(prefix : String) extends Control {
	val PAR_COORDINATES_PROT : String = "coord_protocol"
	val coordPid = Configuration.getPid(prefix + "." + PAR_COORDINATES_PROT)

	def execute : Boolean = {
		checkDouble

		false
	}


	def checkDouble
}