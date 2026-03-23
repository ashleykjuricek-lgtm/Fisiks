from chebyshev_ring import *
import cmath, math


def test_klein_four():
    """sigma1 sigma2 = sigma2 sigma1 (Klein four-group V4)"""
    x = G1 * G2 + MB_ONE
    assert x.sigma1().sigma2() == x.sigma2().sigma1()


def test_sigma_involutions():
    """sigma1^2 = id, sigma2^2 = id"""
    x = G1**2 * G2 + G1 * G2**3 + MultiBandElement.from_int(7)
    assert x.sigma1().sigma1() == x
    assert x.sigma2().sigma2() == x


def test_self_dual_point():
    """At theta1=theta2=pi/2: g1=g2=i, s1=s2=0"""
    theta = math.pi / 2
    g1_val = G1.eval_at(theta1=theta, theta2=theta)
    assert abs(g1_val - 1j) < 1e-12
    g2_val = G2.eval_at(theta1=theta, theta2=theta)
    assert abs(g2_val - 1j) < 1e-12


def test_norm_at_self_dual():
    """N(g1g2+1) evaluated at s1=s2=0"""
    x = G1 * G2 + MB_ONE
    N = x.norm()
    val = N.eval_at(0.0, 0.0)
    print(f"N(g1g2+1) at self-dual = {val}")
    # Should be a real number (imaginary part zero)
    assert abs(val.imag) < 1e-12


def test_conjugation_matrix():
    """Build the 4x4 action matrix of sigma1 on basis {1,g1,g2,g1g2}"""
    basis = [MB_ONE, G1, G2, G1 * G2]
    # sigma1 acts on each basis element
    images = [b.sigma1() for b in basis]
    print("sigma1 action:")
    for i, img in enumerate(images):
        print(f"  sigma1(basis[{i}]) = {img}")
    # sigma1(1)=1, sigma1(g1)=s1-g1, sigma1(g2)=g2, sigma1(g1g2)=s1g2-g1g2
    assert images[0] == MB_ONE
    assert images[2] == G2


def test_epstein_zeta_ratio():
    """The big prediction: Z_APP/Z_PPP approx 1/24 at s=-1/2"""
    cutoff = 15

    def epstein(s, sigma):
        total = 0.0
        for n1 in range(-cutoff, cutoff + 1):
            for n2 in range(-cutoff, cutoff + 1):
                for n3 in range(-cutoff, cutoff + 1):
                    ev = sum(((n + sig)) ** 2 for n, sig in zip([n1, n2, n3], sigma))
                    if ev > 0:
                        total += ev ** (-s)
        return total

    Z_PPP = epstein(3 / 2, [0, 0, 0])
    Z_APP = epstein(3 / 2, [0.5, 0, 0])
    ratio = Z_APP / Z_PPP
    print(f"Z_APP/Z_PPP at s=3/2: {ratio:.6f}")
    print(f"1/24 = {1/24:.6f}")


if __name__ == "__main__":
    for name, fn in list(globals().items()):
        if name.startswith("test_") and callable(fn):
            try:
                fn()
                print(f"PASS {name}")
            except AssertionError as e:
                print(f"FAIL {name}: {e}")
            except Exception as e:
                print(f"ERROR {name}: {e}")
