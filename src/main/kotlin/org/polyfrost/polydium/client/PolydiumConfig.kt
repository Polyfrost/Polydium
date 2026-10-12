package org.polyfrost.polydium.client

import org.polyfrost.oneconfig.api.config.v1.Config
import org.polyfrost.oneconfig.api.config.v1.Tree
import org.polyfrost.oneconfig.api.config.v1.collect.impl.OneConfigCollector

object PolydiumConfig : Config("polydium.json", "Polydium", Category.PERFORMANCE) {
    override fun makeTree(): Tree = Tree.tree(id).also { OneConfigCollector().handle(it, PolydiumToggles(), 0) }
}
