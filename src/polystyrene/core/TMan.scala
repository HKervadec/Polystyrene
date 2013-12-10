package polystyrene.core

import scala.collection.mutable.LinkedList

import peersim.config.Configuration
import peersim.cdsim.CDProtocol
import peersim.core.Protocol
import peersim.core.Node
import peersim.core.Network
import peersim.core.CommonState
import peersim.core.Linkable
import math.{min, abs}


/* Node coordinates : 2 units
 * Node id : 1 unit
 * DataPoint coordinates : 2 units*/

abstract class TMan(prefix : String) extends CDProtocol {
	val PAR_M : String = "m"
	val PAR_PSI : String = "psi"
	val PAR_K : String = "K"
	val PAR_SMART : String = "smart"
	val PAR_LIM : String = "lim"

	val defaultLim = 100000

	val m   : Int = Configuration.getInt(prefix + "." + PAR_M, 20)
	val psi : Int = Configuration.getInt(prefix + "." + PAR_PSI, 5)
	val K   : Int = Configuration.getInt(prefix + "." + PAR_K, 4)
	val lim       = Configuration.getInt(prefix + "." + PAR_LIM, defaultLim)
	val smart     = Configuration.getBoolean(prefix + "." + PAR_SMART, false)
	// println("TMan: " + smart)


	var coord : Coord
	var posVersion = 0

	var view : LinkedList[(Node, Coord, Int)] = LinkedList()
	var workingView : LinkedList[(Node, Coord, Int)] = LinkedList()

	var id = 0

	var totMsg = 0


	override def clone() : Object = {
		var inp : TMan = null
		try{
			inp =  (super.clone).asInstanceOf[TMan]
			inp.view = LinkedList()
			inp.workingView = LinkedList()
			inp.coord = coord.copy
		}catch{
			case _ => {}
		}
		inp
	}


	override def nextCycle(node : Node, protocolId : Int){
		/* - Select a node to exchange with:
			selected randomly in the psi best nodes
		 * - Send him the m best nodes, according to his position
		 * - Receive a buffer from him, and merge it to the current 
		 *   view
		 */

		if(node.isUp){
      // FT05Dec13: totMsg = 0 cannot be reset here. We lose any
      // updates by other nodes in sendBuffer... Moving to Metrics.scala.

			// totMsg = 0
			updateWorkingView

			id = protocolId


			/* Selecting the node */
			var p : (Node, Coord, Int) = selectPeer(node)

			/* Building the buffer to send */
			var buffer = rank(p._2, workingView :+ (node, coord, posVersion)) take m


			/* Send the buffer and merge the returned buffer 
			 * If the partner is not alive, he returns null, and we
			 * remove him from the view */
			var response = p._1.getProtocol(protocolId)
					.asInstanceOf[TMan]
					.sendBuffer(p._1, buffer, (node, coord, posVersion))
			// totMsg += 3*m
      // FT05Dec13
      totMsg += 3*buffer.size
      //println("totMsg "+totMsg)

			merge(node, response)

      // This should take place in merge

			// if(lim != defaultLim){
			// 	view = rank(coord, view) take lim
			// }
		}
	}


	/* Remove the dead nodes from the view */
	def updateWorkingView {
    /* smart = smart propagation of node position. See
     * Polystyrene.setPos(node:Node) for a detailed description of
     * what smart does. */
		if(!smart){
			var newView = LinkedList[(Node, Coord, Int)]()

			for(node <- view) {
				if(node._1.isUp) {
					newView +:= (node._1, node._1.getProtocol(0)
								.asInstanceOf[TMan]
								.coord.copy,
								node._1.getProtocol(0)
								.asInstanceOf[TMan]
								.posVersion)

					totMsg += 2
				}else{
					newView +:= (node._1, node._2, node._3)
				} // EndElse
			} // EnfFor
			view = newView
		} // EndIf if(!smart)

		workingView = view filter {x => x._1.isUp}

		if(workingView.size == 0){
			println("Vide: " + view.size + " " + workingView.size)
		}
	} // EndMethod updateWorkingView


	/* Select a random peer in the psi best nodes from the view */
	def selectPeer(thisNode : Node) : (Node, Coord, Int) = {
		/*if(workingView.size == 0){
			println("Working view vide")
		}*/
		rank(coord,  workingView)(CommonState.r.nextInt(psi))
	}


	/* Select a random peer from the psi-best nodes, plus a random node from
	 * the topology. */
	def randomPeer(thisNode : Node) : Node = {
		/*updateWorkingView - not need to update working view here -> extra comm cost! */

		var tmp = rank(coord,  workingView) map {_._1}

		var random = CommonState.r.nextInt(Network.size)
		var randomNode = Network.get(random)

		while(!randomNode.isUp || 
			(tmp contains randomNode) || 
			randomNode == thisNode){

			random = CommonState.r.nextInt(Network.size)
			randomNode = Network.get(random)
		}

		tmp +:= randomNode
		tmp(CommonState.r.nextInt(psi+1))
	}

	/* Select a random peer from the view */
	def randomPeer : Node = {
		/*updateWorkingView*/

		workingView(CommonState.r.nextInt(workingView.size))._1
	}


	/* Return a view sorted by the dist function */
	def rank(c : Coord, buffer : LinkedList[(Node, Coord, Int)]) 
			: LinkedList[(Node, Coord, Int)] = {

		buffer.sortWith({(a, b) => dist(c, a._2) < dist(c, b._2)})
	}


	/* Return the distance between the coordinates and the node */
	def dist(a : Node, b : Node) : Double

	def dist(a : Node, b : Coord) : Double

	def dist(a : Coord, b : Coord) : Double


	/* build a new buffer for the emitter, merge the buffer to its
		 * view, then return the new buffer */
	def sendBuffer(thisNode : Node, buffer : LinkedList[(Node, Coord, Int)], 
			emitter : (Node, Coord, Int)) 
				: LinkedList[(Node, Coord, Int)] = {

		if(thisNode.isUp){
			//updateWorkingView
			var newBuffer = rank(emitter._2, workingView) take m

			merge(thisNode, buffer)

			/* Totally useless, since the emitter is contained in 
			 * the buffer (see the nextCycle function). */
			// addToView(thisNode, emitter)

			totMsg += 3*newBuffer.size // cost of sending back new buffer
      //println("totMsg "+totMsg)

			newBuffer
		}else{
			println("I is dead, Y did u call me?")
			null
		}
	}


	def merge(thisNode : Node, buffer : LinkedList[(Node, Coord, Int)]){
		for(node <- buffer){
			addToView(thisNode, node)
		}
		if(lim != defaultLim){
			view = rank(coord, view) take lim
    }
	}


	def addToView(thisNode : Node, newNode : (Node, Coord, Int)){
		if(thisNode != newNode._1){

			var n = view.indexWhere({x => x._1 == newNode._1})

			if(n >= 0){//First we test if the node is already in the view
				if(view(n)._3 < newNode._3){
					/*if so, we test if we have to update the coordinates*/
					view(n) = (newNode._1, newNode._2.copy, newNode._3)
				}
			}else{//else, we add the node to the view
				view +:= (newNode._1, newNode._2.copy, newNode._3)
			}

		}
	} // EndMethod addToView


	def getNeighbours : LinkedList[Node] = {
		/*updateWorkingView*/ // FT05Dec13: not needed and creates wrong message cost
		if(workingView.size < K){
			println("vue trop petite: " + workingView.size)
		}
		
		rank(coord, workingView) take K map {_._1}
	}


	def toString : String
}
