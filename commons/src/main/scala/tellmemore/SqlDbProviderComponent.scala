package tellmemore

import java.sql.Connection

trait SqlDbProviderComponent {
  val DB: SqlDbProvider

  trait SqlDbProvider {
    def withConnection[A](block: Connection => A): A
  }
}
