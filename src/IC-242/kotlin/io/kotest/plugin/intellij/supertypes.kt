package io.kotest.plugin.intellij

import org.jetbrains.kotlin.analysis.api.analyze
import org.jetbrains.kotlin.analysis.api.types.symbol
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.psi.KtClassOrObject

/**
 * Recursively returns the list of classes and interfaces extended or implemented by the class.
 */
fun KtClassOrObject.getAllSuperClasses(): List<FqName> = analyze(this) {
   superTypeListEntries.asSequence()
      .mapNotNull { it.typeReference }
      .mapNotNull {
         runCatching {
            it.type
         }.getOrNull()
      }
      .flatMap {
         runCatching {
            it.allSupertypes.toList() + it
         }.getOrElse { emptyList() }
      }
      .mapNotNull {
         runCatching {
            it.symbol?.classId
         }.getOrNull()
      }
      .mapNotNull {
         runCatching {
            val packageName = it.packageFqName
            val simpleName = it.relativeClassName
            FqName("$packageName.$simpleName")
         }.getOrNull()
      }
      .filterNot { it.toString() == "kotlin.Any" }
      .toList()
}
