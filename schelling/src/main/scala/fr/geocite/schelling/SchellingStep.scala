/*
 * Copyright (C) 17/05/13 Romain Reuillon
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

package fr.geocite.schelling

import fr.geocite.simpuzzle._
import scala.util.Random
import scala.collection.mutable.ArrayBuffer
import fr.geocite.simpuzzle.neighbourhood._
import scalaz.Lens
import fr.geocite.simpuzzle.matrix.Torus2D

trait SchellingStep <: Step
    with MatrixNeighbourhood
    with NoLogging {

  trait Place
  case object Free extends Place
  case object White extends Place
  case object Black extends Place

  def side: Int
  def similarWanted: Double
  def neighbourhoodSize: Int

  trait Cells <: Torus2D {
    type CELL = Place
  }
  type CELLS = Cells

  def step: Lens[STATE, Int]
  def cells: Lens[STATE, CELLS]

  // Compute the proportion of similar neighbors in a neighbourhood of neighborhoodSize
  def similarNeighbours(state: STATE, i: Int, j: Int): Double = {
    val n = neighbors(cells.get(state).cell(_, _), i, j, neighbourhoodSize).filter(_ != Free)
    n.count {
      _ == cells.get(state).cell(i, j)
    } / n.size.toDouble
  }

  // Compute the list of coordinates of the agent that want to move
  def moving(state: STATE): Iterable[(Int, Int)] =
    cells.get(state).cellsIndices.filter {
      case ((i, j), c) =>
        if (c == Free) false
        else similarNeighbours(state, i, j) < similarWanted
    }.unzip._1

  def freeCells(state: STATE) = cells.get(state).cellsIndices.filter {
    case (_, c) => c == Free
  }.unzip._1

  def nextState(state: STATE)(implicit rng: Random) = {
    val wantToMove = moving(state)
    val free = freeCells(state)

    val moves = rng.shuffle(wantToMove) zip rng.shuffle(free)

    val newMatrix = ArrayBuffer.tabulate(side, side)((i, j) => cells.get(state).cell(i, j))
    for (((fromI, fromJ), (toI, toJ)) <- moves) {
      newMatrix(toI)(toJ) = cells.get(state).cell(fromI, fromJ)
      newMatrix(fromI)(fromJ) = Free
    }

    cells.set(step.mod(_ + 1, state), new Cells {
      def cells = newMatrix.toSeq.map(_.toSeq)
    })
  }

}
