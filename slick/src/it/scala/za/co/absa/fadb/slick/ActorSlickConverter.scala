package za.co.absa.fadb.slick

import slick.jdbc.{GetResult, PositionedResult}

/**
 *  A trait representing a converter from a Slick PositionedResult to an Actor.
 *  The trait is to be mixed into a SlickFunction returning an Actor.
 */
trait ActorSlickConverter {

  protected def slickConverter: GetResult[Actor] = {
    def converter(r: PositionedResult): Actor = {
      Actor(r.<<, r.<<, r.<<)
    }
    GetResult(converter)
  }
}
