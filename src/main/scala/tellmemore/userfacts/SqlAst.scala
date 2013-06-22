package tellmemore.userfacts

import org.scala_tools.time.Imports._

sealed abstract class SqlAst {
  val sql: String
}

object SqlAst {
  case class IntersectionSql(queries: Seq[SqlAst]) extends SqlAst{
    val sql: String = queries map {"(" + _.sql + ")"} mkString " INTERSECT "
  }

  case class UnionSql(queries: Seq[SqlAst]) extends SqlAst{
    val sql: String = queries map {"(" + _.sql + ")"} mkString " UNION "
  }

  sealed trait SqlCondition extends SqlAst

  case class SqlNumericEquals(factId: Long, value: NumericFact, moment: DateTime) extends SqlCondition {
    val sql = makeSql(factId, moment, "numeric_value", value.value.toString, "=")
  }

  case class SqlNumericGreaterThen(factId: Long, value: NumericFact, moment: DateTime) extends SqlCondition {
    val sql = makeSql(factId, moment, "numeric_value", value.value.toString, ">")
  }

  case class SqlNumericLessThen(factId: Long, value: NumericFact, moment: DateTime) extends SqlCondition {
    val sql = makeSql(factId, moment, "numeric_value", value.value.toString, "<")
  }

  case class SqlStringEquals(factId: Long, value: StringFact, moment: DateTime) extends SqlCondition {
    val sql = makeSql(factId, moment, "string_value", s"'${value.value}'", "=")
  }

  private[this] def makeSql(factId: Long, moment: DateTime, column: String, sqlValue: String, operator: String) =
    s"""SELECT external_id FROM users WHERE id IN (
      WITH slice AS (SELECT * FROM fact_values values1 WHERE values1.fact_id=$factId
                     AND values1.tstamp <= ${moment.millis})
      SELECT DISTINCT values1.user_id FROM
        slice AS values1 LEFT JOIN slice AS values2
        ON values1.user_id = values2.user_id AND values1.tstamp < values2.tstamp
      WHERE values2.id IS NULL AND values1.$column $operator $sqlValue
    )"""
}
