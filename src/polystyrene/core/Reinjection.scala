package polystyrene.core

import scala.collection.mutable.LinkedHashSet

import peersim.config.Configuration
import peersim.core.CommonState
import peersim.core.Control
import peersim.core.Network
import peersim.core.Node


class Reinjection(prefix : String) extends Control{
	val PAR_TURN : String = "turn"
	val PAR_COORDINATES_PROT : String = "coord_protocol"
	val PAR_SIZE : String = "size"
	val PAR_VIEW : String = "view"

	var turn = Configuration.getInt(prefix + "." + PAR_TURN, 70)
	val pid = Configuration.getPid(prefix + "." + PAR_COORDINATES_PROT)
	val size = Configuration.getInt(prefix + "." + PAR_SIZE, 800)
	val view : Int = Configuration.getInt(prefix + "." + PAR_VIEW, 20)


	def execute : Boolean = {
		if(DataMapping.turn == turn){
			reinject
		}

		false
	}

	def reinject2 {
		for(i <- 0 until size){
			var node = Network.get(i)
			node.setFailState(0)
		}
	}


	def reinject {
		wake

		wire

		// updateDataMapping
	}

	def wake {
		for(i <- size until Network.size){
			var node = Network.get(i)

			node.setFailState(0)
		}
	}

	def wire {
		for(i <- size until Network.size){
			var node = Network.get(i)

			var prot = node.getProtocol(pid).asInstanceOf[TMan]
			while(prot.view.size < view){
				var tmp = CommonState.r.nextInt(Network.size)

				var peer = Network.get(tmp)

				if(peer.isUp){
					var peerCoord = peer.getProtocol(0)
							.asInstanceOf[TMan]
							.coord
					prot.addToView(node, (peer, peerCoord, 0))
				}
			}
		}
	}

	def updateDataMapping {
		for(i <- size until Network.size){
			var n : Node = Network.get(i)

			var prot = n.getProtocol(2).asInstanceOf[Polystyrene]
			
			DataMapping.map put (prot.guests.head, LinkedHashSet(n))
		}
	}
}