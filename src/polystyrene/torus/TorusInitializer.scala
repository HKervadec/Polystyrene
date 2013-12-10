package polystyrene.torus

import scala.collection.mutable.{HashMap, LinkedHashSet}

import peersim.config.Configuration
import peersim.core.Control
import peersim.core.Network
import peersim.core.Node
import peersim.core.CommonState

import polystyrene.core.DataMapping
import polystyrene.core.Polystyrene

class TorusInitializer(prefix : String) extends Control {
	val PAR_PROT : String = "protocol"
	val PAR_WIDTH : String = "width"
	val PAR_HEIGHT : String = "height"
	val PAR_VIEW : String = "view"
	val PAR_SIZE : String = "size"

	var pid : Int = Configuration.getPid(prefix + "." + PAR_PROT)
	var width : Int = Configuration.getInt(prefix + "." + PAR_WIDTH, 40)
	var height : Int = Configuration.getInt(prefix + "." + PAR_HEIGHT, 20)
	val view : Int = Configuration.getInt(prefix + "." + PAR_VIEW, 20)
	val size = Configuration.getInt(prefix + "." + PAR_SIZE, 800)

	/*if(width < height){
		var tmp = height
		height = width
		width = tmp
	}*/

	def execute : Boolean = {
		var total = 0
		/*println("WIDTH: " + width + ", HEIGHT: " + height)
		println(size, Network.size)*/

		DataMapping.wipe

		createNodes

		wireNodes

		updateDataMapping

		// var prot = Network.get(0).getProtocol(pid).asInstanceOf[Torus]

		false
	}


	def createNodes {
		TorusDist.width = width
		TorusDist.height = height

		firstNodes

		secondNodes
	}

	def firstNodes {
		var total = 0
		for(x <- 0 until width){
			for(y <- 0 until height){
				var n = Network.get(total)
				var prot = n.getProtocol(pid)
						.asInstanceOf[Torus]

				prot.setX(x)
				prot.setY(y)

				var prot2 = n.getProtocol(2).asInstanceOf[TorusPolystyrene]
				//prot2.guests += new TorusDataPoint(x, y)
        //FT05Dec13: to track changes
        prot2.addSingleGuest(new TorusDataPoint(x, y))

				total += 1
			}
		}

		println("Noeuds initalise: " + total)
	}

	def secondNodes {
		var total = size
		for(x <- 0 until width by 2){
			for(y <- 0 until height){
				var n = Network.get(total)
				n.setFailState(2)

				var prot = n.getProtocol(pid)
						.asInstanceOf[Torus]

				prot.setX(x+0.5)
				prot.setY(y+0.5)


				total += 1
			}
		}
		// println("Noeuds pret pour reinjection: " + (total-size))
	}



	/* Initialise the node's view. This is just a basic RPS system */
	def wireNodes {
		for(i <- 0 until size){
			var n : Node = Network.get(i)

			var prot = n.getProtocol(pid).asInstanceOf[Torus]
			while(prot.view.size < view){
				var tmp = CommonState.r.nextInt(size)

				var peer = Network.get(tmp)
				var peerCoord = peer.getProtocol(0)
						.asInstanceOf[Torus]
						.coord

				prot.addToView(n, (peer, peerCoord, 0))
			}

			prot.updateWorkingView
		}
	}


	def updateDataMapping {
		for(i <- 0 until size){
			var n : Node = Network.get(i)

			var prot = n.getProtocol(2).asInstanceOf[Polystyrene]
			
			DataMapping.map put (prot.guests.head, LinkedHashSet(n))
		}
	}
}
