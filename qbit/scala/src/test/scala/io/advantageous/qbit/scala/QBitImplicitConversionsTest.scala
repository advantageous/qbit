package io.advantageous.qbit.scala

import io.advantageous.qbit.http.request.{HttpBinaryResponse, HttpRequestBuilder, HttpTextResponse}
import io.advantageous.qbit.reactive.CallbackBuilder
import io.advantageous.qbit.scala.QBitImplicitConversions._
import org.scalatest._

class QBitImplicitConversionsTest extends FlatSpec with Matchers {


  "A Function" should "convert to a callback" in {
    CallbackBuilder.newCallbackBuilder().withCallback(classOf[String], (foo: String) => {})
  }



  "A Function" should "convert to a runnable" in {
    CallbackBuilder.newCallbackBuilder().withTimeoutHandler(() => {})
  }


  "A Function" should "convert to a consumer" in {
    CallbackBuilder.newCallbackBuilder().withErrorHandler((error: Throwable) => {})
  }



  "A Function" should "convert to a http text receiver" in {
    HttpRequestBuilder.httpRequestBuilder().setTextReceiver((response: HttpTextResponse) => {})
  }


  "A Function" should "convert to a http binary receiver" in {
    HttpRequestBuilder.httpRequestBuilder().setBinaryReceiver((response: HttpBinaryResponse) => {})
  }


}
