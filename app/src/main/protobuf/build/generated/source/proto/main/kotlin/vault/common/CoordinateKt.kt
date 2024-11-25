//Generated by the protocol buffer compiler. DO NOT EDIT!
// source: coordinate.proto

package vault.common;

@kotlin.jvm.JvmName("-initializecoordinate")
public inline fun coordinate(block: vault.common.CoordinateKt.Dsl.() -> kotlin.Unit): vault.common.CoordinateOuterClass.Coordinate =
  vault.common.CoordinateKt.Dsl._create(vault.common.CoordinateOuterClass.Coordinate.newBuilder()).apply { block() }._build()
public object CoordinateKt {
  @kotlin.OptIn(com.google.protobuf.kotlin.OnlyForUseByGeneratedProtoCode::class)
  @com.google.protobuf.kotlin.ProtoDslMarker
  public class Dsl private constructor(
    private val _builder: vault.common.CoordinateOuterClass.Coordinate.Builder
  ) {
    public companion object {
      @kotlin.jvm.JvmSynthetic
      @kotlin.PublishedApi
      internal fun _create(builder: vault.common.CoordinateOuterClass.Coordinate.Builder): Dsl = Dsl(builder)
    }

    @kotlin.jvm.JvmSynthetic
    @kotlin.PublishedApi
    internal fun _build(): vault.common.CoordinateOuterClass.Coordinate = _builder.build()

    /**
     * <pre>
     * X Value of the coordinate.
     * </pre>
     *
     * <code>double x = 1;</code>
     */
    public var x: kotlin.Double
      @JvmName("getX")
      get() = _builder.getX()
      @JvmName("setX")
      set(value) {
        _builder.setX(value)
      }
    /**
     * <pre>
     * X Value of the coordinate.
     * </pre>
     *
     * <code>double x = 1;</code>
     */
    public fun clearX() {
      _builder.clearX()
    }

    /**
     * <pre>
     * Y Value of the coordinate.
     * </pre>
     *
     * <code>double y = 2;</code>
     */
    public var y: kotlin.Double
      @JvmName("getY")
      get() = _builder.getY()
      @JvmName("setY")
      set(value) {
        _builder.setY(value)
      }
    /**
     * <pre>
     * Y Value of the coordinate.
     * </pre>
     *
     * <code>double y = 2;</code>
     */
    public fun clearY() {
      _builder.clearY()
    }
  }
}
public inline fun vault.common.CoordinateOuterClass.Coordinate.copy(block: vault.common.CoordinateKt.Dsl.() -> kotlin.Unit): vault.common.CoordinateOuterClass.Coordinate =
  vault.common.CoordinateKt.Dsl._create(this.toBuilder()).apply { block() }._build()

