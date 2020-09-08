package viper.gobra.ast.frontend

import org.bitbucket.inkytonik.kiama
import viper.gobra.ast.printing.PrettyPrinterCombinators

trait PrettyPrinter {
  def format(node: PNode): String
}

class DefaultPrettyPrinter extends PrettyPrinter with kiama.output.PrettyPrinter with PrettyPrinterCombinators {

  override val defaultIndent = 2
  override val defaultWidth = 80

  override def format(node: PNode): String = pretty(show(node)).layout

  def show(node: PNode): Doc = node match {
    case n: PPackage => showPackage(n)
    case n: PProgram => showProgram(n)
    case n: PPackageClause => showPackageClause(n)
    case n: PImport => showImport(n)
    case n: PMember => showMember(n)
    case n: PStatement => showStmt(n)
    case n: PExpression => showExpr(n)
    case n: PSpecification => showSpec(n)
    case n: PType => showType(n)
    case n: PIdnNode => showId(n)
    case n: PLabelNode => showLabel(n)
    case n: PPackegeNode => showPackageId(n)
    case n: PMisc => showMisc(n)

    case n: PAssOp => showAssOp(n)
    case n: PLiteralValue => showLiteralValue(n)
    case n: PLiteralType => showLiteralType(n)
    case n: PCompositeKey => showCompositeKey(n)
    case n: PKeyedElement => showKeyedElement(n)

    case n: PIfClause => showIfClause(n)
    case n: PExprSwitchClause => showExprSwitchClause(n)
    case n: PTypeSwitchClause => showTypeSwitchClause(n)
    case n: PSelectClause => showSelectClause(n)

    case n: PStructClause => showStructClause(n)
    case n: PFieldDecl => showFieldDecl(n)
    case n: PInterfaceClause => showInterfaceClause(n)

    case PPos(_) => emptyDoc
  }

  // entire package

  def showPackage(p: PPackage): Doc =
    ssep(p.programs map showProgram, line) <> line

  // program

  def showProgram(p: PProgram): Doc = p match {
    case PProgram(packageClause, imports, declarations) =>
      showPackageClause(packageClause) <> line <> line <>
        ssep(imports map showImport, line) <> line <>
        ssep(declarations map showMember, line <> line) <> line
  }

  // package

  def showPackageClause(node: PPackageClause): Doc = "package" <+> showPackageId(node.id)
  def showPackageId(id: PPackegeNode): Doc = id.name

  // imports

  def showImport(decl: PImport): Doc = decl match {
    case PExplicitQualifiedImport(PWildcard(), pkg) => "import" <+> "_" <+> pkg
    case PExplicitQualifiedImport(qualifier, pkg) => "import" <+> showId(qualifier) <+> pkg
    case PImplicitQualifiedImport(pkg) => "import" <+> pkg
    case PUnqualifiedImport(pkg) => "import" <+> "." <+> pkg
  }

  // members

  def showMember(mem: PMember): Doc = mem match {
    case mem: PActualMember => mem match {
      case n: PConstDecl => showConstDecl(n)
      case n: PVarDecl => showVarDecl(n)
      case n: PTypeDecl => showTypeDecl(n)
      case PFunctionDecl(id, args, res, spec, body) =>
        showSpec(spec) <> "func" <+> showId(id) <> parens(showParameterList(args)) <> showResult(res) <> opt(body)(b => space <> showStmt(b))
      case PMethodDecl(id, rec, args, res, spec, body) =>
        showSpec(spec) <> "func" <+> showReceiver(rec) <+> showId(id) <> parens(showParameterList(args)) <> showResult(res) <>
        opt(body)(b => space <> showStmt(b))
    }
    case member: PGhostMember => member match {
      case PExplicitGhostMember(m) => "ghost" <+> showMember(m)
      case PFPredicateDecl(id, args, body) =>
        "pred" <+> showId(id) <> parens(showParameterList(args)) <> opt(body)(b => space <> block(showExpr(b)))
      case PMPredicateDecl(id, recv, args, body) =>
        "pred" <+> showReceiver(recv) <+> showId(id) <> parens(showParameterList(args)) <> opt(body)(b => space <> block(showExpr(b)))
    }
  }

