package polystyrene.core


class Coord{
	def equals(c : Coord) : Boolean = {
		true
	}

	def copy : Coord = {
		println("Pouet")
		new Coord
	}
}
