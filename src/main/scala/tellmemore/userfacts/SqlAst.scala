package tellmemore.userfacts

import org.scala_tools.time.Imports._

sealed abstract class SqlAst {
  val sql: String
}

case class BasicSql(condition: SqlCondition) extends SqlAst {
  val sql = s"""SELECT external_id FROM users WHERE id IN (
  WITH slice AS (SELECT * FROM fact_values values1 WHERE values1.fact_id=${condition.factId}
                 AND values1.tstamp <= ${condition.moment.millis})
  SELECT DISTINCT values1.user_id FROM
    slice AS values1 LEFT JOIN slice AS values2
    ON values1.user_id = values2.user_id AND values1.tstamp < values2.tstamp
  WHERE values2.id IS NULL AND values1.${condition.column} = ${condition.sqlValue}
)"""
}
case class IntersectionSql(queries: Seq[SqlAst]) extends SqlAst{
  val sql: String = queries map {"(" + _.sql + ")"} mkString " INTERSECT "
}
case class UnionSql(queries: Seq[SqlAst]) extends SqlAst{
  val sql: String = queries map {"(" + _.sql + ")"} mkString " UNION "
}

case class SqlCondition(factId: Long, value: FactValue, moment: DateTime) {
  val column: String = value match {
    case NumericFact(_) => s"numeric_value"
    case StringFact(_) => s"string_value"
  }

  val sqlValue: String = value match {
    case NumericFact(v) => v.toString
    case StringFact(v) => s"'$v'"
  }
}
