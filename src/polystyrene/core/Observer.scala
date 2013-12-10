package polystyrene.core

import java.io.FileOutputStream
import java.io.IOException
import java.io.PrintStream

import peersim.config.Configuration
import peersim.core.Network
import peersim.core.Node
import peersim.graph.Graph
import peersim.reports.GraphObserver
import peersim.util.FileNameGenerator

import collection.JavaConversions._

class Observer(prefix : String) extends GraphObserver(prefix) {
	val PAR_GNUPLOT_BASE : String = "gnuplot_base"
	val PAR_COORDINATES_PROT : String = "coord_protocol"
	val PAR_GNUPLOT : String = "gnuplot"
	val PAR_GUESTS_BASE : String = "guests_base"
	val PAR_GUESTS : String = "guests"

	val graph_filename : String = Configuration.getString(prefix + "." + 
				PAR_GNUPLOT_BASE, "graph_dump")
	val fng = new FileNameGenerator(graph_filename, ".dat")
	val coordPid = Configuration.getPid(prefix + "." + PAR_COORDINATES_PROT)
	val gnuplot = Configuration.getBoolean(prefix + "." + PAR_GNUPLOT, false)

	val guests_base : String = Configuration.getString(prefix + "." + 
				PAR_GUESTS_BASE, "guests_dump")
	val gFng = new FileNameGenerator(guests_base, ".dat")
	val guests = Configuration.getBoolean(prefix + "." + PAR_GUESTS, false)

	def execute : Boolean = {
		if(gnuplot){
			graphTopology
		}

		if(guests){
			guestsToFile
		}

		false
	}


	def graphTopology {
		try{
			var fname : String = fng.nextCounterName
			var fos = new FileOutputStream(fname)
			println("Writing to file " + fname)
			var pstr = new PrintStream(fos)

			graphToFile(g, pstr)

			fos.close()
		}catch{
			case e:IOException => throw new RuntimeException(e)
		}
	}


	def graphToFile(g : Graph, ps : PrintStream) = {
		var total = 0
		for(i <- 0 until Network.size){
			var current = Network.get(i)

			if(current.isUp){
				var tmp = current.getProtocol(coordPid)
						.asInstanceOf[TMan]
						.toString

				var neighbours = current.getProtocol(coordPid)
						.asInstanceOf[TMan]
						.getNeighbours

				if(neighbours.size != 4){
					println("derp size: " + neighbours.size)
				}

				for(n <- neighbours){
					var tmp2 = n.getProtocol(coordPid)
							.asInstanceOf[TMan]
							.toString

					ps.println(tmp)
					ps.println(tmp2)
					ps.println("")
					ps.println("")
				}
				total += 1
			}
		}
	}


	def guestsToFile{
		try{
			var fname : String = gFng.nextCounterName
			var fos = new FileOutputStream(fname)
			println("Writing to file " + fname)
			var pstr = new PrintStream(fos)

			var results = new Array[Int](100)
			for(i <- 0 until Network.size){
				var current = Network.get(i)

				if(current.isUp){
					var prot = current.getProtocol(2)
							.asInstanceOf[Polystyrene]

					results(prot.guests.size) += 1
				}
			}

			for(i <- 0 until results.size){
				pstr.println(i + " " + results(i))
			}


			fos.close()
		}catch{
			case e:IOException => throw new RuntimeException(e)
		}
	}
}