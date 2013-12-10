package polystyrene.torus

import peersim.config.Configuration
import peersim.core.Node
import peersim.core.Network

import polystyrene.core.DataPoint
import polystyrene.core.Coord

import math.{min, abs}

class TorusDataPoint(u : Double, v : Double) extends DataPoint{
	/*var x : Double = u
	var y : Double = v*/

	var coord = new TorusCoord(u, v).asInstanceOf[Coord]

	def getCoord : (Double, Double) = {
		// (x, y)
		(coord.asInstanceOf[TorusCoord].x, coord.asInstanceOf[TorusCoord].y)
	}
}