  def showPure: Doc = "pure" <> line
  def showPre(pre: PExpression): Doc = "requires" <+> showExpr(pre)
  def showPost(post: PExpression): Doc = "ensures" <+> showExpr(post)
  def showInv(inv: PExpression): Doc = "invariant" <+> showExpr(inv)

  def showSpec(spec: PSpecification): Doc = spec match {
    case PFunctionSpec(pres, posts, isPure) =>
      (if (isPure) showPure else emptyDoc) <>
      hcat(pres map (showPre(_) <> line)) <>
        hcat(posts map (showPost(_) <> line))

    case PLoopSpec(inv) =>
      hcat(inv map (showInv(_) <> line))
  }

  def showNestedStmtList[T <: PStatement](list: Vector[T]): Doc = sequence(ssep(list map showStmt, line))
  def showStmtList[T <: PStatement](list: Vector[T]): Doc = ssep(list map showStmt, line)
  def showParameterList[T <: PParameter](list: Vector[T]): Doc = showList(list)(showParameter)
  def showExprList[T <: PExpression](list: Vector[T]): Doc = showList(list)(showExpr)
  def showTypeList[T <: PType](list: Vector[T]): Doc = showList(list)(showType)
  def showIdList[T <: PIdnNode](list: Vector[T]): Doc = showList(list)(showId)

  def showList[T](list: Vector[T])(f: T => Doc): Doc = ssep(list map f, comma <> space)

  def showVarDecl(decl: PVarDecl): Doc = decl match {
    case PVarDecl(typ, right, left, addressable) =>
      "var" <+> showList(left zip addressable){ case (v, a) => showAddressable(a, v) } <> opt(typ)(space <> showType(_)) <+> "=" <+> showExprList(right)
  }

  def showConstDecl(decl: PConstDecl): Doc = decl match {
    case PConstDecl(typ, right, left) => "const" <+> showIdList(left) <> opt(typ)(space <> showType(_)) <+> "=" <+> showExprList(right)
  }

  def showTypeDecl(decl: PTypeDecl): Doc = decl match {
    case PTypeDef(right, left) => "type" <+> showId(left) <+> showType(right)
    case PTypeAlias(right, left) => "type" <+> showId(left) <+> "=" <+> showType(right)
  }

  def showParameter(para: PParameter): Doc = para match {
    case PExplicitGhostParameter(p) => "ghost" <+> showParameter(p)
    case PUnnamedParameter(typ) => showType(typ)
    case PNamedParameter(id, typ, addressable) => showAddressable(addressable, id) <+> showType(typ)
  }

  def showReceiver(rec: PReceiver): Doc = rec match {
    case PNamedReceiver(id, typ, addressable) => parens(showAddressable(addressable, id) <+> showType(typ))
    case PUnnamedReceiver(typ) => parens(showType(typ))
  }

  def showResult(res: PResult): Doc = res match {
    case PResult(outs) => space <> parens(showParameterList(outs))
  }

  def showAddressable(addressable: Boolean, id: PIdnNode): Doc =
    (if (addressable) "!" else "") <> showId(id)

  // statements

