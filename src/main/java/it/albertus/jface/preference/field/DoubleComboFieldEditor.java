package it.albertus.jface.preference.field;

import org.eclipse.swt.widgets.Composite;

public class DoubleComboFieldEditor extends AbstractDecimalComboFieldEditor<Double> {

	private static final int DEFAULT_TEXT_LIMIT = 32;

	public DoubleComboFieldEditor(final String name, final String labelText, final String[][] entryNamesAndValues, final Composite parent) {
		super(name, labelText, entryNamesAndValues, parent);
	}

	@Override
	protected int getDefaultTextLimit() {
		return DEFAULT_TEXT_LIMIT;
	}

	@Override
	protected boolean doCheckState() {
		try {
			final Double number = Double.valueOf(getValue());
			return checkValidRange(number);
		}
		catch (final NumberFormatException nfe) {/* Ignore */}
		return false;
	}

	@Override
	protected String cleanValue(String value) {
		value = super.cleanValue(value);
		try {
			value = Double.valueOf(value).toString();
		}
		catch (final Exception exception) {/* Ignore */}
		return value;
	}

	@Override
	protected void cleanComboText() {
		final String oldText = getComboBoxControl().getText();
		String newText = oldText.trim();

		try {
			newText = getNameForValue(Double.valueOf(newText).toString());
		}
		catch (final Exception exception) {/* Ignore */}
		if (!newText.equals(oldText)) {
			getComboBoxControl().setText(newText);
		}
	}

	@Override
	public String getValue() {
		try {
			return Double.valueOf(super.getValue()).toString();
		}
		catch (final Exception exception) {
			return super.getValue();
		}
	}

	@Override
	protected void setValue(final String value) {
		try {
			super.setValue(Double.valueOf(value).toString());
		}
		catch (final Exception exception) {
			super.setValue(value);
		}
	}

	@Override
	protected String getNameForValue(final String value) {
		for (final String[] entry : getEntryNamesAndValues()) {
			String comboValue;
			try {
				comboValue = Double.valueOf(entry[1]).toString();
			}
			catch (final Exception e) {
				comboValue = entry[1];
			}
			if (value.equals(comboValue)) {
				return entry[0];
			}
		}
		return value; // Name not present in the array.
	}

	@Override
	protected String getDefaultValue() {
		String defaultValue = getPreferenceStore().getDefaultString(getPreferenceName());
		try {
			defaultValue = Double.toString(Double.parseDouble(defaultValue));
		}
		catch (final NumberFormatException nfe) {/* Ignore */}
		return defaultValue;
	}

	@Override
	public Double getNumberValue() throws NumberFormatException {
		return Double.valueOf(getValue());
	}

}