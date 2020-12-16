
//  val retries2: Retries = new Retries {
//    override protected val configuration: Option[Config] =
//      Some(
//        Configuration(
//          "http-verbs.retries.intervals" -> "[ 100 ms, 200 ms,  1 s]",
//        ).underlying
//      )
//    override val actorSystem: ActorSystem = ActorSystem("test-actor-system")
//  }
//
//  trait MyTrait extends Retries
//
//  val retries3: MyTrait = new MyTrait {
//    override val configuration: Option[Config] = None
//    override val actorSystem: ActorSystem = ActorSystem("test-actor-system")
//  }



// output:
//val retries2: Retries = new Retries {
//  override protected val configuration: Config =
//    Configuration(
//      "http-verbs.retries.intervals" -> "[ 100 ms, 200 ms,  1 s]",
//    ).underlying
//  override val actorSystem: ActorSystem = ActorSystem("test-actor-system")
//}
//
//trait MyTrait extends Retries
//
//val retries3: MyTrait = new MyTrait {
//  override val configuration: Config = ConfigFactory.load()
//  override val actorSystem: ActorSystem = ActorSystem("test-actor-system")
//}
//
