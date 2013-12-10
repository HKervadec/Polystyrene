package polystyrene.core

import peersim.core.Node

abstract class Dist {
	def dist(n1 : Node, n2 : Node) : Double = {
		var c1 = n1.getProtocol(0).asInstanceOf[TMan].coord
		var c2 = n2.getProtocol(0).asInstanceOf[TMan].coord

		dist(c1, c2)
	}
	
	def dist(n1 : Node, d1 : DataPoint) : Double = {
		var c1 = n1.getProtocol(0).asInstanceOf[TMan].coord	
		
		dist(c1, d1.coord)
	}

	def dist(n1 : Node, c2 : Coord) : Double = {
		var c1 = n1.getProtocol(0).asInstanceOf[TMan].coord

		dist(c1, c2)
	}
	
	def dist(d1 : DataPoint, n1 : Node) : Double = {
		dist(n1, d1)
	}

	def dist(d1 : DataPoint, d2 : DataPoint) : Double = {
		dist(d1.coord, d2.coord)
	}

	def dist(c1 : Coord, c2 : Coord) : Double
}