  def showStmt(stmt: PStatement): Doc = stmt match {
    case stmt: PActualStatement => stmt match {
      case n: PConstDecl => showConstDecl(n)
      case n: PVarDecl => showVarDecl(n)
      case n: PTypeDecl => showTypeDecl(n)
      case PShortVarDecl(right, left, addressable) =>
        showList(left zip addressable){ case (l, a) => showAddressable(a, l) } <+> ":=" <+> showExprList(right)
      case PLabeledStmt(label, s) => showId(label) <> ":" <+> showStmt(s)
      case PEmptyStmt() => emptyDoc
      case PExpressionStmt(exp) => showExpr(exp)
      case PSendStmt(channel, msg) => showExpr(channel) <+> "<-" <+> showExpr(msg)
      case PAssignment(right, left) => showExprList(left) <+> "=" <+> showExprList(right)
      case PAssignmentWithOp(right, op, left) => showExpr(left) <+> showAssOp(op) <> "=" <+> showExpr(right)
      case PIfStmt(ifs, els) =>
        ssep(ifs map showIfClause, line) <>
          opt(els)(space <> "else" <+> showStmt(_) <> line)
      case PExprSwitchStmt(pre, exp, cases, dflt) =>
        "switch" <> showPreStmt(pre) <+> block(
          ssep(cases map showExprSwitchClause, line) <>
            ssep(dflt map { d =>
              "default"  <> ":" <> showNestedStmtList(d.stmts)
            }, line)
        )
      case PTypeSwitchStmt(pre, exp, binder, cases, dflt) =>
        "switch" <> showPreStmt(pre) <> opt(binder)(space <> showId(_) <+> ":=") <+> showExpr(exp) <> ".(type)" <+> block(
          ssep(cases map showTypeSwitchClause, line)) <>
          ssep(dflt map {d => "default"  <> ":" <> showNestedStmtList(d.stmts) }, line)
      case PForStmt(pre, cond, post, spec, body) => showSpec(spec) <> ((pre, cond, post) match {
        case (None, PBoolLit(true), None) =>  "for" <+> showStmt(body)
        case (None, _, None) => "for" <+> showExpr(cond) <+> showStmt(body)
        case _ => "for" <+> opt(pre)(showStmt) <> ";" <+> showExpr(cond) <> ";" <+> opt(post)(showStmt) <+> showStmt(body)
      })
      case PAssForRange(range, ass, body) =>
        "for" <+> showExprList(ass) <+> "=" <+> showRange(range) <+> block(showStmt(body))
      case PShortForRange(range, shorts, body) =>
        "for" <+> showIdList(shorts) <+> ":=" <+> showRange(range) <+> block(showStmt(body))
      case PGoStmt(exp) => "go" <+> showExpr(exp)
      case PSelectStmt(send, rec, aRec, sRec, dflt) =>
        "select" <+> block(
          ssep(send map showSelectClause, line) <>
            ssep(rec map showSelectClause, line) <>
            ssep(aRec map showSelectClause, line) <>
            ssep(sRec map showSelectClause, line) <>
            ssep(dflt map showSelectClause, line)
        )
      case PReturn(exps) => "return" <+> showExprList(exps)
      case PBreak(label) => "break" <> opt(label)(space <> showLabel(_))
      case PContinue(label) => "continue" <> opt(label)(space <> showLabel(_))
      case PGoto(label) => "goto" <+> showLabel(label)
      case PDeferStmt(exp) => "defer" <+> showExpr(exp)
      case PBlock(stmts) => block(showStmtList(stmts))
      case PSeq(stmts) => showStmtList(stmts)
    }
    case statement: PGhostStatement => statement match {
      case PExplicitGhostStatement(actual) => "ghost" <+> showStmt(actual)
      case PAssert(exp) => "assert" <+> showExpr(exp)
      case PAssume(exp) => "assume" <+> showExpr(exp)
      case PExhale(exp) => "exhale" <+> showExpr(exp)
      case PInhale(exp) => "inhale" <+> showExpr(exp)
      case PUnfold(exp) => "unfold" <+> showExpr(exp)
      case PFold(exp) => "fold" <+> showExpr(exp)
    }
  }

  def showPreStmt(n: Option[PSimpleStmt]): Doc = opt(n)(space <> showStmt(_) <> ";")

  def showAssOp(n: PAssOp): Doc = n match {
    case PAddOp() => "+"
    case PSubOp() => "-"
    case PMulOp() => "*"
    case PDivOp() => "/"
    case PModOp() => "%"
  }

  def showIfClause(n: PIfClause): Doc = n match {
    case PIfClause(pre, condition, body) => "if" <> showPreStmt(pre) <+> showExpr(condition) <+> showStmt(body)
  }

  def showExprSwitchClause(n: PExprSwitchClause): Doc = n match {
    case PExprSwitchDflt(body) => "default"  <> ":" <> showNestedStmtList(body.stmts)
    case PExprSwitchCase(left, body) => "case" <+> showExprList(left) <> ":" <> showNestedStmtList(body.stmts)
  }

