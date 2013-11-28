/*
 * Copyright (C) 20/11/13 Romain Reuillon
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

package fr.geocite.marius.one

import scala.util.Random
import fr.geocite.simpuzzle._
import fr.geocite.simpuzzle.distribution._
import fr.geocite.gis.distance._
import fr.geocite.marius._

trait MariusInitialState <: InitialState
    with PopulationDistribution
    with PositionDistribution
    with GeodeticDistance
    with RegionDistribution
    with CapitalDistribution
    with InitialWealth {
  def distances(implicit rng: Random) = {
    val positions = positionDistribution(rng).toIndexedSeq

    positions.zipWithIndex.map {
      case (c1, i) =>
        positions.zipWithIndex.map { case (c2, _) => distance(c1, c2) }
    }
  }
}