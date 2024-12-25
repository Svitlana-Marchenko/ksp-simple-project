import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration

class MyAnnotationProcessor(
    private val environment: SymbolProcessorEnvironment,
): SymbolProcessor {

    private val logger = environment.logger

    private val codeGenerator = environment.codeGenerator

    override fun process(resolver: Resolver): List<KSAnnotated> {
        val symbols = resolver.getSymbolsWithAnnotation(MyAnnotation::class.qualifiedName!!)
        symbols.filterIsInstance<KSClassDeclaration>().forEach {
            val annotation = it.annotations.find { an -> an.shortName.asString() == MyAnnotation::class.qualifiedName!! }
            val value = annotation?.arguments?.find { arg -> arg.name?.asString() == "value" }?.value as? String ?: ""
            val version = annotation?.arguments?.find { arg -> arg.name?.asString() == "version" }?.value as? Int ?: 0
            val className = it.simpleName.asString()
            val packageName = it.packageName.asString()
            val file = codeGenerator.createNewFile(
                dependencies = Dependencies(false),
                packageName = packageName,
                fileName = "${className}Generated",
            )
            file.bufferedWriter().use { writer ->
                writer.write("package $packageName \n \n")
                writer.write("class ${className}Generated {\n")
                writer.write("  fun printName() = println(\"This is generated class for $className version $version\")\n")
                writer.write("  companion object {\n")
                writer.write("      fun create() = ${className}Generated()\n")
                writer.write("  }\n")
                writer.write("}\n")
            }
            logger.info("Generated file for $className for version $version")
        }
        return emptyList()
    }
}

class MyAnnotationProcessorProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
        return MyAnnotationProcessor(environment)
    }
}
