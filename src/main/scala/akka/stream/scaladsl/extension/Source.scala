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

import akka.NotUsed

import scala.concurrent.Future
import scala.util.Try
import akka.stream.scaladsl.{ Source => S }

object Source {
  def fromTry[T](tr: Try[T]): S[T, NotUsed] = S.fromFuture(Future.fromTry(tr))
}
