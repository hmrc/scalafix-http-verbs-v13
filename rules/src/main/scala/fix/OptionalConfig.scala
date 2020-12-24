/*
 * Copyright 2020 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package fix

import scalafix.v1._

import scala.meta._
import scala.meta.contrib._

class OptionalConfig extends SemanticRule("OptionalConfig") {

  override def fix(implicit doc: SemanticDocument): Patch = {
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
      case t@Defn.Def(_, Term.Name("configuration"), _, _, _, Term.Name("None")) =>
        val newTree = t.copy(
          decltpe = Some(Type.Name("Config")),
          body = Term.Apply(Term.Select(Term.Name("ConfigFactory"), Term.Name("load")), Nil)
        )
        Patch.removeTokens(t.tokens) +
          Patch.addRight(t, newTree.syntax) +
          Patch.addGlobalImport(importer"com.typesafe.config.ConfigFactory")

      case t@Defn.Val(_, Pat.Var(Term.Name("configuration")) :: Nil, _, Term.Name("None")) =>
        val newTree = t.copy(
          decltpe = Some(Type.Name("Config")),
          rhs = Term.Apply(Term.Select(Term.Name("ConfigFactory"), Term.Name("load")), Nil)
        )
        Patch.removeTokens(t.tokens) +
          Patch.addRight(t, newTree.syntax) +
          Patch.addGlobalImport(importer"com.typesafe.config.ConfigFactory")

      case t@Defn.Def(_, Term.Name("configuration"), _, _, _, Term.Apply(Term.Name("Some"), someArgs)) =>
        val newTree = t.copy(
          decltpe = Some(Type.Name("Config")),
          body = someArgs.head
        )
        Patch.removeTokens(t.tokens) + Patch.addRight(t, newTree.syntax)

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
