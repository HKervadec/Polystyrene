package polystyrene.core

import scala.collection.mutable.{HashMap, LinkedHashSet, LinkedList}
import scala.collection.{Set}

import peersim.config.Configuration
import peersim.cdsim.CDProtocol
import peersim.core.Protocol
import peersim.core.Node
import peersim.core.Network
import peersim.core.CommonState
import peersim.core.Linkable
import math.{min, abs}

abstract class Polystyrene(prefix : String) extends CDProtocol{
	val PAR_M : String = "m"
	val PAR_BACKUP_SIZE = "backup_size"
	val PAR_SMART : String = "smart"
	val PAR_ACTIVATED : String = "activated"

	val m : Int    = Configuration.getInt(prefix + "." + PAR_M, 20)
	val backupsize = Configuration.getInt(prefix + "." + PAR_BACKUP_SIZE, 4)
	val smart      = Configuration.getBoolean(prefix + "." + PAR_SMART, false)
  val activated  = Configuration.getBoolean(prefix + "." + PAR_ACTIVATED, true)
	// println("Polystyrene: " + smart)
	

	var id = 0

	var guests : LinkedHashSet[DataPoint] = LinkedHashSet()
	var ghosts : HashMap[Node, LinkedHashSet[DataPoint]] = HashMap()
	var backup : Set[Node] = Set()
  // FT05Dec13 adding cost of backup to message costs
  var guestsHaveChanged : Boolean = false;


	override def clone() : Object = {
		var inp : Polystyrene = null
		try{
			inp =  (super.clone).asInstanceOf[Polystyrene]
			inp.guests = LinkedHashSet()

			inp.ghosts = HashMap()

			inp.backup = Set()
		}catch{
			case _ => {}
		}
		inp
	}

	override def nextCycle(node : Node, protocolId : Int){
		/* - LinkedHashSet Pos
		 * - Migration
		 * - Backup
		 * - Recovery 
		 */
		if(node.isUp){
		// if(false){
			id = protocolId

      // always tell underlying tman of position
			setPos(node : Node)

      // core of polystyrene. Only runs if activated.
      if (activated) {
			  migrate(node)
			  updateBackup(node)
			  recovery
      }
		}
	}



	/* Change the node cordinates according to its guest list */
	def setPos(node : Node) {
		var same = true
		var oldCoord = node.getProtocol(0).asInstanceOf[TMan].coord

		if(guests.size != 0){
	 		var bestDataPoint = guests.minBy[Double](totalSum(guests))

			same = copyCoordinates(bestDataPoint, node)
		}

		if(!same){
			var prot = node.getProtocol(0).asInstanceOf[TMan]
			prot.posVersion += 1
		}

    /* In the 'smart' version of the protocol, changes of position are
     * pushed here when the node detects it has moved (!same) rather
     * than pulled systematically in every round in
     * TMan.updateWorkingView() */
		if(smart && !same){
			var prot = node.getProtocol(0).asInstanceOf[TMan]
			var coord = prot.coord

			var partners = prot.rank(oldCoord, prot.workingView) take m

			for(peer <- partners){
				var peerProt = peer._1.getProtocol(0)
						.asInstanceOf[TMan]

				peerProt.addToView(peer._1, 
						(node, coord, prot.posVersion))
			}

			node.getProtocol(0).asInstanceOf[TMan]
			 	.totMsg += 3*m
		}
	}

	def totalSum(set : LinkedHashSet[DataPoint])(root : DataPoint) 
				: Double = {

		var sum : Double = 0.0
		for(n <- set){
			sum += dist(root, n)
		}

		sum
	}

	def dist(a : DataPoint, b : DataPoint) : Double

	def dist(a : DataPoint, n : Node) : Double

	def dist(n : Node, b : DataPoint) : Double = {
		dist(b, n)
	}

	def copyCoordinates(data : DataPoint, node : Node) : Boolean


	/* Select a random node from the view, then split the data points 
	 * between them. After that, their positions are updated */
	def migrate(thisNode : Node) {
		var peer = thisNode.getProtocol(0)
					.asInstanceOf[TMan]
					.randomPeer(thisNode)

		var peerProt = peer.getProtocol(id)
				.asInstanceOf[Polystyrene]
				
		var tmp = split(thisNode, peer, peerProt.guests)

    // other nodes always sends us its guests list
		peer.getProtocol(0).asInstanceOf[TMan]
			.totMsg += (peerProt.guests.size)*2

    // FT05Dec13: Tracking change to guests for backuo
    if (guests!=tmp._1) {
		  guests = tmp._1
      guestsHaveChanged = true
    }

    if (peerProt.guests!=tmp._2) {
      // we only send back new guests list if changed
		  thisNode.getProtocol(0)
			  .asInstanceOf[TMan].totMsg += (tmp._2.size)*2
		  peerProt.guests = tmp._2
      peerProt.guestsHaveChanged = true
    }

		setPos(thisNode)
		peerProt.setPos(peer)


		/* Update the dataMapping */
		for(data <- guests){
			var tmp = DataMapping.map(data) - peer + thisNode
			DataMapping.map put(data, tmp)
		}
		for(data <- peerProt.guests){
			var tmp = DataMapping.map(data) - thisNode + peer
			DataMapping.map put(data, tmp)
		}
	}

	def split(thisNode : Node, peer : Node, peerGuests : LinkedHashSet[DataPoint]) 
			: (LinkedHashSet[DataPoint], LinkedHashSet[DataPoint])

  def sendBackupToNode(thisNode : Node, destination: Node) {
		destination.getProtocol(id).asInstanceOf[Polystyrene]
			.addGhost(thisNode, guests)
    // counting cost of back up, no optimization
		thisNode.getProtocol(0)
			.asInstanceOf[TMan].totMsg += (guests.size)*2
  }

	def updateBackup(thisNode : Node) {
		/* Remove the dead backup */
		backup = backup filter {x => x.isUp}

    // FT05Dec13 updating current back if a change has taken place
    // since last time
    if (guestsHaveChanged) {
		  for(node <- backup){
        sendBackupToNode(thisNode,node)
		  }
    }
    guestsHaveChanged = false // FT05Dec13: resetting for next round

		/* Add new backup until the backupsize is reached */
		while(backup.size < backupsize){
			var randomHost = Network.get(CommonState.r.nextInt(Network.size))

			while(!randomHost.isUp || (backup contains randomHost)){
				randomHost = Network.get(CommonState.r.nextInt(Network.size))
			}

			/*randomHost.getProtocol(id).asInstanceOf[Polystyrene]
				.addGhost(thisNode, guests)*/
			// backup add randomHost
			backup += randomHost
      // sending backup to random host
			sendBackupToNode(thisNode,randomHost)
		}

	}

	def addGhost(emitter : Node, set : LinkedHashSet[DataPoint]){
		ghosts.put(emitter, set)
	}


	def recovery{
		for(node <- ghosts.keySet if !node.isUp){
			addGuest(ghosts(node))
			ghosts -= node
		}
	}

	def addGuest(set : LinkedHashSet[DataPoint]){
		guests ++= set
    guestsHaveChanged = true
	}

	def addSingleGuest(newPoint : DataPoint){
		guests += newPoint
    guestsHaveChanged = true
	}
}
