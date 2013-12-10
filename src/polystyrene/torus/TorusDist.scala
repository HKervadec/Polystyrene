package polystyrene.torus

import peersim.core.Node
import polystyrene.core.{Coord, Dist}
import math.{abs, min}

/*
 * An object to compute Euclidean distance in a logical torus.
 */
object TorusDist extends Dist {
	var width = 0
	var height = 0

	def dist(c1 : Coord, c2 : Coord) : Double = {
		var xa = c1.asInstanceOf[TorusCoord].x
		var ya = c1.asInstanceOf[TorusCoord].y
		var xb = c2.asInstanceOf[TorusCoord].x
		var yb = c2.asInstanceOf[TorusCoord].y

		var dx = abs(xa-xb)
		var dy = abs(ya-yb)

		dx = min(dx, width - dx)
		dy = min(dy, height - dy)

		dx*dx + dy*dy
	}
}
