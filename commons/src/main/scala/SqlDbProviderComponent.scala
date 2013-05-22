import java.sql.Connection

trait SqlDbProviderComponent {
  val DB: SqlDbProviderComponent

  trait DBProvider {
    def withConnection[A](block: Connection => A): A
  }
}
