/*
 *The MIT License (MIT)
 * 
 *Copyright (c) 2013 Robert Adkins
 *
 *Permission is hereby granted, free of charge, to any person obtaining a copy
 *of this software and associated documentation files (the "Software"), to deal
 *in the Software without restriction, including without limitation the rights
 *to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *copies of the Software, and to permit persons to whom the Software is
 *furnished to do so, subject to the following conditions:
 *
 *The above copyright notice and this permission notice shall be included in
 *all copies or substantial portions of the Software.
 *
 *THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 *THE SOFTWARE.
 */

package collision;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import physical_object.Circle;
import physical_object.PhysicalObject;
import vector.Vector2D;


public class CollisionHandler {

	public static void collide(PhysicalObject one, PhysicalObject two) {

		if(one.selected || two.selected) {
			return;
		}

		Rectangle2D oneBounds = one.futureBody.getBounds2D();
		Rectangle2D twoBounds = two.futureBody.getBounds2D();

		if(oneBounds.intersects(twoBounds)) {

			if(one instanceof Circle && two instanceof Circle) {
				if(new Point2D.Double(oneBounds.getCenterX(), oneBounds.getCenterY()).distance(twoBounds.getCenterX(), twoBounds.getCenterY()) > ((Circle)one).getRadius() + ((Circle)two).getRadius()) {
					return;
				}
			}

			offsetIntersections(one, two);
		}
	}

	// calculate new velocity for PhysicalObject one
	private static Vector2D calcNewVel(PhysicalObject one, PhysicalObject two) {
		double minRestitution = Math.min(one.restitution, two.restitution);
		return (one.futureVelocity.multiply(one.mass).add(two.futureVelocity.multiply(two.mass).add(two.futureVelocity.subtract(one.futureVelocity).multiply(two.mass * minRestitution)))).divide(one.mass + two.mass);
	}

	private static void offsetIntersections(PhysicalObject one, PhysicalObject two) {

		double dx;
		double dy;

		if(one instanceof Circle && two instanceof Circle) {

			Vector2D distance = new Vector2D(one.futureBody.getCenterX() - two.futureBody.getCenterX(), one.futureBody.getCenterY() - two.futureBody.getCenterY());
			distance = distance.scaleTo(((Circle)one).getRadius() + ((Circle)two).getRadius() - distance.magnitude());

			dx = Math.abs(distance.x);
			dy = Math.abs(distance.y);
		}
		else {

			dx = Math.min(Math.abs(one.futureBody.getX() - two.futureBody.getMaxX()), Math.abs(one.futureBody.getMaxX() - two.futureBody.getX()));
			dy = Math.min(Math.abs(one.futureBody.getY() - two.futureBody.getMaxY()), Math.abs(one.futureBody.getMaxY() - two.futureBody.getY()));
		}

		double dVX = Math.abs(one.getNextVX() - two.getNextVX());
		double dVY = Math.abs(one.getNextVY() - two.getNextVY());

		double dAY = Math.abs(one.getAY() - two.getAY());
		double dAX = Math.abs(one.getAX() - two.getAX());

		double Ty = Double.MAX_VALUE;
		double Tx = Double.MAX_VALUE;

		if(dy == 0) {
			Ty = 0;
		}
		else if(dVY == 0) {
			if(dAY != 0) {
				Ty = Math.sqrt(dy / (.5 * dAY));
			}
		}
		else if(dAY == 0) {
			Ty = dy / dVY;
		}
		else {
			double a = .5 * dAY;
			double b = dVY;
			double c = dy;
			double det = b * b - (4.0 * a * c);

			if(det >= 0)
				Ty = Math.min(Math.abs((-b + Math.sqrt(det)) / (2.0 * a)), Math.abs(-b - Math.sqrt(det)) / (2.0 * a));
		}

		if(dx == 0) {
			Tx = 0;
		}
		else if(dVX == 0) {
			if(dAX != 0) {
				Tx = Math.sqrt(dx / (.5 * dAX));
			}
		}
		else if(dAX == 0) {
			Tx = dx / dVX;
		}
		else {
			double a = .5 * dAX;
			double b = dVX;
			double c = dx;
			double det = b * b - (4.0 * a * c);

			if(det >= 0)
				Tx = Math.min(Math.abs((-b + Math.sqrt(det)) / (2.0 * a)), Math.abs(-b - Math.sqrt(det)) / (2.0 * a));
		}

		if(Tx == Double.MAX_VALUE && Ty == Double.MAX_VALUE) {
			if(dy < dx) {
				one.setFutureY(one.getY());
				one.setNextVY(one.getVY());
				two.setFutureY(two.getY());
				two.setNextVY(two.getVY());
			}
			else {
				one.setFutureX(one.getX());
				one.setNextVX(one.getVX());
				two.setFutureX(two.getX());
				two.setNextVX(two.getVX());
			}
			return;
		}

		double T = Math.min(Ty, Tx);


		Vector2D newOne;
		Vector2D newTwo;
		
		if(one instanceof Circle && two instanceof Circle) {
			one.applyTime(-T);
			two.applyTime(-T);
			

			newOne = calcNewVel(one, two);
			newTwo = calcNewVel(two, one);
			one.futureVelocity = newOne;
			two.futureVelocity = newTwo;
		}
		else {
			if(T == Ty) {
				one.applyTimeY(-T);
				two.applyTimeY(-T);

				PhysicalObject top;
				PhysicalObject bottom;

				if(one.futureBody.getY() < two.futureBody.getY()) {
					top = one;
					bottom = two;
				}
				else {
					top = two;
					bottom = one;
				}

				if(bottom.grounded && !top.grounded) {

					if(top.getEdgeCollider() instanceof InelasticEdgeCollider) {
						top.grounded = true;
						top.ground = bottom;
						top.futureVelocity.y = 0;
						top.velocity.y = 0;
					}
					else {
						double deltay = top.futureBody.getMaxY() - bottom.futureBody.getY() - 1;
						double toRoot = 2 * top.getAY() * deltay + top.getNextVY() * top.getNextVY();

						// calculate next VY based off of displacement to edge
						// vf^2 - vi^2 = 2ad
						if(toRoot > 0) {
							top.setNextVY(-Math.sqrt(toRoot) * top.restitution);
						}
						else {
							top.setNextVY(0);
						}
					}
					
					top.setFutureY(top.getFutureY() - (top.futureBody.getMaxY() - bottom.getFutureY()) - 1);
					//return;
				}
				else if(bottom.grounded && top.grounded) {
					top.setFutureY(top.getFutureY() - (top.futureBody.getMaxY() - bottom.getFutureY()) - 1);
				}
				

				newOne = calcNewVel(one, two);
				newTwo = calcNewVel(two, one);
				
				// Only apply recalculated velocity if neither object is now grounded on the other
				// Grounded objects should maintain zero Y velocity to prevent collapse
				if(!(one.grounded && one.ground == two) && !(two.grounded && two.ground == one)) {
					one.futureVelocity.y = newOne.y;
					two.futureVelocity.y = newTwo.y;
				}
			}
			else {
				one.applyTimeX(-T);
				two.applyTimeX(-T);

				newOne = calcNewVel(one, two);
				newTwo = calcNewVel(two, one);
				one.futureVelocity.x = newOne.x;
				two.futureVelocity.x = newTwo.x;
			}
		}
	}
}
