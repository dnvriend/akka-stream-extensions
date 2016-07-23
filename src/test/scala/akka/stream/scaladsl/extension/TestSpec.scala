/*
 * Copyright 2016 Dennis Vriend
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package akka.stream.scaladsl.extension

import java.util.UUID

import akka.NotUsed
import akka.actor.ActorSystem
import akka.event.{ Logging, LoggingAdapter }
import akka.stream.scaladsl.Source
import akka.stream.scaladsl.extension.xml.{ PersonParser, XMLEventSource }
import akka.stream.testkit.TestSubscriber
import akka.stream.testkit.scaladsl.TestSink
import akka.stream.{ ActorMaterializer, Materializer }
import akka.util.Timeout
import org.scalatest._
import org.scalatest.concurrent.{ Eventually, ScalaFutures }

import scala.concurrent.duration._
import scala.concurrent.{ ExecutionContext, Future }
import scala.util.Try
import scala.xml.pull.XMLEvent

final case class Address(street: String = "", houseNumber: String = "", zipCode: String = "", city: String = "")

final case class Person(firstName: String = "", lastName: String = "", age: Int = 0, address: Address = Address())

trait TestSpec extends FlatSpec
    with Matchers
    with ScalaFutures
    with BeforeAndAfterAll
    with BeforeAndAfterEach
    with OptionValues
    with Eventually
    with ClasspathResources {

  implicit val system: ActorSystem = ActorSystem()
  implicit val mat: Materializer = ActorMaterializer()
  implicit val ec: ExecutionContext = system.dispatcher
  val log: LoggingAdapter = Logging(system, this.getClass)
  implicit val pc: PatienceConfig = PatienceConfig(timeout = 60.seconds)
  implicit val timeout = Timeout(30.seconds)

  val testPerson1 = Person("Barack", "Obama", 54, Address("Pennsylvania Ave", "1600", "20500", "Washington"))
  val testPerson2 = Person("Anon", "Ymous", 42, Address("Here", "1337", "12345", "InUrBase"))

  final val PersonsXmlFile = "xml/persons.xml"
  final val LotOfPersonsXmlFile = "xml/lot-of-persons.xml"

  implicit class PimpedByteArray(self: Array[Byte]) {
    def getString: String = new String(self)
  }

  implicit class PimpedFuture[T](self: Future[T]) {
    def toTry: Try[T] = Try(self.futureValue)
  }

  def withTestXMLEventSource()(filename: String)(f: TestSubscriber.Probe[XMLEvent] => Unit): Unit =
    withInputStream(filename) { is =>
      f(XMLEventSource.fromInputStream(is).runWith(TestSink.probe[XMLEvent]))
    }

  def withTestXMLPersonParser()(filename: String)(f: TestSubscriber.Probe[Person] => Unit): Unit =
    withInputStream(filename) { is =>
      f(XMLEventSource.fromInputStream(is).via(PersonParser.flow).runWith(TestSink.probe[Person]))
    }

  implicit class SourceOps[A](src: Source[A, NotUsed]) {
    def testProbe(f: TestSubscriber.Probe[A] => Unit): Unit =
      f(src.runWith(TestSink.probe(system)))
  }

  def randomId = UUID.randomUUID.toString

  override protected def afterAll(): Unit = {
    system.terminate().toTry should be a 'success
  }
}