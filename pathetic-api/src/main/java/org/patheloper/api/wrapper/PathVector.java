package org.patheloper.api.wrapper;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import org.patheloper.api.util.NumberUtils;

@AllArgsConstructor
@EqualsAndHashCode
public class PathVector implements Cloneable {

  private double x;
  private double y;
  private double z;

  /**
   * Finds the distance between the line BC and the point A
   *
   * @param A The point
   * @param B The first point of the line
   * @param C The second point of the line
   * @return The distance
   */
  public static double computeDistance(PathVector A, PathVector B, PathVector C) {

    PathVector d = C.subtract(B).divide(C.distance(B));
    PathVector v = A.subtract(B);

    double t = v.dot(d);
    PathVector P = B.add(d.multiply(t));

    return P.distance(A);
  }

  /**
   * Calculates the dot product of two vectors
   *
   * @param otherVector The other vector
   * @return The dot product
   */
  public double dot(PathVector otherVector) {
    return this.x * otherVector.x + this.y * otherVector.y + this.z * otherVector.z;
  }

  /**
   * Gets the length of the {@link PathVector}
   *
   * @return The length
   */
  public double length() {
    return Math.sqrt(
        NumberUtils.square(this.x) + NumberUtils.square(this.y) + NumberUtils.square(this.z));
  }

  /**
   * Gets the distance between this vector and another vector
   *
   * @param otherVector The other vector
   * @return The distance
   */
  public double distance(PathVector otherVector) {
    return Math.sqrt(
        NumberUtils.square(this.x - otherVector.x)
            + NumberUtils.square(this.y - otherVector.y)
            + NumberUtils.square(this.z - otherVector.z));
  }

  /**
   * Sets the x component of the vector
   *
   * @param x The x component
   * @return A new {@link PathVector}
   */
  public PathVector setX(double x) {
    return new PathVector(x, this.y, this.z);
  }

  /**
   * Sets the y component of the vector
   *
   * @param y The y component
   * @return A new {@link PathVector}
   */
  public PathVector setY(double y) {
    return new PathVector(this.x, y, this.z);
  }

  /**
   * Sets the z component of the vector
   *
   * @param z The z component
   * @return A new {@link PathVector}
   */
  public PathVector setZ(double z) {
    return new PathVector(this.x, this.y, z);
  }

  /**
   * Subtracts one vector from another
   *
   * @param otherVector {@link PathVector} to vector to subtract from the current Vector
   * @return A new {@link PathVector}
   */
  @NonNull
  public PathVector subtract(PathVector otherVector) {
    return new PathVector(this.x - otherVector.x, this.y - otherVector.y, this.z - otherVector.z);
  }

  /**
   * Multiplies itself by a scalar constant
   *
   * @param value The constant to multiply by
   * @return A new {@link PathVector}
   */
  @NonNull
  public PathVector multiply(double value) {
    return new PathVector(this.x * value, this.y * value, this.z * value);
  }

  /**
   * Normalises the {@link PathVector} (Divides the components by its magnitude)
   *
   * @return A new {@link PathVector}
   */
  @NonNull
  public PathVector normalize() {
    double magnitude = this.length();
    return new PathVector(this.x / magnitude, this.y / magnitude, this.z / magnitude);
  }

  /**
   * Divide the vector by a scalar constant
   *
   * @param value The constant to divide by
   * @return A new {@link PathVector}
   */
  public PathVector divide(double value) {
    return new PathVector(this.x / value, this.y / value, this.z / value);
  }

  /**
   * Adds two vectors together
   *
   * @param otherVector The other vector
   * @return A new {@link PathVector}
   */
  public PathVector add(PathVector otherVector) {
    return new PathVector(this.x + otherVector.x, this.y + otherVector.y, this.z + otherVector.z);
  }

  /**
   * Calculates the cross product of two vectors
   *
   * @param o The other vector
   * @return The cross product vector
   */
  public PathVector getCrossProduct(PathVector o) {
    double x = this.y * o.getZ() - o.getY() * this.z;
    double y = this.z * o.getX() - o.getZ() * this.x;
    double z = this.x * o.getY() - o.getX() * this.y;
    return new PathVector(x, y, z);
  }

  /**
   * Gets the x component of the vector
   *
   * @return The x component
   */
  public double getX() {
    return this.x;
  }

  /**
   * Gets the y component of the vector
   *
   * @return The y component
   */
  public double getY() {
    return this.y;
  }

  /**
   * Gets the z component of the vector
   *
   * @return The z component
   */
  public double getZ() {
    return this.z;
  }

  /**
   * Calculates the projection of this vector onto another vector. The projection represents the
   * component of this vector that lies along the direction of the other vector.
   *
   * @param other The vector onto which to project this vector
   * @return A new PathVector representing the projection of this vector onto the other vector
   */
  public PathVector projectOnto(PathVector other) {
    double dotProduct = this.dot(other);
    double otherSquaredLength = other.lengthSquared();

    double scaleFactor = dotProduct / otherSquaredLength;
    return other.multiply(scaleFactor);
  }

  /**
   * Calculates the square of the length (magnitude) of this vector. This is more efficient than
   * calculating the length directly and then squaring it.
   *
   * @return The squared length of this vector
   */
  public double lengthSquared() {
    return dot(this);
  }

  @Override
  public PathVector clone() {
    final PathVector clone;
    try {
      clone = (PathVector) super.clone();
    } catch (CloneNotSupportedException ex) {
      throw new IllegalStateException("Superclass messed up", ex);
    }

    clone.x = this.x;
    clone.y = this.y;
    clone.z = this.z;
    return clone;
  }
}
