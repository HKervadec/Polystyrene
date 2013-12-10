package polystyrene.core

import java.lang.Runtime

import math.{sqrt, max}
import scala.collection.mutable.LinkedHashSet
import scala.collection.Set

import peersim.config.Configuration
import peersim.core.Network
import peersim.core.Node
import peersim.core.Control

class Metrics(prefix : String) extends Control {
	val PAR_COORDINATES_PROT : String = "coord_protocol"
	val PAR_SIZE : String = "size"
	val PAR_VIEWSIZE : String = "viewsize"
	val PAR_MSG : String = "msg"
	val PAR_QUALITY : String = "quality"
	
	val coordPid = Configuration.getPid(prefix + "." + PAR_COORDINATES_PROT)
	val size = Configuration.getInt(prefix + "." + PAR_SIZE, 800)
	val viewsize = Configuration.getBoolean(prefix + "." + PAR_VIEWSIZE, true)
	val msg = Configuration.getBoolean(prefix + "." + PAR_MSG, true)
	val quality = Configuration.getBoolean(prefix + "." + PAR_QUALITY, true)


	var poly = Network.get(0).getProtocol(2).asInstanceOf[Polystyrene]


	def execute : Boolean = {
		if(viewsize){
			println("Average View: " + DataMapping.turn + " " + averageViewSize)
		}


		if(msg){
			var avrgMsg = averageMessage
			DataMapping.maxAvrgMsg = max(avrgMsg, DataMapping.maxAvrgMsg)
			println("AverageMesssage: " + DataMapping.turn + " " + avrgMsg)
		}

		if(quality){
			println("NeighorhoodQuality: " + DataMapping.turn + " " + neighorhoodQuality)
		}

		// println("Memory used: " + (Runtime.getRuntime.totalMemory/1000000))

		// println("\n\n")

		false
	}


	def averageViewSize : Double = {
		var total = 0.0
		var nodeCount = 0.0
		for(n <- 0 until Network.size){
			var node = Network.get(n)
			if(node.isUp){
				nodeCount += 1

				var prot =  node.getProtocol(coordPid)
						.asInstanceOf[TMan]
				
        // FT05Dec13 : commenting out. The metrics should not
        // interfere with the protocol. Here this adds additional
        // communication in totMsg. Bad
        
        //  prot.updateWorkingView

				total += prot.workingView.size
			}
		}

		total / nodeCount
	}


	def averageMessage : Double = {
		var total = 0
		var activeNode = 0

		for(i <- 0 until Network.size){
			var node = Network.get(i)
			var prot = node.getProtocol(coordPid).asInstanceOf[TMan]

			if(node.isUp){
				total += prot.totMsg
        //println("SUM totMsg "+prot.totMsg)
				activeNode += 1
			}
      prot.totMsg = 0 // resetting for next round
		}

		total.toDouble / activeNode.toDouble
	}

	def neighorhoodQuality : Double = {
		var total = 0.0
		var edges = 0

		for(i <- 0 until Network.size){
			var node = Network.get(i)

			if(node.isUp){

				var prot = node.getProtocol(coordPid)
						.asInstanceOf[TMan]
				var coord = prot.coord
				var neighbours = prot.getNeighbours

				for(neighbor <- neighbours){
					var coord2 = neighbor.getProtocol(coordPid)
							.asInstanceOf[TMan]
							.coord
					total += sqrt(prot.dist(coord, coord2)) // dist is a square in our implementation
          //println("Dist: "+sqrt(prot.dist(coord, coord2)))
				  edges += 1
				}
			}
		}

		//total / (4 * activeNode)
    	total / edges
	}
}
