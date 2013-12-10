package polystyrene.torus

import peersim.config.Configuration
import peersim.core.Network
import peersim.core.Node
import peersim.core.Control

import polystyrene.core.Sanity

class TorusSanity(prefix : String) extends Sanity(prefix) {
	override def checkDouble {
		var coordSet = Set[(Double, Double)]()
		var derp = false
		var total = 0

		for(i <- 0 until Network.size){
			var node = Network.get(i)

			if(node.isUp){
				var prot = node.getProtocol(coordPid).asInstanceOf[Torus]

				// var coord = prot.getCoord
				var coord = (prot.coord.asInstanceOf[TorusCoord].x, 
					prot.coord.asInstanceOf[TorusCoord].y)

				if(!(coordSet contains coord)){
					coordSet += coord
				}else{
					derp = true
					total += 1
				}
			}
		}

		if(derp){
			println(total + " noeuds aux meme coordonnees.")
		}
	}
}