package it.albertus.jface.listener;

import it.albertus.util.Configured;

abstract class AbstractDecimalVerifyListener<T extends Number> extends AbstractNumberVerifyListener<T> {

	public AbstractDecimalVerifyListener(final boolean allowNegatives) {
		super(allowNegatives);
	}

	public AbstractDecimalVerifyListener(final Configured<Boolean> allowNegatives) {
		super(allowNegatives);
	}

	@Override
	protected boolean isNumeric(final String string) {
		try {
			parseNumber(string);
			return true;
		}
		catch (final Exception e) {
			if (".".equals(string) || "e".equalsIgnoreCase(string) || (allowNegatives.getValue() && "-".equals(string))) {
				return true;
			}
			return false;
		}
	}

}