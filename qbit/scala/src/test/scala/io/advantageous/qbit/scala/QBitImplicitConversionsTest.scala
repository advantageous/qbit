package io.advantageous.qbit.scala

import io.advantageous.qbit.http.request.{HttpBinaryResponse, HttpTextResponse, HttpRequestBuilder}
import io.advantageous.qbit.reactive.CallbackBuilder
import org.scalatest._

import io.advantageous.qbit.scala.QBitImplicitConversions._

class QBitImplicitConversionsTest extends FlatSpec with Matchers {



  "A Function" should "convert to a callback" in {
    CallbackBuilder.callbackBuilder().setCallback(classOf[String], (foo: String)=> {})
  }



  "A Function" should "convert to a runnable" in {
    CallbackBuilder.callbackBuilder().setOnTimeout(() => {})
  }


  "A Function" should "convert to a consumer" in {
    CallbackBuilder.callbackBuilder().setOnError((error: Throwable)=> {})
  }



  "A Function" should "convert to a http text receiver" in {
    HttpRequestBuilder.httpRequestBuilder().setTextReceiver((response: HttpTextResponse) => {})
  }


  "A Function" should "convert to a http binary receiver" in {
    HttpRequestBuilder.httpRequestBuilder().setBinaryReceiver((response: HttpBinaryResponse) => {})
  }


}
