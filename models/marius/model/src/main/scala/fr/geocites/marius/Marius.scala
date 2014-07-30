/*
 * Copyright (C) 03/02/14 Romain Reuillon
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

package fr.geocites.marius

import fr.geocites.simpuzzle._
import scala.util.Random
import scala.math._
import fr.geocites.simpuzzle.state.{ TimeEndingCondition, StepByStep }
import fr.geocites.marius.balance.{ Balances, Exchange }
import fr.geocites.gis.distance.GeodeticDistance
import fr.geocites.marius.structure.Network
import monocle._
import monocle.syntax._

trait Marius <: StepByStep
    with TimeEndingCondition
    with MariusLogging
    with Balances {

  type CITY

  def economicMultiplier: Double
  def sizeEffectOnDemand: Double
  def sizeEffectOnSupply: Double
  def populationToWealthExponent: Double

  def cities: SimpleLens[STATE, Seq[CITY]]
  def population: SimpleLens[CITY, Double]
  def wealth: SimpleLens[CITY, Double]
  def region: SimpleLens[CITY, String]
  def nation: SimpleLens[CITY, String]
  def regionalCapital: SimpleLens[CITY, Boolean]
  def nationalCapital: SimpleLens[CITY, Boolean]
  def network: SimpleLens[STATE, Network]
  def distances: SimpleLens[STATE, DistanceMatrix]

  def nextState(s: STATE)(implicit rng: Random) = {
    for {
      newWealths <-  wealths(s)
    } yield {
      def populations =
        ((s |-> cities get) zip newWealths).zipWithIndex.map {
          case ((city, newWealth), i) =>
            check(newWealth >= 0, s"City $i error in wealth before conversion toPop $newWealth")
            val deltaPopulation = wealthToPopulation(newWealth) - wealthToPopulation(city |-> wealth get)
            val newPopulation = (city |-> population get) + deltaPopulation
            check(newPopulation >= 0, s"Error in population $newWealth $newPopulation")
            newPopulation
        }

      def newCities =
        ((s |-> cities get) zip populations zip newWealths).map(flatten).map {
          case (c, p, w) =>
            check(p >= 0, s"The population of $c is negative $p, $w")
            (c |-> wealth set w) |-> population set p
        }

      (s |-> cities set newCities) |-> step modify (_ + 1)
    }
  }

  def wealths(s: STATE)(implicit rng: Random) = {
    val ss = supplies(cities.get(s))
    val ds = demands(cities.get(s))

    for {
      bs <- balances(s, ss, ds)
    } yield {
      val newWealths =
        (cities.get(s) zip
          ss zip
          ds zip
          bs zipWithIndex).map(flatten).map {
          case (city, supply, demand, b, i) =>
            val newWealth =
              (city |-> wealth get) + supply - demand + b
            if (newWealth <= 0.0) 0.0 else newWealth
        }
        resourcesEffect(newWealths)
    }
  }

  def resourcesEffect(wealths: Seq[Double]) = wealths
  def supplies(cities: Seq[CITY]) = cities.map(c => supply(c |-> population get))
  def demands(cities: Seq[CITY]) = cities.map(c => demand(c |-> population get))

  def demand(population: Double) = economicMultiplier * pow(population, sizeEffectOnDemand)
  def supply(population: Double) = economicMultiplier * pow(population, sizeEffectOnSupply)

  def rescaleWealth(wealth: Seq[Double], population: Seq[Double]) = {
    val factor = population.sum / wealth.sum.toDouble
    wealth.map(_ * factor)
  }

  def wealthToPopulationExponent: Double

  def initialWealth(population: Double)(implicit rng: Random): Double = pow(population, populationToWealthExponent)

  def wealthToPopulation(wealth: Double) = {
    check(wealth >= 0, s"Negative wealth $wealth")
    pow(wealth, wealthToPopulationExponent)
  }

}
