package magic;

import java.io.IOException;
import java.io.PrintStream;
import java.util.Iterator;

import com.google.common.collect.Iterators;

public abstract class WholePrinting extends Whole<Printing> {

	private final WholeCard card;
	private final Expansion expansion;
	private final boolean isTimeshifted;
	private final Rarity rarity;

	private WholePrinting(Builder builder) {
		this.card = builder.card;
		this.expansion = builder.expansion;
		this.rarity = builder.rarity;
		this.isTimeshifted = builder.isTimeshifted;
	}

	@Override public abstract PrintingPair pair();

	public WholeCard card() {
		return card;
	}

	public Expansion expansion() {
		return expansion;
	}

	public Rarity rarity() {
		return rarity;
	}

	public boolean isTimeshifted() {
		return isTimeshifted;
	}

	public void print() {
		try {
			writeTo(System.out);
		} catch (IOException impossible) {
			throw new AssertionError(impossible);
		}
	}

	public abstract void writeTo(PrintStream out) throws IOException;
	
	public Builder builder() {
		return new Builder();
	}

	private static final class StandalonePrinting extends WholePrinting {

		private final Printing printing;

		private StandalonePrinting(Builder builder, Printing.Builder only) {
			super(builder);
			this.printing = only.build();
		}

		@Override public boolean isStandalone() {
			return true;
		}

		@Override public Printing only() {
			return printing;
		}

		@Override public PrintingPair pair() {
			throw new IllegalStateException();
		}

		@Override public void writeTo(PrintStream out) throws IOException {
			printing.writeTo(out);
		}

		@Override public Iterator<Printing> iterator() {
			return Iterators.singletonIterator(printing);
		}
	}

	private static final class CompositePrinting extends WholePrinting {

		private final PrintingPair printings;

		private CompositePrinting(Builder builder,
				Printing.Builder first,
				Printing.Builder second) {
			super(builder);
			PartialBuilder.link(first, second, this);
			this.printings = new PrintingPair(builder.card.pair(),
					first.build(), first.getOther());
		}

		@Override public boolean isStandalone() {
			return false;
		}

		@Override public Printing only() {
			throw new IllegalStateException();
		}

		@Override public PrintingPair pair() {
			return printings;
		}

		@Override public void writeTo(PrintStream out) throws IOException {
			printings.first().writeTo(out);
			out.append("* ")
					.append(printings.cards().layout().toString().toUpperCase())
					.append(" *")
					.append(System.lineSeparator());
			printings.second().writeTo(out);
		}

		@Override public Iterator<Printing> iterator() {
			return Iterators.forArray(printings.first(), printings.second());
		}
	}

	public static class Builder {

		private boolean isTimeshifted = false;
		private Rarity rarity;
		private Expansion expansion;
		private WholeCard card;
		private Printing.Builder firstOrOnly;
		private Printing.Builder second;

		private Builder() {}
		
		public Builder setTimeshifted(boolean isTimeshifted) {
			this.isTimeshifted = isTimeshifted;
			return this;
		}

		public Builder setRarity(Rarity rarity) {
			this.rarity = rarity;
			return this;
		}

		public Builder setExpansion(Expansion expansion) {
			this.expansion = expansion;
			return this;
		}

		public Builder setCard(WholeCard card) {
			this.card = card;
			return this;
		}
		
		public Builder setOnly(Printing.Builder only) {
			this.firstOrOnly = only;
			return this;
		}

		public Builder setFirst(Printing.Builder first) {
			this.firstOrOnly = first;
			return this;
		}

		public Builder setSecond(Printing.Builder second) {
			this.second = second;
			return this;
		}

		public WholePrinting build() {
			if (card.isStandalone()) {
				if (second != null) {
					throw new IllegalStateException();
				}
				return new StandalonePrinting(this, firstOrOnly);
			}
			return new CompositePrinting(this, firstOrOnly, second);
		}
	}

}