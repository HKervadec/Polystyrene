package polystyrene.torus


import peersim.config.Configuration
import peersim.cdsim.CDProtocol
import peersim.core.Protocol
import peersim.core.Node
import peersim.core.Network
import peersim.core.CommonState
import peersim.core.Linkable

import math.{min, abs, sin, cos, Pi}
import polystyrene.core.TMan
import polystyrene.core.Coord

class Torus(prefix : String) extends TMan(prefix : String) {
	var coord = new TorusCoord().asInstanceOf[Coord]

	val PAR_WIDTH : String = "width"
	val PAR_HEIGHT : String = "height"

	var width : Int = Configuration.getInt(prefix + "." + PAR_WIDTH, 40)
	var height : Int = Configuration.getInt(prefix + "." + PAR_HEIGHT, 20)

	var R : Double = width/(2*Pi)
	var r : Double = height/(2*Pi)



	def getX : Double = {
		coord.asInstanceOf[TorusCoord].x
	}

	def getY : Double = {
		coord.asInstanceOf[TorusCoord].y
	}


	def setX(newX : Double){
		coord.asInstanceOf[TorusCoord].x = newX
	}

	def setY(newY : Double){
		coord.asInstanceOf[TorusCoord].y = newY
	}


	def setCoord(coord : Coord){
		setX(coord.asInstanceOf[TorusCoord].x)
		setY(coord.asInstanceOf[TorusCoord].y)
	}

	def getDim() : (Int, Int) = {
		(width, height)
	}

	def getCoord() : (Double, Double) = {
		(coord.asInstanceOf[TorusCoord].x, coord.asInstanceOf[TorusCoord].y)
	}

	def dist(a : Node, b : Node) : Double = {
		TorusDist.dist(a, b)
	}

	def dist(a : Node, b : Coord) : Double = {
		TorusDist.dist(a, b)
	}

	def dist(a : Coord, b : Coord) : Double = {
		TorusDist.dist(a, b)
	}


	override def toString : String = {
		var tmp = to3D
		tmp._1 + " " + tmp._2 + " " + tmp._3
		// x + " " + y
	}

	def to3D : (Double, Double, Double) = {
		var theta : Double = coord.asInstanceOf[TorusCoord].x/width*2*Pi
		var phi : Double = coord.asInstanceOf[TorusCoord].y/height*2*Pi

		var baseX = cos(theta)
		var baseY = sin(theta)
		var tmp = (R + r * cos(phi))
		var m = tmp * baseX
		var n = tmp * baseY
		var p = r * sin(phi)
		(m, n, p)
	}
}