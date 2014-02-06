/*
 * Copyright (C) 23/10/13 Romain Reuillon
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package fr.geocite.marius.matching

import scala.util.Random
import fr.geocite.marius.{ Transaction, Marius }

trait ProportionalMatching <: Matching
    with InteractionPotential
    with Marius {

  def distanceOrderSell: Double

  def matchCities(
    s: STATE,
    supplies: Seq[Double],
    demands: Seq[Double])(implicit rng: Random) = {
    lazy val interactionMatrix =
      interactionPotentialMatrix(
        cities.get(s),
        supplies,
        distanceMatrix.get(s))

    lazy val transactions = interactionMatrix.zipWithIndex.map {
      case (interactions, from) =>
        val interactionPotentialSum = interactions.sum
        interactions.zipWithIndex.map {
          case (ip, to) =>
            val transacted = (ip / interactionPotentialSum) * supplies(from)
            Transaction(from, to, transacted)
	}
    }.transpose
	//println("size dernière lign trans", transactions(1144).size)
	//println("col dernière ligne trans", transactions(1144))
	
	

    def unsatisfieds =
      for {
        (d, i) <- demands.zipWithIndex
        transactionsTo = transactions(i)
      } yield {

	//println("transac[",i,"]",transactionsTo.size)
d - transactionsTo.map(_.transacted).sum
}
    /*    val effectiveTransactedFrom: Map[Int, Seq[Transaction]] =
      effectiveTransactedTo.toSeq.flatMap(_._2).groupBy(_.from).withDefaultValue(Seq.empty)


    def unsolds =
      for {
        (s, i) <- supplies.zipWithIndex
        transactions = effectiveTransactedFrom(i)
      } yield s - transactions.map(_.transacted).sum
*/

    Matched(transactions.flatten, cities.get(s).map(c => 0.0), unsatisfieds.toSeq)
  }

}