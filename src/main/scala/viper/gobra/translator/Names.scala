// This Source Code Form is subject to the terms of the Mozilla Public
// License, v. 2.0. If a copy of the MPL was not distributed with this
// file, You can obtain one at http://mozilla.org/MPL/2.0/.
//
// Copyright (c) 2011-2020 ETH Zurich.

package viper.gobra.translator

import viper.silver.{ast => vpr}

object Names {
  def pointerFields(t: vpr.Type): String = s"val$$_$t"
  def returnLabel: String = "returnLabel"
  def fieldExtension(base: String, ext: String): String = s"${base}_$ext"

  def addressableField(base: String): String = s"${base}R"
  def nonAddressableField(base: String): String = s"${base}V"

  private var freshCounter = 0
  def freshName: String = {
    val str = s"fn$$$$$freshCounter"
    freshCounter += 1
    str
  }
}