  def showTypeSwitchClause(n: PTypeSwitchClause): Doc = n match {
    case PTypeSwitchDflt(body) => "default"  <> ":" <> showNestedStmtList(body.stmts)
    case PTypeSwitchCase(left, body) => "case" <+> showTypeList(left) <> ":" <> sequence(ssep(body.stmts map showStmt, line))
  }

  def showSelectClause(n: PSelectClause): Doc = n match {
    case PSelectDflt(body) =>
      "default" <> ":" <> showNestedStmtList(body.stmts)
    case PSelectSend(send, body) =>
      "case" <+> showStmt(send) <> ":" <> showNestedStmtList(body.stmts)
    case PSelectRecv(recv, body) =>
      "case" <+> showExpr(recv) <> ":" <> showNestedStmtList(body.stmts)
    case PSelectAssRecv(recv, ass, body) =>
      "case" <+> showExprList(ass) <+> "=" <+> showExpr(recv) <> ":" <> showNestedStmtList(body.stmts)
    case PSelectShortRecv(recv, shorts, body) =>
      "case" <+> showIdList(shorts) <+> "=" <+> showExpr(recv) <> ":" <> showNestedStmtList(body.stmts)
  }

  def showRange(n: PRange): Doc = "range" <+> showExpr(n.exp)

  // expressions

  def showExprOrType(expr: PExpressionOrType): Doc = expr match {
    case expr: PExpression => expr match {
      case _: PReference => parens(showExpr(expr))
      case _ => showExpr(expr)
    }
    case typ: PType => showType(typ)
  }


  def showMultSubExpr(expr: PExpression): Doc = expr match {
    case _: PAdd => parens(showExpr(expr))
    case _: PSub => parens(showExpr(expr))
    case _ => showExpr(expr)
  }

  def showSubtractionSubExpr(expr: PExpression): Doc = expr match {
    case _: PAdd => parens(showExpr(expr))
    case _ => showExpr(expr)
  }

  def showDivSubExpr(expr: PExpression): Doc = expr match {
    case _: PAdd => parens(showExpr(expr))
    case _: PSub => parens(showExpr(expr))
    case _: PMul => parens(showExpr(expr))
    case _ => showExpr(expr)
  }

  /**
    * Precedence of expressions.
    */
  object Precedence extends Enumeration {
    val p1 = Value(1)
    val p1P5 = Value(2)
    val p2 = Value(3)
    val p3 = Value(4)
    val p4 = Value(5)
    val p5 = Value(6)
    val p6 = Value(7)
    val p7 = Value(8)
  }

  def getPrecedence(expr: PExpression): Precedence.Value = expr match {
    case _: PConditional => Precedence.p1
    case _: PImplication => Precedence.p1P5
    case _: POr => Precedence.p2
    case _: PAnd => Precedence.p3
    case _: PEquals | _: PUnequals | _: PLess | _: PAtMost | _: PGreater | _: PAtLeast => Precedence.p4
    case _: PAdd | _: PSub => Precedence.p5
    case _: PMul | _: PDiv | _: PMod => Precedence.p6
    case _ => Precedence.p7
  }

  def showSubExpr(expr: PExpression, subExpr: PExpression): Doc = {
    val exprPrecedence = getPrecedence(expr)
    val subExprPrecedence = getPrecedence(subExpr)

    if (subExprPrecedence < exprPrecedence)
      parens(showExpr(subExpr))
    else
      showExpr(subExpr)
  }


