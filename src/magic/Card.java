package magic;

import java.io.IOException;
import java.util.Set;

import javax.annotation.Nullable;

import com.google.common.base.Joiner;
import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

public final class Card implements Comparable<Card> {

	private final WholeCard whole;
	private final @Nullable Link link;

	private final String name;
	private final ManaCost manaCost;
	private final ImmutableSet<Color> colorOverride;
	private final ImmutableSet<Supertype> supertypes;
	private final ImmutableSet<Type> types;
	private final ImmutableSet<String> subtypes;
	private final String text;
	private final @Nullable Expression power;
	private final @Nullable Expression toughness;
	private final @Nullable Integer loyalty;

	private Card(Builder builder) {
		this.whole = builder.whole;
		this.link = builder.buildLink(this);

		this.name = builder.name;
		this.manaCost = builder.manaCost;
		this.colorOverride = builder.colorOverride;
		this.supertypes = builder.supertypes;
		this.types = builder.types;
		this.subtypes = builder.subtypes;
		this.text = builder.text;
		this.power = builder.power;
		this.toughness = builder.toughness;
		this.loyalty = builder.loyalty;
	}

	public String name() {
		return name;
	}

	public ManaCost manaCost() {
		return manaCost;
	}

	public @Nullable Set<Color> colorIndicator() {
		if (colorOverride == null) {
			return null;
		}
		return colorOverride.isEmpty()
				? null
				: colorOverride;
	}

	public @Nullable Set<Color> colorOverride() {
		return colorOverride;
	}

	public Set<Supertype> supertypes() {
		return supertypes;
	}

	public Set<Type> types() {
		return types;
	}

	public Set<String> subtypes() {
		return subtypes;
	}

	public String text() {
		return text;
	}

	public @Nullable Expression power() {
		return power;
	}

	public @Nullable Expression toughness() {
		return toughness;
	}

	public @Nullable Integer loyalty() {
		return loyalty;
	}

	public Set<Color> colors() {
		return MoreObjects.firstNonNull(colorOverride, manaCost.colors());
	}

	public WholeCard whole() {
		return whole;
	}

	public @Nullable Link link() {
		return link;
	}

	@Override public int compareTo(Card other) {
		return name.compareToIgnoreCase(other.name);
	}

	@Override public String toString() {
		return name;
	}

	private static final Joiner SPACE_JOINER = Joiner.on(' ');

	public void writeTo(Appendable out) throws IOException {
		String newline = System.lineSeparator();
		out.append(name());
		if (!manaCost.isEmpty()) {
			out.append(' ').append(manaCost.toString());
		}
		out.append(newline);
		if (colorOverride != null && !colorOverride.isEmpty()) {
			out.append('(');
			for (Color color : colorOverride) {
				out.append(color.code());
			}
			out.append(") ");
		}
		if (!supertypes.isEmpty()) {
			SPACE_JOINER.appendTo(out, supertypes()).append(' ');
		}
		SPACE_JOINER.appendTo(out, types);
		if (!subtypes.isEmpty()) {
			out.append(" - ");
			SPACE_JOINER.appendTo(out, subtypes());
		}
		out.append(newline);
		if (!text.isEmpty()) {
			out.append(text()).append(newline);
		}
		if (power != null) {
			out.append(power().toString()).append('/')
					.append(toughness.toString()).append(newline);
		} else if (loyalty != null) {
			out.append(Integer.toString(loyalty())).append(newline);
		}
	}

	public void print() {
		try {
			writeTo(System.out);
		} catch (IOException impossible) {
			throw new AssertionError(impossible);
		}
	}

	public static class Builder {

		private String name;
		private ManaCost manaCost = ManaCost.EMPTY;
		private ImmutableSet<Color> colorOverride = ImmutableSet.of();
		private ImmutableSet<Supertype> supertypes = ImmutableSet.of();
		private ImmutableSet<Type> types;
		private ImmutableSet<String> subtypes = ImmutableSet.of();
		private String text = "";
		private @Nullable Expression power = null;
		private @Nullable Expression toughness = null;
		private @Nullable Integer loyalty = null;

		private WholeCard whole;
		// only the first half will set this
		private @Nullable Builder linked;
		// each linked builder sets this field for the other
		private @Nullable Card other;

		public Builder() {}

		public Builder setName(String name) {
			this.name = name;
			return this;
		}

		public Builder setManaCost(ManaCost manaCost) {
			this.manaCost = manaCost;
			return this;
		}

		public Builder setColorOverride(Set<Color> colorOverride) {
			this.colorOverride = Sets.immutableEnumSet(colorOverride);
			return this;
		}

		public Builder setSupertypes(Set<Supertype> supertypes) {
			this.supertypes = Sets.immutableEnumSet(supertypes);
			return this;
		}

		public Builder setTypes(Set<Type> types) {
			this.types = Sets.immutableEnumSet(types);
			return this;
		}

		public Builder setSubtypes(Set<String> subtypes) {
			this.subtypes = ImmutableSet.copyOf(subtypes);
			return this;
		}

		public Builder setText(String text) {
			this.text = text;
			return this;
		}

		public Builder setPower(Expression power) {
			this.power = power;
			return this;
		}

		public Builder setToughness(Expression toughness) {
			this.toughness = toughness;
			return this;
		}

		public Builder setLoyalty(Integer loyalty) {
			this.loyalty = loyalty;
			return this;
		}

		void setWhole(WholeCard whole) {
			this.whole = whole;
		}

		Card buildCard() {
			return new Card(this);
		}

		void setLinked(Builder linked) {
			this.linked = linked;
		}

		Card getOther() {
			return other;
		}

		private Link buildLink(Card partiallyBuilt) {
			if (linked != null) {
				linked.other = partiallyBuilt;
				other = linked.buildCard();
				return new Link(other, true);
			}
			if (other != null) {
				return new Link(other, false);
			}
			return null;
		}
	}

	public static Builder builder() {
		return new Builder();
	}

}
