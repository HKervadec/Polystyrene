package polystyrene.torus

import peersim.core.Network
import peersim.core.Node
import polystyrene.core.TMan

import scala.collection.mutable.LinkedHashSet

import polystyrene.core.Catastrophe
import polystyrene.core.DataMapping
import polystyrene.core.Polystyrene
import polystyrene.core.DataPoint

/*
 * Tempory debugging class. Needs to be fold back in Catastrophe code
 */
class TorusCatastropheFrancois(prefix : String) extends Catastrophe(prefix){
	def testFailure(prot : TMan) : Boolean = {
		// prot.asInstanceOf[Torus].x < prot.asInstanceOf[Torus].width/2
		prot.asInstanceOf[Torus]
			.coord.asInstanceOf[TorusCoord]
			.x < prot.asInstanceOf[Torus].width/2
	}

  // FT02Dec13: correcting problematic loss counting, which seems to
  // happen to late in original Catastrophe class
	override def execute : Boolean = {
		if(DataMapping.turn == DataMapping.failureTurn){
			breakShape
			DataMapping.failState = true
		// }else if(DataMapping.turn == DataMapping.failureTurn + 1){
      // FT02Dec13: We now count lossess in the same round as the
      // catastrophic failure -> this does not work because ghost data
      // points haven't been reactivated yet
			countLosses
			println(DataMapping.losses + 
				" datapoints died in an epic battle.")
		} // EndIf

		DataMapping.turn += 1

		false
	}

  // we count guests *and* ghosts to work around the problem of ghosts
  // not having been reactivated yet.
  override def countLosses : Int = {
		var dataSet = LinkedHashSet[DataPoint]()

		for(i <- 0 until Network.size){
			var node = Network.get(i)

			if(node.isUp){
				var prot = node.getProtocol(2)
						.asInstanceOf[Polystyrene]
				dataSet ++= prot.guests
        for(primary <- prot.ghosts.keySet if !primary.isUp){
			    dataSet ++= prot.ghosts(primary) // adding ghost datapoints of dead primary
		    }
      }
		}

		var tmp = size - dataSet.size

		DataMapping.losses = tmp
		tmp
	}

}
