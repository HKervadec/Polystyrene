package polystyrene.core
/* Ia plus de mousse de canard?! */

import scala.collection.mutable.LinkedHashSet

import peersim.config.Configuration
import peersim.core.Control
import peersim.core.Network

abstract class Catastrophe(prefix : String) extends Control{
	val PAR_TURN : String = "turn"
	val PAR_COORDINATES_PROT : String = "coord_protocol"
	val PAR_SIZE : String = "size"

	DataMapping.failureTurn = Configuration.getInt(prefix + "." + PAR_TURN, 15)
	val pid = Configuration.getPid(prefix + "." + PAR_COORDINATES_PROT)
	val size = Configuration.getInt(prefix + "." + PAR_SIZE, 800)


	def execute : Boolean = {
		if(DataMapping.turn == DataMapping.failureTurn){
			breakShape
			DataMapping.failState = true
		}else if(DataMapping.turn == DataMapping.failureTurn + 1){
			countLosses

			println(DataMapping.losses + 
				" datapoints died in an epic battle.")
		}


		DataMapping.turn += 1

		false
	}

	def breakShape {
		// println("#######################################\nIT'S HAPPENING")
		var total = 0
		for(n <- 0 until size){
			var node = Network.get(n)
			var prot = node.getProtocol(pid).asInstanceOf[TMan]

			if(testFailure(prot)){
				node.setFailState(2)
				total += 1
			}
		}
		// println(total + " nodes just died :(")

		/*for(n <- 0 until size){
			var node = Network.get(n)
			var prot = node.getProtocol(pid).asInstanceOf[TMan]
			prot.updateWorkingView
		}*/

		for(n <- DataMapping.map.keySet){
			DataMapping.map(n) =  DataMapping.map(n) filter {x => x.isUp}
		}
	}

	def testFailure(prot : TMan) : Boolean

	def countLosses : Int = {
		var dataSet = LinkedHashSet[DataPoint]()

		for(i <- 0 until Network.size){
			var node = Network.get(i)

			if(node.isUp){
				var prot = node.getProtocol(2)
						.asInstanceOf[Polystyrene]
				dataSet ++= prot.guests
			}
		}

		var tmp = size - dataSet.size

		DataMapping.losses = tmp
		tmp
	}
}