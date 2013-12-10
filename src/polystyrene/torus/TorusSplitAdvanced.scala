package polystyrene.torus

import scala.collection.mutable.LinkedHashSet
import polystyrene.core.DataPoint
import peersim.core.Node
import peersim.config.Configuration

/*
 * A configurable split function, to experiment between SplitBasic and
 * normal split
 */
class TorusSplitAdvanced(prefix : String) extends TorusPolystyrene(prefix){

	val PAR_MINMOVE : String = "min_move"
	val PAR_MAXDIAM : String = "max_diameter"

	var minmove : Int = Configuration.getInt(prefix + "." + PAR_MINMOVE, 0)
	var maxdiam : Int = Configuration.getInt(prefix + "." + PAR_MAXDIAM, 0)

	override def split(thisNode : Node, peer : Node, peerGuests : LinkedHashSet[DataPoint]) 
			: (LinkedHashSet[DataPoint], LinkedHashSet[DataPoint]) = {

		if(thisNode == peer){
			println("Split with the same point")
		}

    // appends the two sets.
		var points = guests ++ peerGuests

		if(points.size < 1){
			(guests, peerGuests)
		} else {

      var (u,v) = 
        if (maxdiam==1)
          largest_pairwise_distance(points)
        else
          (findRepr(guests, thisNode),findRepr(peerGuests, peer))

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
			if (minmove==1)
        // We try to minimize the displacement of nodes
        minimize_displacement(f1, thisNode, f2, peer)
      else
        // We don't try to minimise displacement of nodes
        (f1, f2)
		}
	} // EndMethod split
}
