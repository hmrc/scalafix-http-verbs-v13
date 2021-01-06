/*
 * Copyright 2021 HM Revenue & Customs
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

class HeaderCarrier extends SemanticRule("HeaderCarrier") {

  val headerCarrierConverterExactMatcher =
    SymbolMatcher.exact("uk/gov/hmrc/play/HeaderCarrierConverter.")

  override def fix(implicit doc: SemanticDocument): Patch = {
    doc.tree.collect {
      case headerCarrierConverterImport@Importee.Name(Name("HeaderCarrierConverter")) =>
        Patch.removeImportee(headerCarrierConverterImport) +
          Patch.addGlobalImport(importer"uk.gov.hmrc.play.http.HeaderCarrierConverter")

      case headerCarrierConverterExactMatcher(headerCarrierConverter) =>
        headerCarrierConverter.ancestors.collectFirst {
          case headerCarrierDef@Defn.Def(mods, name, typeParams, params, /*returnType*/Some(Type.Name("HeaderCarrier")), body) =>
            val argName = params.flatten.collectFirst {
              case param@Term.Param(pMod, pName, Some(Type.Name("RequestHeader")), _) =>
                pName.value
            } getOrElse("requestHeader")

            val newTree = headerCarrierDef.copy(
              body = Term.Apply(
                Term.Select(Term.Name("HeaderCarrierConverter"), Term.Name("fromRequest")),
                List(Term.Name(argName))
              )
            )

            Patch.removeTokens(headerCarrierDef.tokens) +
              Patch.addRight(headerCarrierDef, newTree.syntax)

          case headerCarrierVal@Defn.Val(mod, valName, /*returnType*/Some(Type.Name("HeaderCarrier")), rhs) =>
            val argName = headerCarrierVal.rhs match {
              case app @ Term.Apply(_, List(pRH, _*)) =>
                pRH match {
                  case Term.Select(a, _) => a.toString()
                  case Term.Assign(_, Term.Select(a, _)) => a.toString()
                }
            }

            val newTree = headerCarrierVal.copy(
              decltpe = Some(Type.Name("HeaderCarrier")),
              rhs = Term.Apply(
                Term.Select(Term.Name("HeaderCarrierConverter"), Term.Name("fromRequest")),
                List(Term.Name(argName))
              )
            )

            Patch.removeTokens(headerCarrierVal.rhs.tokens) +
              Patch.addRight(headerCarrierVal.rhs, newTree.rhs.syntax)
        }.asPatch
    }.foldLeft(Patch.empty)(_ + _)
  }
}
