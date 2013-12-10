package polystyrene.torus

import polystyrene.core.Coord

class TorusCoord(u : Double = 0.0, v : Double = 0.0) extends Coord{
	var x = u
	var y = v

	override def equals(c : Coord) : Boolean = {
		(x == c.asInstanceOf[TorusCoord].x) &&
			(y == c.asInstanceOf[TorusCoord].y)
	}

	override def copy : Coord = {
		new TorusCoord(x, y)
	}
}