  def showExpr(expr: PExpression): Doc = expr match {
    case expr: PActualExpression => expr match {
      case PReceive(operand) => "<-" <> showExpr(operand)
      case PReference(operand) => "&" <> showExpr(operand)
      case PDeref(base) => "*" <> showExprOrType(base)
      case PDot(base, id) => showExprOrType(base) <> "." <>  showId(id)
      case PNegation(operand) => "!" <> showExpr(operand)
      case PNamedOperand(id) => showId(id)
      case PBoolLit(lit) => if(lit) "true" else "false"
      case PIntLit(lit) => lit.toString
      case PNilLit() => "nil"
      case PCompositeLit(typ, lit) => showLiteralType(typ) <+> showLiteralValue(lit)
      case PFunctionLit(args, result, body) =>
        "func" <> parens(showParameterList(args)) <> showResult(result) <> block(showStmt(body))
      case PInvoke(base, args) => showExprOrType(base) <> parens(showExprList(args))
      case PIndexedExp(base, index) => showExpr(base) <> brackets(showExpr(index))
      case PSliceExp(base, low, high, cap) => (low, high, cap) match {
        case (l, h, None)    => showExpr(base) <> brackets(showExprList(Vector(l, h)))
        case (l, h, Some(c)) => showExpr(base) <> brackets(showExprList(Vector(l, h, c)))
      }
      case PTypeAssertion(base, typ) => showExpr(base) <> "." <> parens(showType(typ))
      case PEquals(left, right) => showSubExpr(expr, left) <+> "==" <+> showSubExpr(expr, right)
      case PUnequals(left, right) => showSubExpr(expr, left) <+> "!=" <+> showSubExpr(expr, right)
      case PAnd(left, right) => showSubExpr(expr, left) <+> "&&" <+> showSubExpr(expr, right)
      case POr(left, right) => showSubExpr(expr, left) <+> "||" <+> showSubExpr(expr, right)
      case PLess(left, right) => showSubExpr(expr, left) <+> "<" <+> showSubExpr(expr, right)
      case PAtMost(left, right) => showSubExpr(expr, left) <+> "<=" <+> showSubExpr(expr, right)
      case PGreater(left, right) => showSubExpr(expr, left) <+> ">" <+> showSubExpr(expr, right)
      case PAtLeast(left, right) => showSubExpr(expr, left) <+> ">=" <+> showSubExpr(expr, right)
      case PAdd(left, right) => showSubExpr(expr, left) <+> "+" <+> showSubExpr(expr, right)
      case PSub(left, right) => showSubExpr(expr, left) <+> "-" <+> showSubExpr(expr, right)
      case PMul(left, right) => showSubExpr(expr, left) <+> "*" <+> showSubExpr(expr, right)
      case PMod(left, right) => showSubExpr(expr, left) <+> "%" <+> showSubExpr(expr, right)
      case PDiv(left, right) => showSubExpr(expr, left) <+> "/" <+> showSubExpr(expr, right)
      case PUnfolding(acc, op) => "unfolding" <+> showExpr(acc) <+> "in" <+> showExpr(op)
    }
    case expr: PGhostExpression => expr match {
      case POld(op) => "old(" <> showExpr(op) <> ")"
      case PConditional(cond, thn, els) => showSubExpr(expr, cond) <> "?" <> showSubExpr(expr, thn) <> ":" <> showSubExpr(expr, els)
      case PForall(vars, triggers, body) =>
        "forall" <+> showList(vars)(showMisc) <+> "::" <+> showList(triggers)(showMisc) <+> showExpr(body)
      case PExists(vars, triggers, body) =>
        "exists" <+> showList(vars)(showMisc) <+> "::" <+> showList(triggers)(showMisc) <+> showExpr(body)
      case PImplication(left, right) => showSubExpr(expr, left) <+> "==>" <+> showSubExpr(expr, right)
      case PAccess(exp) => "acc" <> parens(showExpr(exp))
      case PPredicateAccess(exp) => exp match {
        case n: PInvoke => showExpr(n)
        case n: PExpression => "acc" <> parens(showExpr(n))
      }
    }
  }

  def showLiteralType(typ: PLiteralType): Doc = typ match {
    case t: PType => showType(t)
    case PImplicitSizeArrayType(elem) => "[...]" <> showType(elem)
  }

  def showCompositeKey(n: PCompositeKey): Doc = n match {
    case PIdentifierKey(id) => showId(id)
    case cv: PCompositeVal => showCompositeVal(cv)
  }

  def showCompositeVal(n: PCompositeVal): Doc = n match {
    case PExpCompositeVal(exp) => showExpr(exp)
    case PLitCompositeVal(l) => showLiteralValue(l)
  }

