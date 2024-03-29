package tellmemore.scripts

import org.scala_tools.time.Imports._
import scala.util.Random
import scala.collection.mutable
import scala.util.control.Breaks._

import tellmemore.{Client, Event}
import tellmemore.userfacts.FactValue
import tellmemore.users.{User, UserId, UserModel}
import tellmemore.clients.ClientModel
import tellmemore.events.EventModel
import tellmemore.userfacts.UserFactModel
import tellmemore.userfacts.UserFactValues

/**
 * Singleton that will actually generate the data.
 * All the data will be generated for Query object.
 * Idea behind the Generator - that he has a set of users that it knows
 * state of them and when he provided with query it will assign data to this
 * users objects in the way that it will satisfy this queries.
 *
 * Why this is so complex? We are thinking that having independent way of filling
 * data with params that we give is important to be able to test system and will be very
 * helpful especially on the first phases.
 */
class DataGenerator(userModel: UserModel, clientModel: ClientModel,
                    eventModel: EventModel, factModel: UserFactModel) {
  private var usersGenerated: Int = 0
  val random = new Random()
  private var clientId = ""

  // some internal state for
  var users: mutable.HashMap[UserId, User] = new mutable.HashMap[UserId, User]
  var usersRules: mutable.HashMap[UserId, mutable.MutableList[String]] = new mutable.HashMap[UserId, mutable.MutableList[String]]

  /**
   * Generates UserId object based on amount of generated users
   * @return
   */
  private def generateUserId(): UserId = {
    UserId(this.clientId, String.valueOf(this.usersGenerated))
  }

  /**
   * Generate unique User DO based on the amount of generated users before
   * @return
   */
  private def generateUser() : User = {
    val created = DateTime.now - this.random.nextInt(86400*7)
    val uid = generateUserId()
    this.usersGenerated += 1

    User(uid, s"User $uid", created)
  }

  /**
   * Saves sets of objects events and so on based on type of Set specified
   * @param objs
   *
   */
  private def saveObjSet(objs: Set[_]) {
    val firstVal = if (!objs.isEmpty) objs.head else ???
    firstVal match {
      case u: User => this.userModel.bulkInsert(objs.asInstanceOf[Set[User]])
      case e: Event => this.eventModel.bulkInsert(objs.asInstanceOf[Set[Event]])
      case f: ReallyConvenientFactObject => {
        this.factModel.setForUser(f.user.id, f.fact)
      }
    }

    println("Successfully saved resources: " + objs.toList.length.toString)

  }

  /**
   * Stores a rule match in internal state
   * @param users
   * @param rule_name
   */
  private def addRuleForUsers( users: Set[User], rule_name: String) {
    println("Adding rule " + rule_name + " for users")
    for (user <- users) {
      var rulesForUser = this.usersRules.getOrElse(user.id, new mutable.MutableList[String])
      rulesForUser += rule_name
      this.usersRules += user.id -> rulesForUser
    }
  }
  /**
   * Function saves users to db and return set of Users to work with
   * @param total
   *              amount of users to create
   */
  private def saveUsers(total: Int) : Set[User] = {
    val generated_users = ((1 to total) map {_ => generateUser()}).toSet
    saveObjSet(generated_users)
    for (user <- generated_users) {
      this.users += user.id -> user
    }
    generated_users
  }

  /**
   * Function decides to which users this rule match need to be saved.
   * For safety now it just pick users if they don't have this rule events before.
   * @param rule
   * @param usersNum
   * @return
   */
  private def getUsersForRule(rule: Rule[_], usersNum: Int) : Set[User] = {
    var users_for_rule : mutable.MutableList[User] = new mutable.MutableList[User]
    val userObjName = rule.getRelatedObjectName
    breakable {
      for ((key, value) <- this.usersRules) {
        if (users_for_rule.length == usersNum) break
        // if there are no events/facts with this name assigned to this user
        // then we are safe to generate for him more events
        if (!value.contains(userObjName)) {
          users_for_rule += this.users.getOrElse(key, generateUser())
        }
      }
    }

    val usersLeft = usersNum - users_for_rule.length
    if (usersLeft > 0) users_for_rule ++= saveUsers(usersLeft)

    users_for_rule.toSet
  }

  /**
   * Creates a client with needed id. Must be called before any gen starts
   * @param clientId
   */
  def createClient(clientId: String) {
    clientModel.create(Client(clientId, "Vasya Pup;in", DateTime.now))
    this.clientId = clientId
  }

  /**
   * Generates objects for Rule
   * @param rule
   *             rule that specify matches
   * @param usersNum
   *                 amount of users that need to be generated in order to get
   *                 query matched
   */
  def generateObjectsForRule(rule: Rule[_], usersNum: Int) {
    val users = getUsersForRule(rule, usersNum)
    for (user <- users) {
      saveObjSet(rule.generateRuleMatch(user))  // can be optimized gathering all objects to one
                                                // and make one huge bulk
    }
    addRuleForUsers(users, rule.getRelatedObjectName)

  }

}

