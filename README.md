Fisiks
======

Java Physics Engine

DESCRIPTION:
	
Fisiks is a project I began for my Carnegie Mellon application. Even though my application is now sent in,
I am going to continue improving Fisiks. Fisiks is a 2D Physics Engine that does the backend processing. In the example simulations provided here, I draw the raw shapes to the screen for demonstration, but ideally the shapes would be disguised with images for better graphics.

KNOWN ISSUES:

	* Stacking objects occasionally collapse onto eachother
	* Objects sometimes stick (unintentionally) during collision

IN PROGRESS:

	* Add Rotational motion
	* Circle-Circle collisions should use tangent line to point of collision for new velocity vectors
	* Circle-Rectangle collisions should allow Circle to collide exactly with corner of Rectangle
	* Color Themes for World
	* File IO for storing simulations
	* Add Polygons
	* Add lines
	* Add static objects

RUNNING:

	This project now runs as a desktop Swing application through simulation.Main.

	Compile:
	find src -type f -name '*.java' -print0 | xargs -0 javac -d bin

	Run:
	java -cp bin simulation.Main

	Note:
	A graphical desktop environment is required. In headless environments (for example,
	a container without DISPLAY), Java UI startup will fail.

COTT SOLVER (VENDORED):

	The COTT solver is included in:
	third_party/cott_solver

	Quick sanity check:
	cd third_party/cott_solver
	python3 -c "from chebyshev_ring import *; print('OK:', U * V == ONE)"

	Run upstream solver tests:
	cd third_party/cott_solver
	pytest test_chebyshev_ring.py -v

	Run Sp(4,Z) verification tests:
	cd third_party/cott_solver
	pytest test_sp4z.py -v
	python3 test_sp4z.py
