package soottocfg.ast.Absyn; // Java Package generated by the BNF Converter.

public class Statem extends LVarStatement {
  public final Stm stm_;
  public Statem(Stm p1) { stm_ = p1; }

  public <R,A> R accept(soottocfg.ast.Absyn.LVarStatement.Visitor<R,A> v, A arg) { return v.visit(this, arg); }

  public boolean equals(Object o) {
    if (this == o) return true;
    if (o instanceof soottocfg.ast.Absyn.Statem) {
      soottocfg.ast.Absyn.Statem x = (soottocfg.ast.Absyn.Statem)o;
      return this.stm_.equals(x.stm_);
    }
    return false;
  }

  public int hashCode() {
    return this.stm_.hashCode();
  }


}