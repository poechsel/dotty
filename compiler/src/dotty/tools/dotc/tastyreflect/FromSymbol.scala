package dotty.tools.dotc.tastyreflect

import dotty.tools.dotc.ast.tpd
import dotty.tools.dotc.core.Contexts.Context
import dotty.tools.dotc.core.Flags._
import dotty.tools.dotc.core.StdNames._
import dotty.tools.dotc.core.Symbols._
import dotty.tools.dotc.core.Types._

object FromSymbol {

  def definitionFromSym(sym: Symbol)(implicit ctx: Context): tpd.Tree = {
    if (sym.is(Package)) packageDefFromSym(sym)
    else if (sym == defn.AnyClass) tpd.EmptyTree // FIXME
    else if (sym == defn.NothingClass) tpd.EmptyTree // FIXME
    else if (sym.isClass) classDef(sym.asClass)
    else if (sym.isType) typeDefFromSym(sym.asType)
    else if (sym.is(Method)) defDefFromSym(sym.asTerm)
    else valDefFromSym(sym.asTerm)
  }

  def packageDefFromSym(sym: Symbol)(implicit ctx: Context): PackageDefinition = PackageDefinitionImpl(sym)

  def classDef(cls: ClassSymbol)(implicit ctx: Context): tpd.TypeDef = {
    val constrSym = cls.unforcedDecls.find(_.isPrimaryConstructor).orElse(
      // Dummy constructor for classes such as `<refinement>`
      ctx.newSymbol(cls, nme.CONSTRUCTOR, EmptyFlags, NoType)
    )
    val constr = tpd.DefDef(constrSym.asTerm)
    val parents = cls.classParents.map(tpd.TypeTree(_))
    val body = cls.unforcedDecls.filter(!_.isPrimaryConstructor).map(s => definitionFromSym(s))
    tpd.ClassDefWithParents(cls, constr, parents, body)
  }

  def typeDefFromSym(sym: TypeSymbol)(implicit ctx: Context): tpd.TypeDef = tpd.TypeDef(sym)

  def defDefFromSym(sym: TermSymbol)(implicit ctx: Context): tpd.DefDef = tpd.DefDef(sym)

  def valDefFromSym(sym: TermSymbol)(implicit ctx: Context): tpd.ValDef = tpd.ValDef(sym)

}