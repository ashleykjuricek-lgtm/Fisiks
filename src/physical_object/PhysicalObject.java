package physical_object;
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

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.RectangularShape;
import java.util.concurrent.ScheduledFuture;

import vector.Vector2D;
import world.World;
import collision.EdgeCollider;
import collision.CollisionHandler;

public abstract class PhysicalObject implements Runnable, MouseListener, MouseMotionListener {

	public static final double mPerPixel = .226 / 20;
	public static final double pixelPerM = 20 / .226;
	public static final double earthG = 9.81 * pixelPerM;

	public static final double minVelocity = .00001;

	private long lastTime;
	public ScheduledFuture<?> future;

	public RectangularShape body;
	public RectangularShape futureBody;

	public Vector2D velocity;
	public Vector2D futureVelocity;
	public Vector2D acceleration;

	public double mass;
	public double restitution;

	private int id;
	public boolean grounded;
	public PhysicalObject ground;

	private EdgeCollider edgeCollider;

	public boolean selected;

	public PhysicalObject(double vx, double vy, double ax, double ay, double mass, double coeff, RectangularShape shape, EdgeCollider edgeCollider) {

		if(coeff < 0 || coeff > 1) {
			throw new IllegalArgumentException("Coefficient of Restitution is [0, 1]");
		}

		this.velocity = new Vector2D(vx * pixelPerM, vy * pixelPerM);
		this.futureVelocity = new Vector2D(velocity);
		this.acceleration = new Vector2D(ax * pixelPerM, ay);
		this.mass = mass;
		this.restitution = coeff;
		this.body = shape;
		this.futureBody = (RectangularShape)shape.clone();
		this.grounded = false;
		this.ground = null;
		this.id = World.getWorld().numObjects();
		this.edgeCollider = edgeCollider;
		this.selected = false;
	}

	public EdgeCollider getEdgeCollider() {
		return edgeCollider;
	}
	
	public int getID() {
		return id;
	}

	public double getX() {
		return body.getX();
	}

	public void setX(double x) {
		body.setFrame(x, getY(), getWidth(), getHeight());
	}

	public double getWidth() {
		return body.getWidth();
	}
	public double getHeight() {
		return body.getHeight();
	}

	public double getY() {
		return body.getY();
	}
	public void setY(double y) {
		body.setFrame(getX(), y, getWidth(), getHeight());
	}
	public double getFutureX() {
		return futureBody.getX();
	}
	public void setFutureX(double x) {
		futureBody.setFrame(x, getFutureY(), getWidth(), getHeight());
	}
	public double getFutureY() {
		return futureBody.getY();
	}
	public void setFutureY(double y) {
		futureBody.setFrame(getFutureX(), y, getWidth(), getHeight());
	}

	public double getVX() {
		return velocity.x;
	}

	public void setVX(double vx) {
		this.velocity.x = vx;
	}

	public double getVY() {
		return velocity.y;
	}

	public void setVY(double vy) {
		this.velocity.y = vy;
	}

	public double getNextVX() {
		return futureVelocity.x;
	}

	public void setNextVX(double tempVX) {
		this.futureVelocity.x = tempVX;
	}

	public double getNextVY() {
		return futureVelocity.y;
	}

	public void setNextVY(double tempVY) {
		this.futureVelocity.y = tempVY;
	}

	public double getAX() {
		return acceleration.x;
	}

	public void setAX(double ax) {
		this.acceleration.x = ax;
	}

	public double getAY() {
		return acceleration.y;
	}

	public void setAY(double ay) {
		this.acceleration.y = ay;
	}

	public long getLastTime() {
		return lastTime;
	}

	public void setLastTime(long t) {
		lastTime = t;
	}

	public void updatePos() {

		long curTime = System.currentTimeMillis();
		double T = (curTime - getLastTime()) / 1000.0;
		applyTime(T);
		setLastTime(curTime);
	}

	public void applyTime(double T) {
		applyTimeX(T);
		if(!grounded) {
			applyTimeY(T);
		}
		else {
			futureVelocity.y = 0;
			// Light damping (5%) only when velocity is significant, prevents creep but allows rolling
			if(Math.abs(futureVelocity.x) > minVelocity) {
				futureVelocity.x -= futureVelocity.x * .05;
			} else {
				futureVelocity.x = 0;
			}
		}
	}

	public void applyTimeX(double T) {
		setFutureX(getFutureX() + getNextVX() * T + .5 * getAX() * Math.pow(T, 2));
		setNextVX(getNextVX() + getAX() * T);
	}

	public void applyTimeY(double T) {
		setFutureY(getFutureY() + getNextVY() * T + .5 * getAY() * Math.pow(T, 2));
		setNextVY(getNextVY() + getAY() * T);
	}

	public void run() {
		
		if(selected) {
			return;
		}
		
		if(grounded && ground != null) {
			// Unground if object separates horizontally, or if it's moving upward, or if ground itself is no longer grounded
			if(this.futureBody.getMaxX() < ground.getFutureX() || getFutureX() > ground.futureBody.getMaxX() || this.getNextVY() > 0 || !ground.grounded) {
				grounded = false;
				ground = null;
			}
		}

		if(World.getWorld().isBounded()) {
			edgeCollider.collideWithEdge(this);
		}

		for(int j = id + 1; j < World.getWorld().numObjects(); j++) {
			CollisionHandler.collide(this, World.getWorld().getPhysicalObjects().get(j));
		}

		this.setVX(getNextVX());
		this.setVY(getNextVY());
		this.setX(this.getFutureX());
		this.setY(this.getFutureY());

		if(velocity.y < 0 && velocity.y > -minVelocity) {
			velocity.y = 0;
		}

		updatePos();
	}

	// pick up atom
	public void mousePressed(MouseEvent e) {

		// check mouse is touching atom
		if(e.getX() >= getFutureX() && e.getX() <= getFutureX() + futureBody.getBounds2D().getWidth()) {
			if(e.getY() >= getFutureY() && e.getY() <= getFutureY() + futureBody.getBounds2D().getHeight()) {
				selected = true;
			}
		}
	}

	// drop atom
	public void mouseReleased(MouseEvent e) {

		// check atom was picked up
		if(selected) {
			setLastTime(System.currentTimeMillis());
			selected = false;
			grounded = false;
			ground = null;
		}
	}

	// move atom
	public void mouseDragged(MouseEvent e) {

		// check atom was picked up
		if(selected) {
			setX(e.getX() - getWidth() / 2);
			setY(e.getY() - getHeight() / 2);
			setFutureX(e.getX() - getWidth() / 2);
			setFutureY(e.getY() - getHeight() / 2);
			grounded = false;
			ground = null;
		}
	}

	// not needed
	public void mouseClicked(MouseEvent e) {}
	public void mouseEntered(MouseEvent e) {}
	public void mouseExited(MouseEvent e) {}
	public void mouseMoved(MouseEvent e) {}
}
