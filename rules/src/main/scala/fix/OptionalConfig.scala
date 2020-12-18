package fix

import scalafix.v1._

import scala.meta._
import scala.meta.contrib._

class OptionalConfig extends SemanticRule("OptionalConfig") {

  // Example in the wild:
  // https://github.com/hmrc/rate-scheduling/blob/d977b6007a6ec6529f2d6f9883cb6c73d8d164ef/app/config/RateHttpGet.scala

  override def fix(implicit doc: SemanticDocument): Patch = {
    // which traits have changed?
    //WSRequestBuilder
    //Retries
    // Which traits then extend from these traits?

    doc.tree.collect {
      case Template(_, inits, _, bodyStatements) if matchingTraits(inits) =>
        bodyStatements
          .map(x => convertConfigurationVals(x.tree))
          .foldLeft(Patch.empty)(_ + _)
    }.asPatch
  }

  def matchingTraits(inits: List[Init])(implicit doc: SemanticDocument): Boolean = {
    val symbols = inits.map(_.symbol)
    val parents = symbols.flatMap(getParentSymbols)

    (symbols ++ parents).map(_.value)
      .contains("uk/gov/hmrc/http/Retries#")
  }

  private def convertConfigurationVals(tree: Tree): Patch = {
    tree.collect {
      case t@Defn.Val(_, List(Pat.Var(Term.Name("configuration"))), _, Term.Name("None")) =>
        val newTree = t.copy(
          decltpe = Some(Type.Name("Config")),
          rhs = Term.Apply(Term.Select(Term.Name("ConfigFactory"), Term.Name("load")), Nil)
        )
        Patch.removeTokens(t.tokens) +
          Patch.addRight(t, newTree.syntax) +
          Patch.addGlobalImport(importer"com.typesafe.config.ConfigFactory")

      case t@Defn.Val(_, List(Pat.Var(Term.Name("configuration"))), _, Term.Apply(Term.Name("Some"), someArgs)) =>
        val newTree = t.copy(
          decltpe = Some(Type.Name("Config")),
          rhs = someArgs.head
        )
        Patch.removeTokens(t.tokens) + Patch.addRight(t, newTree.syntax)

    }.asPatch
  }

  // Lookup parent classes/traits
  // - we need this to identify traits that have extended the ones we are changing.
  // - https://scalacenter.github.io/scalafix/docs/developers/symbol-information.html#lookup-class-parents
  private def getParentSymbols(symbol: Symbol)(implicit doc: SemanticDocument): Set[Symbol] = {
    symbol.info match {
      case None => Set.empty
      case Some(symbolInfo) =>
        symbolInfo.signature match {
          case ClassSignature(_, parents, _, _) =>
            Set(symbol) ++ parents.collect {
              case TypeRef(_, symbol, _) => getParentSymbols(symbol)
            }.flatten
          case _ =>
            Set.empty
        }
    }
  }
}
