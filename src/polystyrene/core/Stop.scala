package polystyrene.core

import scala.collection.mutable.LinkedHashSet

import peersim.config.Configuration
import peersim.core.CommonState
import peersim.core.Control
import peersim.core.Network
import peersim.core.Node


class Stop(prefix : String) extends Control{
	// val PAR_COORDINATES_PROT : String = "coord_protocol"
	val PAR_SIZE : String = "size"
	val PAR_VIEW : String = "view"
	val PAR_LIMHOMO : String = "lim_homo"
	val PAR_REDUN : String = "redun"
	val PAR_OPTI : String = "opti"

	// val pid = Configuration.getPid(prefix + "." + PAR_COORDINATES_PROT)
	val size = Configuration.getInt(prefix + "." + PAR_SIZE, 800)
	val view = Configuration.getInt(prefix + "." + PAR_VIEW, 20)
	val lim_homo = Configuration.getDouble(prefix + "." 
						+ PAR_LIMHOMO, 0.71)
	val redun = Configuration.getInt(prefix + "." + PAR_REDUN, 0)
	val opti = Configuration.getBoolean(prefix + "." + PAR_OPTI, false)

	var checkHomo = true
	var checkRedundancy = true

	def execute : Boolean = {
		var m = Network.get(0).getProtocol(2).asInstanceOf[Polystyrene].m

		if(DataMapping.failState){
			if(checkHomo){
				checkHomo = !(DataMapping.homogeneity < lim_homo)

				if(!checkHomo){
					println("Converge time: " + DataMapping.turn)
				}
			}
			if(checkRedundancy){
				checkRedundancy = !(DataMapping.redundancy <= redun)

				if(!checkRedundancy){
					println("Redundancy time: " + DataMapping.turn)
				}
			}
		}

		if(opti){
			DataMapping.checkHomo = checkHomo
			DataMapping.checkRedundancy = checkRedundancy
		}

		if(!(checkHomo || checkRedundancy)){
			println("Max Average Message cost: " + m + " " + DataMapping.maxAvrgMsg)
			true
		}else{
			false
		}
	}
}
