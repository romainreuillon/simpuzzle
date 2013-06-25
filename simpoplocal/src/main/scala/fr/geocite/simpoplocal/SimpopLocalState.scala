/*
 * Copyright (C) 25/04/13 Romain Reuillon
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

package fr.geocite.simpoplocal

import java.util.concurrent.atomic.AtomicInteger
import fr.geocite.simpuzzle.city.{ Id, Radius, Position }

trait SimpopLocalState extends fr.geocite.simpuzzle.State {

  type STATE = SimpopLocalState

  case class SimpopLocalState(date: Int, cities: Seq[City]) {
    def step = date
  }

  case class City(
      id: Int,
      x: Double,
      y: Double,
      population: Double,
      availableResource: Double,
      percolationIndex: Int,
      cityClass: Int,
      tradePlace: TradePlace) extends Position with Radius with Id {

    def rangeRadiusClass1 = 20.0
    def rangeRadiusClass2 = 10.0
    def rangeRadiusClass3 = 5.0

    def radius =
      cityClass match {
        case 1 => rangeRadiusClass1
        case 2 => rangeRadiusClass2
        case 3 => rangeRadiusClass3
        case _ => sys.error(s"Invalid city class $cityClass")
      }
  }

  case class TradePlace(
      innovations: List[Innovation] = List.empty,
      totalInnovations: Int = 0) {
    lazy val sortedInnovations = innovations.sorted(Innovation.orderByRootId)
  }

  object Innovation {
    implicit val orderByRootId = Ordering.by((_: Innovation).rootId)
    val curId = new AtomicInteger
  }

  class Innovation(
      val city: Int,
      val date: Int,
      _rootId: Option[Int] = None,
      val id: Int = Innovation.curId.getAndIncrement) {

    val rootId = _rootId.getOrElse(id)

    def copy(
      city: Int = this.city,
      date: Int = this.date) = {

      new Innovation(city,
        date,
        Some(rootId)
      )
    }

    override def toString = "Id=" + id + ", RootId=" + rootId
  }

}