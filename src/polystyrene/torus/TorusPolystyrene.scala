package polystyrene.torus

import math.{min, abs}
import scala.collection.mutable.LinkedHashSet
import scala.collection.Set

import peersim.config.Configuration
import peersim.core.Node

import polystyrene.core.Polystyrene
import polystyrene.core.DataPoint


class TorusPolystyrene(prefix : String) extends Polystyrene(prefix){
	val PAR_WIDTH  : String = "width"
	val PAR_HEIGHT : String = "height"

	var width  : Int = Configuration.getInt(prefix + "." + PAR_WIDTH , 40)
	var height : Int = Configuration.getInt(prefix + "." + PAR_HEIGHT, 20)

	def copyCoordinates(data : DataPoint, node : Node) : Boolean = {
		var prot = node.getProtocol(0).asInstanceOf[Torus]
		var same = prot.coord.equals(data.coord)
		
		prot.coord = data.coord.copy	

		same	
	} // EndMethod copyCoordinates

	def dist(a : DataPoint, b : DataPoint) : Double = {
		TorusDist.dist(a, b)
	} // EndMethod dist

	def dist(a : DataPoint, n : Node) : Double = {
		TorusDist.dist(a, n)
	} // EndMethod dist

  /*
   *  computes the points amomg a set with the smallest sum of
   *  square distances to the other points
   */
	def findRepr(set : LinkedHashSet[DataPoint], node : Node) : DataPoint = {
		if(set.size != 0){
      // totalSum defined in polystyrene.core.Polystyrene
			set.minBy[Double](totalSum(set)).asInstanceOf[TorusDataPoint]
		}else{
			var tmp = node.getProtocol(0).asInstanceOf[Torus].getCoord
			new TorusDataPoint(tmp._1, tmp._2)
		}
	} // End findRepr

  def largest_pairwise_distance(points : LinkedHashSet[DataPoint]) = {
		var maxDist : Double = 0.0 ; var tmp : Double = 0.0
    // Computing the largest pairwise distance
		var u = points.head
		var v = points.head
		for(m <- points){
			for(n <- points){
				tmp = dist(m, n)
				if(tmp > maxDist){
					maxDist = tmp
					u = m
					v = n
				}
			}
		}
    (u,v)
  } // End largest_pairwise_distance

  def minimize_displacement(
    f1 : LinkedHashSet[DataPoint], thisNode : Node,
    f2 : LinkedHashSet[DataPoint], peer     : Node) = 
  {
		var u = findRepr(f1, thisNode)
		var v = findRepr(f2, peer)

    // we try to minimise the amount of movement nodes do
		var d1 = dist(u, thisNode) + dist(v, peer)
		var d2 = dist(u, peer) + dist(v, thisNode)

    if (d1<d2) { (f1,f2) } else { (f2,f1) }
  } // minimize_displacement

	def split(thisNode : Node, peer : Node, peerGuests : LinkedHashSet[DataPoint]) 
			: (LinkedHashSet[DataPoint], LinkedHashSet[DataPoint]) = {

    // FT05Nov13: definition of findRepr moved out of split

		if(thisNode == peer){
			println("Split with the same point")
		}

    // appends the two sets.
		var points = guests ++ peerGuests
		// println("Spliting")
		if(points.size < 1){
		// if(false){
			// (points, LinkedHashSet[DataPoint]())
			(guests, peerGuests)
		}else{
      var (u,v) = largest_pairwise_distance(points)
      // Distribute nodes according to which point (u or v) they are
      // closest to.
			var f1 = LinkedHashSet[DataPoint]()
			var f2 = LinkedHashSet[DataPoint]()
			
			for (p <- points){
				if(dist(u, p) < dist(v, p))
					f1 += p
				else
					f2 += p
			}

			/*if(f1.size == 0)
				println("F1 vide")
			if(f2.size == 0)
				println("F2 vide")*/


			/*if((f1++f2).size != points.size){
				println("bad split")
				println(guests + " " + peerGuests)
				println(f1 + " " + f2)
			}*/

			minimize_displacement(f1, thisNode, f2, peer)
      //(f1, f2)
		}
	} // EndMethod split

} // EndClass TorusPolystyrene
