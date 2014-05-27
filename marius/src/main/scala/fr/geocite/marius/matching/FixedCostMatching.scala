/*
* Copyright (C) 2014 Romain Reuillon
*
* This program is free software: you can redistribute it and/or modify
* it under the terms of the GNU Affero General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

package fr.geocite.marius.matching

import scala.util.Random
import fr.geocite.marius._
import structure.Matrix._
import scala.collection.mutable.ListBuffer
import fr.geocite.simpuzzle._
import fr.geocite.marius.structure.SparseMatrix

trait FixedCostMatching <: Matching with InteractionPotential { this: Marius =>

  def fixedCost: Double

  override def matchCities(
    s: STATE,
    supplies: Seq[Double],
    demands: Seq[Double])(implicit rng: Random) = {

    def splitTheCake(neighbours: Seq[(Int, Double)], cakeSize: Double) = {
      val nPI = neighbours.unzip._2
      val totalNeighboursPI = nPI.sum
      val relativeNeighboursPI = nPI.map { _ / totalNeighboursPI }
      (neighbours.unzip._1 zip relativeNeighboursPI).map { case (n, pi) => (n, pi * cakeSize) }
    }

    val supplied =
      for {
        (city, cityId) <- cities.get(s).zipWithIndex
      } yield {
        val outNeighbours = network.get(s).outNodes(cityId)

        // Interaction potential is not symmetric but has the same expression for both ways
        val neighboursPI =
          outNeighbours.map {
            neighbourId =>
              interactionPotential(supplies(cityId), demands(neighbourId), distanceMatrix(cityId)(neighbourId))
          }.toIndexedSeq

        val supply = supplies(cityId)

        val viableTransactingNeighbours = splitTheCake(outNeighbours zip neighboursPI, supply).filter { case (_, r) => r >= fixedCost }.unzip._1
        val supplied = splitTheCake(viableTransactingNeighbours.map(n => (n, neighboursPI(n))), supply)
        supplied.map { case (neighbour, supplied) => (neighbour, supplied, neighboursPI(neighbour)) }
      }

    case class SuppliedToDestination(from: Int, interactionPotential: Double, supplied: Double)

    val suppliedIndexedByDestination = Vector.fill(cities.get(s).size)(ListBuffer[SuppliedToDestination]())
    for {
      (from, to, supply, pi) <- supplied.zipWithIndex.map { case (s, from) => s.map { s => from -> s }.map(flatten) }.flatten
    } suppliedIndexedByDestination(to) += SuppliedToDestination(from, pi, supply)

    val demanded =
      for {
        (supplied, cityId) <- suppliedIndexedByDestination.zipWithIndex
      } yield {
        splitTheCake(supplied.map(s => (s.from, s.interactionPotential)), demands(cityId))
      }

    val cells =
      for {
        (suppliesForCity, demandsForCity) <- supplied zip demanded
      } yield {
        val supplies = Array.fill[Double](cities.get(s).size)(0.0)
        for ((n, q, _) <- suppliesForCity) supplies(n) = q

        val demands = Array.fill[Double](cities.get(s).size)(0.0)
        for ((n, q) <- demandsForCity) demands(n) = q

        (supplies zip demands zipWithIndex).map(flatten).flatMap {
          case (s, d, j) =>
            if (s == 0.0 || d == 0.0) None
            else Some(Cell(j, math.min(s, d)))
        }
      }.toSeq

    SparseMatrix(cells)

  }
}