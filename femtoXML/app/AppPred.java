package femtoXML.app;

/** AppPred = Pred<AppTree> */
public abstract class AppPred extends Pred<AppTree> {
	@Override
	public abstract boolean pass(AppTree t);

}
