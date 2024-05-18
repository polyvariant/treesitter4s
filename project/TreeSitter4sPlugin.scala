package org.polyvariant.treesitter4s.sbt

import sbt.AutoPlugin

import sbt.PluginTrigger

object TreeSitter4sPlugin extends AutoPlugin {
  override def trigger: PluginTrigger = noTrigger
}
