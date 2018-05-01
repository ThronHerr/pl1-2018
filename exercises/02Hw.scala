/**
Homework 02
============

Note: For some tasks, test examples are already provided.
Be sure to provide tests for all tasks and check your solution with them.

From now on, the tasks will not explicitly require tests any more,
but I advise you to nevertheless use tests for all programming tasks.
*/

/**
Part 1: Desugaring to Nor (1 subtask)
------
*/
object Hw02Part1 {
/**
Consider again the language of propositional logic formulae from the previous homework:
*/
sealed abstract class Exp
case class True() extends Exp  // constant true
case class False() extends Exp // constant false
case class And(lhs: Exp, rhs: Exp) extends Exp
case class Or(lhs: Exp, rhs: Exp) extends Exp
case class Not(e: Exp) extends Exp
case class Impl(lhs: Exp, rhs: Exp) extends Exp

def eval(e: Exp) : Boolean = e match {
  case True()     => true
  case False()    => false
  case And(l, r) => eval(l) && eval(r)
  case Or(l, r)  => eval(l) || eval(r)
  case Not(e)    => ! eval(e)
  case Impl(l,r) => ! eval(l) || eval(r) // l -> r
  case Nor(l,r)  => ! (eval(l) || eval(r)) // l nor r
}

/**
Subtasks:
      
      1) Introduce a new kind of expression "Nor" (neither ... nor ...).
      Eliminate And, Or, Not, and Impl by defining them as syntactic sugar for Nor.
*/
def desugar(e: Exp) : Exp = e match{
  case And(l,r)  => Nor(Nor(l,l),Nor(r,r))
  case Or(l,r)   => Nor(Nor(l,r),Nor(l,r))
  case Not(e)    => Nor(e,e)
  case Impl(l,r) => Nor(Nor(Nor(l,l),r),Nor(Nor(l,l),r))
}
}


/**
Part 2: Binding constructs (2 subtasks, plus 1 optional subtask)
------
*/
object Hw02Part2 {
/**
Consider the language of arithmetic expressions with "with", 
as illustrated by the following abstract syntax:
*/
sealed abstract class Exp 
case class Num(n: Int) extends Exp
case class Add(lhs: Exp, rhs: Exp) extends Exp
case class Mul(lhs: Exp, rhs: Exp) extends Exp
case class Id(x: Symbol) extends Exp 
case class With(x: Symbol, xdef: Exp, body: Exp) extends Exp
 
/**
We use implicits again to make example programs less verbose. 
*/
implicit def num2exp(n: Int) = Num(n)
implicit def sym2exp(x: Symbol) = Id(x)

/**
Your task is to extend the language with the following new binding construct:
*/

case class Let(defs: List[(Symbol, Exp)], body: Exp) extends Exp

/**
The purpose of the Let construct is to bind a list of identifiers in such a way
that the scope of the bound variables is only in the body, but not any of the
right hand sides of definitions. In particular, there is no shadowing between the definitions. 
For instance, the following test case should evaluate to 7 and not to 11:
*/

val test1 = 
  With('x, 1,
   Let(List('x -> 5, 'y -> Add('x,1)),Add('x,'y)))

/**
Note: The names "Let" and "LetStar" (see below) have been choosen in analogy to the
"let" and "let*" binding constructs in Scheme and Racket.
 */

/**
Subtasks:
      
      1) Implement the missing part of the eval and subst function
      to support Let.
      
      2) There is some redundancy in the binding constructs of this
      language. Eliminate the construct With by defining it as
      syntactic sugar.
      
      3) Bonus exercise (not mandatory): See below.
*/

def subst(e: Exp,i: Symbol,v : Num) : Exp = e match {
  case Num(n) => e
  case Id(x) => if (x == i) v else e
  case Add(l,r) => Add( subst(l,i,v), subst(r,i,v))
  case Mul(l,r) => Mul( subst(l,i,v), subst(r,i,v))
  case With(x,xdef,body) => With(x,
                                subst(xdef,i,v),
                                if (x == i) body else subst(body,i,v))
  case Let(defs, body) =>   Let(defs.map(x=>(x._1,subst(x._2,i,v))),body)                                    
  case LetStar(defs, body) => sys.error("not yet implemented (not mandatory)")                                   
}

def eval(e: Exp) : Int = e match {
  case Num(n) => n
  case Id(x) => sys.error("unbound variable: " + x.name)
  case Add(l,r) => eval(l) + eval(r)
  case Mul(l,r) => eval(l) * eval(r)
  case With(x, xdef, body) => eval(subst(body,x,Num(eval(xdef)))) 
  case Let(defs,body) =>if (defs.length == 1) eval(subst(body,defs(0)._1,eval(defs(0)._2)))
                        else eval(Let(defs.drop(1),subst(body,defs(0)._1,eval(defs(0)._2))))
  case LetStar(defs,body) => sys.error("not yet implemented (not mandatory)")
}
def desugar(e: Exp) : Exp = e match{
  case With(x,xdef,body)  => Let(List(x->xdef), body)
}
/**
Bonus exercise (not mandatory)
 */
/**
The LetStar construct is similar to let, but the scope of a definition contains all
right hand sides of definitions that follow the current one.
The following test case should hence evaluate to 11.   
*/

val test2 = 
     With('x, 1,
      LetStar(List('x -> 5, 'y -> Add('x,1)),Add('x,'y)))

case class LetStar(defs: List[(Symbol, Exp)], body: Exp) extends Exp

/**
Your task: First, implement the missing parts of subst and eval to support LetStar.
Then, eliminate LetStar by defining it as syntactic sugar.
 */
}
