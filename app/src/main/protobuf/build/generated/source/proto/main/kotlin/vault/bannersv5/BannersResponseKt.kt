//Generated by the protocol buffer compiler. DO NOT EDIT!
// source: bannersv5.proto

package vault.bannersv5;

@kotlin.jvm.JvmName("-initializebannersResponse")
public inline fun bannersResponse(block: vault.bannersv5.BannersResponseKt.Dsl.() -> kotlin.Unit): vault.bannersv5.Bannersv5.BannersResponse =
  vault.bannersv5.BannersResponseKt.Dsl._create(vault.bannersv5.Bannersv5.BannersResponse.newBuilder()).apply { block() }._build()
public object BannersResponseKt {
  @kotlin.OptIn(com.google.protobuf.kotlin.OnlyForUseByGeneratedProtoCode::class)
  @com.google.protobuf.kotlin.ProtoDslMarker
  public class Dsl private constructor(
    private val _builder: vault.bannersv5.Bannersv5.BannersResponse.Builder
  ) {
    public companion object {
      @kotlin.jvm.JvmSynthetic
      @kotlin.PublishedApi
      internal fun _create(builder: vault.bannersv5.Bannersv5.BannersResponse.Builder): Dsl = Dsl(builder)
    }

    @kotlin.jvm.JvmSynthetic
    @kotlin.PublishedApi
    internal fun _build(): vault.bannersv5.Bannersv5.BannersResponse = _builder.build()

    /**
     * An uninstantiable, behaviorless type to represent the field in
     * generics.
     */
    @kotlin.OptIn(com.google.protobuf.kotlin.OnlyForUseByGeneratedProtoCode::class)
    public class BannersProxy private constructor() : com.google.protobuf.kotlin.DslProxy()
    /**
     * <code>repeated .vault.bannersv5.Banner banners = 1;</code>
     */
     public val banners: com.google.protobuf.kotlin.DslList<vault.bannersv5.Bannersv5.Banner, BannersProxy>
      @kotlin.jvm.JvmSynthetic
      get() = com.google.protobuf.kotlin.DslList(
        _builder.getBannersList()
      )
    /**
     * <code>repeated .vault.bannersv5.Banner banners = 1;</code>
     * @param value The banners to add.
     */
    @kotlin.jvm.JvmSynthetic
    @kotlin.jvm.JvmName("addBanners")
    public fun com.google.protobuf.kotlin.DslList<vault.bannersv5.Bannersv5.Banner, BannersProxy>.add(value: vault.bannersv5.Bannersv5.Banner) {
      _builder.addBanners(value)
    }
    /**
     * <code>repeated .vault.bannersv5.Banner banners = 1;</code>
     * @param value The banners to add.
     */
    @kotlin.jvm.JvmSynthetic
    @kotlin.jvm.JvmName("plusAssignBanners")
    @Suppress("NOTHING_TO_INLINE")
    public inline operator fun com.google.protobuf.kotlin.DslList<vault.bannersv5.Bannersv5.Banner, BannersProxy>.plusAssign(value: vault.bannersv5.Bannersv5.Banner) {
      add(value)
    }
    /**
     * <code>repeated .vault.bannersv5.Banner banners = 1;</code>
     * @param values The banners to add.
     */
    @kotlin.jvm.JvmSynthetic
    @kotlin.jvm.JvmName("addAllBanners")
    public fun com.google.protobuf.kotlin.DslList<vault.bannersv5.Bannersv5.Banner, BannersProxy>.addAll(values: kotlin.collections.Iterable<vault.bannersv5.Bannersv5.Banner>) {
      _builder.addAllBanners(values)
    }
    /**
     * <code>repeated .vault.bannersv5.Banner banners = 1;</code>
     * @param values The banners to add.
     */
    @kotlin.jvm.JvmSynthetic
    @kotlin.jvm.JvmName("plusAssignAllBanners")
    @Suppress("NOTHING_TO_INLINE")
    public inline operator fun com.google.protobuf.kotlin.DslList<vault.bannersv5.Bannersv5.Banner, BannersProxy>.plusAssign(values: kotlin.collections.Iterable<vault.bannersv5.Bannersv5.Banner>) {
      addAll(values)
    }
    /**
     * <code>repeated .vault.bannersv5.Banner banners = 1;</code>
     * @param index The index to set the value at.
     * @param value The banners to set.
     */
    @kotlin.jvm.JvmSynthetic
    @kotlin.jvm.JvmName("setBanners")
    public operator fun com.google.protobuf.kotlin.DslList<vault.bannersv5.Bannersv5.Banner, BannersProxy>.set(index: kotlin.Int, value: vault.bannersv5.Bannersv5.Banner) {
      _builder.setBanners(index, value)
    }
    /**
     * <code>repeated .vault.bannersv5.Banner banners = 1;</code>
     */
    @kotlin.jvm.JvmSynthetic
    @kotlin.jvm.JvmName("clearBanners")
    public fun com.google.protobuf.kotlin.DslList<vault.bannersv5.Bannersv5.Banner, BannersProxy>.clear() {
      _builder.clearBanners()
    }

    /**
     * <code>optional string next_page_token = 2;</code>
     */
    public var nextPageToken: kotlin.String
      @JvmName("getNextPageToken")
      get() = _builder.getNextPageToken()
      @JvmName("setNextPageToken")
      set(value) {
        _builder.setNextPageToken(value)
      }
    /**
     * <code>optional string next_page_token = 2;</code>
     */
    public fun clearNextPageToken() {
      _builder.clearNextPageToken()
    }
    /**
     * <code>optional string next_page_token = 2;</code>
     * @return Whether the nextPageToken field is set.
     */
    public fun hasNextPageToken(): kotlin.Boolean {
      return _builder.hasNextPageToken()
    }
  }
}
public inline fun vault.bannersv5.Bannersv5.BannersResponse.copy(block: vault.bannersv5.BannersResponseKt.Dsl.() -> kotlin.Unit): vault.bannersv5.Bannersv5.BannersResponse =
  vault.bannersv5.BannersResponseKt.Dsl._create(this.toBuilder()).apply { block() }._build()

