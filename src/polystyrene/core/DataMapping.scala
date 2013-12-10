package polystyrene.core

import scala.collection.mutable.{HashMap, LinkedHashSet}
import scala.collection.Set

import peersim.core.Node

object DataMapping{
	var map = HashMap[DataPoint, LinkedHashSet[Node]]()
	// var map = HashMap[DataPoint, Set[Node]]()

	var homogeneity : Double = 0.0
	var redundancy : Int = 0

	var turn = 0

	var failureTurn = 0

	var losses = 0

	var failState = false

	var maxAvrgMsg = 0.0

	var checkHomo = true
	var checkRedundancy = true

	def wipe{
		map = HashMap()
		homogeneity = 0.0
		redundancy = 0
		turn = 0
		failureTurn = 0
		losses = 0
		failState = false
		maxAvrgMsg = 0
		checkHomo = true
		checkRedundancy = true
	}
}
