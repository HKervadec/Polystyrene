package polystyrene.torus

import peersim.core.Node

import math.{min, abs, sin, cos, Pi}
import polystyrene.core.TMan
import polystyrene.core.Coord


/*
 * a class to test variants of basic TMan 
 */
class TorusFrancois(prefix : String) extends Torus(prefix : String) {

	override def addToView(thisNode : Node, newNode : (Node, Coord, Int)){

		if(thisNode != newNode._1){
      /* QUESTION: check with Hoel */

			/* Remove the node, then add it again */
			/* view = view filterNot {x => x._1 == newNode._1}
			view ::= (newNode._1, newNode._2.copy, newNode._3) */

			// var n = view.indexWhere({x => x._1 == newNode._1 &&	x._3 <= newNode._3})
      var n = view.indexWhere({x => x._1 == newNode._1})

			if(n >= 0){
        // the newNode is already in the view. We only update if its
        // position has a newer version, overwise do nothing
        if (view(n)._3 < newNode._3) {
				  view(n) = (newNode._1, newNode._2.copy, newNode._3)
        }
			}else{
				view +:= (newNode._1, newNode._2.copy, newNode._3)
			}

		}
	} // EndMethod addToView
}
