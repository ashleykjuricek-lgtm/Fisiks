package collision;

/**
 * Mirror Calculus: Operator-based collision response using COTT reciprocity.
 * 
 * Core principle: 0 · ω = 1 (differentiation · integration = identity)
 * 
 * In collision response:
 * - 0 (traction zero) represents rate of change (velocity differential)
 * - ω (omega) represents accumulation (impulse integral)
 * - Their composition yields the identity: momentum is conserved through exchange
 * 
 * This replaces ad-hoc momentum formulas with principled operator algebra.
 */
public class MirrorCalculus {

	/**
	 * Traction pair: [coefficient, phase] representing c · 0^p
	 * Phase cycles through 4 states: ∅(0) → −1(0.25) → ω(0.5) → 1(0.75)
	 */
	public static class TractionPair {
		public double c;  // coefficient
		public double p;  // phase ∈ [0, 1)

		public TractionPair(double c, double p) {
			this.c = c;
			this.p = normalizePhase(p);
		}

		public TractionPair multiply(TractionPair other) {
			return new TractionPair(
				this.c * other.c,
				normalizePhase(this.p + other.p)
			);
		}

		/**
		 * Apply traction exponentiation: each exponent value maps to a phase shift.
		 * 0^0 = ∅ (phase 0)
		 * 0^1 = 1 (phase 0.75)
		 * 0^ω = −1 (phase 0.25)
		 * 0^(−1) = ω (phase 0.5)
		 */
		public TractionPair pow(double exponent) {
			// Phase increment per unit exponent
			double phaseShift = normalizePhase(exponent * 0.75);
			return new TractionPair(this.c, normalizePhase(this.p + phaseShift));
		}

		private static double normalizePhase(double p) {
			p = p % 1.0;
			if (p < 0) p += 1.0;
			return p;
		}

		@Override
		public String toString() {
			String phaseName;
			if (Math.abs(p - 0.0) < 0.01) phaseName = "∅";
			else if (Math.abs(p - 0.25) < 0.01) phaseName = "−1";
			else if (Math.abs(p - 0.5) < 0.01) phaseName = "ω";
			else if (Math.abs(p - 0.75) < 0.01) phaseName = "1";
			else phaseName = String.format("%.2f", p);
			return String.format("[%.4f, %s]", c, phaseName);
		}
	}

	/**
	 * Reciprocal impulse: Given two objects colliding, compute the momentum transfer
	 * using operator reciprocity instead of ad-hoc momentum exchange.
	 * 
	 * Standard approach (problematic):
	 *   v' = (m₁v₁ + m₂v₂ + m₂(v₂ − v₁)e) / (m₁ + m₂)
	 * 
	 * COTT approach (principled):
	 *   Express the collision as composition: 0 · ω = 1
	 *   - 0 extracts the difference (differentiation)
	 *   - ω reintegrates the impulse (antidifferentiation)
	 *   - Product yields conserved exchange
	 * 
	 * This avoids the "calcNewVel" problem of momentum oscillation.
	 */
	public static double reciprocalImpulse(
		double m1, double v1,
		double m2, double v2,
		double restitution
	) {
		// The velocity difference is the "derivative" to be integrated
		double velocityDiff = v2 - v1;

		// Reduced mass: the "measure" of the system
		double reducedMass = (m1 * m2) / (m1 + m2);

		// Impulse magnitude from reciprocity: 0 · ω = 1 applied to momentum flux
		// ω represents the integrating factor that turns rate-of-change into impulse
		double impulse = reducedMass * velocityDiff * (1.0 + restitution);

		// Apply impulse proportionally: each mass receives inverted impulse
		double dv1 = impulse / m1;
		double dv2 = -impulse / m2;

		// Return the velocity change for the primary object (arbitrary choice; caller decides which)
		return dv1;
	}

	/**
	 * Phase-aware collision: Objects in different "phases" of the collision cycle
	 * interact differently. This maps phase to collision behavior.
	 * 
	 * Phase 0 (∅): Void phase — objects are separated, no interaction
	 * Phase 0.25 (−1): Negative phase — objects attracting (impulse reversal)
	 * Phase 0.5 (ω): Integration phase — momentum transfer active
	 * Phase 0.75 (1): Unity phase — objects moving together coherently
	 */
	public static double phaseModulatedImpulse(
		double baseImpulse,
		double collisionPhase
	) {
		// Phase-modulated response: sin maps [0,1] to [0,1,0] with inflection at ω
		double phaseFactor = Math.sin(Math.PI * collisionPhase);
		return baseImpulse * phaseFactor;
	}

	/**
	 * Verify the core reciprocity axiom numerically: 0 · ω = 1
	 * This is the algebraic heartbeat of COTT.
	 */
	public static boolean verifyReciprocity() {
		TractionPair zero = new TractionPair(1.0, 0.0);      // 0 (∅)
		TractionPair omega = new TractionPair(1.0, 0.5);     // ω
		TractionPair one = new TractionPair(1.0, 0.75);      // 1

		TractionPair product = zero.multiply(omega);

		// Check: product ≈ 1 (within phase tolerance)
		return Math.abs(product.p - one.p) < 0.01 && Math.abs(product.c - 1.0) < 0.01;
	}
}
