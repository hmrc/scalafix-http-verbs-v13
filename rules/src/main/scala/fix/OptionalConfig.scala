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
      // - https://scalacenter.github.io/scalafix/docs/developers/symbol-information.html#lookup-class-parents

    // find their usage
    // look for the config
      // if None, load from ConfigFactory (plus do the import)
      // if Some, remove Some.

//    println(doc.tree.syntax)
//    println(doc.tree.structure)

    doc.tree.collect {
      case t @ Defn.Val(_, List(Pat.Var(Term.Name("configuration"))), _, Term.Name("None")) =>
        val newTree = Defn.Val(
          t.mods,
          t.pats,
          decltpe = Some(Type.Name("Config")),
          rhs = Term.Apply(Term.Select(Term.Name("ConfigFactory"), Term.Name("load")), Nil)
        )

        Patch.addGlobalImport(importer"com.typesafe.config.ConfigFactory") +
          Patch.removeTokens(t.tokens) +
          Patch.addRight(t, newTree.syntax)

//      case t @ Term.Name(name) if name == "configuration" =>
//        println(name)
//
//        Patch.replaceTree(t, "config")
//      case t =>
//        val parents = getParentSymbols(t.symbol)
//        val lookFor = Set("uk/gov/hmrc/http/Retries#", "uk.gov.hmrc.http.Retries")
//        if (lookFor.exists(parents.map(_.value))) {
//          patchConfigurationVal(t)
//        }
    }.asPatch
  }

  // Lookup parent classes/traits
  // - we need this to identify traits that have extended the ones we are changing.
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

  private def patchConfigurationVal(t: Tree)(implicit doc: SemanticDocument): Patch = {
//    println(t)
    Patch.empty
  }

}