package polystyrene.torus

import scala.collection.mutable.LinkedHashSet

import peersim.config.Configuration
import peersim.core.Control
import peersim.core.Network
import peersim.core.Node
import peersim.core.CommonState

import polystyrene.core.DataPoint

class TorusPolyInitializer(prefix : String) extends Control {
	val PAR_PROT : String = "protocol"
	val PAR_WIDTH : String = "width"
	val PAR_HEIGHT : String = "height"
	val PAR_VIEW : String = "view"

	var pid : Int = Configuration.getPid(prefix + "." + PAR_PROT)
	var width : Int = Configuration.getInt(prefix + "." + PAR_WIDTH, 40)
	var height : Int = Configuration.getInt(prefix + "." + PAR_HEIGHT, 20)

	if(width < height){
		var tmp = height
		height = width
		width = tmp
	}

	def execute : Boolean = {
		var total = 0
		// println("WIDTH: " + width + ", HEIGHT: " + height)

		// createDataPoints

		false
	}


	def createDataPoints {
		var total = 0
		for(x <- 0 until width){
			for(y <- 0 until height){
				var n = Network.get(total)
				var prot = n.getProtocol(pid)
						.asInstanceOf[TorusPolystyrene]

				prot.addSingleGuest(new TorusDataPoint(x, y))

				total += 1
			}
		}

		/*var datapoints = LinkedHashSet[DataPoint]()
		for(n <- 0 until Network.size){
			var prot = Network.get(n).getProtocol(pid).asInstanceOf[TorusPolystyrene]
			datapoints ++= prot.guests
		}*/

		println("Data Point ajouté: " + total)
		/*println(datapoints.size)*/
	}
}
