# akka-stream-extensions

[![Build Status](https://travis-ci.org/dnvriend/akka-stream-extensions.svg?branch=master)](https://travis-ci.org/dnvriend/akka-stream-extensions)
[![Download](https://api.bintray.com/packages/dnvriend/maven/akka-stream-extensions/images/download.svg) ](https://bintray.com/dnvriend/maven/akka-stream-extensions/_latestVersion)
[![Codacy Badge](https://api.codacy.com/project/badge/Grade/dbd00ed89680435d8b96e57bd22d352f)](https://www.codacy.com/app/dnvriend/akka-stream-extensions?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=dnvriend/akka-stream-extensions&amp;utm_campaign=Badge_Grade)
[![License](http://img.shields.io/:license-Apache%202-red.svg)](http://www.apache.org/licenses/LICENSE-2.0.txt)

Non-official extension library for akka-stream that contains operations like zipWithIndex, Source.fromTry and so on

## Installation
Add the following to your `build.sbt`:

```scala
resolvers += Resolver.jcenterRepo

libraryDependencies += "com.github.dnvriend" %% "akka-stream-extensions" % "0.0.1"
```

## Contribution policy ##

Contributions via GitHub pull requests are gladly accepted from their original author. Along with any pull requests, please state that the contribution is your original work and that you license the work to the project under the project's open source license. Whether or not you state this explicitly, by submitting any copyrighted material via pull request, email, or other means you agree to license the material under the project's open source license and warrant that you have the legal authority to do so.

## License ##

This code is open source software licensed under the [Apache 2.0 License](http://www.apache.org/licenses/LICENSE-2.0.html).


## akka.stream.scaladsl.extension.io.DigestCalculator
Given a stream of ByteString, it calculates a digest given a certain Algorithm.

## akka.stream.scaladsl.extension.io.FileUtils
A stage that does file operations. Very handy for stream processing file operations.

## akka.stream.scaladsl.extension.xml.Validation
Given a stream of ByteString, it validates an XML file given an XSD.

## akka.stream.scaladsl.extension.xml.XMLEventSource
Given an inputstream or filename, it creates a `Source[XMLEvent, NotUsed]` that can be used to process
an XML file. It can be used together with akka-stream's processing stages and the
`akka.persistence.query.extension.Journal` to store the transformed messages in the journal to be consumed
by other components. It can also be used with `reactive-activemq`'s
`akka.stream.scaladsl.extension.activemq.ActiveMqProducer` to send these messages to a VirtualTopic.

## akka.stream.scaladsl.extension.xml.XMLParser
It should be easy to write XML parsers to process large XML files efficiently. Most often this means reading the XML
sequentially, parsing a known XML fragment and converting it to DTOs using case classes. For such a use case the
`akka.stream.scaladsl.extension.xml.XMLParser` should help you get you up and running fast!

For example, let's process the following XML:

```xml
<orders>
    <order id="1">
        <item name="Pizza" price="12.00">
            <pizza>
                <crust type="thin" size="14"/>
                <topping>cheese</topping>
                <topping>sausage</topping>
            </pizza>
        </item>
        <item name="Breadsticks" price="4.00"/>
        <tax type="federal">0.80</tax>
        <tax type="state">0.80</tax>
        <tax type="local">0.40</tax>
    </order>
</orders>
```

Imagine we are interested in only orders, and only the tax, lets write two parsers:

```scala
import scala.xml.pull._
import akka.stream.scaladsl._
import akka.stream.scaladsl.extension.xml.XMLParser
import akka.stream.scaladsl.extension.xml.XMLParser._
import akka.stream.scaladsl.extension.xml.XMLEventSource

case class Order(id: String)

val orderParser: Flow[XMLEvent, Order] = {
 var orderId: String = null
 XMLParser.flow {
  case EvElemStart(_, "order", meta, _) ⇒
    orderId = getAttr(meta)("id"); emit()
  case EvElemEnd(_, "order") ⇒
    emit(Order(orderId))
 }
}

case class Tax(taxType: String, value: String)

val tagParser: Flow[XMLEvent, Tax] = {
  var taxType: String = null
  var taxValue: String = null
  XMLParser.flow {
    case EvElemStart(_, "tax", meta, _) =>
      taxType = getAttr(meta)("type"); emit()
    case EvText(text) ⇒
      taxValue = text; emit()
    case EvElemEnd(_, "tax") ⇒
      emit(Tax(taxType, taxValue))
  }
}

XMLEventSource.fromFileName("orders.xml")
 .via(orderParser).runForeach(println)

XMLEventSource.fromFileName("orders.xml")
 .via(tagParser).runForeach(println)
```

For a more complex example, please take a look at `akka.stream.scaladsl.extension.xml.PersonParser` in the test package of this library.

- v0.0.1 (2016-07-23)
  - Initial release

Have fun!

