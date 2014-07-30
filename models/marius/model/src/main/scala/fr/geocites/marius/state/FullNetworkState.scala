/*
 * Copyright (C) 27/06/13 Romain Reuillon
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

package fr.geocites.marius.state

import scala.util.Random
import scalaz._
import fr.geocites.marius._
import fr.geocites.marius.structure.Network

trait FullNetworkState <: NetworkState {

  def networkShare: Double = 1.0

  override def initialState(implicit rng: Random) = {
    val cities = initialCities
    NetworkState.State(0, initialCities.toVector, Network.full(cities.size), MariusFile.distanceMatrix)
  }
}