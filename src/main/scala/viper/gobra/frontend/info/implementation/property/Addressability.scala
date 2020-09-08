// This Source Code Form is subject to the terms of the Mozilla Public
// License, v. 2.0. If a copy of the MPL was not distributed with this
// file, You can obtain one at http://mozilla.org/MPL/2.0/.
//
// Copyright (c) 2011-2020 ETH Zurich.

package viper.gobra.frontend.info.implementation.property

import viper.gobra.ast.frontend._
import viper.gobra.frontend.info.base.SymbolTable.Variable
import viper.gobra.frontend.info.base.Type.{ArrayT, SliceT}
import viper.gobra.frontend.info.implementation.TypeInfoImpl
import viper.gobra.ast.frontend.{AstPattern => ap}

trait Addressability extends BaseProperty { this: TypeInfoImpl =>

  lazy val effAddressable: Property[PExpression] = createBinaryProperty("effective addressable") {
    case _: PCompositeLit => true
    case e => addressable(e)
  }

  lazy val goEffAddressable: Property[PExpression] = createBinaryProperty("effective addressable") {
    case _: PCompositeLit => true
    case e => goAddressable(e)
  }

  // depends on: entity, tipe
  lazy val addressable: Property[PExpression] = createBinaryProperty("addressable") {
    case PNamedOperand(id) => addressableVar(id)
    case n: PDeref => resolve(n).exists(_.isInstanceOf[ap.Deref])
    case PIndexedExp(b, _) => val bt = exprType(b); bt.isInstanceOf[SliceT] || (b.isInstanceOf[ArrayT] && addressable(b))
    case n: PDot => resolve(n) match {
      case Some(s: ap.FieldSelection) => goAddressable(s.base)
      case _ => false
    }
    case _ => false
  }

  lazy val goAddressable: Property[PExpression] = createBinaryProperty("addressable") {
    case PNamedOperand(id) => entity(id).isInstanceOf[Variable]
    case n: PDeref => resolve(n).exists(_.isInstanceOf[ap.Deref])
    case PIndexedExp(b, _) => val bt = exprType(b); bt.isInstanceOf[SliceT] || (b.isInstanceOf[ArrayT] && goAddressable(b))
    case n: PDot => resolve(n) match {
      case Some(s: ap.FieldSelection) => goAddressable(s.base)
      case _ => false
    }
    case _ => false
  }

  private lazy val addressableVarAttr: PIdnNode => Boolean =
    attr[PIdnNode, Boolean] { n => regular(n) match {
      case v: Variable => v.addressable
      case _ => false
    }}

  override def addressableVar(id: PIdnNode): Boolean = addressableVarAttr(id)
}
