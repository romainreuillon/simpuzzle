/*
 * Copyright (C) 2014 Guillaume Chérel
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

package fr.geocites.marius.behaviour

import fr.geocites.marius.SimpleModel
import java.util.Random

object TestMariusBehaviour extends App {
  implicit val rng = new Random
  println((new BehaviourComputing).compute(
    new SimpleModel(
      economicMultiplier = 1,
      sizeEffectOnSupply = 0.06,
      sizeEffectOnDemand = 0.005,
      distanceDecay = 4.4,
      wealthToPopulationExponent = 1,
      populationToWealthExponent = 1)
    )(rng).mkString(" "))
}