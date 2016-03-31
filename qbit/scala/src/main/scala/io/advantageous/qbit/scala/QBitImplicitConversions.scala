package io.advantageous.qbit.scala

import java.util.function.Consumer

import io.advantageous.qbit.http.request.HttpResponseBuilder._
import io.advantageous.qbit.http.request._
import io.advantageous.qbit.reactive.Callback

object QBitImplicitConversions {

  /**
    * Converts a Scala function to a Runnable.
    *
    * @param func scala function
    * @return new Runnable
    */
  implicit def convertFunction2Runnable(func: () => Unit): Runnable = new Runnable() {
    def run() = func()
  }


  /**
    * Converts a Scala function to a Consumer.
    *
    * @param function scala function
    * @return new Consumer
    */
  implicit def convertFunction2Consumer[A](function: A => Unit): Consumer[A] = new Consumer[A]() {
    override def accept(arg: A): Unit = function.apply(arg)
  }


  /**
    * Convert a Scala function to a QBit Callback
    *
    * @param function scala function
    * @tparam A param A
    * @return QBit Callback
    */
  implicit def convertFunction2Callback[A](function: A => Unit): Callback[A] = new Callback[A]() {
    override def accept(arg: A): Unit = function.apply(arg)
  }


  /**
    * Convert a Scala function to a QBit HttpTextReceiver
    *
    * @param function function
    * @return http text receiver
    */
  implicit def convertFunc2HttpTextReceiver(function: HttpTextResponse => Unit): HttpTextReceiver = new HttpTextReceiver() {
    override def response(code: Int, contentType: String, body: String): Unit = {
      val builder = httpResponseBuilder()
      builder.setBody(body)
      builder.setContentType(contentType)
      builder.setCode(code)
      function.apply(builder.buildTextResponse())
    }
  }

  /**
    * Convert a Scala function to a QBit HttpBinaryReceiver
    *
    * @param function function
    * @return http text receiver
    */
  implicit def convertFunc2HttpBinaryReceiver(function: HttpBinaryResponse => Unit): HttpBinaryReceiver = new HttpBinaryReceiver() {
    override def response(code: Int, contentType: String, body: Array[Byte]): Unit = {
      val builder = httpResponseBuilder()
      builder.setBody(body)
      builder.setContentType(contentType)
      builder.setCode(code)
      function.apply(builder.buildBinaryResponse())
    }
  }


}