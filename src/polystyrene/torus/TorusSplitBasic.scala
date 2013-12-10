package polystyrene.torus

import scala.collection.mutable.LinkedHashSet
import polystyrene.core.DataPoint
import peersim.core.Node

/*
 * An alternative torus class to experiment with a basic split function
 */
class TorusSplitBasic(prefix : String) extends TorusPolystyrene(prefix){

	override def split(thisNode : Node, peer : Node, peerGuests : LinkedHashSet[DataPoint]) 
			: (LinkedHashSet[DataPoint], LinkedHashSet[DataPoint]) = {

		if(thisNode == peer){
			println("Split with the same point")
		}

    // appends the two sets.
		var points = guests ++ peerGuests

		if(points.size < 1){
			(guests, peerGuests)
		}else{
      // find two representative for each set
		  var u = findRepr(guests    , thisNode)
		  var v = findRepr(peerGuests, peer)

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
      // We don't try to minimise displacement of nodes
			(f1, f2)
		}
	} // EndMethod split
}
