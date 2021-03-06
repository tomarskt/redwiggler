package com.nike.redwiggler.core.models

import org.everit.json.schema.{ValidationException, Schema => JsSchema}

import collection.JavaConverters._
import org.json.{JSONException, JSONObject}

trait Schema {
  def validate(payload : String) : Option[ValidationFailure]
}

case class JsonSchema(schema : JsSchema) extends Schema {
  override def validate(payload: String): Option[ValidationFailure] = try {
    schema.validate(new JSONObject(payload))
    None
  } catch {
    case ve : ValidationException => Some(validationException2ValidationFailure(Seq())(ve))
    case jsonException : JSONException => Some(ValidationFailure(
      message = jsonException.getMessage,
      pointer = None,
      path = Seq()
    ))
  }

  private def validationException2ValidationFailure(seen : Seq[ValidationException])(cause : ValidationException) : ValidationFailure = ValidationFailure(
    message = cause.getErrorMessage,
    pointer = Option(cause.getPointerToViolation),
    path = cause.getCausingExceptions.asScala.map(validationException2ValidationFailure(seen ++ Seq(cause)))
  )
}

