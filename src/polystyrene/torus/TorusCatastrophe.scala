package polystyrene.torus

import peersim.core.Network
import peersim.core.Node
import polystyrene.core.TMan

import polystyrene.core.Catastrophe

class TorusCatastrophe(prefix : String) extends Catastrophe(prefix){
	def testFailure(prot : TMan) : Boolean = {
		// prot.asInstanceOf[Torus].x < prot.asInstanceOf[Torus].width/2
		prot.asInstanceOf[Torus]
			.coord.asInstanceOf[TorusCoord]
			.x < prot.asInstanceOf[Torus].width/2
	}
}
