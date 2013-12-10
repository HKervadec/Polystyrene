package polystyrene.core

import java.lang.Runtime

import math.{sqrt, max}
import scala.collection.mutable.LinkedHashSet
import scala.collection.Set

import peersim.config.Configuration
import peersim.core.Network
import peersim.core.Node
import peersim.core.Control

class MetricsPoly(prefix : String) extends Control {
	val PAR_COORDINATES_PROT : String = "coord_protocol"
	val PAR_SIZE : String = "size"
	val PAR_DATAPOINT : String = "datapoint"
	val PAR_MEMORY : String = "memory"
	val PAR_GUESTS : String = "guest"
	val PAR_DEVIATION : String = "deviation"
	
	val coordPid = Configuration.getPid(prefix + "." + PAR_COORDINATES_PROT)
	val size = Configuration.getInt(prefix + "." + PAR_SIZE, 800)
	val datapoint = Configuration.getBoolean(prefix + "." + PAR_DATAPOINT, true)
	val memory = Configuration.getBoolean(prefix + "." + PAR_MEMORY, true)
	val guest = Configuration.getBoolean(prefix + "." + PAR_GUESTS, true)
	val deviation = Configuration.getBoolean(prefix + "." + PAR_DEVIATION, true)

	var poly = Network.get(0).getProtocol(coordPid).asInstanceOf[Polystyrene]


	def execute : Boolean = {
		if(DataMapping.checkHomo){
			homogeneity
		}

		if(datapoint){
			countDataPoint
		}

		if(DataMapping.checkRedundancy){
			redundancy
		}

		if(memory){
			averageMemorySize
		}


		if(guest){
			println("AverageGuests: " + DataMapping.turn + " " + averageGuest)
		}

		if(deviation){
			standardDeviation
		}

		println("\n\n")

		false
	}


	def homogeneity : Double = {
		var sum = 0.0

		for(data <- DataMapping.map.keySet){
			var protectors = DataMapping.map(data)

			if(protectors.size > 0){
				sum += nearest(data, protectors)
			}else{
				sum += nearestWhole(data)
			}
		}

		var tmp = sum / DataMapping.map.size

		DataMapping.homogeneity = tmp

		println("Homogeneity: " + DataMapping.turn + " " + tmp)

		tmp
	}

	def nearest(data : DataPoint, list : Set[Node]) : Double = {
		var min = Double.MaxValue

		for(node <- list){
			if(node.isUp){
				var tmp = poly.dist(data, node)
				if(tmp < min){
					min = tmp
				}
			}
		}

		sqrt(min)
	}

	def nearestWhole(data : DataPoint) : Double = {
		var min = Double.MaxValue

		for(n <- 0 until Network.size){
			var node = Network.get(n)
			if(node.isUp){	
				var tmp = poly.dist(data, node)
				if(tmp < min){
					min = tmp
				}
			}
		}

		sqrt(min) 
	}


	def countDataPoint : Int = {
		var dataSet = Set[DataPoint]()
		for(n <- 0 until Network.size){
			var node = Network.get(n)
			if(node.isUp){
				var prot =  node.getProtocol(coordPid)
						.asInstanceOf[Polystyrene]

				dataSet ++= prot.guests
			}
		}

		var tmp = dataSet.size

		println("Data points: " + DataMapping.turn + " " + tmp)

		tmp
	}


	def redundancy : Int = {
		var total = 0
		for(i <- 0 until Network.size){
			var node = Network.get(i)

			if(node.isUp){
				var prot = node.getProtocol(coordPid)
						.asInstanceOf[Polystyrene]
				total += prot.guests.size
			}
		}

		var tmp = total - size + DataMapping.losses

		DataMapping.redundancy = tmp

		println("Redundancy: " + DataMapping.turn + " " + tmp)

		tmp
	}


  def averageMemorySize : Double = {
    var total = 0.0
    var activeNode = 0
    
    for(i <- 0 until Network.size){
      var node = Network.get(i)
      
      if(node.isUp){
        var prot = node.getProtocol(coordPid)
          .asInstanceOf[Polystyrene]
        total += prot.guests.size
        
        for(cpl <- prot.ghosts){
          total += cpl._2.size
        }
        activeNode += 1
      }
    }
    
    var tmp = total / activeNode
    
    println("AverageMemorySize: " + DataMapping.turn + " " + tmp)
    
    tmp
  }

	// def averageMemorySize : Double = {
	// 	var total = 0.0
	// 	var activeNode = 0

	// 	for(i <- 0 until Network.size){
	// 		var node = Network.get(i)

	// 		if(node.isUp){
	// 			var prot = node.getProtocol(coordPid)
	// 					.asInstanceOf[Polystyrene]
  // BUG -> ghosts is a dictionnary
	// 			total += prot.guests.size + prot.ghosts.size
	// 			activeNode += 1
	// 		}
	// 	}

	// 	var tmp = total / activeNode

	// 	println("AverageMemorySize: " + DataMapping.turn + " " + tmp)

	// 	tmp
	// }

	def averageGuest : Double = {
		var total = 0.0
		var activeNode = 0

		for(i <- 0 until Network.size){
			var node = Network.get(i)

			if(node.isUp){
				var prot = node.getProtocol(coordPid)
						.asInstanceOf[Polystyrene]
				total += prot.guests.size
				activeNode += 1
			}
		}

		var tmp = total / activeNode
	

		tmp
	}

	def standardDeviation : Double = {
		var total = 0.0
		var activeNode = 0

		for(i <- 0 until Network.size){
			var node = Network.get(i)

			if(node.isUp){
				var prot = node.getProtocol(coordPid)
						.asInstanceOf[Polystyrene]
				total += prot.guests.size*prot.guests.size
				activeNode += 1
			}
		}

		var tmp = averageGuest
		tmp = sqrt(total/activeNode - tmp*tmp)

		println("StandardDeviation: " + DataMapping.turn + " " + tmp)

		tmp
	}
}
