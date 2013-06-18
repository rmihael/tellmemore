package tellmemore.userfacts

import org.joda.time.DateTime

import tellmemore.{StringFact, NumericFact, FactValue}

sealed abstract class SqlAst {
  val sql: String
}

case class BasicSql(condition: SqlCondition) extends SqlAst {
  val sql = "SELECT DISTINCT users.external_id FROM fact_values JOIN users ON fact_values.user_id = users.id WHERE " + condition.sql
}
case class IntersectionSql(queries: Seq[SqlAst]) extends SqlAst{
  val sql: String = queries map {"(" + _.sql + ")"} mkString " INTERSECT "
}
case class UnionSql(queries: Seq[SqlAst]) extends SqlAst{
  val sql: String = queries map {"(" + _.sql + ")"} mkString " UNION "
}

case class SqlCondition(factId: Long, value: FactValue, moment: DateTime) extends SqlAst {
  val sql: String = value match {
    case NumericFact(v) => s"fact_values.fact_id=$factId AND fact_values.numeric_value=$v"
    case StringFact(v) => s"fact_values.fact_id=$factId AND fact_values.string_value='$v'"
  }
}