  def showLiteralValue(lit: PLiteralValue): Doc = braces(ssep(lit.elems map showKeyedElement, comma))

  def showKeyedElement(n: PKeyedElement): Doc = n match {
    case PKeyedElement(key, exp) => opt(key)(showCompositeKey(_) <> ":") <+> showCompositeVal(exp)
  }

  // types

  def showType(typ: PType): Doc = typ match {
    case actualType: PActualType => actualType match {
      case PNamedOperand(id) => showId(id)
      case PBoolType() => "bool"
      case PIntType() => "int"
      case PArrayType(len, elem) => brackets(showExpr(len)) <> showType(elem)
      case PSliceType(elem) => brackets(emptyDoc) <> showType(elem)
      case PMapType(key, elem) => "map" <> brackets(showType(key)) <> showType(elem)
      case PDeref(base) => "*" <> showExprOrType(base)
      case PDot(base, id) => showExprOrType(base) <> "." <>  showId(id)
      case channelType: PChannelType => channelType match {
        case PBiChannelType(elem)   => "chan" <+> showType(elem)
        case PSendChannelType(elem) => "<-" <> "chan" <+> showType(elem)
        case PRecvChannelType(elem) => "chan" <> "<-" <+> showType(elem)
      }
      case PStructType(clauses) => "struct" <+> block(ssep(clauses map showStructClause, line))
      case PFunctionType(args, result) => "func" <> parens(showParameterList(args)) <> showResult(result)
      case PInterfaceType(embedded, mspec, pspec) =>
        "interface" <+> block(
          ssep(embedded map showInterfaceClause, line) <>
            ssep(mspec map showInterfaceClause, line) <>
            ssep(pspec map showInterfaceClause, line)
        )
      case PMethodReceiveName(t) => showType(t)
      case PMethodReceivePointer(t) => "*" <> showType(t)
    }
    case ghostType: PGhostType => ???
  }

  def showStructClause(c: PStructClause): Doc = c match {
    case clause: PActualStructClause => clause match {
      case PFieldDecls(fields) =>
        require(fields.nonEmpty && fields.forall(_.typ == fields.head.typ))
        showIdList(fields map (_.id)) <+> showType(fields.head.typ)
      case PEmbeddedDecl(typ, _) => showEmbeddedType(typ)
    }
    case PExplicitGhostStructClause(actual) => showStructClause(actual)
  }

  def showFieldDecl(f: PFieldDecl): Doc = f match {
    case PFieldDecl(id, typ) => showId(id) <+> showType(typ)
  }

  def showEmbeddedType(t: PEmbeddedType): Doc = t match {
    case PEmbeddedName(typ) => showType(typ)
    case PEmbeddedPointer(typ) => "*" <> showType(typ)
  }

  def showInterfaceClause(n: PInterfaceClause): Doc = n match {
    case PInterfaceName(typ) => showType(typ)
    case PMethodSig(id, args, result) => showId(id) <> parens(showParameterList(args)) <> showResult(result)
    case PMPredicateSig(id, args) => "predicate"  <+> showId(id) <> parens(showParameterList(args))
  }

  // ids

  def showId(id: PIdnNode): Doc = id.name

  def showLabel(id: PLabelNode): Doc = id.name

  // misc

  def showMisc(id: PMisc): Doc = id match {
    case n: PRange => showRange(n)
    case receiver: PReceiver => showReceiver(receiver)
    case result: PResult => showResult(result)
    case embeddedType: PEmbeddedType => showEmbeddedType(embeddedType)
    case parameter: PParameter => showParameter(parameter)
    case literalValue: PLiteralValue => showLiteralValue(literalValue)
    case keyedElement: PKeyedElement => showKeyedElement(keyedElement)
    case compositeVal: PCompositeVal => showCompositeVal(compositeVal)
    case misc: PGhostMisc => misc match {
      case PBoundVariable(v, typ) => showId(v) <> ":" <+> showType(typ)
      case PTrigger(exps) => "{" <> showList(exps)(showExpr) <> "}"
      case PExplicitGhostParameter(actual) => showParameter(actual)
    }
  }
}