/**
 * Represents basic rules that user will be able to use for search.
 * Examples "Total amount of event happened 'Event 1' less then 25"
 * "Give me users who has fact "Fact 1""
 * @tparam T
 */
abstract class Rule[T] {

  /**
   * Returns a name of object to which this user is related
   * @return
   */
  def getRelatedObjectName : String

  /**
   * Generates sequence of objects that need to be put inside DB
   * to match the rule
   * @param user
   *             user for which this need to be generated
   * @return
   *         Sequence of objects generated for this event
   */
  def generateRuleMatch(user: User) : Set[T]
}

/**
 * Rule that represents any number characteristic of event
 * @param event_name
 *                   name of event to save
 * @param operator
 *                 operator to compare - available: ==, <=, >=, >, <.
 *                 Note that operator must be in underscores. E.g. '_>=_'
 * @param total
 *              number to which compare amount of events with operator
 */
class TotalEventRule(event_name: String, operator: (Int, Int) => Boolean, total: Int) extends Rule[Event] {
  val name : String = event_name
  val compare_func: (Int, Int) => Boolean = operator
  val threshold: Int = total
  val random: Random = new Random()

  def getRelatedObjectName : String = this.name

  private def generateRandomEvent(userId: UserId) : Event = {
    Event(userId, this.name, DateTime.now - random.nextInt(86000).seconds)  // TODO add time variety
  }

  private def getMatchSize : Int = {
    var i = 1
    while (!this.compare_func(i, this.threshold)) {
      i += 1
    }
    i
  }

  def generateRuleMatch(user: User) : Set[Event] = {
    val res = List.tabulate(this.getMatchSize) {_ => generateRandomEvent(user.id)}
    res.toSet
  }
}

/**
 * Represents numeric fact value for user
 * @param fact_name
 * @param operator
 * @param threshold
 */
class IntFactRule(fact_name: String, operator: (Int, Int) => Boolean, threshold: Int) extends Rule[ReallyConvenientFactObject] {
  /**
   * Returns a name of object to which this user is related
   * @return
   */
  def getRelatedObjectName: String = fact_name

  private def getFactValue : Int = {
    var i = 1
    while (!this.operator(i, this.threshold)) {
      i += 1
    }
    i
  }
  /**
   * Generates sequence of objects that need to be put inside DB
   * to match the rule
   * @param user
   * user for which this need to be generated
   * @return
   * Sequence of objects generated for this event
   */
  def generateRuleMatch(user: User): Set[ReallyConvenientFactObject] = {
    val s = ReallyConvenientFactObject(user, Map[String, FactValue](this.fact_name -> FactValue(this.getFactValue) ))
    Set[ReallyConvenientFactObject](s)
  }
}


/**
 * Represents string fact value for user.
 * It will just translate facts with different values for user. If you want to create not_contains rule,
 * just create fact with name that will not_contain your name
 */
class StringFactRule(fact_name: String, fact_value: String) extends Rule[ReallyConvenientFactObject] {
  /**
   * Returns a name of object to which this user is related
   * @return
   */
  def getRelatedObjectName: String = fact_name

  /**
   * Generates sequence of objects that need to be put inside DB
   * to match the rule
   * @param user
   * user for which this need to be generated
   * @return
   * Sequence of objects generated for this event
   */
  def generateRuleMatch(user: User): Set[ReallyConvenientFactObject] = {
    Set[ReallyConvenientFactObject](ReallyConvenientFactObject(user, Map[String, FactValue](this.fact_name -> FactValue(this.fact_value))))
  }
}

/**
 * Never ask why this born. Just use it.
 * @param user
 * @param fact
 */
case class ReallyConvenientFactObject(user: User, fact: UserFactValues)


object FakeDataGenerator {

  /**
   * Generates a data by rules specified.
   * Modify this function to get the needed set for you. Create similar methods
   * for different data sets.
   */
  def generate(dataGenerator: DataGenerator) {
    val clientId = "bestclient@example.com"
    dataGenerator.createClient(clientId)
    val rule1 = new TotalEventRule("Cool event", _>_, 30)
    val rule2 = new TotalEventRule("Cool event", _<_, 5)
    val rule3 = new TotalEventRule("Second event", _>=_, 100)
    dataGenerator.generateObjectsForRule(rule1, 5)
    dataGenerator.generateObjectsForRule(rule2, 5)
    dataGenerator.generateObjectsForRule(rule3, 10)
    dataGenerator.generateObjectsForRule(new IntFactRule("Fact user", _>=_, 25), 25)
    dataGenerator.generateObjectsForRule(new StringFactRule("String fact", "cool value"), 50)
  }

  def generateEvents(dataGenerator: DataGenerator, clientId: String, eventsMap: Map[String, Int]) {
    dataGenerator.createClient(clientId)
    eventsMap.foreach { case (name, amount) =>
      dataGenerator.generateObjectsForRule(new TotalEventRule(name, _==_, amount), 1) }
  }

  def main(args: Array[String]) {
  }

}
